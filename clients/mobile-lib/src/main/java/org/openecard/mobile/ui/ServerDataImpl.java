/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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

package org.openecard.mobile.ui;

import java.util.List;
import org.openecard.mobile.activation.SelectableItem;
import org.openecard.mobile.activation.ServerData;
import org.openecard.mobile.activation.TermsOfUsage;


/**
 *
 * @author Tobias Wich
 */
class ServerDataImpl implements ServerData {

    private final String subject;
    private final String issuer;
    private final String subjectUrl;
    private final String issuerUrl;
    private final String validity;
    private final TermsOfUsage termsOfUsage;
    private final List<SelectableItem> readAccessAttributes;
    private final List<SelectableItem> writeAccessAttributes;

    public ServerDataImpl(String subject, String issuer, String subjectUrl, String issuerUrl, String validity, TermsOfUsage termsOfUsage, List<SelectableItem> readAccessAttributes, List<SelectableItem> writeAccessAttributes) {
	this.subject = subject;
	this.issuer = issuer;
	this.subjectUrl = subjectUrl;
	this.issuerUrl = issuerUrl;
	this.validity = validity;
	this.termsOfUsage = termsOfUsage;
	this.readAccessAttributes = readAccessAttributes;
	this.writeAccessAttributes = writeAccessAttributes;
    }

    @Override
    public String getSubject() {
	return subject;
    }

    @Override
    public String getSubjectUrl() {
	return subjectUrl;
    }

    @Override
    public String getIssuer() {
	return issuer;
    }

    @Override
    public String getIssuerUrl() {
	return issuerUrl;
    }

    @Override
    public String getValidity() {
	return validity;
    }

    @Override
    public TermsOfUsage getTermsOfUsage() {
	return termsOfUsage;
    }

    @Override
    public List<SelectableItem> getReadAccessAttributes() {
	return readAccessAttributes;
    }

    @Override
    public List<SelectableItem> getWriteAccessAttributes() {
	return writeAccessAttributes;
    }
}
