/**
 * **************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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
 **************************************************************************
 */
package org.openecard.richclient.gui.manage.core;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.xml.bind.JAXBException;
import org.openecard.common.I18n;
import org.openecard.mdlw.sal.config.MiddlewareConfigLoader;
import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import org.slf4j.LoggerFactory;

/**
 * Custom settings group for Middleware selection
 *
 * @author Sebastian Schuberth
 */
public class MiddlewareSelectionGroup extends OpenecardPropertiesSettingsGroup {

    private static final I18n lang = I18n.getTranslation("addon");
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MiddlewareSelectionGroup.class);

    public MiddlewareSelectionGroup() {
	super("Middleware selection");
	addMiddlewares();

    }

    private void addMiddlewares() {

	try {
	    MiddlewareConfigLoader mwConfigLoader = new MiddlewareConfigLoader();
	    List<MiddlewareSALConfig> mwSALConfigs = mwConfigLoader.getMiddlewareSALConfigs();

	    for (MiddlewareSALConfig mwSALConfig : mwSALConfigs) {

		if (!mwSALConfig.isDisabled()) {
		    String middName = mwSALConfig.getMiddlewareName();
		    if (mwSALConfig.isSALRequired()) {
			JCheckBox box = addBoolItem(middName + " Middleware (required)", "The " + middName + " Middleware is required and cannot be en-/disabled", middName + ".enabled");
			box.setEnabled(false);
		    } else {
			addBoolItem(middName + " Middleware", "En-/Disable the " + middName + " Middleware", middName + ".enabled");
		    }
		}
	    }

	} catch (IOException | JAXBException ex) {
	    LOG.error("Could not read middleware config.", ex);
	}
    }

}
