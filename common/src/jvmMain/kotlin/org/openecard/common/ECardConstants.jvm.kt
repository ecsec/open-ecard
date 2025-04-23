/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
 ***************************************************************************/

package org.openecard.common

import java.math.BigInteger
import java.util.*

/**
 * Constants regarding the eCard-API-Framework.
 * Most of the values in here are defined in BSI TR-03112-1.
 *
 * @author Tobias Wich
 */
object ECardConstants {
	private const val ECARD_PREFIX = "http://www.bsi.bund.de/ecard/api/1.1/"
	private const val MAJOR_PREFIX = ECARD_PREFIX + "resultmajor#"
	private const val DSS_PREFIX = "urn:oasis:names:tc:dss:1.0:"
	private const val MINOR_PREFIX = ECARD_PREFIX + "resultminor/"

	//
	// Inofficial Constants
	//
	const val CONTEXT_HANDLE_DEFAULT_SIZE: Int = 16
	const val SLOT_HANDLE_DEFAULT_SIZE: Int = 24

	const val UNKNOWN_CARD: String = "http://bsi.bund.de/cif/unknown"
	const val NPA_CARD_TYPE: String = "http://bsi.bund.de/cif/npa.xml"

	const val PAOS_NEXT: String = ECARD_PREFIX + "PAOS/GetNextCommand"

	const val PATH_SEC_PROTO_TLS_PSK: String = "urn:ietf:rfc:4279"
	const val PATH_SEC_PROTO_MTLS: String = "urn:ietf:rfc:5246"

	const val BINDING_HTTP: String = "urn:ietf:rfc:2616"
	const val BINDING_PAOS: String = "urn:liberty:paos:2006-08"

	const val ACTOR_NEXT: String = "http://schemas.xmlsoap.org/soap/actor/next"
	const val SOAP_ENVELOPE: String = "http://schemas.xmlsoap.org/soap/envelope/"
	const val PAOS_VERSION_11: String = "urn:liberty:paos:2003-08"
	const val PAOS_VERSION_20: String = "urn:liberty:paos:2006-08"
	const val WS_ADDRESSING: String = "http://www.w3.org/2005/03/addressing"

	@JvmField
	val ECARD_API_VERSION_MAJOR: BigInteger = BigInteger.valueOf(1)

	@JvmField
	val ECARD_API_VERSION_MINOR: BigInteger = BigInteger.valueOf(1)

	@JvmField
	val ECARD_API_VERSION_SUBMINOR: BigInteger = BigInteger.valueOf(5)

	private val msgMap: Map<String, String> =
		mapOf(
			// major types
			Major.OK to "No error occurred during execution of the operation.",
			Major.WARN to "If the result of the operation is in principle OK, but there is a detail which may " +
				"require closer investigation, a warning is given as a response.",
			Major.ERROR to "An error occurred during execution of the operation.",
			Major.NEXT to "This result appears if at least one more request is expected within a protocol.",
			// minor App
			// minor App common
			Minor.App.NO_PERM to "Use of the function by the client application is not permitted.",
			Minor.App.INT_ERROR to "Internal error.",
			Minor.App.PARM_ERROR to "There was some problem with a provided or omitted parameter.",
			// minor SAL
			Minor.SAL.UNKNOWN_HANDLE to "Unknown connection handle specified.",
			Minor.SAL.UNKNOWN_CARDTYPE to
				"Unknown card type specified.",
			// minor SAL support
			Minor.SAL.Support.REPO_UNREACHABLE to "The CardInfo repository server is not accessible",
		)

	@Deprecated("")
	fun URI2Msg(uri: String): String {
		val result = msgMap[uri]
		return if ((result != null)) result else ""
	}

	object CIF {
		const val GET_SPECIFIED: String = ECARD_PREFIX + "cardinfo/action#getSpecifiedFile"
		const val GET_RELATED: String = ECARD_PREFIX + "cardinfo/action#getRelatedFiles"
		const val GET_OTHER: String = ECARD_PREFIX + "cardinfo/action#getOtherFiles"
	}

	object Profile {
		const val ECARD_1_1: String = "http://www.bsi.bund.de/ecard/api/1.1"
	}

	object Protocol {
		const val PIN_COMPARE: String = "urn:oid:1.3.162.15480.3.0.9"
		const val MUTUAL_AUTH: String = "urn:oid:1.3.162.15480.3.0.12"
		const val EAC_GENERIC: String = "urn:oid:1.3.162.15480.3.0.14"
		const val EAC2: String = "urn:oid:1.3.162.15480.3.0.14.2"
		const val RSA_AUTH: String = "urn:oid:1.3.162.15480.3.0.15"
		const val GENERIC_CRYPTO: String = "urn:oid:1.3.162.15480.3.0.25"
		const val TERMINAL_AUTH: String = "urn:oid:0.4.0.127.0.7.2.2.2"
		const val CHIP_AUTH: String = "urn:oid:0.4.0.127.0.7.2.2.3"
		const val PACE: String = "urn:oid:0.4.0.127.0.7.2.2.4"
		const val RESTRICTED_ID: String = "urn:oid:0.4.0.127.0.7.2.2.5"
	}

