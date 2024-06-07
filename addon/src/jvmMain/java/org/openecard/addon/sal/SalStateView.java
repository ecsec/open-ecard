/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.addon.sal;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.List;

/**
 *
 * @author Neil Crossley
 */
public interface SalStateView {

    List<ConnectionHandleType> listCardHandles();
	boolean hasConnectedCard(ConnectionHandleType handle);

    boolean isDisconnected(byte[] contextHandle, String givenIfdName, byte[] givenSlotIndex);

}
