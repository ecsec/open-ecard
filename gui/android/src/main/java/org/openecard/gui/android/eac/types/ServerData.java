/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.gui.android.eac.types;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;


/**
 *
 * @author Tobias Wich
 */
public class ServerData implements Serializable {

    private static final long serialVersionUID = 1L;

    public ServerData(@Nonnull String subject, @Nonnull String subjectUrl, @Nonnull TermsOfUsage termsOfUsage,
	    @Nonnull String validity, @Nonnull String issuer, @Nonnull String issuerUrl,
	    @Nonnull List<BoxItem> readAccessAttributes, @Nonnull List<BoxItem> writeAccessAttributes) {
	this.subject = subject;
	this.subjectUrl = subjectUrl;
	this.termsOfUsage = termsOfUsage;
	this.validity = validity;
	this.issuer = issuer;
	this.issuerUrl = issuerUrl;
	this.readAccessAttributes = readAccessAttributes;
	this.writeAccessAttributes = writeAccessAttributes;
    }


    protected String subject;
    protected String subjectUrl;
    protected TermsOfUsage termsOfUsage;
    // Format: "from $date to $date"
    protected String validity;
    protected String issuer;
    protected String issuerUrl;

    protected List<BoxItem> readAccessAttributes;
    protected List<BoxItem> writeAccessAttributes;


    public String getSubject() {
	return subject;
    }

    public String getSubjectUrl() {
	return subjectUrl;
    }

    public TermsOfUsage getTermsOfUsage() {
	return termsOfUsage;
    }

    public String getValidity() {
	return validity;
    }

    public String getIssuer() {
	return issuer;
    }

    public String getIssuerUrl() {
	return issuerUrl;
    }

    public List<BoxItem> getReadAccessAttributes() {
	return readAccessAttributes;
    }

    public List<BoxItem> getWriteAccessAttributes() {
	return writeAccessAttributes;
    }

}
