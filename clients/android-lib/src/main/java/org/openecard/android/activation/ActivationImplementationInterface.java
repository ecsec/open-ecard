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
import org.openecard.gui.android.AndroidGui;


/**
 * Interface containing all methods which can/must be overridden in order to create a functional Activation Handler.
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 * @param <GUI> Type of the UI that should be used.
 */
public interface ActivationImplementationInterface <GUI extends AndroidGui> extends ActivationCallbackInterface<GUI> {

    /**
     * Key for the return class which is used in extra arguments of the Intent used to transport the activation URL.
     * <p>The return class, if set in the Intent, is used to perform an explicit Intent with the refresh URL at the
     * end of the authentication procedure.</p>
     */
    public static final String RETURN_CLASS = "org.openecard.android.activation.return-class";


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

    /**
     * Cancels the running authentication if there is one running.
     * This is the same as calling {@link #cancelAuthentication(boolean)} with the parameter set to {@code false}.
     *
     * @see #cancelAuthentication(boolean)
     */
    // TODO: set deprecation to only use overloaded method
    void cancelAuthentication();

    /**
     * Cancels the running authentication if there is one running.
     * This method is safe to call even though there is no running authentication.<br>
     *
     * @param runInThread If {@code true}, the cancel action is performed in the background and does not block the
     */
    void cancelAuthentication(boolean runInThread);

}
