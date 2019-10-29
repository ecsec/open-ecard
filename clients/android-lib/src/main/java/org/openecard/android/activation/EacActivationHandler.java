/****************************************************************************
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
 ***************************************************************************/

package org.openecard.android.activation;

import android.app.Activity;
import android.net.Uri;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openecard.gui.android.eac.EacGui;

/**
 * Implementation of the activation handler for EAC.
 *
 * @author Tobias Wich
 * @param <T>
 */
public abstract class EacActivationHandler <T extends Activity> extends AbstractActivationHandler<T, EacGui> {

    public EacActivationHandler(T parent) {
	super(parent, EacGui.class);
    }

    protected static final Set<String> SUPPORTED_CARDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
	    "http://bsi.bund.de/cif/npa.xml"
    )));
    @Override
    public Set<String> getSupportedCards() {
	return SUPPORTED_CARDS;
    }

    @Override
    protected boolean isActivateUrlAllowed(Uri eIDUrl) {
	String path = eIDUrl.getPath();
	boolean pathOk = (path == null) ? false : path.startsWith("/eID-Client");
	boolean isEIDActivate = eIDUrl.getQueryParameter("tcTokenURL") != null;
	return pathOk && isEIDActivate;
    }

}