	class IFD {
		object Protocol {
			@JvmStatic
			fun isProtocol(proto: String): Boolean =
				proto == T0 || proto == T1 || proto == T2 || proto == TYPE_A || proto == TYPE_B

			@JvmStatic
			fun isWired(proto: String): Boolean = proto == T0 || proto == T1

			@JvmStatic
			fun isWireless(proto: String): Boolean = !isWired(proto)

			const val T0: String = "urn:iso:std:iso-iec:7816:-3:tech:protocols:T-equals-0"
			const val T1: String = "urn:iso:std:iso-iec:7816:-3:tech:protocols:T-equals-1"
			const val T2: String = "urn:iso:std:iso-iec:10536:tech:protocols:T-equals-2"
			const val TYPE_A: String = "urn:iso:std:iso-iec:14443:-2:tech:protocols:Type-A"
			const val TYPE_B: String = "urn:iso:std:iso-iec:14443:-2:tech:protocols:Type-B"
		}
	}

	//
	// Major values
	//

	object Major {
		const val OK: String = MAJOR_PREFIX + "ok"
		const val PENDING: String = DSS_PREFIX + "profiles:asynchronousprocessing:resultmajor:Pending"
		const val WARN: String = MAJOR_PREFIX + "warning"
		const val ERROR: String = MAJOR_PREFIX + "error"
		const val NEXT: String = MAJOR_PREFIX + "nextRequest"
	}

	//
	// Minor values
	//

	class Minor {
		/**
		 * Application layer minor codes.
		 * The error codes assigned to this layer indicate operation errors of the applications using the
		 * eCard-API-Framework. These include in particular incorrect or faulty (configuration) data.
		 */
		object App {
			private const val APP_PREFIX = MINOR_PREFIX + "al"
			private const val APP_CIF_PREFIX = APP_PREFIX + "/CardInfo#" // CardInfo
			private const val APP_FW_PREFIX = APP_PREFIX + "/FrameworkUpdate#" // Framework Update
			private const val APP_IFD_PREFIX = APP_PREFIX + "/IFD#" // IFD
			private const val APP_TV_PREFIX = APP_PREFIX + "/TrustedViewer#" // TrustedViewer
			private const val APP_TSL_PREFIX = APP_PREFIX + "/TSL#" // TrustedViewer

			/**
			 * There was some unknown error.
			 * An unexpected error has occurred during processing which cannot be represented by the standard codes or
			 * specific service error codes. The error and detail texts can describe the error more closely.
			 */
			const val UNKNOWN_ERROR: String = APP_PREFIX + "/common#unknownError"

			/**
			 * Use of the function by the client application is not permitted.
			 */
			const val NO_PERM: String = APP_PREFIX + "/common#noPermission"

			/**
			 * Internal error.
			 */
			const val INT_ERROR: String = APP_PREFIX + "/common#internalError"
			const val PARM_ERROR: String = APP_PREFIX + "/common#parameterError"

			/**
			 * API function unknown.
			 */
			const val UNKNOWN_API: String = APP_PREFIX + "/common#unknownAPIFunction"

			/**
			 * Framework or layer not initialised.
			 */
			const val NOT_INITIALIZED: String = APP_PREFIX + "/common#notInitialized"

			/**
			 * Warning indicating termination of an active session.
			 */
			const val CON_DISCONNECT: String = APP_PREFIX + "/common#warningConnectionDisconnected"

			/**
			 * Warning indicating termination of an active session.
			 */
			const val SESS_TERMINATED: String = APP_PREFIX + "/common#SessionTerminatedWarning"

			/**
			 * Parameter error.
			 * There was some problem with a provided or omitted parameter.
			 */
			const val INCORRECT_PARM: String = APP_PREFIX + "/common#incorrectParameter"
			const val COMMUNICATION_ERROR: String = APP_PREFIX + "/common#communicationError"

			/**
			 * Application layer - CardInfo management minor codes.
			 * The error codes of the errors which can occur in conjunction with faulty invocations of the management
			 * functions for CardInfo structures are grouped in the CardInfo category.
			 */
			object CIF {
				/**
				 * CardInfo file cannot be added.
				 */
				const val ADD_NOT_POSSIBLE: String = APP_CIF_PREFIX + "addNotPossible"

				/**
				 * CardInfo file does not exist.
				 */
				const val NOT_EXISTING: String = APP_CIF_PREFIX + "notExisting"

