/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins.wrapper;

import java.security.AccessController;
import java.security.PrivilegedAction;
import org.openecard.gui.FileDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;


/**
 * Wrapper for implementations of the {@code UserConsent}-interface that should be used by plugins. Each method-call is
 * wrapped by a doPrivileged-call to give the plugins the possibility to call our code that performs actions, which they
 * would otherwise not have the permissions for.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginUserConsent implements UserConsent {

    private final UserConsent userconsent;

    /**
     * Creates a new PluginUserConsent wrapping the given UserConsent. 
     * 
     * @param uc The UserConsent which is wrapped and to which calls are forwarded to.
     */
    public PluginUserConsent(UserConsent uc) {
	this.userconsent = uc;
    }

    @Override
    public UserConsentNavigator obtainNavigator(final UserConsentDescription uc) {

	UserConsentNavigator response = AccessController.doPrivileged(new PrivilegedAction<UserConsentNavigator>() {
	    public UserConsentNavigator run() {
		return userconsent.obtainNavigator(uc);
	    }
	});
	return response;
    }

    @Override
    public FileDialog obtainFileDialog() {

	FileDialog response = AccessController.doPrivileged(new PrivilegedAction<FileDialog>() {
	    public FileDialog run() {
		return userconsent.obtainFileDialog();
	    }
	});
	return response;
    }

}
