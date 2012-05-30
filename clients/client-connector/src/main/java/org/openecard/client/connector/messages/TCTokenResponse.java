package org.openecard.client.connector.messages;

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


import java.net.URL;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenResponse implements ClientResponse {

    private URL refreshAddress;
    private String errorMessage;
    private String errorPage;

    /**
     * Returns the refresh address.
     *
     * @return Refresh address
     */
    public URL getRefreshAddress() {
	return refreshAddress;
    }

    /**
     * Sets the refresh address.
     *
     * @param refreshAddress Refresh address
     */
    public void setRefreshAddress(URL refreshAddress) {
	this.refreshAddress = refreshAddress;
    }

    /**
     * Returns the error message.
     *
     * @return Error message
     */
    public String getErrorMessage() {
	return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage Error message
     */
    public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
    }

    /**
     * Returns the error page.
     *
     * @return Error page
     */
    public String getErrorPage() {
	return errorPage;
    }

    /**
     * Sets the error page.
     *
     * @param errorPage Error page
     */
    public void setErrorPage(String errorPage) {
	this.errorPage = errorPage;
    }
}