				/**
				 * CardInfo file cannot be deleted.
				 */
				const val DEL_NOT_POSSIBLE: String = APP_CIF_PREFIX + "deleteNotPossible"

				/**
				 * The CardInfo file already exists.
				 */
				const val ALREADY_EXISTING: String = APP_CIF_PREFIX + "alreadyExisting"

				/**
				 * The CardInfo file is incorrect.
				 */
				const val INCORRECT_FILE: String = APP_CIF_PREFIX + "incorrectFile"
			}

			/**
			 * Application layer - Framework Update minor codes.
			 * This section describes the error codes which can occur during execution of a framework update.
			 */
			object FW {
				/**
				 * Update service is not accessible.
				 */
				const val SERVICE_NA: String = APP_FW_PREFIX + "serviceNotAvailable"

				/**
				 * Unknown module.
				 */
				const val UNKNOWN_MODULE: String = APP_FW_PREFIX + "unknownModule"

				/**
				 * Invalid version number for module.
				 */
				const val INVALID_VERSION: String = APP_FW_PREFIX + "invalidVersionNumber"

				/**
				 * Operating system not supported.
				 */
				const val OS_NOT_SUPPORTED: String = APP_FW_PREFIX + "operationSystemNotSupported"

				/**
				 * No available space.
				 */
				const val NO_SPACE: String = APP_FW_PREFIX + "noSpaceAvailable"

				/**
				 * Access denied.
				 */
				const val SEC_NOT_SATISFIED: String = APP_FW_PREFIX + "securityConditionsNotSatisfied"
			}

			/**
			 * Application layer - IFD minor codes.
			 * The following errors can occur as the result of incorrect information during management of card
			 * terminals. These errors are grouped in the IFD category.
			 */
			object IFD {
				/**
				 * The card terminal configuration cannot be written.
				 */
				const val WRITE_CONF_IMPOSS: String = APP_IFD_PREFIX + "writeConfigurationNotPossible"

				/**
				 * The card terminal cannot be added.
				 */
				const val COULD_NOT_ADD: String = APP_IFD_PREFIX + "couldNotAdd"

				/**
				 * The card terminal cannot be deleted.
				 */
				const val DEL_IMPOSS: String = APP_IFD_PREFIX + "deleteNotPossible"

				/**
				 * The card terminal already exists.
				 */
				const val ADD_IMPOSS: String = APP_IFD_PREFIX + "addNotPossible"
			}

			/**
			 * Application layer - Trusted Viewer minor codes.
			 * The following errors can occur during use of the management functions for trusted viewers.
			 */
			object Viewer {
				/**
				 * The trusted viewer cannot be deleted.
				 */
				const val DEL_IMPOSS: String = APP_TV_PREFIX + "deleteNotPossible"

				/**
				 * Invalid TrustedViewerId.
				 */
				const val INVALID_ID: String = APP_TV_PREFIX + "invalidID"

				/**
				 * Invalid configuration information for the trusted viewer.
				 */
				const val INVALID_CONF: String = APP_TV_PREFIX + "invalidConfiguration"

				/**
				 * The trusted viewer already exists with the entered ID.
				 */
				const val EXISTING: String = APP_TV_PREFIX + "alreadyExisting"
			}

			/**
			 * Application layer - TSL minor codes.
			 * The following error codes are generated by the functions for management of trust service status lists
			 * (TSLs).
			 */
			object TSL {
				/**
				 * TSLSequenceNumber has been ignored.
				 * As only a `TSLSequenceNumber` but no `SchemeName` has been specified, the
				 * `TSLSequenceNumber`-element has been ignored.
				 */
				const val SEQNUM_IGNORED: String = APP_TSL_PREFIX + "TSLSequenceNumberIgnoredWarning"
			}
		}

		/**
		 * Dispatcher layer minor codes.
		 * The errors caused by communication to and from the eCard-API-Framework are described here.
		 */
		object Disp {
			private const val DP_PREFIX = MINOR_PREFIX + "dp"

			/**
			 * Time exceeded (timeout).
			 * The operation was terminated as the set time was exceeded.
			 */
			const val TIMEOUT: String = DP_PREFIX + "#timeoutError"
			const val UNKNOWN_CHANNEL_HANDLE: String = DP_PREFIX + "#unknownChannelHandle"

			/**
			 * Communication error.
			 */
			const val COMM_ERROR: String = DP_PREFIX + "#communicationError"

			/**
			 * Failure to open a trusted channel.
			 */
			const val CHANNEL_ESTABLISHMENT_FAILED: String = DP_PREFIX + "#trustedChannelEstablishmentFailed"

			/**
			 * Unknown protocol.
			 */
			const val UNKOWN_PROTOCOL: String = DP_PREFIX + "#unknownProtocol"

			/**
			 * Unknown cipher suite.
			 */
			const val UNKNOWN_CIPHER: String = DP_PREFIX + "#unknownCipherSuite"

