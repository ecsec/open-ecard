/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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
package org.openecard.addon.manifest

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType
import org.openecard.addon.utils.LocalizedStringExtractor
import org.openecard.common.util.FileUtils
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Root element of an AddonSpecification (Manifest file).
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */

private val logger = KotlinLogging.logger { }

@XmlRootElement(name = "AddonSpecification")
@XmlType(
	propOrder = [
		"id", "version", "license", "licenseText", "localizedName", "localizedDescription", "about",
		"logo", "configDescription", "bindingActions", "applicationActions", "ifdActions", "salActions",
	],
)
@XmlAccessorType(
	XmlAccessType.FIELD,
)
class AddonSpecification : Comparable<AddonSpecification> {
	@XmlElement(name = "ID", required = true)
	private var id: String? = null

	@XmlElement(name = "Logo", required = true)
	private var logo: String? = null

	// private byte[] logoBytes;
	@XmlElement(name = "Version", required = true)
	private var version: String? = null

	@JvmField
	@XmlElement(name = "License", required = true)
	var license: String? = null

	@XmlElement(name = "LicenseText", required = false)
	val licenseText: MutableList<LocalizedString> = ArrayList()

	@JvmField
	@XmlElement(name = "ConfigDescription", required = true)
	var configDescription: Configuration? = null

	@JvmField
	@XmlElement(name = "LocalizedName", type = LocalizedString::class, required = false)
	val localizedName: MutableList<LocalizedString> = ArrayList()

	@XmlElement(name = "LocalizedDescription", type = LocalizedString::class, required = false)
	val localizedDescription: MutableList<LocalizedString> = ArrayList()

	@XmlElement(name = "About", type = LocalizedString::class, required = false)
	val about: MutableList<LocalizedString> = ArrayList()

	@JvmField
	@XmlElement(name = "AppExtensionSpecification", type = AppExtensionSpecification::class, required = false)
	@XmlElementWrapper(name = "ApplicationActions", required = false)
	val applicationActions: ArrayList<AppExtensionSpecification> = ArrayList()

	@JvmField
	@XmlElementWrapper(name = "BindingActions", required = false)
	@XmlElement(name = "AppPluginSpecification", type = AppPluginSpecification::class, required = false)
	val bindingActions: ArrayList<AppPluginSpecification> = ArrayList()

	@JvmField
	@XmlElementWrapper(name = "IFDActions", required = false)
	@XmlElement(name = "ProtocolPluginSpecification", type = ProtocolPluginSpecification::class, required = false)
	val ifdActions: ArrayList<ProtocolPluginSpecification> = ArrayList()

	@JvmField
	@XmlElementWrapper(name = "SALActions", required = false)
	@XmlElement(name = "ProtocolPluginSpecification", type = ProtocolPluginSpecification::class, required = false)
	val salActions: ArrayList<ProtocolPluginSpecification> = ArrayList()

	fun getId(): String = id!!

	fun getLocalizedName(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(localizedName, languageCode)

	fun getLocalizedDescription(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(localizedDescription, languageCode)

	fun getLogo(): String? = logo

	fun getVersion(): String = version!!

	fun getAbout(languageCode: String): String = LocalizedStringExtractor.getLocalizedString(about, languageCode)

	fun getLicenseText(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(licenseText, languageCode)

	fun setId(id: String) {
		this.id = id
	}

	fun setLogo(logo: String) {
		this.logo = logo
		logger.debug { "LogoFile: $logo" }
	}

	val logoBytes: ByteArray?
		/**
		 * Get a byte array containing the logo.
		 * <br></br>
		 * Note: This method creates always a new input stream and does not store the byte array internally.
		 *
		 * @return A byte array containing the logo bytes or null if no logo is present or an error occurred.
		 */
		get() {
			if (logo != null && !logo!!.isEmpty()) {
				try {
					// TODO security checks and maybe modified loading
					val logoStream =
						FileUtils.resolveResourceAsStream(
							AddonSpecification::class.java,
							logo!!,
						)
					return FileUtils.toByteArray(logoStream!!)
				} catch (e: FileNotFoundException) {
					logger.error(
						e,
					) { "Logo file couldn't be found." }
					return null
				} catch (e: IOException) {
					logger.error(
						e,
					) { "Logo file couldn't be read." }
					return null
				} catch (e: NullPointerException) {
					logger.error(
						e,
					) { "Logo file couldn't be read." }
					return null
				}
			}
			return null
		}

	fun setVersion(version: String) {
		this.version = version
	}

	fun searchByResourceName(resourceName: String): AppPluginSpecification? {
		for (desc in bindingActions) {
			// check the resource of the manifest against the prefixes derived from the resource
			// most specific comes first
			for (prefix in prefixResourceList(resourceName)) {
				// in case we have a match, return the specification
				if (prefix == desc.resourceName) {
					return desc
				}
			}
		}
		return null
	}

	private fun prefixResourceList(resourceName: String): MutableList<String> {
		val parts: Array<String?> = resourceName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		val result = ArrayList<String>(parts.size)
		var nextResource: String? = ""

		// construct list of prefixes
		for (part in parts) {
			nextResource += part
			result.add(nextResource)
			nextResource += "/"
		}

		// reverse prefixes
		result.reverse()
		return result
	}

	fun searchByActionId(id: String?): AppExtensionSpecification? {
		for (desc in applicationActions) {
			if (desc.id == id) {
				return desc
			}
		}
		return null
	}

	fun searchIFDActionByURI(uri: String): ProtocolPluginSpecification? {
		for (desc in ifdActions) {
			if (desc.uri == uri) {
				return desc
			}
		}
		return null
	}

	fun searchSALActionByURI(uri: String?): ProtocolPluginSpecification? {
		for (desc in salActions) {
			if (desc.uri == uri) {
				return desc
			}
		}
		return null
	}

	override fun compareTo(o: AddonSpecification): Int {
		val versionRes = version!!.compareTo(o.getVersion())
		val idRes = id!!.compareTo(o.getId())

		// Same id and version so they are equal
		if (versionRes == 0 && idRes == 0) {
			return 0
		} else if (versionRes == 0) {
			// the version equals so we have differnt addons with the same version
			if (idRes > 0) {
				return 1
			}

			if (idRes < 0) {
				return -1
			}
			return -2
		} else if (idRes == 0) {
			// same addon in different versions
			if (versionRes > 0) {
				return 1
			}
			if (versionRes < 0) {
				return -1
			}
			return -2
		} else {
			// different addons with different versions
			// convention the id is higher rated.
			if (idRes > 0) {
				return 1
			}

			if (idRes < 0) {
				return -1
			}
			return -2
		}
	}

	override fun hashCode(): Int {
		val versionHash = version.hashCode()
		val idHash = id.hashCode()
		return versionHash + idHash
	}

	override fun equals(addonSpec: Any?): Boolean {
		if (addonSpec is AddonSpecification) {
			if (compareTo(addonSpec) == 0) {
				return true
			} else {
				return false
			}
		} else {
			return false
		}
	}
}
