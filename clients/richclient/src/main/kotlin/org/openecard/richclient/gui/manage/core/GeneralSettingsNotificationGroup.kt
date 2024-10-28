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
 ***************************************************************************/

package org.openecard.richclient.gui.manage.core

import org.openecard.common.I18n

/**
 * Custom settings group for general settings.
 *
 * @author Tobias Wich
 */
class GeneralSettingsNotificationGroup : OpenecardPropertiesSettingsGroup(lang.translationForKey(GROUP)) {
    init {
        addBoolItem(
            lang.translationForKey(REMOVE_CARD), lang.translationForKey(REMOVE_CARD_DESC),
            "notification.omit_show_remove_card"
        )
    }

    companion object {
        private const val serialVersionUID: Long = 1L
        private val lang: I18n = I18n.getTranslation("addon")
        private const val GROUP: String = "addon.list.core.general.notification.group_name"
        private const val REMOVE_CARD: String = "addon.list.core.general.notification.omit_show_remove_card"
        private const val REMOVE_CARD_DESC: String = "addon.list.core.general.notification.omit_show_remove_card.desc"
    }
}
