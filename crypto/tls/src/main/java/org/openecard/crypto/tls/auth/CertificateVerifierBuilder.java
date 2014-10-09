/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.crypto.tls.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Builder class for CertificateVerifier instances.
 * This class helps to combine different verifiers with AND and OR operators.
 * <p>The builder class is mutable, however the built CertificateVerifier instances are not.</p>
 *
 * @author Tobias Wich
 */
public class CertificateVerifierBuilder {

    private final CertificateVerifierBuilder parent;
    private final LinkedList<CertificateVerifierBuilder> orChilds = new LinkedList<>();
    private final LinkedList<CertificateVerifier> andList = new LinkedList<>();

    /**
     * Creates an empty builder instance.
     * The empty builder node can be either an AND or an OR node, depending how it is initialized.
     */
    public CertificateVerifierBuilder() {
	this.parent = null;
    }

    private CertificateVerifierBuilder(CertificateVerifierBuilder parent) {
	this.parent = parent;
    }


    /**
     * Adds all verifiers which are checked together with all other verifiers in a boolean AND expression.
     * This function may only be called when no OR function has been called on this node before.
     *
     * @param verifier The list of verifiers to add.
     * @return This instance of the builder to enable command chaining.
     * @throws IllegalArgumentException Thrown in case this builder node is already an OR node.
     */
    public CertificateVerifierBuilder and(CertificateVerifier... verifier) {
	if (orChilds.isEmpty()) {
	    return and(Arrays.asList(verifier));
	} else {
	    throw new IllegalStateException("The CertificateVerifierBuilder already contains OR elements.");
	}
    }

    /**
     * Adds all verifiers which are checked together with all other verifiers in a boolean AND expression.
     * This function may only be called when no OR function has been called on this node before.
     *
     * @param verifier The list of verifiers to add. The list may not be null.
     * @return This instance of the builder to enable command chaining.
     * @throws IllegalArgumentException Thrown in case this builder node is already an OR node.
     * @throws NullPointerException Thrown in case the given list is null.
     */
    public CertificateVerifierBuilder and(@Nonnull Collection<CertificateVerifier> verifier) {
	if (orChilds.isEmpty()) {
	    andList.addAll(verifier);
	    return this;
	} else {
	    throw new IllegalStateException("The CertificateVerifierBuilder already contains OR elements.");
	}
    }


    /**
     * Creates a new OR node where CertificateVerifier instances can be added.
     * This function may only be called when no AND function has been called on this node before.
     *
     * @return A new instance which is a child of the node this function is called on.
     * @throws IllegalArgumentException Thrown in case this builder node is already an AND node.
     */
    public CertificateVerifierBuilder or() {
	if (andList.isEmpty()) {
	    CertificateVerifierBuilder cvb = new CertificateVerifierBuilder(this);
	    orChilds.add(cvb);
	    return cvb;
	} else {
	    throw new IllegalStateException("The CertificateVerifierBuilder already contains AND elements.");
	}
    }


    /**
     * Derives a CertificateVerifier instance based on the configuration of the builder.
     *
     * @return The derived CertificateVerifier.
     */
    public CertificateVerifier build() {
	if (parent != null) {
	    // get back to the root element and start building from there
	    return parent.build();
	} else {
	    return buildInternal();
	}
    }

    private CertificateVerifier buildInternal() {
	// copy and elements so that further modification of the builder does not affect the validation
	final Collection<CertificateVerifier> andCopy = Collections.unmodifiableCollection(andList);
	// convert OR builder to verifier
	final ArrayList<CertificateVerifier> orCopy = new ArrayList<>(orChilds.size());
	for (CertificateVerifierBuilder next : orChilds) {
	    orCopy.add(next.buildInternal());
	}

	return new CertificateVerifier() {
	    @Override
	    public void isValid(Certificate chain, String hostname) throws CertificateVerificationException {
		if (! andCopy.isEmpty()) {
		    // process each AND check and pass if none failed
		    for (CertificateVerifier cv : andCopy) {
			cv.isValid(chain, hostname);
		    }
		} else if (! orCopy.isEmpty()) {
		    // process all OR values and fail if none passed
		    boolean noSuccess = true;
		    for (CertificateVerifier cv : orCopy) {
			try {
			    cv.isValid(chain, hostname);
			    // a successful outcome means we passed, so break the loop
			    break;
			} catch (CertificateVerificationException ex) {
			    noSuccess = false;
			}
		    }
		    if (noSuccess) {
			String msg = "None of the possible validation paths succeeded.";
			throw new CertificateVerificationException(msg);
		    }
		}
	    }
	};
    }

}
