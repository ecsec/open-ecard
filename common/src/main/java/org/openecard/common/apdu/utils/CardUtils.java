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
import java.util.Arrays;
import org.openecard.common.apdu.ReadBinary;
import org.openecard.common.apdu.ReadRecord;
import org.openecard.common.apdu.Select;
import org.openecard.common.apdu.Select.MasterFile;
import org.openecard.common.apdu.UpdateRecord;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardCommandStatus;
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
     * @return The CardResponseAPDU from the selection of the file
     * @throws APDUException
     */
    public static CardResponseAPDU selectFile(Dispatcher dispatcher, byte[] slotHandle, short fileID) throws APDUException {
	return selectFile(dispatcher, slotHandle, ShortUtils.toByteArray(fileID));
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
	Select selectFile;
	CardResponseAPDU result = null;

	// respect the possibility that fileID could be a path
	int i = 0;
	while (i < fileID.length) {
	    if (fileID[i] == (byte) 0x3F && fileID[i + 1] == (byte) 0x00 && i == 0 && i + 1 == 1) {
		selectFile = new MasterFile();
		i = i + 2;
	    } else if (i == fileID.length - 2) {
		selectFile = new Select.ChildFile(new byte[]{fileID[i], fileID[i + 1]});
		selectFile.setFCP();
		i = i + 2;
	    } else {
		selectFile = new Select.ChildDirectory(new byte[]{fileID[i], fileID[i + 1]});
		i = i + 2;
	    }

	    result = selectFile.transmit(dispatcher, slotHandle);
	}

	return result;
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
	boolean isRecord = isRecordEF(fcp);
	int i = isRecord ? 1 : 0; // records start at index 1

	try {
	    CardResponseAPDU response;
	    do {
		if (! isRecord) {
		    CardCommandAPDU readBinary = new ReadBinary((short) (i * (length & 0xFF)), length);
		    // 0x6A84 code for the estonian identity card. The card returns this code
		    // after the last read process.
		    response = readBinary.transmit(dispatcher, slotHandle, CardCommandStatus.response(0x9000, 0x6282, 0x6A84));
		} else {
		    CardCommandAPDU readRecord = new ReadRecord((byte) i);
		    response = readRecord.transmit(dispatcher, slotHandle, CardCommandStatus.response(0x9000, 0x6282));
		}

		if (! Arrays.equals(response.getTrailer(), new byte[] {(byte) 0x6A, (byte) 0x84})) {
		    baos.write(response.getData());
		}
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
    @Deprecated
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
    @Deprecated
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

    public static byte[] selectReadFile(Dispatcher dispatcher, byte[] slotHandle, short fileID) throws APDUException {
	return readFile(dispatcher, slotHandle, fileID);
    }

    public static byte[] selectReadFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID) throws APDUException {
	return readFile(dispatcher, slotHandle, fileID);
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
