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

package org.openecard.android.activation;

import android.app.Dialog;
import org.openecard.gui.android.eac.EacGui;


/**
 *
 * @author Tobias Wich
 */
public interface ActivationImplementationInterface {

    /**
     * This method is called if the Eac Gui is available. If this method is called you can access the server data and
     * start the authentication process.
     *
     * @param eacGui
     */
    void onEacIfaceSet(EacGui eacGui);

    void onAuthenticationSuccess(final ActivationResult result);

    /**
     * Implement this method to recognize a failed authentication in the Sub-Activity. You can handle the following
     * steps on your own, for example show that the authentication failed and then close the Activity with finish().
     * This method is already running on the UI thread.
     *
     * @param result  which contains additional information to the authentication.
     */
    void onAuthenticationFailure(ActivationResult result);

    /**
     * Implement this method to show the card remove dialog. If the authentication process ends, the card should be
     * removed. To enable this, a card remove dialog is shown to the user. The dialog should contain only a hint for
     * the user. The dialog can not be removed by the user with a button click, only by the app when the card is removed.
     *
     * @return
     */
    Dialog showCardRemoveDialog();

}