			/**
			 * Unknown web service binding.
			 */
			const val UNKNOWN_BINDING: String = DP_PREFIX + "#unknownWebserviceBinding"

			/**
			 * Node not reachable.
			 */
			const val NODE_NOT_REACHABLE: String = DP_PREFIX + "#nodeNotReachable"
			// this one is mentioned in EstablishContexResponse TR-03112-6
			/**
			 * Invalid channel handle.
			 */
			const val INVALID_CHANNEL_HANDLE: String = DP_PREFIX + "#invalidChannelHandle"
		}

		/**
		 * Identity layer minor codes.
		 * Errors caused by the identity layer of the eCard-API-Framework are assigned to the identity layer.
		 */
		object Ident {
			private const val IL_PREFIX = MINOR_PREFIX + "il"
			private const val ALG_PFX = IL_PREFIX + "/algorithm#"
			private const val CR_PFX = IL_PREFIX + "/certificateRequest#"
			private const val ENC_PFX = IL_PREFIX + "/encryption#"
			private const val KEY_PFX = IL_PREFIX + "/key#"
			private const val SERVICE_PFX = IL_PREFIX + "/service#"
			private const val SIG_PFX = IL_PREFIX + "/signature#"
			private const val VIEW_PFX = IL_PREFIX + "/viewer#"

			/**
			 * Identity layer - Algorithm minor codes.
			 * The errors of the identity layer caused by the algorithms stated for use are grouped in this category.
			 */
			object Algorithm {
				/**
				 * Stated hash algorithm is not supported.
				 */
				const val HASH_NOT_SUPPORTED: String = ALG_PFX + "hashAlgorithmNotSupported"

				/**
				 * The stated signature algorithm is not supported.
				 */
				const val SIG_NOT_SUPPORTED: String = ALG_PFX + "signatureAlgorithmNotSupported"
			}

			/**
			 * Identity layer - CertificateRequest minor codes.
			 * The errors of the identity layer which can occur when a certificate is requested are grouped in this
			 * category.
			 */
			object CertificateRequest {
				/**
				 * Unknown attribute in the certificate application.
				 */
				const val UNKNOWN_ATTR: String = CR_PFX + "unknownAttribute"

				/**
				 * Generation of the certificate application failed.
				 */
				const val CREATION_FAILED: String = CR_PFX + "creationOfCertificateRequestFailed"

				/**
				 * Submission of the certificate application failed.
				 */
				const val SUBMISSION_FAILED: String = CR_PFX + "submissionFailed"

				/**
				 * Unknown transaction identifier.
				 */
				const val UNKOWN_TRANSACTION: String = CR_PFX + "unknownTransactionID"

				/**
				 * Not possible to collect the certificate.
				 */
				const val DOWNLOAD_FAILED: String = CR_PFX + "certificateDownloadFailed"

				/**
				 * No subject specified in request.
				 */
				const val SUBJECT_MISSING: String = CR_PFX + "subjectMissing"
			}

			/**
			 * Identity layer - Encryption minor codes.
			 * The Encryption category contains the errors of the identity layer which occur during encryption or
			 * decryption.
			 */
			object Encryption {
				/**
				 * Specific nodes can only be encrypted in case of an XML document.
				 */
				const val NODES_ENC: String = ENC_PFX + "encryptionOfCertainNodesOnlyForXMLDocuments"

				/**
				 * The encryption format is not supported.
				 */
				const val FORMAT_NOT_SUPPORTED: String = ENC_PFX + "encryptionFormatNotSupported"

				/**
				 * The encryption certificate of an intended recipient is invalid.
				 */
				const val INVALID_CERT: String = ENC_PFX + "invalidCertificate"
			}

			/**
			 * Identity layer - Key minor codes.
			 * The errors of the identity layer which can occur when a key is generated or keys are used are grouped in
			 * this category.
			 */
			object Key {
				/**
				 * Key generation is not possible.
				 */
				const val KEYGEN_NOT_POSSIBLE: String = KEY_PFX + "keyGenerationNotPossible"

				/**
				 * The stated encryption algorithm is not supported.
				 */
				const val ENC_ALG_NOT_SUPPORTED: String = KEY_PFX + "encryptionAlgorithmNotSupported"
			}

			/**
			 * Identity layer - Service minor codes.
			 * The Service category contains the errors of the identity layer which occur due to the non-accessibility
			 * of the service to be used.
			 */
			object Service {
				/**
				 * The OCSP responder is inaccessible.
				 */
				const val OCSP_UNREACHABLE: String = SERVICE_PFX + "ocspResponderUnreachable"

				/**
				 * The directory service is inaccessible.
				 */
				const val DIR_UNREACHABLE: String = SERVICE_PFX + "directoryServiceUnreachable"

