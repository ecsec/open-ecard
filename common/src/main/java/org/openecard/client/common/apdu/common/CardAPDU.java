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
package org.openecard.client.common.apdu.common;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
class CardAPDU {

    /**
     * 0x00 byte
     */
    protected static final byte x00 = (byte) 0x00;
    /**
     * 0xFF byte
     */
    protected static final byte xFF = (byte) 0xFF;
    /**
     * Data field of the APDU.
     */
    protected byte[] data = new byte[0];

    /**
     * Returns the data field of the APDU.
     *
     * @return Data field
     */
    public byte[] getData() {
	if (data.length == 0) {
	    return null;
	} else {
	    byte[] ret = new byte[data.length];
	    System.arraycopy(data, 0, ret, 0, data.length);

	    return ret;
	}
    }

    /**
     * Sets the data field of the APDU.
     *
     * @param data Data field
     */
    public void setData(byte[] data) {
	this.data = data;
    }

}
