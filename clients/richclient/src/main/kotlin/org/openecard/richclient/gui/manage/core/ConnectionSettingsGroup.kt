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
 ***************************************************************************/

package org.openecard.richclient.gui.manage.core

import org.openecard.addon.AddonPropertiesException
import org.openecard.addon.manifest.ScalarListEntryType
import org.openecard.crypto.tls.proxy.ProxySettings
import org.openecard.i18n.I18N
import org.openecard.richclient.gui.components.ScalarListItem
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.io.IOException
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JTextField

/**
 * Custom settings group for proxy settings.
 * The settings are made dynamic to reflect the choice made by the user.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class ConnectionSettingsGroup : OpenecardPropertiesSettingsGroup(GROUP) {
	private val selection: JComboBox<*>
	private val host: JTextField?
	private val port: JTextField?
	private val vali: JCheckBox
	private val user: JTextField?
	private val pass: JTextField
	private val excl: ScalarListItem

	init {
		selection =
			addSelectionItem(
				SCHEME,
				SCHEME_DESC,
				"proxy.scheme",
				"System Proxy",
				"SOCKS",
				"HTTP",
				"HTTPS",
				"No Proxy",
			)
		host = addInputItem(HOST, HOST_DESC, "proxy.host")
		port = addInputItem(PORT, PORT_DESC, "proxy.port")
		vali = addBoolItem(VALI, VALI_DESC, "proxy.validate_tls")
		user = addInputItem(USER, USER_DESC, "proxy.user")
		pass = addInputItem(PASS, PASS_DESC, "proxy.pass", true)
		excl =
			addScalarListItem(
				EXCL,
				EXCL_DESC,
				"proxy.excludes",
				ScalarListEntryType.STRING,
			)

		// register event and trigger initial setup
		val manager: ItemManager = ItemManager()
		val selectedItem: Any? = selection.getSelectedItem()
		val trigger: ItemEvent = ItemEvent(selection, ItemEvent.ITEM_FIRST, selectedItem, ItemEvent.SELECTED)
		manager.itemStateChanged(trigger)
		selection.addItemListener(manager)
	}

	@Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
	override fun saveProperties() {
		super.saveProperties()
		// reload proxy settings
		ProxySettings.load()
	}

	private inner class ItemManager : ItemListener {
		override fun itemStateChanged(e: ItemEvent) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				val `val`: Any = e.getItem()
				when (`val`) {
					"SOCKS" -> {
						setEnabledComponent(host!!, true)
						setEnabledComponent(port!!, true)
						setEnabledComponent(vali, false)
						setEnabledComponent(user!!, false)
						setEnabledComponent(pass, false)
						setEnabledComponent(excl, true)
					}
					"HTTP" -> {
						setEnabledComponent(host!!, true)
						setEnabledComponent(port!!, true)
						setEnabledComponent(vali, false)
						setEnabledComponent(user!!, true)
						setEnabledComponent(pass, true)
						setEnabledComponent(excl, true)
					}
					"HTTPS" -> {
						setEnabledComponent(host!!, true)
						setEnabledComponent(port!!, true)
						setEnabledComponent(vali, true)
						setEnabledComponent(user!!, true)
						setEnabledComponent(pass, true)
						setEnabledComponent(excl, true)
					}
					else -> {
						setEnabledComponent(host!!, false)
						setEnabledComponent(port!!, false)
						setEnabledComponent(vali, false)
						setEnabledComponent(user!!, false)
						setEnabledComponent(pass, false)
						setEnabledComponent(excl, false)
					}
				}
			}
		}
	}

	companion object {
		private val GROUP = I18N.strings.addon_list_core_connection_proxy_group_name.localized()
		private val SCHEME = I18N.strings.addon_list_core_connection_proxy_scheme.localized()
		private val SCHEME_DESC = I18N.strings.addon_list_core_connection_proxy_scheme_desc.localized()
		private val HOST = I18N.strings.addon_list_core_connection_proxy_host.localized()
		private val HOST_DESC = I18N.strings.addon_list_core_connection_proxy_host_desc.localized()
		private val PORT = I18N.strings.addon_list_core_connection_proxy_port.localized()
		private val PORT_DESC = I18N.strings.addon_list_core_connection_proxy_port_desc.localized()
		private val VALI = I18N.strings.addon_list_core_connection_proxy_vali.localized()
		private val VALI_DESC = I18N.strings.addon_list_core_connection_proxy_vali_desc.localized()
		private val USER = I18N.strings.addon_list_core_connection_proxy_user.localized()
		private val USER_DESC = I18N.strings.addon_list_core_connection_proxy_user_desc.localized()
		private val PASS = I18N.strings.addon_list_core_connection_proxy_pass.localized()
		private val PASS_DESC = I18N.strings.addon_list_core_connection_proxy_pass_desc.localized()
		private val EXCL = I18N.strings.addon_list_core_connection_proxy_excludes.localized()
		private val EXCL_DESC = I18N.strings.addon_list_core_connection_proxy_excludes_desc.localized()
	}
}
