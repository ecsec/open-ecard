/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.transport.paos

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.EmptyResponseDataType
import iso.std.iso_iec._24727.tech.schema.StartPAOS
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse
import iso.std.iso_iec._24727.tech.schema.Transmit
import oasis.names.tc.dss._1_0.core.schema.ResponseBaseType
import org.apache.http.HttpException
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.binding.tctoken.TlsConnectionHandler
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.DispatcherException
import org.openecard.common.interfaces.DocumentSchemaValidator
import org.openecard.common.interfaces.DocumentValidatorException
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.FileUtils.toByteArray
import org.openecard.common.util.Promise
import org.openecard.common.util.ValueGenerators.generateRandom
import org.openecard.httpcore.HttpRequestHelper.setDefaultHeader
import org.openecard.httpcore.KHttpUtils.dumpHttpRequest
import org.openecard.httpcore.KHttpUtils.dumpHttpResponse
import org.openecard.httpcore.StreamHttpClientConnection
import org.openecard.sal.protocol.eac.transport.paos.MessageIdGenerator
import org.openecard.ws.marshal.MarshallingTypeException
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.openecard.ws.soap.SOAPException
import org.openecard.ws.soap.SOAPMessage
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.net.URISyntaxException
import javax.xml.namespace.QName
import javax.xml.transform.TransformerException

private val logger = KotlinLogging.logger { }