				/**
				 * The time stamp service is inaccessible.
				 */
				const val TS_UNREACHABLE: String = SERVICE_PFX + "timeStampServiceUnreachable"
			}

			/**
			 * Identity layer - Signature minor codes.
			 * All errors and warnings of the identity layer which occur during signature generation or signature
			 * verification are assigned to the Signature category.
			 */
			object Signature {
				/**
				 * The signature format is not supported.
				 * The stated signature or time stamp format is not supported.
				 */
				const val FORMAT_NOT_SUPPORTED: String = SIG_PFX + "signatureFormatNotSupported"

				/**
				 * PDF signature for non-PDF document requested.
				 */
				const val SIG_NON_PDFDOC: String = SIG_PFX + "PDFSignatureForNonPDFDocument"

				/**
				 * IncludeEContent not possible.
				 * This warning is returned if the `IncludeEContent` flag is set when a PDF signature or a time
				 * stamp is generated, or when a hash value is transmitted for signature generation.
				 */
				const val INCLUDE_ECONTENT_NOT_POSSIBLE: String = SIG_PFX + "unableToIncludeEContentWarning"

				/**
				 * The SignaturePlacement flag was ignored.
				 * This warning is returned when the `SignaturePlacement` flag was set for a non-XML-based signature.
				 */
				const val IGNORE_SIG_PLACEMENT: String = SIG_PFX + "ignoredSignaturePlacementFlagWarning"

				/**
				 * The certificate is not available.
				 * The stated certificate is not available for the function. This could be due to an incorrect
				 * reference or a deleted data field.
				 */
				const val CERT_NOT_FOUND: String = SIG_PFX + "certificateNotFound"

				/**
				 * The certificate cannot be interpreted.
				 * The format of the stated certificate is unknown and cannot be interpreted.
				 */
				const val WRONG_CERT_FORMAT: String = SIG_PFX + "certificateFormatNotCorrect"

				/**
				 * Invalid certificate reference.
				 */
				const val INVALID_CERT_REF: String = SIG_PFX + "invalidCertificateReference"

				/**
				 * The certificate chain is interrupted.
				 * The stated certificate chain is interrupted. It is therefore not possible to complete full
				 * verification up to the root certificate.
				 */
				const val CHAIN_INTERRUPTED: String = SIG_PFX + "certificateChainInterrupted"

				/**
				 * It was not possible to resolve the object reference.
				 */
				const val OBJREF_UNRESOLVEABLE: String = SIG_PFX + "resolutionOfObjectReferenceImpossible"

				/**
				 * The transformation algorithm is not supported.
				 */
				const val TRANSFORM_NOT_SUPPORTED: String = SIG_PFX + "transformationAlgorithmNotSupported"

				/**
				 * The viewer is unknown or not available.
				 */
				const val UNKNOWN_VIEWER: String = SIG_PFX + "unknownViewer"

				/**
				 * The certificate path was not checked.
				 * Due to some problems it was not possible to validate the certificate path.
				 */
				const val CERT_PATH_NOT_VALIDATED: String = SIG_PFX + "certificatePathNotValidated"

				/**
				 * The certificate status was not checked.
				 * Due to some problems it was not possible to check the certificate status.
				 */
				const val CERT_STATUS_NOT_CHECKED: String = SIG_PFX + "certificateStatusNotChecked"

				/**
				 * The signature manifest was not verified.
				 * This is a warning.
				 */
				const val SIG_MANIFEST_NOT_CHECKED: String = SIG_PFX + "signatureManifestNotCheckedWarning"

				/**
				 * The suitability of the signature and hash algorithms was not checked.
				 */
				const val ALGS_NOT_CHECKED: String = SIG_PFX + "suitabilityOfAlgorithmsNotChecked"

				/**
				 * No signature-related data were found (detached signature without EContent).
				 */
				const val DETACHED_NO_CONTENT: String = SIG_PFX + "detachedSignatureWithoutEContent"

				/**
				 * It is not possible to interpret revocation information.
				 */
				const val WRONG_REVOCATION_INFO: String = SIG_PFX + "improperRevocationInformation"

				/**
				 * The signature format is incorrect.
				 * The format of the transmitted signature does not correspond to the respective specification. This
				 * error occurs when a supported format is recognised (e.g. in accordance with [RFC3275] or [RFC3369]),
				 * but the signature does not meet the respective form requirements. If the transmitted format was
				 * already not recognised, error [.FORMAT_NOT_SUPPORTED] is returned.
				 */
				const val INVALID_SIG_FORMAT: String = SIG_PFX + "invalidSignatureFormat"

				/**
				 * The security of the signature algorithm is not suitable at the relevant point of time.
				 */
				const val SIGALG_NOT_SUITABLE: String = SIG_PFX + "signatureAlgorithmNotSuitable"

