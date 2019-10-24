/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import java.util.List;
import org.openecard.mobile.activation.BoxItem;
import org.openecard.mobile.activation.ServerData;
import org.openecard.mobile.activation.TermsOfUsage;

/**
 *
 * @author Neil Crossley
 */
public class CommonServerData implements ServerData {

    private final String issuer;
    private final String issuerUrl;
    private final String subject;
    private final String subjectUrl;
    private final TermsOfUsage termsOfUsage;
    private final String validity;
    private final List<BoxItem> readAccessAttributes;
    private final List<BoxItem> writeAccessAttributes;

    public CommonServerData(String issuer, String issuerUrl, String subject, String subjectUrl, TermsOfUsage termsOfUsage, String validity, List<BoxItem> readAccessAttributes, List<BoxItem> writeAccessAttributes) {
	this.issuer = issuer;
	this.issuerUrl = issuerUrl;
	this.subject = subject;
	this.subjectUrl = subjectUrl;
	this.termsOfUsage = termsOfUsage;
	this.validity = validity;
	this.readAccessAttributes = readAccessAttributes;
	this.writeAccessAttributes = writeAccessAttributes;
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
    public String getSubject() {
	return subject;
    }

    @Override
    public String getSubjectUrl() {
	return subjectUrl;
    }

    @Override
    public TermsOfUsage getTermsOfUsage() {
	return termsOfUsage;
    }

    @Override
    public String getValidity() {
	return validity;
    }

    @Override
    public List<BoxItem> getReadAccessAttributes() {
	return readAccessAttributes;
    }

    @Override
    public List<BoxItem> getWriteAccessAttributes() {
	return writeAccessAttributes;
    }

}
