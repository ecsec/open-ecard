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

package org.openecard.richclient.gui.manage.core;

import org.openecard.common.I18n;


/**
 * Custom settings group for general settings.
 *
 * @author Tobias Wich
 */
public class GeneralSettingsLegacyGroup extends OpenecardPropertiesSettingsGroup {

    private static final long serialVersionUID = 1L;
    private static final I18n LANG = I18n.getTranslation("addon");
    private static final String GROUP            = "addon.list.core.general.legacy.group_name";
    private static final String TLS1             = "addon.list.core.general.legacy.tls1";
    private static final String	TLS1_DESC        = "addon.list.core.general.legacy.tls1.desc";
    private static final String NS               = "addon.list.core.general.legacy.invalid_schema";
    private static final String	NS_DESC          = "addon.list.core.general.legacy.invalid_schema.desc";
    private static final String PATH_CASE        = "addon.list.core.general.legacy.case_insensitive_path";
    private static final String PATH_CASE_DESC   = "addon.list.core.general.legacy.case_insensitive_path.desc";


    public GeneralSettingsLegacyGroup() {
	super(LANG.translationForKey(GROUP));

	addBoolItem(LANG.translationForKey(TLS1), LANG.translationForKey(TLS1_DESC), "legacy.tls1");
	addBoolItem(LANG.translationForKey(NS), LANG.translationForKey(NS_DESC), "legacy.invalid_schema");
	addBoolItem(LANG.translationForKey(PATH_CASE), LANG.translationForKey(PATH_CASE_DESC), "legacy.case_insensitive_path");
    }

}