				/**
				 * The security of the hash algorithm is not suitable at the relevant point of time.
				 */
				const val HASHALG_NOT_SUITABLE: String = SIG_PFX + "hashAlgorithmNotSuitable"

				/**
				 * The certificate path is invalid.
				 */
				const val CERT_PATH_INVALID: String = SIG_PFX + "invalidCertificatePath"

				/**
				 * The certificate has been revoked.
				 */
				const val CERT_REVOKED: String = SIG_PFX + "certificateRevoked"

				/**
				 * The reference time is outside the validity period of a certificate.
				 */
				const val CERT_EXPIRED: String = SIG_PFX + "referenceTimeNotWithinCertificateValidityPeriod"

				/**
				 * Invalid extensions in a certificate.
				 */
				const val INVALID_CERT_EXT: String = SIG_PFX + "invalidCertificateExtension"

				/**
				 * Verification of a signature manifest has failed.
				 */
				const val SIG_MANIFEST_WRONG: String = SIG_PFX + "signatureManifestNotCorrect"

				/**
				 * The stated `SignatureType` does not support `SignatureForm` parameter.
				 */
				const val SIG_NOT_SUPPORT_FORM_CLARIFICATION: String =
					SIG_PFX + "signatureTypeDoesNot" +
						"SupportSignatureFormClarificationWarning"

				/**
				 * Unknown `SignatureForm`.
				 */
				const val UNKNOWN_SIG_FORM: String = SIG_PFX + "unknownSignatureForm"

				/**
				 * `IncludeObject` only permitted with XML signatures.
				 */
				const val INCLUDE_ONLY_XML: String = SIG_PFX + "includeObjectOnlyForXMLSignatureAllowedWarning"

				/**
				 * It was not possible to resolve the XPath expression.
				 */
				const val XPATH_ERROR: String = SIG_PFX + "xPathEvaluationError"

				/**
				 * Wrong message digest.
				 * The calculated digest of the message is not equal to the message digest in the
				 * `MessageDigest`-attribute of the CMS-Signature or the `DigestValue`-element of the
				 * XML-signature respectively.
				 */
				const val WRONG_MSG_DIGEST: String = SIG_PFX + "wrongMessageDigest"

				/**
				 * IFD inconsistency.
				 * The compatibility-check with the present IFD failed.
				 */
				const val IFD_INCONSISTENCY: String = SIG_PFX + "IFDInconsistency"
			}

			/**
			 * Identity layer - Viewer minor codes.
			 * The Viewer category contains the errors which occur in connection with the trusted viewer within the
			 * identity layer.
			 */
			object Viewer {
				/**
				 * Unsuitable stylesheet for transmitted document.
				 */
				const val UNSUITABLE_SS: String = VIEW_PFX + "unsuitableStylesheetForDocument"

				/**
				 * Cancellation by the viewer.
				 */
				const val USER_CANCEL: String = VIEW_PFX + "cancelationByUser"

				/**
				 * Time exceeded (timeout).
				 * The operation was terminated as the set time was exceeded.
				 */
				const val TIMEOUT: String = VIEW_PFX + "timeout"

				/**
				 * The ViewerMessage is too long.
				 */
				const val MSG_TOO_LONG: String = VIEW_PFX + "viewerMessageTooLong"
			}
		}

		/**
		 * Service-Access layer minor codes.
		 * Errors resulting from the Support-Interface or the ISO24727-3-Interface are assigned to the
		 * Service-Access-Layer.
		 */
		object SAL {
			private const val SAL_PFX = MINOR_PREFIX + "sal"
			private const val SUPPORT_PFX = SAL_PFX + "/support#"
			private const val EAC_PFX = SAL_PFX + "/mEAC#"

			/**
			 * Cancellation by the user.
			 * A necessary user intervention (e.g. PIN entry or confirmation of the signature generation in the trusted
			 * viewer) was terminated by cancellation.
			 */
			const val CANCELLATION_BY_USER: String = SAL_PFX + "#cancellationByUser"

			/**
			 * The name already exists.
			 */
			const val NAME_EXISTS: String = SAL_PFX + "#nameExists"

			/**
			 * The prerequisite is not met.
			 */
			const val PREREQUISITES_NOT_SATISFIED: String = SAL_PFX + "#prerequisitesNotSatisfied"

			/**
			 * Unknown protocol.
			 */
			const val PROTOCOL_NOT_RECOGNIZED: String = SAL_PFX + "#protocolNotRecognized"

			/**
			 * Unsuitable protocol for the required action.
			 */
			const val INAPPROPRIATE_PROTOCOL_FOR_ACTION: String = SAL_PFX + "#inappropriateProtocolForAction"

			/**
			 * The verified signature is not valid.
			 */
			const val INVALID_SIGNATURE: String = SAL_PFX + "#invalidSignature"

