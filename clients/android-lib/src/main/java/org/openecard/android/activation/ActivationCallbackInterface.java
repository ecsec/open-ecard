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

import java.util.Set;
import org.openecard.gui.mobile.MobileGui;
import org.openecard.mobile.activation.ActivationResult;


/**
 * Interface with all user changeable functions in the activation.
 *
 * @param <GUI> Type of the UI that should be used.
 * @author Tobias Wich
 */
public interface ActivationCallbackInterface <GUI extends MobileGui> {

    /**
     * Gets a set of card types supported by this activity.
     * This list is used to determine whether {@link #onCardInserted(String)} is called or not when a card is recognized by
     * the system. In case all cards should be matched, {@code null} can be returned by this method.
     *
     * @return The set of supported cards, or {@code null} in case all cards are supported.
     */
    Set<String> getSupportedCards();

    /**
     * Callback for card insert events.
     * By implementing this method, it is possible to notify the user (via the UI) that the card has been inserted and
     * the EAC process is running. Without this callback the Activity can not distinguish between 'insert card' and
     * 'please wait for the server'.
     *
     * @param cardType Type of the card which has been inserted into the terminal.
     */
    void onCardInserted(String cardType);

    /**
     * Callback for card removed events.
     * This method can be used to directly see when a card gets removed from the system.
     * <p>Causes for removal are either:</p>
     * <ul>
     * <li>User removed the card</li>
     * <li>Card gets disconnected after a finished eID process</li>
     * <li>Card is removed from the system due to an error</li>
     * </ul>
     * <p>Given the various options why a card could have been removed, it is important to take the result of the
     * eID process (cf. {@link #onAuthenticationSuccess(ActivationResult)} etc.) into consideration.</p>
     */
    void onCardRemoved();

    /**
     * This method is called when the specific android GUI is available to the activity.
     * <p>If this method is called the server data can be accessed and the PIN can be entered.</p>
     * <p>This method marks the starting point when interaction between the EAC process and the implementing activity
     * can start.</p>
     *
     * @param androidGui Instance of the Android GUI interface connected to the current process.
     */
    void onGuiIfaceSet(GUI androidGui);

    /**
     * Callback when the authentication process is concluded with a redirect address or OK status.
     * <p>Receiving a redirect address does not necessarily mean that the authentication has been successful. This is
     * for the server to decide. The URL contains more information about the actual outcome of the authentication.
     * Functionality handling these kind of errors may be added to this method before calling the default
     * implementation.</p>
     * <p>The default implementation in {@link AbstractActivationHandler} of this method performs a TR-03124 conforming
     * URL intent (redirect). When the Intent starting the Activity contained the return class in the extra arguments
     * (see {@link ActivationImplementationInterface#RETURN_CLASS}, then an explicit Intent is performed and the user
     * does not have the choice to select a suitable Application to receive the URL.</p>
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
     * Callback when the authentication process has been interrupted.
     * <p>An interruption can be caused by either a user cancel call or by the shutdown of a subsystem.</p>
     *
     * @param result Result possibly containg a message describing what interrupted the process.
     */
    void onAuthenticationInterrupted(ActivationResult result);

}
