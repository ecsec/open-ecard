/****************************************************************************
 * Copyright (C) 2013-2016 ecsec GmbH.
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
import org.openecard.addon.manifest.AppExtensionSpecification
import org.openecard.addon.manifest.AppPluginSpecification
import org.openecard.addon.manifest.ProtocolPluginSpecification
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.xml.sax.SAXException
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/**
 * Addon registry serving add-ons from the classpath of the base app.
 * This type of registry works for JNLP and integrated plugins.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */

private val LOG = KotlinLogging.logger { }

class ClasspathRegistry : AddonRegistry {
	private val registeredAddons: FutureTask<MutableList<AddonSpecification>>

	init {
		registeredAddons =
			FutureTask<MutableList<AddonSpecification>> {
				val addons = mutableListOf<AddonSpecification>()

				val marshaller = createInstance()
				marshaller.removeAllTypeClasses()
				marshaller.addXmlTypeClass(AddonSpecification::class.java)

				loadManifest(addons, marshaller, "TR-03112", "TCToken-Manifest.xml")
				loadManifest(addons, marshaller, "CardLink", "CardLink-Manifest.xml")
				loadManifest(addons, marshaller, "ChipGateway", "ChipGateway-Manifest.xml")
				loadManifest(addons, marshaller, "PIN-Management", "PIN-Plugin-Manifest.xml")
				loadManifest(addons, marshaller, "GenericCrypto", "GenericCrypto-Plugin-Manifest.xml")
				loadManifest(addons, marshaller, "Status", "Status-Plugin-Manifest.xml")
				loadManifest(addons, marshaller, "PKCS#11", "PKCS11-Manifest.xml")

				addons
			}
		Thread(registeredAddons, "Init-Classpath-Addons").start()
	}

	private fun loadManifest(
		addons: MutableList<AddonSpecification>,
		m: WSMarshaller,
		addonName: String,
		fileName: String,
	) {
		try {
			val manifestStream = resolveResourceAsStream(ClasspathRegistry::class.java, fileName)
			if (manifestStream == null) {
				LOG.warn { "${"Skipped loading internal add-on {}, because it is not available."} $addonName" }
				return
			}
			val manifestDoc = m.str2doc(manifestStream)
			registerInt(addons, m.unmarshal(manifestDoc) as AddonSpecification)
			LOG.info { "${"Loaded internal {} add-on."} $addonName" }
		} catch (ex: IOException) {
			LOG.warn(ex) { String.format("Failed to load internal %s add-on.", addonName) }
		} catch (ex: SAXException) {
			LOG.warn(ex) { String.format("Failed to load internal %s add-on.", addonName) }
		} catch (ex: WSMarshallerException) {
			LOG.warn(ex) { String.format("Failed to load internal %s add-on.", addonName) }
		}
	}

	private val addons: MutableList<AddonSpecification>
		get() {
			try {
				return registeredAddons.get()
			} catch (ex: InterruptedException) {
				val msg = "Initialization of the built-in Add-ons has been interrupted."
				LOG.warn { msg }
				throw RuntimeException(msg)
			} catch (ex: ExecutionException) {
				val msg = "Initialization of the built-in Add-ons yielded an error."
				LOG.error(ex) { msg }
				throw RuntimeException(msg, ex.cause)
			}
		}

	private fun registerInt(
		registeredAddons: MutableList<AddonSpecification>,
		desc: AddonSpecification,
	) {
		registeredAddons.add(desc)
	}

	fun register(desc: AddonSpecification) {
		registerInt(this.addons, desc)
	}

	override fun listAddons(): MutableSet<AddonSpecification> = this.addons.toMutableSet()

	override fun search(id: String): AddonSpecification? {
		for (desc in this.addons) {
			if (desc.getId() == id) {
				return desc
			}
		}
		return null
	}

	override fun searchByName(name: String): MutableSet<AddonSpecification> {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons) {
			for (s in desc.localizedName) {
				if (s.value == name) {
					matchingAddons.add(desc)
				}
			}
		}
		return matchingAddons
	}

	override fun searchIFDProtocol(uri: String): MutableSet<AddonSpecification> {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons) {
			val protocolDesc: ProtocolPluginSpecification? = desc.searchIFDActionByURI(uri)
			if (protocolDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	override fun searchSALProtocol(uri: String): MutableSet<AddonSpecification> {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons) {
			val protocolDesc: ProtocolPluginSpecification? = desc.searchSALActionByURI(uri)
			if (protocolDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	override fun downloadAddon(addonSpec: AddonSpecification): ClassLoader? {
		// TODO use other own classloader impl with security features
		return this.javaClass.getClassLoader()
	}

	override fun searchByResourceName(resourceName: String): MutableSet<AddonSpecification> {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons) {
			val actionDesc: AppPluginSpecification? = desc.searchByResourceName(resourceName)
			if (actionDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	override fun searchByActionId(actionId: String): MutableSet<AddonSpecification> {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons) {
			val actionDesc: AppExtensionSpecification? = desc.searchByActionId(actionId)
			if (actionDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	override fun listInstalledAddons(): MutableSet<AddonSpecification> {
		// There aren't addons which are not installed so just return the output of listAddons()
		return listAddons()
	}
}
