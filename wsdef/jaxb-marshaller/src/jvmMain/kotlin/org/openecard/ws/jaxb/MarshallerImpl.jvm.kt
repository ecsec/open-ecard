/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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
package org.openecard.ws.jaxb

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import jakarta.xml.bind.Unmarshaller
import jakarta.xml.bind.annotation.XmlRegistry
import jakarta.xml.bind.annotation.XmlType
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

private val LOG = KotlinLogging.logger {}

/**
 * Wrapper for JAXB marshaller and unmarshaller capable of modifying the supported JAXB types on the fly.
 *
 * @author Tobias Wich
 */
class MarshallerImpl {
	private var userOverride = false
	private val userClasses: TreeSet<Class<*>> = TreeSet(ClassComparator())

	private var marshaller: Marshaller? = null
	private var unmarshaller: Unmarshaller? = null

	/**
	 * Adds the specified JAXB element types class to the list of supported JAXB types.
	 * This method triggers a recreation of the wrapped marshaller and unmarshaller.
	 *
	 * @param c Class of the JAXB element type.
	 */
	@Synchronized
	fun addXmlClass(c: Class<*>) {
		addBaseClasses()
		if (!userClasses.contains(c)) {
			// addJaxbClasses(c);
			userClasses.add(c)
			// class added to set
			userOverride = true
			resetMarshaller()
		}
	}

	private fun addBaseClasses() {
		// add all base classes if the user did not delete them before
		if (!userOverride) {
			userClasses.addAll(baseXmlElementClasses)
		}
	}

	/**
	 * Remove all JAXB element types from this instance.
	 * New types must be added first before this instance is usable for marshalling and unmarshalling again.
	 */
	@Synchronized
	fun removeAllClasses() {
		userOverride = true
		userClasses.clear()
		resetMarshaller()
	}

	/**
	 * Gets the wrapped JAXB marshaller instance.
	 *
	 * @return The wrapped JAXB marshaller instance.
	 * @throws JAXBException If the marshaller could not be created.
	 */
	@kotlin.Throws(JAXBException::class)
	fun getMarshaller(): Marshaller {
		if (marshaller == null) {
			loadInstances()
		}
		return marshaller!!
	}

	/**
	 * Gets the wrapped JAXB unmarshaller instance.
	 *
	 * @return The wrapped JAXB unmarshaller instance.
	 * @throws JAXBException If the unmarshaller could not be created.
	 */
	@kotlin.Throws(JAXBException::class)
	fun getUnmarshaller(): Unmarshaller {
		if (unmarshaller == null) {
			loadInstances()
		}
		return unmarshaller!!
	}

	private fun resetMarshaller() {
		marshaller = null
		unmarshaller = null
	}

	@Synchronized
	@kotlin.Throws(JAXBException::class)
	private fun loadInstances() {
		var jaxbCtx: JAXBContext
		if (userOverride) {
			val classHash = calculateClassesHash()
			synchronized(specificContexts) {
				if (!specificContexts.containsKey(classHash)) {
					jaxbCtx = JAXBContext.newInstance(*userClasses.toArray(arrayOfNulls<Class<*>>(userClasses.size)))
					specificContexts.put(classHash, jaxbCtx)
				} else {
					jaxbCtx = specificContexts[classHash]!!
				}
			}
		} else {
			try {
				jaxbCtx = baseJaxbContext.get()
			} catch (ex: ExecutionException) {
				LOG.error(ex) { "Failed to create JAXBContext instance." }
				throw RuntimeException("Failed to create JAXBContext.")
			} catch (ex: InterruptedException) {
				LOG.error(ex) { "Thread terminated waiting for the JAXBContext to be created.." }
				throw RuntimeException("Thread interrupted during waiting on the creation of the JAXBContext.")
			}
		}
		marshaller = jaxbCtx.createMarshaller()
		unmarshaller = jaxbCtx.createUnmarshaller()
	}

	private fun calculateClassesHash(): String {
		try {
			val md: MessageDigest = MessageDigest.getInstance("SHA-256")
			for (c in userClasses) {
				md.update(c.getName().toByteArray())
			}
			val digest: ByteArray = md.digest()
			return toHexString(digest)
		} catch (ex: NoSuchAlgorithmException) {
			throw RuntimeException("SHA-256 hash algorithm is not supported on your platform.", ex)
		}
	}

	companion object {
		private val baseXmlElementClasses = arrayListOf<Class<*>>()
		private val baseJaxbContext: FutureTask<JAXBContext>
		private val specificContexts: HashMap<String, JAXBContext>

		init {
			// load predefined classes
			baseXmlElementClasses.addAll(jaxbClasses)

			baseJaxbContext =
				FutureTask(
					object : Callable<JAXBContext> {
						@kotlin.Throws(Exception::class)
						override fun call(): JAXBContext {
							try {
								return JAXBContext.newInstance(*jaxbClasses)
							} catch (ex: JAXBException) {
								LOG.error(ex) { "Failed to create JAXBContext instance." }
								throw RuntimeException("Failed to create JAXBContext.")
							}
						}
					},
				)
			Thread(baseJaxbContext, "JAXB-Classload").start()

			specificContexts = HashMap()
		}

		private val jaxbClasses: Array<Class<*>>
			get() {
				val cl: ClassLoader = Thread.currentThread().getContextClassLoader()
				val classes = arrayListOf<Class<*>>()
				val classListStream =
					cl.getResourceAsStream("classes.lst")
						?: cl.getResourceAsStream("/classes.lst")

				try {
					if (classListStream == null) {
						throw IOException("Failed to load classes.lst.")
					} else {
						val r = LineNumberReader(InputStreamReader(classListStream))
						var next: String?
						// read all entries from file
						while ((r.readLine().also { next = it }) != null) {
							try {
								// load class and see if it is a JAXB class
								val c = cl.loadClass(next)
								if (isJaxbClass(c)) {
									classes.add(c)
								}
							} catch (ex: ClassNotFoundException) {
								LOG.error(ex) { "Failed to load class: $next" }
							}
						}
					}
				} catch (ex: IOException) {
					LOG.error(ex) { "Failed to read classes from file classes.lst." }
				}

				return classes.toTypedArray()
			}

		private fun isJaxbClass(c: Class<*>): Boolean =
			c.isAnnotationPresent(XmlType::class.java) ||
				c.isAnnotationPresent(XmlRegistry::class.java)

		private fun toHexString(bytes: ByteArray): String {
			val writer = StringWriter(bytes.size * 2)
			val out = PrintWriter(writer)

			for (i in 1..bytes.size) {
				out.printf("%02X", bytes[i - 1])
			}

			return writer.toString()
		}
	}
}
