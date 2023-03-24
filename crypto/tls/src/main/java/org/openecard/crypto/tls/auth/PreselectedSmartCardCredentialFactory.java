/****************************************************************************
 * Copyright (C) 2013-2023 ecsec GmbH.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openecard.bouncycastle.tls.CertificateRequest;
import org.openecard.bouncycastle.tls.TlsContext;
import org.openecard.bouncycastle.tls.TlsCredentialedSigner;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.Promise;
import org.openecard.crypto.common.sal.TokenFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of CredentialFactory operating on generic crypto SAL DIDs.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class PreselectedSmartCardCredentialFactory extends BaseSmartCardCredentialFactory {

    private ConnectionHandleType inputHandle;


    public PreselectedSmartCardCredentialFactory(
	@Nonnull Dispatcher dispatcher,
	@Nonnull ConnectionHandleType handle,
	boolean filterAlwaysReadable
    ) {
	super(dispatcher, filterAlwaysReadable);
	this.inputHandle = handle;
    }

    @Nullable
    public ConnectionHandleType getUsedHandle() {
	return inputHandle;
    }

    @Override
    public List<TlsCredentialedSigner> getClientCredentials(CertificateRequest cr) {
	// use the one prepared handle
	return getClientCredentialsForCard(cr, inputHandle);
    }

}