			/**
			 * The selected key is not valid.
			 */
			const val INVALID_KEY: String = SAL_PFX + "#invalidKey"

			/**
			 * No initialisation carried out.
			 * The used operation requires initialisation.
			 */
			const val NOT_INITIALIZED: String = SAL_PFX + "#notInitialized"

			/**
			 * Warning - Too many results.
			 */
			const val TOO_MANY_RESULTS: String = SAL_PFX + "#tooManyResults"

			/**
			 * Warning - The connection has been disconnected.
			 */
			const val WARN_CONNECTION_DISCONNECT: String = SAL_PFX + "#warningConnectionDisconnected"

			/**
			 * Warning – An established session was terminated.
			 */
			const val WARN_SESSION_END: String = SAL_PFX + "#warningSessionEnded"

			/**
			 * The name does not exist.
			 * The stated name of a card application service, DID, Data Set etc. does not exist.
			 */
			const val NAMED_ENTITY_NOT_FOUND: String = SAL_PFX + "#namedEntityNotFound"

			/**
			 * The resources are insufficient.
			 */
			const val INSUFFICIENT_RES: String = SAL_PFX + "#insufficientResources"

			/**
			 * Access denied.
			 */
			const val SECURITY_CONDITION_NOT_SATISFIED: String = SAL_PFX + "#securityConditionNotSatisfied"

			/**
			 * Exclusive reservation is not possible.
			 * Exclusive reservation of the eCard is not possible, because other applications are currently accessing
			 * the eCard.
			 */
			const val EXCLUSIVE_NA: String = SAL_PFX + "#exclusiveNotAvailable"

			/**
			 * Warning - there is no active session.
			 * This warning indicates that there is no active session, which can be terminated with
			 * `CardApplicationEndSession`.
			 */
			const val NO_SESSION: String = SAL_PFX + "#noActiveSession"

			/**
			 * Decryption not possible.
			 * No suitable keys for decryption found.
			 */
			const val DEC_NOT_POSSIBLE: String = SAL_PFX + "#decryptionNotPossible"

			/**
			 * Invalid access control information.
			 */
			const val INVALID_ACL: String = SAL_PFX + "#invalidAccessControlInformation"

			/**
			 * Unknown protocol.
			 * The requested protocol is unknown.
			 */
			const val UNKOWN_PROTOCOL: String = SAL_PFX + "#unknownProtocol"

			/**
			 * Unknown card type specified.
			 */
			const val UNKNOWN_CARDTYPE: String = SAL_PFX + "#unknownCardType"

			/**
			 * Unknown DID name specified.
			 */
			const val UNKOWN_DID_NAME: String = SAL_PFX + "#unknownDIDName"

			/**
			 * File not found.
			 */
			const val FILE_NOT_FOUND: String = SAL_PFX + "#fileNotFound"

			// Not in TR-03112-1, but GetCardInfoOrACD call ?!?
			const val UNKNOWN_HANDLE: String = SAL_PFX + "#unknownConnectionHandle"

			/**
			 * The requested session could not be created as one with the same identifier already exists.
			 */
			const val SESSION_EXISTS: String = SAL_PFX + "#sessionAlreadyExists"

			/**
			 * Service-Access layer - Support-Interface minor codes.
			 */
			object Support {
				/**
				 * Encoding not possible.
				 */
				const val ENCODING_ERROR: String = SUPPORT_PFX + "encodingError"

				/**
				 * Decoding not possible.
				 */
				const val DECODING_ERROR: String = SUPPORT_PFX + "decodingError"

				/**
				 * Validation of the schema has failed.
				 */
				const val SCHEMA_VAILD_FAILED: String = SUPPORT_PFX + "schemaValidationError"

				/**
				 * A warning occurred during validation of the schema.
				 */
				const val SCHEMA_VAILD_WARN: String = SUPPORT_PFX + "schemaValidationWarning"

				/**
				 * No suitable schema is available.
				 */
				const val NO_SCHEMA: String = SUPPORT_PFX + "noAppropriateSchema"

				/**
				 * The CardInfo repository server is not accessible.
				 */
				const val REPO_UNREACHABLE: String = SUPPORT_PFX + "cardInfoRepositoryUnreachable"
			}

			/**
			 * Service-Access layer - EAC minor codes.
			 */
			object EAC {
				const val AGE_VERIF_FAILED: String = EAC_PFX + "AgeVerificationFailedWarning"
				const val COMMUNITY_VERIF_FAILED: String = EAC_PFX + "CommunityVerificationFailedWarning"
				const val DOC_VALID_FAILED: String = EAC_PFX + "DocumentValidityVerificationFailed"
			}
		}

		/**
		 * IFD layer minor codes.
		 */
		object IFD {
			private const val IFD_PREFIX = MINOR_PREFIX + "ifdl"
			private const val IO_PREFIX = IFD_PREFIX + "/IO#"
			private const val TERM_PREFIX = IFD_PREFIX + "/terminal#"

