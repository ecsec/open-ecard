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

package org.openecard.client.connector.messages;

import java.net.URL;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 *
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
