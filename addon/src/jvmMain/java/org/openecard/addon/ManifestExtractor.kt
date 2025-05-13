/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.addon

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.jar.JarFile

private val logger = KotlinLogging.logger { }

/**
 * This class implements an extractor which extracts the AddonSpecification from an addon jar file.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class ManifestExtractor {
	private val marshaller: WSMarshaller

	init {
		marshaller = createInstance()
		marshaller.removeAllTypeClasses()
		marshaller.addXmlTypeClass(AddonSpecification::class.java)
	}

	/**
	 * Get an AddonSpecification object from the jar file containing the addon.
	 *
	 * @param file jar file to get the AddonSpecification from.
	 * @return The [AddonSpecification] of the addon, or `null` if it could not be extracted.
	 */
	fun getAddonSpecificationFromFile(file: File): AddonSpecification? {
		val name = file.getName()
		val jarFile: JarFile?
		var abd: AddonSpecification?
		try {
			jarFile = JarFile(file)
		} catch (e: IOException) {
			logger.debug { "${"File {} will not be registered as plugin because it's not a JarFile."} $name" }
			return null
		}
		try {
			val manifestStream = getPluginEntryClass(jarFile)

			if (manifestStream == null) {
				logger.debug { "${"File {} will not be registered as plugin because it doesn't contain a addon.xml."} $name" }
				return null
			} else {
				marshaller.addXmlTypeClass(AddonSpecification::class.java)
				val manifestDoc = marshaller.str2doc(manifestStream)
				abd = marshaller.unmarshal(manifestDoc) as AddonSpecification
			}
		} catch (ex: IOException) {
			logger.error(ex) { "Failed to process addon.xml entry for file $name." }
			return null
		} catch (ex: SAXException) {
			logger.error(ex) { "Failed to process addon.xml entry for file $name." }
			return null
		} catch (ex: WSMarshallerException) {
			logger.error(ex) { "Failed to process addon.xml entry for file $name." }
			return null
		} finally {
			try {
				jarFile.close()
			} catch (ex: IOException) {
				logger.error(ex) { "Failed to close jar file." }
			}
		}
		return abd
	}

	/**
	 * Get the addon.xml file as InputStream object from the jar file.
	 *
	 * @param jarFile The jar file which should contain the addon.xml file.
	 * @return A InputStream object to the addon.xml file.
	 * @throws IOException Thrown if the [InputStream] can't be returned.
	 */
	@Throws(IOException::class)
	private fun getPluginEntryClass(jarFile: JarFile): InputStream? {
		val manifest = jarFile.getEntry(MANIFEST_XML)
		return if (manifest == null) {
			null
		} else {
			jarFile.getInputStream(manifest)
		}
	}

	companion object {
		private const val MANIFEST_XML = "META-INF/addon.xml"
	}
}
