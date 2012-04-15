/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.common.apdu.utils;

import java.io.ByteArrayOutputStream;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.apdu.ReadBinary;
import org.openecard.client.common.apdu.Select;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.util.ShortUtils;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardUtils {

    private Dispatcher dispatcher;

    /**
     * Creates a new utility class for file operations.
     *
     * @param ifd IFD
     */
    public CardUtils(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    /**
     * Select the Master File.
     *
     * @param slotHandle Slot handle
     * @throws org.openecard.client.common.WSHelper.WSException
     */
    public void selectMF(byte[] slotHandle) throws WSException {
	CardCommandAPDU selectMF = new Select.MasterFile();
	selectMF.transmit(dispatcher, slotHandle);
    }

    /**
     * Select a File.
     *
     * @param slotHandle Slot handle
     * @param fileID File identifier
     * @throws org.openecard.client.common.WSHelper.WSException
     */
    public void selectFile(byte[] slotHandle, short fileID) throws WSException {
	CardCommandAPDU selectFile = new Select.File(ShortUtils.toByteArray(fileID));
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

}
