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
import org.openecard.common.I18n
import org.openecard.crypto.tls.proxy.ProxySettings
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
class ConnectionSettingsGroup : OpenecardPropertiesSettingsGroup(lang.translationForKey(GROUP)) {
    private val selection: JComboBox<*>
    private val host: JTextField?
    private val port: JTextField?
    private val vali: JCheckBox
    private val user: JTextField?
    private val pass: JTextField
    private val excl: ScalarListItem


    init {
        selection = addSelectionItem(
            lang.translationForKey(SCHEME), lang.translationForKey(SCHEME_DESC),
            "proxy.scheme", "System Proxy", "SOCKS", "HTTP", "HTTPS", "No Proxy"
        )
        host = addInputItem(lang.translationForKey(HOST), lang.translationForKey(HOST_DESC), "proxy.host")
        port = addInputItem(lang.translationForKey(PORT), lang.translationForKey(PORT_DESC), "proxy.port")
        vali = addBoolItem(lang.translationForKey(VALI), lang.translationForKey(VALI_DESC), "proxy.validate_tls")
        user = addInputItem(lang.translationForKey(USER), lang.translationForKey(USER_DESC), "proxy.user")
        pass = addInputItem(lang.translationForKey(PASS), lang.translationForKey(PASS_DESC), "proxy.pass", true)
        excl = addScalarListItem(
            lang.translationForKey(EXCL), lang.translationForKey(EXCL_DESC), "proxy.excludes",
            ScalarListEntryType.STRING
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
                if ("SOCKS" == `val`) {
                    setEnabledComponent(host!!, true)
                    setEnabledComponent(port!!, true)
                    setEnabledComponent(vali, false)
                    setEnabledComponent(user!!, false)
                    setEnabledComponent(pass, false)
                    setEnabledComponent(excl, true)
                } else if ("HTTP" == `val`) {
                    setEnabledComponent(host!!, true)
                    setEnabledComponent(port!!, true)
                    setEnabledComponent(vali, false)
                    setEnabledComponent(user!!, true)
                    setEnabledComponent(pass, true)
                    setEnabledComponent(excl, true)
                } else if ("HTTPS" == `val`) {
                    setEnabledComponent(host!!, true)
                    setEnabledComponent(port!!, true)
                    setEnabledComponent(vali, true)
                    setEnabledComponent(user!!, true)
                    setEnabledComponent(pass, true)
                    setEnabledComponent(excl, true)
                } else {
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

    companion object {
        private const val serialVersionUID: Long = 1L
        private val lang: I18n = I18n.getTranslation("addon")
        private const val GROUP: String = "addon.list.core.connection.proxy.group_name"
        private const val SCHEME: String = "addon.list.core.connection.proxy.scheme"
        private const val SCHEME_DESC: String = "addon.list.core.connection.proxy.scheme.desc"
        private const val HOST: String = "addon.list.core.connection.proxy.host"
        private const val HOST_DESC: String = "addon.list.core.connection.proxy.host.desc"
        private const val PORT: String = "addon.list.core.connection.proxy.port"
        private const val PORT_DESC: String = "addon.list.core.connection.proxy.port.desc"
        private const val VALI: String = "addon.list.core.connection.proxy.vali"
        private const val VALI_DESC: String = "addon.list.core.connection.proxy.vali.desc"
        private const val USER: String = "addon.list.core.connection.proxy.user"
        private const val USER_DESC: String = "addon.list.core.connection.proxy.user.desc"
        private const val PASS: String = "addon.list.core.connection.proxy.pass"
        private const val PASS_DESC: String = "addon.list.core.connection.proxy.pass.desc"
        private const val EXCL: String = "addon.list.core.connection.proxy.excludes"
        private const val EXCL_DESC: String = "addon.list.core.connection.proxy.excludes.desc"
    }
}
