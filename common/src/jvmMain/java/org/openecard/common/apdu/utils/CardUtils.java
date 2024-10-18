/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openecard.common.apdu.ReadBinary;
import org.openecard.common.apdu.ReadRecord;
import org.openecard.common.apdu.Select;
import org.openecard.common.apdu.Select.MasterFile;
import org.openecard.common.apdu.UpdateRecord;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardCommandStatus;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.common.TrailerConstants;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.iso7816.DataElements;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.ShortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for elementary file operations with smart cards.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Simon Potzernheim
 * @author Tobias Wich
 */
public class CardUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CardUtils.class);

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
	return selectFileWithOptions(dispatcher, slotHandle, fileID, null, FileControlParameters.NONE);
    }

    /**
     * Select a file with different options.
     *
     * @param dispatcher The Dispatcher for dispatching of the card commands.
     * @param slotHandle The SlotHandle which identifies the card terminal.
     * @param fileIdOrPath File identifier or path to the file to select.
     * @param responses List of byte arrays with the trailers which should not thrown as errors.
     * @param resultType Int value which indicates whether the select should be performed with a request of the FCP, FCI,
     * FMD or without any data. There are four public variables available in this class to use.
     * @return A CardResponseAPDU object with the requested response data.
     * @throws APDUException Thrown if the selection of a file failed.
     */
    public static CardResponseAPDU selectFileWithOptions(Dispatcher dispatcher, byte[] slotHandle, byte[] fileIdOrPath,
	    List<byte[]> responses, FileControlParameters resultType) throws APDUException {
	Select selectFile;
	CardResponseAPDU result = null;

	// respect the possibility that fileID could be a path
	int i = 0;
	while (i < fileIdOrPath.length) {
	    if (fileIdOrPath[i] == (byte) 0x3F && fileIdOrPath[i + 1] == (byte) 0x00 && i == 0) {
		selectFile = new MasterFile();
		i = i + 2;
	    } else if (i == fileIdOrPath.length - 2) {
		selectFile = new Select.ChildFile(new byte[]{fileIdOrPath[i], fileIdOrPath[i + 1]});
		switch(resultType) {
		    case NONE:
			selectFile.setNoMetadata();
			break;
		    case FCP:
			selectFile.setFCP();
			break;
		    case FCI:
			selectFile.setFCI();
			break;
		    case FMD:
			selectFile.setFMD();
			break;
		    default:
			throw new APDUException("There is no value associated with the returnType value " + resultType);
		}

		i = i + 2;
	    } else {
		selectFile = new Select.ChildDirectory(new byte[]{fileIdOrPath[i], fileIdOrPath[i + 1]});
		i = i + 2;
	    }

	    if (responses == null) {
		// not all cards, e.g. Estonian id card, support P1 = 00 and DataFile filled with MF Fid so work around this
		if (i == 2 && fileIdOrPath[0] == (byte) 0x3F && fileIdOrPath[1] == (byte) 0x00) {
		    responses = new ArrayList<>();
		    responses.add(new byte[] {(byte) 0x90, (byte) 0x00});
		    responses.add(new byte[] {(byte) 0x67, (byte) 0x00});
		    responses.add(new byte[] {(byte) 0x6A, (byte) 0x86});
		}
		result = selectFile.transmit(dispatcher, slotHandle, responses);

		if (! Arrays.equals(result.getTrailer(), new byte[] {(byte) 0x90, (byte) 0x00}) && i == 2 &&
			fileIdOrPath[0] == (byte) 0x3F && fileIdOrPath[1] == (byte) 0x00) {
		    selectFile = new Select((byte) 0x00, (byte) 0x0c);
		    result = selectFile.transmit(dispatcher, slotHandle, responses);

		    // if the result is still not 9000 the card probably does not support single directory/file selection
		    // so lets try selection by path
		    if (! Arrays.equals(result.getTrailer(), new byte[] {(byte) 0x90, (byte) 0x00}) &&
			    fileIdOrPath.length > 2) {
			selectFile = new Select.AbsolutePath(fileIdOrPath);
			result = selectFile.transmit(dispatcher, slotHandle);
			if (Arrays.equals(result.getTrailer(), TrailerConstants.Success.OK())) {
			    return result;
			}
		    }
		}
	    } else {
		result = selectFile.transmit(dispatcher, slotHandle, responses);
	    }

	}

	return result;
    }

    /**
     * Select an application by it's file identifier.
     *
     * @param dispatcher The message dispatcher for the interaction with the card.
     * @param slotHandle
     * @param fileID File identitfier of an application or a path to the application.
     * @return The {@link CardResponseAPDU} from the last select which means the select of the application to select.
     * @throws APDUException
     */
    public static CardResponseAPDU selectApplicationByFID(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID) throws APDUException {
	Select selectApp;
	CardResponseAPDU result = null;

	// respect the possibility that fileID could be a path
	int i = 0;
	while (i < fileID.length) {
	    if (fileID[i] == (byte) 0x3F && fileID[i + 1] == (byte) 0x00 && i == 0 && i + 1 == 1) {
		selectApp = new MasterFile();
		i = i + 2;
	    } else {
		selectApp = new Select.ChildDirectory(new byte[]{fileID[i], fileID[i + 1]});
		selectApp.setLE((byte) 0xFF);
		selectApp.setFCP();
		i = i + 2;
	    }

	    result = selectApp.transmit(dispatcher, slotHandle);
	}

	return result;
    }

    /**
     * Select an application by the application identifier.
     * This method requests the FCP of the application.
     *
     * @param dispatcher
     * @param slotHandle
     * @param aid Application identifier
     * @return Response APDU of the select command.
     * @throws APDUException Thrown in case there was an error while processing the command APDU.
     */
    public static CardResponseAPDU selectApplicationByAID(Dispatcher dispatcher, byte[] slotHandle, byte[] aid)
	    throws APDUException {
	Select selectApp = new Select((byte) 0x04, (byte) 0x04);
	selectApp.setData(aid);
	selectApp.setLE((byte) 0xFF);
	CardResponseAPDU result = selectApp.transmit(dispatcher, slotHandle);
	return result;
    }

	public static byte[] readFile(FCP fcp, Dispatcher dispatcher, byte[] slotHandle) throws APDUException {
		return readFile(fcp, null, dispatcher, slotHandle);
	}

    /**
     * Reads a file.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param fcp File Control Parameters, may be null
	 * @param shortEf Short EF identifier, may be null
     * @return File content
     * @throws APDUException
     */
    public static byte[] readFile(FCP fcp, Byte shortEf, Dispatcher dispatcher, byte[] slotHandle) throws APDUException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// Read 255 bytes per APDU
	byte length = (byte) 0xFF;
	short numToRead = -1; // -1 indicates I don't know
	if (fcp != null) {
	    Long fcpNumBytes = fcp.getNumBytes();
	    if (fcpNumBytes != null) {
		// more than short is not possible and besides that very unrealistic
		numToRead = fcpNumBytes.shortValue();
		// reduce readout size
		if (numToRead < 255) {
		    length = (byte) numToRead;
		}
	    }
	}

	boolean isRecord = isRecordEF(fcp);
	byte i = (byte) (isRecord ? 1 : 0); // records start at index 1
	short numRead = 0;

	try {
	    CardResponseAPDU response;
	    byte[] trailer;
	    int lastNumRead = 0;
	    boolean goAgain;
	    do {
		if (! isRecord) {
		    CardCommandAPDU readBinary;
			if (shortEf != null) {
				readBinary = new ReadBinary(shortEf, numRead, length);
			} else {
				readBinary = new ReadBinary(numRead, length);
			}
		    // 0x6A84 code for the estonian identity card. The card returns this code
		    // after the last read process.
		    response = readBinary.transmit(dispatcher, slotHandle, CardCommandStatus.response(0x9000, 0x6282,
			    0x6A84, 0x6A83, 0x6A86, 0x6B00));
		} else {
		    CardCommandAPDU readRecord;
			if (shortEf != null) {
				readRecord = new ReadRecord(shortEf, (byte) i);
			} else {
				readRecord = new ReadRecord((byte) i);
			}
		    response = readRecord.transmit(dispatcher, slotHandle, CardCommandStatus.response(0x9000, 0x6282,
			    0x6A84, 0x6A83));
		}

		trailer = response.getTrailer();
		if (! Arrays.equals(trailer, new byte[] {(byte) 0x6A, (byte) 0x84}) &&
			! Arrays.equals(trailer, new byte[] {(byte) 0x6A, (byte) 0x83}) &&
			! Arrays.equals(trailer, new byte[] {(byte) 0x6A, (byte) 0x86})) {
		    byte[] data = response.getData();
		    // some cards are just pure shit and return 9000 when no bytes have been read
		    baos.write(data);
		    lastNumRead = data.length;
		    numRead += lastNumRead;
		}
		i++;

		// update length value
		goAgain = response.isNormalProcessed() && lastNumRead != 0
			|| (Arrays.equals(trailer, new byte[]{(byte) 0x62, (byte) 0x82}) && isRecord);
		if (goAgain && numToRead != -1) {
		    // we have a limit, enforce it
		    short remainingBytes = (short) (numToRead - numRead);
		    if (remainingBytes <= 0) {
			goAgain = false;
		    } else if (remainingBytes < 255) {
			// update length when we reached the area below 255
			length = (byte) remainingBytes;
		    }
		}
	    } while (goAgain);
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
    public static byte[] selectReadFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID) throws APDUException {
		FCP fcp = null;
		if (! isShortEFIdentifier(fileID)) {
			CardResponseAPDU selectResponse = selectFileWithOptions(dispatcher, slotHandle, fileID, null, FileControlParameters.FCP);
			try {
				fcp = new FCP(selectResponse.getData());
			} catch (TLVException e) {
				LOG.warn("Couldn't get File Control Parameters from Select response.", e);
			}
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

    public static boolean isShortEFIdentifier(byte[] fileID) {
	return fileID.length == 1;
    }

    public static void writeFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID, byte[] data) throws APDUException {
	CardResponseAPDU selectResponse = selectFile(dispatcher, slotHandle, fileID);
	FCP fcp = null;
	try {
	    fcp = new FCP(selectResponse.getData());
	} catch (TLVException e) {
	    LOG.warn("Couldn't get File Control Parameters from Select response.", e);
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
