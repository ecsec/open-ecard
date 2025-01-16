/****************************************************************************
 * Copyright (C) 2023 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.bouncycastle.tls.CertificateRequest;
import org.openecard.bouncycastle.tls.TlsContext;
import org.openecard.bouncycastle.tls.TlsCredentialedSigner;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.util.Promise;
import org.openecard.crypto.common.sal.TokenFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Implementation of CredentialFactory operating on generic crypto SAL DIDs.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class SearchingSmartCardCredentialFactory extends BaseSmartCardCredentialFactory {

    private final EventDispatcher evtDispatcher;
    private final ConnectionHandleType sessionHandle;

    private ConnectionHandleType usedHandle;
    private Set<String> allowedCardTypes;


    public SearchingSmartCardCredentialFactory(
	@Nonnull Dispatcher dispatcher,
	boolean filterAlwaysReadable,
	@Nonnull EventDispatcher evtDispatcher,
	@Nonnull ConnectionHandleType sessionHandle,
	Set<String> allowedCardTypes
    ) {
	super(dispatcher, filterAlwaysReadable);
	this.evtDispatcher = evtDispatcher;
	this.sessionHandle = sessionHandle;
	this.allowedCardTypes = allowedCardTypes;
    }

    @Nullable
    public ConnectionHandleType getUsedHandle() {
	return usedHandle;
    }

    protected boolean isAllowedCardType(String cardType) {
	if (allowedCardTypes.isEmpty()) {
	    return true;
	} else {
	    return allowedCardTypes.contains(cardType);
	}
    }

    @Override
    public List<TlsCredentialedSigner> getClientCredentials(CertificateRequest cr) {
	// find a card which can be used to answer the request
	TokenFinder f = new TokenFinder(dispatcher, evtDispatcher, sessionHandle, allowedCardTypes);
	try (TokenFinder.TokenFinderWatcher fw = f.startWatching()) {
	    Promise<ConnectionHandleType> card = fw.waitForNext();
	    ConnectionHandleType handle = card.deref();
	    usedHandle = handle;
	    return getClientCredentialsForCard(cr, handle);
	} catch (InterruptedException ex) {
	    LOG.warn("Interrupted while waiting for a card to be inserted, continuing without certificate authentication.");
	    return Collections.emptyList();
	} catch (WSHelper.WSException ex) {
	    LOG.warn("Error while accessing the smartcard, continuing without certificate authentication.");
	    return Collections.emptyList();
	}
    }

}
