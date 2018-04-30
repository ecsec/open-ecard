/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.richclient.gui.update;

import java.util.ArrayList;
import org.openecard.common.I18n;
import org.openecard.common.util.VersionUpdate;
import org.openecard.common.util.VersionUpdateChecker;


/**
 *
 * @author Sebastian Schuberth
 */
public class UpdateMessageCreator {

    private final I18n lang = I18n.getTranslation("update");

    public String getMessage(VersionUpdateChecker updateChecker) {
	VersionUpdate currentVersion = updateChecker.getCurrentVersion();

	VersionUpdate majUpdate = updateChecker.getMajorUpgrade();
	ArrayList<String> updateVersions = new ArrayList<>();

	if (majUpdate != null) {
	    String maj = majUpdate.getVersion().toString() + " (major update)";
	    updateVersions.add(maj);
	}
	VersionUpdate minUpdate = updateChecker.getMinorUpgrade();

	if (minUpdate != null) {
	    String min = minUpdate.getVersion().toString() + " (minor update)";
	    updateVersions.add(min);
	}
	VersionUpdate secUpdate = updateChecker.getSecurityUpgrade();

	if (secUpdate != null) {
	    String sec = secUpdate.getVersion().toString() + " (security update)";
	    updateVersions.add(sec);
	}

	int numberOfVersions = updateVersions.size();

	String msg = "";
	if (numberOfVersions == 1) {
	    msg = lang.translationForKey("new_version_msg", updateVersions.get(0));
	} else if (numberOfVersions > 1) {
	    StringBuilder sb = new StringBuilder();

	    for (int i = 0; i < numberOfVersions; i++) {
		sb.append(updateVersions.get(i));
		if (i < numberOfVersions - 1) {
		    sb.append(", ");
		}
	    }

	    msg = lang.translationForKey("new_versions_msg", sb.toString());
	}

	if (! updateChecker.isCurrentMaintained()) {
	    msg = lang.translationForKey("version_not_maintained", currentVersion.getVersion().toString(), updateVersions.get(0));
	}

	return msg;
    }

}