			/**
			 * Time exceeded (timeout).
			 * The operation was terminated as the set time was exceeded.
			 */
			const val TIMEOUT_ERROR: String = IFD_PREFIX + "/common#timeoutError"

			/**
			 * Unknown context handle.
			 */
			const val INVALID_CONTEXT_HANDLE: String = IFD_PREFIX + "/common#invalidContextHandle"

			/**
			 * Cancellation by the user.
			 * A necessary user intervention (e.g. PIN entry) was terminated by cancellation.
			 */
			const val CANCELLATION_BY_USER: String = IFD_PREFIX + "#cancellationByUser"

			/**
			 * Unknown session identifier.
			 */
			const val UNKNOWN_SESSION: String = IFD_PREFIX + "/common#unkownSessionIdentifier"

			/**
			 * Unknown slot handle.
			 */
			const val INVALID_SLOT_HANDLE: String = IFD_PREFIX + "/common#invalidSlotHandle"

			// Not yet specified by the BSI
			const val PASSWORD_SUSPENDED: String = IFD_PREFIX + "/passwordSuspended"
			const val PASSWORD_BLOCKED: String = IFD_PREFIX + "/passwordBlocked"
			const val PASSWORD_ERROR: String = IFD_PREFIX + "/passwordError"
			const val PASSWORD_DEACTIVATED: String = IFD_PREFIX + "/passwordDeactivated"
			const val AUTHENTICATION_FAILED: String = IFD_PREFIX + "/authenticationFailed"
			const val PASSWORDS_DONT_MATCH: String = IFD_PREFIX + "/passwordDontMatch"
			const val UNKNOWN_ERROR: String = IFD_PREFIX + "/unknownError"

			/**
			 * IFD layer - IO minor codes.
			 * The errors of the Terminal-Layer which occur in connection with the input or output of data on the
			 * terminal are grouped in the IO category.
			 */
			object IO {
				/**
				 * Unknown input unit.
				 */
				const val UNKNOWN_INPUT_UNIT: String = IO_PREFIX + "unknownInputUnit"

				/**
				 * Unknown display index.
				 */
				const val UNKNOWN_DISPLAY_INDEX: String = IO_PREFIX + "unknownDisplayIndex"

				/**
				 * It is not possible to cancel the command.
				 */
				const val CANCEL_NOT_POSSIBLE: String = IO_PREFIX + "cancelNotPossible"

				/**
				 * No smart card transaction has been started.
				 */
				const val NO_TRANSACTION_STARTED: String = IFD_PREFIX + "noTransactionStarted"

				/**
				 * Newly recorded identification data do not correspond.
				 */
				const val REPEATED_DATA_MISMATCH: String = IFD_PREFIX + "repeatedDataMismatch"

				/**
				 * Unknown pin format.
				 */
				const val UNKNOWN_PIN_FORMAT: String = IO_PREFIX + "unknownPINFormat"

				/**
				 * Unknwon output device.
				 */
				const val UNKNOWN_OUTPUT_DEVICE: String = IO_PREFIX + "unknownOutputDevice"

				/**
				 * Unknown biometric sub-type.
				 */
				const val UNKNOWN_BIOMETRIC_TYPE: String = IO_PREFIX + "unknownBiometricSubtype"
			}

			/**
			 * IFD layer - Terminal minor codes.
			 * The Terminal category groups the errors which occur due to the status or properties of the terminal.
			 */
			object Terminal {
				/**
				 * The card terminal does not exist.
				 * The addressed card terminal (`IFDName`) is unknown.
				 */
				const val UNKNOWN_IFD: String = TERM_PREFIX + "unknownIFD"

				/**
				 * No eCard available.
				 * The request was not successful, because there is no card captured by the indicated slot.
				 */
				const val NO_CARD: String = TERM_PREFIX + "noCard"

				/**
				 * The request was not successful, because the card is already used by another process.
				 */
				const val IFD_SHARING_VIOLATION: String = TERM_PREFIX + "IFDSharingViolation"

				/**
				 * Unknown Action.
				 * The requested action to be performed is unknown.
				 */
				const val UNKNOWN_ACTION: String = TERM_PREFIX + "unknownAction"

				/**
				 * Unknown Slot.
				 */
				const val UNKNOWN_SLOT: String = TERM_PREFIX + "unknownSlot"

				/**
				 * Access Error.
				 */
				const val ACCESS_ERROR: String = TERM_PREFIX + "accessError"

				/**
				 * Access Error.
				 */
				const val PREPARE_DEVICES_ERROR: String = TERM_PREFIX + "prepareDevicesError"

				const val WAIT_FOR_DEVICE_TIMEOUT: String = TERM_PREFIX + "waitForDeviceTimeout"
			}
		}
	}
}