/**
 * PAOS implementation for JAXB types.
 * This implementation can be configured to speak TLS by creating the instance with a TlsClient. The dispatcher instance
 * is used to deliver the messages to the instances implementing the webservice interfaces.
 *
 * @author Johannes Schmoelz
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class PAOS(
	dispatcher: Dispatcher,
	private val tlsHandler: TlsConnectionHandler,
	private val schemaValidator: DocumentSchemaValidator,
) {
	private val headerValuePaos: String
	private val idGenerator: MessageIdGenerator
	private val m: WSMarshaller
	private val dispatcher: Dispatcher = dispatcher.filter

	private val serviceString: String
	private val validationError: Promise<DocumentValidatorException?> = Promise<DocumentValidatorException?>()

	/**
	 * Creates a PAOS instance and configures it for a given endpoint.
	 * If tlsClient is not null the connection must be HTTPs, else HTTP.
	 *
	 * @param dispatcher The dispatcher instance capable of dispatching the received messages.
	 * @param tlsHandler The TlsClient containing the configuration of the yet to be established TLS channel, or
	 * `null` if TLS should not be used.
	 * @param schemaValidator Schema Validator used to validate incoming messages.
	 * @throws PAOSException In case the PAOS module could not be initialized.
	 */
	init {
		this.serviceString = buildServiceString()
		this.headerValuePaos = String.format("ver=\"%s\" %s", ECardConstants.PAOS_VERSION_20, this.serviceString)

		try {
			this.idGenerator = MessageIdGenerator()
			this.m = createInstance()
		} catch (ex: WSMarshallerException) {
			logger.error(ex) { "${ex.message}" }
			throw PAOSException(ex)
		}
	}

	private fun getRelatesTo(msg: SOAPMessage): String? = getHeaderElement(msg, RELATES_TO)

	private fun setRelatesTo(
		msg: SOAPMessage,
		value: String?,
	) {
		val elem = getHeaderElement(msg, RELATES_TO, true)!!
		elem.textContent = value
	}

	private fun getHeaderElement(
		msg: SOAPMessage,
		elem: QName,
	): String? {
		val headerElem = getHeaderElement(msg, elem, false)
		return headerElem?.textContent?.trim { it <= ' ' }
	}

	private fun getHeaderElement(
		msg: SOAPMessage,
		elem: QName,
		create: Boolean,
	): Element? {
		var result: Element? = null
		val h = msg.soapHeader
		// try to find a header
		for (e in h.childElements) {
			if (e.localName == elem.localPart && e.namespaceURI == elem.namespaceURI) {
				result = e
				break
			}
		}
		// if no such element in header, create new
		if (result == null && create) {
			result = h.addHeaderElement(elem)
		}
		return result
	}

	private fun addMessageIDs(msg: SOAPMessage) {
		val otherID = idGenerator.remoteID
		val newID = idGenerator.createNewID() // also swaps messages in
		// MessageIdGenerator
		if (otherID != null) {
			// add relatesTo element
			setRelatesTo(msg, otherID)
		}
		// add MessageID element
		setMessageID(msg, newID)
	}

	private fun updateMessageID(msg: SOAPMessage) {
		try {
			val id = getMessageID(msg)
			if (id == null) {
				throw PAOSException(ErrorTranslations.NO_MESSAGE_ID)
			}
			if (!idGenerator.setRemoteID(id)) {
				// IDs don't match throw exception
				throw PAOSException(ErrorTranslations.MESSAGE_ID_MISSMATCH)
			}
		} catch (e: SOAPException) {
			logger.error(e) { "${e.message}" }
			throw PAOSException(e.message, e)
		}
	}

	private fun getMessageID(msg: SOAPMessage): String? = getHeaderElement(msg, MESSAGE_ID)

	private fun setMessageID(
		msg: SOAPMessage,
		value: String?,
	) {
		val elem = getHeaderElement(msg, MESSAGE_ID, true)!!
		elem.textContent = value
	}

	private fun processPAOSRequest(content: InputStream): Any {
		try {
			val doc = m.str2doc(content)
			val msg = m.doc2soap(doc)
			var body = msg.soapBody.childElements.get(0)
			updateMessageID(msg)

			if (logger.isDebugEnabled()) {
				try {
					logger.debug { "${"Message received:\n${m.doc2str(doc)}"} " }
				} catch (ex: TransformerException) {
					logger.warn(ex) { "Failed to log PAOS request message." }
				}
			}

			// fix profile attribute if it is not present
			// while there are the eID-Servers to blame, some don't get it right and actually Profile is a useless attribute anyway
			if (body.localName == "StartPAOSResponse" && !body.hasAttribute("Profile")) {
				logger.warn { "Received message without Profile attribute, adding one for proper validation." }
				body.setAttribute("Profile", ECardConstants.Profile.ECARD_1_1)

				try {
					// copy or the validation produces strange errors
					val docStr = m.doc2str(body)
					val newDoc = m.str2doc(docStr)
					body = newDoc.documentElement
				} catch (ex: SAXException) {
					throw PAOSException("Failed to copy document.", ex)
				} catch (ex: TransformerException) {
					throw PAOSException("Failed to copy document.", ex)
				}
			}

			// validate input message
			schemaValidator.validate(body)

			return m.unmarshal(body)
		} catch (ex: MarshallingTypeException) {
			logger.error(ex) { "${ex.message}" }
			throw PAOSException(ex.message, ex)
		} catch (ex: WSMarshallerException) {
			val msg = "Failed to read/process message from PAOS server."
			logger.error(ex) { msg }
			throw PAOSException(ErrorTranslations.MARSHALLING_ERROR, ex)
		} catch (ex: IOException) {
			val msg = "Failed to read/process message from PAOS server."
			logger.error(ex) { msg }
			throw PAOSException(ErrorTranslations.SOAP_MESSAGE_FAILURE, ex)
		} catch (ex: SAXException) {
			val msg = "Failed to read/process message from PAOS server."
			logger.error(ex) { msg }
			throw PAOSException(ErrorTranslations.SOAP_MESSAGE_FAILURE, ex)
		}
	}

	private fun createPAOSResponse(obj: Any): String {
		val msg = createSOAPMessage(obj)
		val result = m.doc2str(msg.document)

		logger.debug { "Message sent:\n$result" }

		return result
	}

	private fun createSOAPMessage(content: Any): SOAPMessage {
		val contentDoc = m.marshal(content)

		try {
			val docStr = m.doc2str(contentDoc)
			val newDoc = m.str2doc(docStr)
			schemaValidator.validate(newDoc)
		} catch (ex: DocumentValidatorException) {
			logger.warn(ex) { "Schema validation of outgoing message failed." }
		} catch (ex: SAXException) {
			throw MarshallingTypeException("Failed to copy document.", ex)
		} catch (ex: TransformerException) {
			throw MarshallingTypeException("Failed to copy document.", ex)
		}

		val msg = m.add2soap(contentDoc)
		val header = msg.soapHeader

		// fill header with paos stuff
		val paos = header.addHeaderElement(PAOS_PAOS)
		paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "actor", ECardConstants.ACTOR_NEXT)
		paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "mustUnderstand", "1")

		val version = header.addChildElement(paos, PAOS_VERSION)
		version.textContent = ECardConstants.PAOS_VERSION_20

		val endpointReference = header.addChildElement(paos, PAOS_ENDPOINTREF)
		var address = header.addChildElement(endpointReference, PAOS_ADDRESS)
		address.textContent = "http://www.projectliberty.org/2006/01/role/paos"
		val metaData = header.addChildElement(endpointReference, PAOS_METADATA)
		val serviceType = header.addChildElement(metaData, PAOS_SERVICETYPE)
		serviceType.textContent = ECardConstants.PAOS_NEXT

		val replyTo = header.addHeaderElement(REPLY_TO)
		address = header.addChildElement(replyTo, ADDRESS)
		address.textContent = "http://www.projectliberty.org/2006/02/role/paos"

		// add message IDs
		addMessageIDs(msg)
		return msg
	}

	/**
	 * Sends start PAOS and answers all successor messages to the server associated with this instance.
	 * Messages are exchanged until the server replies with a `StartPAOSResponse` message.
	 *
	 * @param message The StartPAOS message which is sent in the first message.
	 * @return The `StartPAOSResponse` message from the server.
	 * @throws DispatcherException In case there errors with the message conversion or the dispatcher.
	 * @throws PAOSException In case there were errors in the transport layer.
	 * @throws PAOSConnectionException
	 */
	fun sendStartPAOS(message: StartPAOS): StartPAOSResponse {
		var msg: Any? = message
		var conn: StreamHttpClientConnection? = null
		val ctx: HttpContext = BasicHttpContext()
		val httpexecutor = HttpRequestExecutor()
		val reuse = DefaultConnectionReuseStrategy()
		var connectionDropped = false
		var lastResponse: ResponseBaseType? = null
		var firstOecMinorError: String? = null
		var fakeSlotHandle: ByteArray? = null
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		var internalSessionIdentifier: String? = null
		val connectionHandles = message.getConnectionHandle()
		if (connectionHandles != null) {
			for (connectionHandle in connectionHandles) {
				if (fakeSlotHandle == null) {
					fakeSlotHandle = generateRandom(32)
				}
				connectionHandle.setSlotHandle(fakeSlotHandle)

				val channelHandle = connectionHandle.getChannelHandle()
				if (channelHandle != null) {
					val currentSessionIdentifier = channelHandle.getSessionIdentifier()
					if (currentSessionIdentifier != null) {
						internalSessionIdentifier = currentSessionIdentifier
						channelHandle.setSessionIdentifier(null)
					}
				}
			}
		}

		try {
			// loop and send makes a computer happy
			while (true) {
				// set up connection to PAOS endpoint
				// if this one fails we may not continue
				conn = openHttpStream()

				var isReusable: Boolean
				// send as long as connection is valid
				try {
					do {
						// save the last message we sent to the eID-Server.
						if (msg is ResponseBaseType) {
							lastResponse = msg
							// save first minor code if there is one returned from our stack
							if (firstOecMinorError == null) {
								val r = lastResponse.getResult()
								if (r != null) {
									val minor = r.getResultMinor()
									if (minor != null) {
										firstOecMinorError = minor
									}
								}
							}
						}
						// prepare request
						val resource = tlsHandler.getResource()
						val req = BasicHttpEntityEnclosingRequest("POST", resource)
						setDefaultHeader(req, tlsHandler.getServerAddress())
						req.setHeader(HEADER_KEY_PAOS, headerValuePaos)
						req.setHeader("Accept", "text/xml, application/xml, application/vnd.paos+xml")

						val reqContentType = ContentType.create("application/vnd.paos+xml", "UTF-8")
						dumpHttpRequest(logger, "before adding content", req)
						val reqMsgStr = createPAOSResponse(msg!!)
						val reqMsg = StringEntity(reqMsgStr, reqContentType)
						req.entity = reqMsg
						req.setHeader(reqMsg.getContentType())
						req.setHeader("Content-Length", reqMsg.contentLength.toString())
						// send request and receive response
						logger.debug { "Sending HTTP request." }
						val response = httpexecutor.execute(req, conn, ctx)
						logger.debug { "HTTP response received." }
						val statusCode = response.statusLine.statusCode

						try {
							checkHTTPStatusCode(statusCode)
						} catch (ex: PAOSConnectionException) {
							// The eID-Server or at least the test suite may have aborted the communication after an
							// response with error. So check the status of our last response to the eID-Server
							if (lastResponse != null) {
								checkResult<ResponseBaseType>(lastResponse)
							}
							throw ex
						}

						conn.receiveResponseEntity(response)
						val entity = response.entity
						val entityData = toByteArray(entity.content)
						dumpHttpResponse(logger, response, entityData)
						try {
							// consume entity
							val requestObj = processPAOSRequest(ByteArrayInputStream(entityData))

							// break when message is startpaosresponse
							if (requestObj is StartPAOSResponse) {
								val startPAOSResponse = requestObj

								// if the last error was a schema validation error, then we must communicate that to caller
								val validateEx = validationError.derefNonblocking()
								if (validateEx != null) {
									logger.debug { "Previous validation error found, terminating process." }
									throw validateEx
								}

								// Some eID-Servers ignore error from previous steps so check whether our last message was ok.
								// This does not in case we sent a correct message with wrong content and the eID-Server returns
								// an ok.
								if (lastResponse != null) {
									checkResult<ResponseBaseType>(lastResponse)
								}
								checkResult<StartPAOSResponse>(startPAOSResponse)
								return startPAOSResponse
							}
							if (requestObj is DIDAuthenticate) {
								val asDidAuthenticate = requestObj
								val currentHandle = asDidAuthenticate.getConnectionHandle()
								if (currentHandle != null) {
									val currentSlotHandle = currentHandle.getSlotHandle()
									val fixedSlotHandle = fixSlotHandle(fakeSlotHandle, currentSlotHandle, dynCtx)
									currentHandle.setSlotHandle(fixedSlotHandle)

									if (internalSessionIdentifier != null) {
										var currentChannelHandle = currentHandle.getChannelHandle()
										if (currentChannelHandle == null) {
											currentChannelHandle = ChannelHandleType()
											currentHandle.setChannelHandle(currentChannelHandle)
										}
										val currentSessionIdentifier = currentChannelHandle.getSessionIdentifier()
										if (internalSessionIdentifier != currentSessionIdentifier) {
											currentChannelHandle.setSessionIdentifier(internalSessionIdentifier)
										}
									}
								}
							} else if (requestObj is Transmit) {
								val asTransmit = requestObj
								val currentSlotHandle = asTransmit.getSlotHandle()
								val fixedSlotHandle = fixSlotHandle(fakeSlotHandle, currentSlotHandle, dynCtx)
								asTransmit.setSlotHandle(fixedSlotHandle)
							}

							// send via dispatcher
							msg = dispatcher.deliver(requestObj)
						} catch (ex: DocumentValidatorException) {
							logger.error(ex) { "PAOS input message failed to validate." }

							// the ecard API forces us to interpret the message because the response must be the equivalent message not a fault
							val responseObj = synthesizeObj(ByteArrayInputStream(entityData), ex)
							if (responseObj != null) {
								msg = responseObj
								if (!validationError.isDelivered()) {
									validationError.deliver(ex)
								}
							} else {
								// do the right thing and throw an error
								throw ex
							}
						}

						// check if connection can be used one more time
						isReusable = reuse.keepAlive(response, ctx)
						connectionDropped = false
					} while (isReusable)
				} catch (ex: IOException) {
					if (!connectionDropped) {
						connectionDropped = true
						logger.warn { "PAOS server closed the connection. Trying to connect again." }
					} else {
						val errMsg = "Error in the link to the PAOS server."
						logger.error { errMsg }
						throw PAOSException(ErrorTranslations.DELIVERY_FAILED, ex)
					}
				}
			}
		} catch (ex: HttpException) {
			throw PAOSException(ErrorTranslations.DELIVERY_FAILED, ex)
		} catch (ex: SOAPException) {
			throw PAOSException(ErrorTranslations.SOAP_MESSAGE_FAILURE, ex)
		} catch (ex: DocumentValidatorException) {
			throw PAOSException(ErrorTranslations.SCHEMA_VALIDATION_FAILED, ex)
		} catch (ex: MarshallingTypeException) {
			throw PAOSDispatcherException(ErrorTranslations.MARSHALLING_ERROR, ex)
		} catch (ex: InvocationTargetException) {
			throw PAOSDispatcherException(ErrorTranslations.DISPATCHER_ERROR, ex)
		} catch (ex: TransformerException) {
			throw DispatcherException(ex.message, ex)
		} catch (ex: WSHelper.WSException) {
			val newEx = PAOSException(ex)
			if (firstOecMinorError != null) {
				newEx.additionalResultMinor = firstOecMinorError
			}
			throw newEx
		} finally {
			try {
				conn?.close()
			} catch (_: IOException) {
// 		throw new PAOSException(ex);
			}
		}
	}

	private fun fixSlotHandle(
		fakeSlotHandle: ByteArray?,
		currentSlotHandle: ByteArray?,
		dynCtx: DynamicContext,
	): ByteArray? {
		var currentSlotHandle = currentSlotHandle
		if (fakeSlotHandle != null && ByteUtils.compare(currentSlotHandle, fakeSlotHandle)) {
			val conHandle = dynCtx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType?
			if (conHandle != null) {
				currentSlotHandle = conHandle.getSlotHandle()
			}
		}
		return currentSlotHandle
	}

	private fun openHttpStream(): StreamHttpClientConnection {
		val conn: StreamHttpClientConnection
		try {
			logger.debug { "Opening connection to PAOS server." }
			val handler = tlsHandler.createTlsConnection()
			conn = StreamHttpClientConnection(handler.getInputStream(), handler.getOutputStream())
			logger.debug { "Connection to PAOS server established." }
			return conn
		} catch (ex: IOException) {
			throw PAOSConnectionException(ex)
		} catch (ex: URISyntaxException) {
			throw PAOSConnectionException(ex)
		}
	}

	/**
	 * Check the status code returned from the server.
	 * If the status code indicates an error, a PAOSException will be thrown.
	 *
	 * @param statusCode The status code we received from the server
	 * @throws PAOSException If the server returned a HTTP error code
	 */
	private fun checkHTTPStatusCode(statusCode: Int) {
		// Check the result code. According to the PAOS Spec section 9.4 the server has to send 202
		// All tested test servers return 200 so accept both but generate a warning message in case of 200
		if (statusCode != 200 && statusCode != 202) {
			throw PAOSConnectionException(ErrorTranslations.INVALID_HTTP_STATUS, statusCode)
		} else if (statusCode == 200) {
			val msg2 = (
				"The PAOS endpoint sent the http status code 200 which does not conform to the " +
					"PAOS specification. (See section 9.4 Processing Rules of the PAOS Specification)"
			)
			logger.warn { msg2 }
		}
	}

	/**
	 * Creates a String with all available services.
	 *
	 * @return A String containing all available services.
	 */
	private fun buildServiceString(): String {
		val builder = StringBuilder()
		for (service in dispatcher.serviceList) {
			builder.append(";")
			builder.append('"')
			builder.append(service)
			builder.append('"')
		}
		return builder.toString()
	}

	private fun synthesizeObj(
		content: ByteArrayInputStream,
		cause: DocumentValidatorException,
	): Any? {
		try {
			val doc = m.str2doc(content)
			val msg = m.doc2soap(doc)
			val body = msg.soapBody.childElements.get(0)
			val obj = m.unmarshal(body)

			if (obj is DIDAuthenticate) {
				val didAuth = obj
				val protoIn = didAuth.getAuthenticationProtocolData()

				val r = makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, cause.message)
				val res: DIDAuthenticateResponse =
					WSHelper.makeResponse(
						DIDAuthenticateResponse::class.java,
						r,
					)
				val protoData = EmptyResponseDataType()
				res.setAuthenticationProtocolData(protoData)
				if (protoIn != null) {
					protoData.setProtocol(protoIn.getProtocol())
				}

				return res
			}

			// no special case needed
			return null
		} catch (_: IOException) {
			// in case of error, just quit
			return null
		} catch (_: SAXException) {
			return null
		} catch (_: WSMarshallerException) {
			return null
		}
	}

	companion object {
		const val HEADER_KEY_PAOS: String = "PAOS"

		val RELATES_TO: QName = QName(ECardConstants.WS_ADDRESSING, "RelatesTo")
		val REPLY_TO: QName = QName(ECardConstants.WS_ADDRESSING, "ReplyTo")
		val MESSAGE_ID: QName = QName(ECardConstants.WS_ADDRESSING, "MessageID")
		val ADDRESS: QName = QName(ECardConstants.WS_ADDRESSING, "Address")

		val PAOS_PAOS: QName = QName(ECardConstants.PAOS_VERSION_20, "PAOS")
		val PAOS_VERSION: QName = QName(ECardConstants.PAOS_VERSION_20, "Version")
		val PAOS_ENDPOINTREF: QName = QName(ECardConstants.PAOS_VERSION_20, "EndpointReference")
		val PAOS_ADDRESS: QName = QName(ECardConstants.PAOS_VERSION_20, "Address")
		val PAOS_METADATA: QName = QName(ECardConstants.PAOS_VERSION_20, "MetaData")
		val PAOS_SERVICETYPE: QName = QName(ECardConstants.PAOS_VERSION_20, "ServiceType")
	}
}
