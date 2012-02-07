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
package org.openecard.client.common.util;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class ByteUtilsTest {

    @Test
    public void testtohexString() {
        byte[] testData = new byte[20];

        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) i;
        }
//        assertEquals(ByteUtils.toHexString(testData), "000102030405060708090A0B0C0D0E0F10111213");
//        assertEquals(ByteUtils.toHexString(testData, true), "0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 0x09 0x0A 0x0B 0x0C 0x0D 0x0E 0x0F 0x10 0x11 0x12 0x13 ");
        System.out.println(ByteUtils.toHexString(testData));
        System.out.println(ByteUtils.toHexString(testData, true));
        System.out.println(ByteUtils.toHexString(testData, true, true));
    }
}
