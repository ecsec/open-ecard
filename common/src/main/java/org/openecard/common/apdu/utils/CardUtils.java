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
import org.openecard.common.apdu.Select;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ShortUtils;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardUtils {

    private Dispatcher dispatcher;

    /**
     * Creates a new utility class for file operations.
     *
     * @param dispatcher Dispatcher
     */
    @Deprecated
    public CardUtils(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    /**
     * Select the Master File.
     *
     * @param slotHandle Slot handle
     * @throws APDUException
     */
    @Deprecated
    public void selectMF(byte[] slotHandle) throws APDUException {
	CardCommandAPDU selectMF = new Select.MasterFile();
	selectMF.transmit(dispatcher, slotHandle);
    }

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
     * Select a File.
     *
     * @param slotHandle Slot handle
     * @param fileID File identifier
     * @throws APDUException
     */
    @Deprecated
    public void selectFile(byte[] slotHandle, short fileID) throws APDUException {
	CardCommandAPDU selectFile = new Select.File(ShortUtils.toByteArray(fileID));
	selectFile.transmit(dispatcher, slotHandle);
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
     * @throws APDUException
     */
    public static void selectFile(Dispatcher dispatcher, byte[] slotHandle, byte[] fileID) throws APDUException {
	CardCommandAPDU selectFile = new Select.File(fileID);
	selectFile.transmit(dispatcher, slotHandle);
    }

    /**
     * Read a file.
     *
     * @param slotHandle Slot handle
     * @param fileID File identifier
     * @return Read file content.
     * @throws Exception
     */
    @Deprecated
    public byte[] readFile(byte[] slotHandle, short fileID) throws Exception {
	// Select MF
	selectMF(slotHandle);
	// Select file
	selectFile(slotHandle, fileID);

	// Read file
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	byte length = (byte) 0xFF;
	int i = 0;
	CardResponseAPDU response;

	do {
	    CardCommandAPDU readBinary = new ReadBinary((short) (i * (length & 0xFF)), length);
	    response = readBinary.transmit(dispatcher, slotHandle);

	    baos.write(response.getData());
	    i++;

	} while (response.isNormalProcessed());
	baos.close();

	return baos.toByteArray();
    }

    /**
     * Reads a file.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @return File content
     * @throws APDUException
     */
    public static byte[] readFile(Dispatcher dispatcher, byte[] slotHandle) throws APDUException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// Read 255 bytes per APDU
	byte length = (byte) 0xFF;
	int i = 0;
	CardResponseAPDU response;

	try {
	    do {
		CardCommandAPDU readBinary = new ReadBinary((short) (i * (length & 0xFF)), length);
		response = readBinary.transmit(dispatcher, slotHandle);

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
	selectFile(dispatcher, slotHandle, fileID);
	return readFile(dispatcher, slotHandle);
    }

}
