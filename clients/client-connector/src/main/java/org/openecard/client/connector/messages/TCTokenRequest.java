package org.openecard.client.connector.messages;

/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.tctoken.TCToken;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenRequest implements ClientRequest {

    private TCToken token;
    private byte[] slotHandle;
    private byte[] contextHandle;

    /**
     * Returns the TCToken.
     *
     * @return TCToken
     */
    public TCToken getTCToken() {
	return token;
    }

    /**
     * Sets the TCToken.
     *
     * @param token TCToken
     */
    public void setTCToken(TCToken token) {
	this.token = token;
    }

    /**
     * Returns the context handle.
     *
     * @return Context handle
     */
    public byte[] getContextHandle() {
	return contextHandle;
    }

    /**
     * Sets the context handle.
     *
     * @param contextHandle Context handle
     */
    public void setContextHandle(String contextHandle) {
	this.contextHandle = StringUtils.toByteArray(contextHandle);
    }

    /**
     * Returns the slot handle.
     *
     * @return Slot handle
     */
    public byte[] getSlotHandle() {
	return slotHandle;
    }

    /**
     * Sets the slot handle.
     *
     * @param slotHandle Slot handle
     */
    public void setSlotHandle(String slotHandle) {
	this.slotHandle = StringUtils.toByteArray(slotHandle);
    }
}
