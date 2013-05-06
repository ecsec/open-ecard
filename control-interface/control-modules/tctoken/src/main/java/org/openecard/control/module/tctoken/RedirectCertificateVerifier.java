/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.control.module.tctoken;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.DynamicContext;
import org.openecard.common.I18n;
import org.openecard.common.TR03112Keys;
import org.openecard.common.util.Promise;
import org.openecard.common.util.TR03112Utils;
import org.openecard.control.ControlException;
import org.openecard.crypto.common.asn1.cvc.CertificateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation performing the redirect checks according to TR-03112.
 * The checks are described in BSI TR-03112 sec. 3.4.5.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class RedirectCertificateVerifier implements CertificateVerifier {

    private static final Logger logger = LoggerFactory.getLogger(RedirectCertificateVerifier.class);
    private final I18n lang = I18n.getTranslation("tctoken");

    private final Promise<Object> descPromise;
    private final boolean redirectChecks;

    private boolean firstInvocation;
    private boolean lastRedirect;

    /**
     * Creates an object of this class bound to the values in the current dynamic context.
     *
     * @param redirectChecks True if the TR-03112 checks must be performed.
     */
    public RedirectCertificateVerifier(boolean redirectChecks) {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	descPromise = dynCtx.getPromise(TR03112Keys.ESERVICE_CERTIFICATE_DESC);
	this.redirectChecks = redirectChecks;
	firstInvocation = true;
	lastRedirect = false;
    }


    @Override
    public VerifierResult verify(URL url, Certificate cert) {
	try {
	    // disable certificate checks according to BSI TR03112-7 in some situations
	    if (redirectChecks) {
		CertificateDescription desc = null;
		// wait for certificate description
		try {
		    desc = (CertificateDescription) descPromise.deref(60, TimeUnit.SECONDS);
		    // no error assumes that the promise is set a meaningful value
		} catch (InterruptedException ex) {
		    String msg = "Couldn't retrieve the CertificateDescription from the DynamicContext.";
		    logger.error(msg);
		    throw new ControlException(msg);
		} catch (TimeoutException ex) {
		    String msg = "Couldn't retrieve the CertificateDescription from the DynamicContext.";
		    logger.error(msg);
		    throw new ControlException(msg);
		}

		// check points certificate
		if (! TR03112Utils.isInCommCertificates(cert, desc.getCommCertificates())) {
		    logger.error("The retrieved server certificate is NOT contained in the CommCertificates of " +
				"the CertificateDescription extension of the eService certificate.");
		    throw new ControlException(lang.translationForKey("invalid_redirect"));
		}

		// check if we match the SOP
		URL subjectUrl = new URL(desc.getSubjectURL());
		boolean SOP = TR03112Utils.checkSameOriginPolicy(url, subjectUrl);
		if (! SOP) {
		    firstInvocation = false;
		    // there is more to come
		    return VerifierResult.CONTINE;
		} else {
		    // if the refresh address is SOP, then no redirect is expected
		    if (firstInvocation) {
			return VerifierResult.FINISH;
		    }

		    if (lastRedirect) {
			// stop execution
			return VerifierResult.FINISH;
		    } else {
			// same origin satisfied, memorize this state and stop execution in the next invocation
			lastRedirect = true;
			return VerifierResult.CONTINE;
		    }
		}
	    } else {
		// without the nPA there is no sensible exit point and as a result the last call is executed twice
		// in that case its equally valid to let the browser do the redirects
		return VerifierResult.FINISH;
	    }
	} catch (MalformedURLException ex) {
	    throw new ControlException("Failed to convert SubjectURL to URL class.", ex);
	}
    }

}
