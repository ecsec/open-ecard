/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.common.apdu.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.openecard.common.apdu.ReadBinary;
import org.openecard.common.apdu.ReadRecord;
import org.openecard.common.apdu.Select;
import org.openecard.common.apdu.UpdateRecord;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.iso7816.DataElements;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.ShortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 */
public class CardUtils {

    private static final Logger logger = LoggerFactory.getLogger(CardUtils.class);

    /**
     * Selects the Master File.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @throws APDUException
     */
    public static void selectMF(Dispatcher dispatcher, byte[] slotHandle) throws APDUException {
	CardCommandAPDU selectMF = new Select.MasterFile();
	selectMF.transmit(dispatcher, slotHandle);
    }

    /**
     * Selects a File.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param fileID File ID
     * @throws APDUException
     */
    public static void selectFile(Dispatcher dispatcher, byte[] slotHandle, short fileID) throws APDUException {
	selectFile(dispatcher, slotHandle, ShortUtils.toByteArray(fileID));
    }

    /**
     * Selects a File.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param fileID File ID
     * @return CardREsponseAPDU containing the File Control Parameters
     * @throws APDUException
     */
    public static CardResponseAPDU selectFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID) throws APDUException {
	Select selectFile = new Select.ChildFile(fileID);
	selectFile.setFCP();
	return selectFile.transmit(dispatcher, slotHandle);
    }

    /**
     * Reads a file.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param fcp File Control Parameters
     * @return File content
     * @throws APDUException
     */
    public static byte[] readFile(FCP fcp, Dispatcher dispatcher, byte[] slotHandle) throws APDUException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// Read 255 bytes per APDU
	byte length = (byte) 0xFF;
	int i = 0;
	CardResponseAPDU response;

	try {
	    do {
		if (! isRecordEF(fcp)) {
		    CardCommandAPDU readBinary = new ReadBinary((short) (i * (length & 0xFF)), length);
		    response = readBinary.transmit(dispatcher, slotHandle);
		} else {
		    CardCommandAPDU readRecord = new ReadRecord();
		    response = readRecord.transmit(dispatcher, slotHandle);
		}

		baos.write(response.getData());
		i++;
	    } while (response.isNormalProcessed());
	    baos.close();
	} catch (IOException e) {
	    throw new APDUException(e);
	}

	return baos.toByteArray();
    }

    /**
     * Selects and reads a file.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param fileID File ID
     * @return File content
     * @throws APDUException
     */
    public static byte[] readFile(Dispatcher dispatcher, byte[] slotHandle, short fileID) throws APDUException {
	return readFile(dispatcher, slotHandle, ShortUtils.toByteArray(fileID));
    }

    /**
     * Selects and reads a file.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param fileID File ID
     * @return File content
     * @throws APDUException
     */
    public static byte[] readFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID) throws APDUException {
	CardResponseAPDU selectResponse = selectFile(dispatcher, slotHandle, fileID);
	FCP fcp = null;
	try {
	    fcp = new FCP(selectResponse.getData());
	} catch (TLVException e) {
	    logger.warn("Couldn't get File Control Parameters from Select response.", e);
	}
	return readFile(fcp, dispatcher, slotHandle);
    }

    private static boolean isRecordEF(FCP fcp) {
	if (fcp == null) {
	    // TODO inspect EF.ATR as described in ISO/IEC 7816-4 Section 8.4
	    return false;
	} else {
	    DataElements dataElements = fcp.getDataElements();
	    if (dataElements.isLinear() || dataElements.isCyclic()) {
		return true;
	    } else {
		return false;
	    }
	}
    }

    public static void writeFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID, byte[] data) throws APDUException {
	CardResponseAPDU selectResponse = selectFile(dispatcher, slotHandle, fileID);
	FCP fcp = null;
	try {
	    fcp = new FCP(selectResponse.getData());
	} catch (TLVException e) {
	    logger.warn("Couldn't get File Control Parameters from Select response.", e);
	}
	writeFile(fcp, dispatcher, slotHandle, data);
    }

    private static void writeFile(FCP fcp, Dispatcher dispatcher, byte[] slotHandle, byte[] data) throws APDUException {
	if (isRecordEF(fcp)) {
	    UpdateRecord updateRecord = new UpdateRecord(data);
	    updateRecord.transmit(dispatcher, slotHandle);
	} else {
	    // TODO implement writing for non record files
	    throw new UnsupportedOperationException("Not yet implemented.");
	}
    }

}
