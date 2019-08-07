/****************************************************************************
 * Copyright (C) 2013-2019 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import org.openecard.httpcore.ValidationError;
import org.openecard.httpcore.CertificateValidator;
import java.net.MalformedURLException;
import java.net.URL;
import org.openecard.common.DynamicContext;
import org.openecard.common.util.Promise;
import org.openecard.common.util.TR03112Utils;
import org.openecard.crypto.common.asn1.cvc.CertificateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.I18n;


/**
 * Implementation performing the redirect checks according to TR-03112.
 * The checks are described in BSI TR-03112 sec. 3.4.5.
 *
 * @author Tobias Wich
 */
public class RedirectCertificateValidator implements CertificateValidator {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectCertificateValidator.class);

    private static final I18n LANG = I18n.getTranslation("tr03112");

    private final Promise<Object> descPromise;
    private final boolean redirectChecks;

    private boolean certDescExists;
    private URL lastURL;

    /**
     * Creates an object of this class bound to the values in the current dynamic context.
     *
     * @param redirectChecks True if the TR-03112 checks must be performed.
     */
    public RedirectCertificateValidator(boolean redirectChecks) {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	descPromise = dynCtx.getPromise(TR03112Keys.ESERVICE_CERTIFICATE_DESC);
	this.redirectChecks = redirectChecks;
    }


    @Override
    public VerifierResult validate(URL url, TlsServerCertificate cert) throws ValidationError {
	try {
	    // disable certificate checks according to BSI TR03112-7 in some situations
	    if (redirectChecks) {
		CertificateDescription desc = (CertificateDescription) descPromise.derefNonblocking();
		certDescExists = desc != null;

		String host = url.getProtocol() + "://" + url.getHost() + (url.getPort() == -1 ? "" : (":" + url.getPort()));
		// check points certificate (but just in case we have a certificate description)
		if (certDescExists && ! TR03112Utils.isInCommCertificates(cert, desc.getCommCertificates(), host)) {
		    LOG.error("The retrieved server certificate is NOT contained in the CommCertificates of "
			    +	"the CertificateDescription extension of the eService certificate.");
		    throw new ValidationError(LANG, INVALID_REDIRECT);
		}

		// check if we match the SOP
		URL sopUrl;
		if (certDescExists && desc.getSubjectURL() != null && ! desc.getSubjectURL().isEmpty()) {
		    sopUrl = new URL(desc.getSubjectURL());
		} else {
		    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
		    sopUrl = (URL) dynCtx.get(TR03112Keys.TCTOKEN_URL);
		}
		// determine the URL that has to be SOP checked (TR-03124 Determine refreshURL)
		// on th efirst invocation this is the current URL, on the following invocations this is the last used
		if (lastURL == null) {
		    lastURL = url;
		}

		// check SOP for last URL and update the URL
		boolean SOP = TR03112Utils.checkSameOriginPolicy(lastURL, sopUrl);
		lastURL = url;
		if (! SOP) {
		    // there is more to come
		    return VerifierResult.CONTINUE;
		} else {
		    // SOP fulfilled
		    return VerifierResult.FINISH;
		}
	    } else {
		// without the nPA there is no sensible exit point and as a result the last call is executed twice
		// in that case its equally valid to let the browser do the redirects
		return VerifierResult.FINISH;
	    }
	} catch (MalformedURLException ex) {
	    throw new ValidationError(LANG, REDIRECT_MALFORMED_URL, ex);
	}
    }

}
