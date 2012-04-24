/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.crypto.common.asn1.cvc;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.StringUtils;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CHATTest {

    private CHAT chat;
    private byte[] chatBytes;

    public CHATTest() {
    }

    @Before
    public void setUp() throws TLVException {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffb7");
        chat = new CHAT(chatBytes);
    }

    @Test
    public void testEncoding() throws TLVException {
        assertArrayEquals(chatBytes, chat.toByteArray());
    }

    @Test
    public void testWriteAccess() throws TLVException {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305000301ffb7");
        chat.setWriteAccess(CHAT.DataGroup.DG17, false);
        chat.setWriteAccess("DG18", false);
        assertArrayEquals(chatBytes, chat.toByteArray());
    }

    @Test
    public void testReadAccess() throws TLVException {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301f0b7");
        chat.setReadAccess(CHAT.DataGroup.DG01, false);
        chat.setReadAccess(CHAT.DataGroup.DG02, false);
        chat.setReadAccess(CHAT.DataGroup.DG03.name(), false);
        chat.setReadAccess("DG04", false);
        assertArrayEquals(chatBytes, chat.toByteArray());
    }

    @Test
    public void testSpecialFunctions() throws TLVException {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffAF");
        chat.setSpecialFunctions(CHAT.SpecialFunction.PRIVILEGED_TERMINAL, true);
        chat.setSpecialFunctions("CAN_ALLOWED", false);
        assertArrayEquals(chatBytes, chat.toByteArray());
    }

}
