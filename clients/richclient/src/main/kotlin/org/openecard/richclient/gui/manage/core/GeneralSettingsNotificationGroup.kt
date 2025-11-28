/*
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

package org.openecard.richclient.gui.manage.core

import org.openecard.i18n.I18N
import org.openecard.richclient.tr03124.Tr03124SettingsLoader

/**
 * Custom settings group for notification settings.
 *
 * @author Tobias Wich
 */
class GeneralSettingsNotificationGroup :
	OpenecardPropertiesSettingsGroup(I18N.strings.addon_list_core_general_notification_group_name.localized()) {
	init {
		addBoolItem(
			I18N.strings.addon_list_core_general_notification_omit_show_remove_card.localized(),
			I18N.strings.addon_list_core_general_notification_omit_show_remove_card_desc.localized(),
			Tr03124SettingsLoader.REMOVE_CARD_DIALOG,
		)
	}
}
