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

package org.openecard.gui.android.eac;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.gui.android.AndroidGui;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.PinStatus;
import org.openecard.gui.android.eac.types.ServerData;


/**
 *
 * @author Tobias Wich
 */
public interface EacGui extends AndroidGui {

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
     * Enter the PIN and if needed also the CAN.
     * In case the CAN is not needed, the parameter may be null.
     *
     * @param can
     * @param pin
     * @return rue if PIN is accepted, false otherwise.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    boolean enterPin(String can, String pin) throws InterruptedException;

    /**
     * Cancel EAC process.
     */
    void cancel();

}
