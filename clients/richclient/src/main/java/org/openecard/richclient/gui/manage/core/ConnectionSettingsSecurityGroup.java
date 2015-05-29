/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

import java.io.IOException;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.common.I18n;
import org.openecard.crypto.tls.verify.TrustStoreLoader;


/**
 * Custom settings group for security settings.
 *
 * @author Tobias Wich
 */
public class ConnectionSettingsSecurityGroup extends OpenecardPropertiesSettingsGroup {

    private static final long serialVersionUID = 1L;
    private static final I18n lang = I18n.getTranslation("addon");
    private static final String GROUP            = "addon.list.core.general.security.group_name";
    private static final String SYS_TRUSTSTORE      = "addon.list.core.general.security.truststore-use-system";
    private static final String	SYS_TRUSTSTORE_DESC=  "addon.list.core.general.security.truststore-use-system.desc";


    public ConnectionSettingsSecurityGroup() {
	super(lang.translationForKey(GROUP));

	addBoolItem(lang.translationForKey(SYS_TRUSTSTORE), lang.translationForKey(SYS_TRUSTSTORE_DESC),
		"tls.truststore.use-system");
    }

    @Override
    protected void saveProperties() throws IOException, SecurityException, AddonPropertiesException {
	super.saveProperties();
	// reload truststore
	TrustStoreLoader.load();
    }

}
