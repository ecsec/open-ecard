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
import javax.annotation.Nullable;
import org.openecard.gui.android.eac.EacGui;


/**
 * Interface containing all methods which can/must be overridden in order to create a functional Activation Activity.
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 */
public interface ActivationImplementationInterface {

    /**
     * Key for the return class which is used in extra arguments of the Intent used to transport the activation URL.
     * <p>The return class, if set in the Intent, is used to perform an explicit Intent with the refresh URL at the
     * end of the authentication procedure.</p>
     */
    public static final String RETURN_CLASS = "org.openecard.android.activation.return-class";


    /**
     * This method is called when the EacGui is available to the activity.
     * <p>If this method is called the server data can be accessed and the PIN can be entered.</p>
     * <p>This method marks the starting point when interaction between the EAC process and the implementing activity
     * can start.</p>
     *
     * @param eacGui Instance of the EacGui interface connected to the current EAC process.
     */
    void onEacIfaceSet(EacGui eacGui);

    /**
     * Callback when the authentication process is concluded with a redirect address.
     * <p>Receiving a redirect address does not necessarily mean that the authentication has been successful. This is
     * for the server to decide. The URL contains more information about the actual outcome of the authentication.
     * Functionality handling these kind of errors may be added to this method before calling the default
     * implementation.</p>
     * <p>The default implementation in {@link AbstractActivationActivity} of this method performs a TR-03124 conforming
     * URL intent (redirect). When the Intent starting the Activity contained the return class in the extra arguments
     * (see {@link #RETURN_CLASS}, then an explicit Intent is performed and the user does not have the choice to select
     * a suitable Application to receive the URL.</p>
     *
     * @param result Result of the finished eID Activation process.
     */
    void onAuthenticationSuccess(final ActivationResult result);

    /**
     * Callback when the authentication process failed without providing a redirect address.
     * <p>These are unrecoverable errors with regard to a server driven application. That means the error must be
     * handled independent of the server.</p>
     * <p>An implementation could for example open move to an activity providing a suitable error view.</p>
     *
     * @param result Result containing details about the error happened during the authentication. The refresh URL will
     *   not be present.
     */
    void onAuthenticationFailure(ActivationResult result);

    /**
     * Function creating a dialog instructing the user to remove the card for safety purposes.
     * <p>The dialog is shown by the default implementation of
     * {@link AbstractActivationActivity#onAuthenticationSuccess(ActivationResult)}. In case no dialog shall be shown,
     * {@code null} may be returned by this function.</p>
     *
     * @see Requirement from BSI TR-03124-1 Sec.3.8
     * @return Dialog instructing the user to remove the card from the reader, or {@code null} in case no dialog shall
     *   be shown.
     */
    @Nullable
    Dialog showCardRemoveDialog();

}
