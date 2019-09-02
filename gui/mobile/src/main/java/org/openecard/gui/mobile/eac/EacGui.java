/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

package org.openecard.gui.mobile.eac;

import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.gui.mobile.eac.types.BoxItem;
import org.openecard.gui.mobile.eac.types.PinStatus;
import org.openecard.gui.mobile.eac.types.ServerData;
import org.openecard.gui.mobile.MobileGui;


/**
 *
 * @author Tobias Wich
 */
public interface EacGui extends MobileGui {

    /**
     * Gets the server data according to the CertificateDescription data structure.
     *
     * @return Returns the Server Data as soon it is available.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    ServerData getServerData() throws InterruptedException;

    /**
     * Gets the TransactionInfo from the EAC1Input message.
     * 
     * @return The TransactionInfo value, or {@code null} if none is sent by the eID Server.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    @Nullable
    String getTransactionInfo() throws InterruptedException;

    /**
     * Sets the attribute selection made by the user in the EAC process.
     * The selection should be made based on the values received in the ServerInfo. The values must not be null.
     *
     * @param readAccessAttr Read only attributes
     * @param writeAccessAttr
     */
    void selectAttributes(@Nonnull List<BoxItem> readAccessAttr, @Nonnull List<BoxItem> writeAccessAttr);

    /**
     * Get status of the PIN on the card.
     * This method uses the name value of the PinStatus enum type. The enum type has values to check whether CAN must be
     * entered or whether the PIN is operational.
     *
     * @return Status of the PIN.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    PinStatus getPinStatus() throws InterruptedException;

    /**
     * Enter the user's password (usually PIN) and if needed also the CAN.
     * In case the CAN is not needed, the parameter may be null.
     *
     * @param can
     * @param password
     * @return {@code true} if PIN is accepted, {@code false} otherwise.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    boolean enterPin(String can, String password) throws InterruptedException;

    /**
     * Cancel EAC process.
     */
    void cancel();

    /**
     * Non blocking call returning whether the UI is corrently open or if it is closed.
     * The done status is {@code true} if either the UI has been cancelled, or the UI is done being displayed.
     * This information can be used to determine whether
     *
     * @return {@code true} in case the GUI is not isplayed anymore, {@code false} otherwise.
     */
    boolean isDone();

    /**
     * Waits until the UI is finished.
     * The UI is finished either if it is cancelled, it terminated with an error, or it finished without an error.
     *
     * @param timeout Timeout in milliseconds. The value is a positive integer. Use {@link Long#MAX_VALUE} to wait
     *   quasi forever.
     * @throws InterruptedException Thrown in case waiting has been interrupted.
     * @throws TimeoutException Thrown in case the timeout limi is reached without the UI closing in the meantime.
     */
    void waitForDone(long timeout) throws InterruptedException, TimeoutException;

}
