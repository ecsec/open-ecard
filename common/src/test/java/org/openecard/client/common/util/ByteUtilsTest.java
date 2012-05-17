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
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ByteUtilsTest {

    @Test
    public void testtohexString() {
	byte[] testData = new byte[20];

	for (int i = 0; i < testData.length; i++) {
	    testData[i] = (byte) i;
	}
	assertEquals(ByteUtils.toHexString(testData), "000102030405060708090A0B0C0D0E0F10111213");
    }

    @Test
    public void testcutLeadingNullBytes() {
	byte[] testData = new byte[20];

	for (int i = 0; i < testData.length - 9; i++) {
	    testData[i+9] = (byte) i;
	}
	assertEquals(ByteUtils.toHexString(ByteUtils.cutLeadingNullBytes(testData)), "0102030405060708090A");
    }

}
