package org.openecard.client.richclient.activation.messages;

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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.client.richclient.activation.messages.common.ClientRequest;
import org.openecard.client.richclient.activation.tctoken.TCToken;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenRequest implements ClientRequest {

    private TCToken token;
    private ConnectionHandleType connectionHandle;

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

    public ConnectionHandleType getConnectionHandle() {
	return connectionHandle;
    }

    public void setConnectionHandle(ConnectionHandleType connectionHandle) {
	this.connectionHandle = connectionHandle;
    }
}
