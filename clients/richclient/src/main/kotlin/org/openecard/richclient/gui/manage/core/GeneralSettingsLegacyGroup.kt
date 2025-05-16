/****************************************************************************
 * Copyright (C) 2015-2022 ecsec GmbH.
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

import org.openecard.i18n.I18N

/**
 * Custom settings group for general settings.
 *
 * @author Tobias Wich
 */
class GeneralSettingsLegacyGroup : OpenecardPropertiesSettingsGroup(GROUP) {
	init {
		addBoolItem(TLS1, TLS1_DESC, "legacy.tls1")
		addBoolItem(NS, NS_DESC, "legacy.invalid_schema")
		addBoolItem(
			PATH_CASE,
			PATH_CASE_DESC,
			"legacy.case_insensitive_path",
		)
	}

	companion object {
		private val GROUP = I18N.strings.addon_list_core_general_legacy_group_name.localized()
		private val TLS1 = I18N.strings.addon_list_core_general_legacy_tls1.localized()
		private val TLS1_DESC = I18N.strings.addon_list_core_general_legacy_tls1_desc.localized()
		private val NS = I18N.strings.addon_list_core_general_legacy_invalid_schema.localized()
		private val NS_DESC = I18N.strings.addon_list_core_general_legacy_invalid_schema_desc.localized()
		private val PATH_CASE = I18N.strings.addon_list_core_general_legacy_case_insensitive_path.localized()
		private val PATH_CASE_DESC = I18N.strings.addon_list_core_general_legacy_case_insensitive_path_desc.localized()
	}
}
