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

package org.openecard.mdlw.sal.config;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This class contains the config of a specified Middleware SAL. The class contains information to the SAL itself,
 * for example paths to the pkcs#11 module, the specifications of the cards which are used by this SAL and the
 * corresponding images to the cards.
 *
 * @author Tobias Wich
 * @author Mike Prechtl
 */
public class MiddlewareSALConfig {

    private final MiddlewareSpecType mwSpec;
    private final MiddlewareConfig mwConfig;

    public MiddlewareSALConfig(MiddlewareConfig mwConfig, MiddlewareSpecType mwSpec) {
	this.mwConfig = mwConfig;
        this.mwSpec = mwSpec;
    }

    public String getMiddlewareName() {
	return mwSpec.getMiddlewareName();
    }

    /**
     * Returns true if the Middleware SAL is required, otherwise false.
     *
     * @return {@code true} if Middleware SAL is required
     */
    public boolean isSALRequired() {
	return mwSpec.isRequired();
    }

    public String getLibName() {
	return getLibSpec().getLibName();
    }

    /**
     * Returns the search paths of the pkcs#11 module.
     *
     * @return
     */
    public List<String> getSearchPaths() {
	return Collections.unmodifiableList(getLibSpec().getSearchPath());
    }

    /**
     * Returns the x32 search paths of the pkcs#11 module.
     *
     * @return
     */
    public List<String> getX32SearchPaths() {
	return Collections.unmodifiableList(getLibSpec().getX32searchPath());
    }

    /**
     * Returns the x64 search paths of the pkcs#11 module.
     *
     * @return
     */
    public List<String> getX64SearchPaths() {
	return Collections.unmodifiableList(getLibSpec().getX64searchPath());
    }

    @Nonnull
    protected LibSpecType getLibSpec() {
	LibSpecType defaultSpec = null;
	String osName = System.getProperty("os.name", "");
	for (LibSpecType spec : mwSpec.getLibSpec()) {
	    if (spec.getOperatingSystem() == null) {
		defaultSpec = spec;
	    } else if (osName.startsWith(spec.getOperatingSystem())) {
		return spec;
	    }
	}

	if (defaultSpec == null) {
	    throw new NullPointerException("No default LibSpec defined in the XML config.");
	}
	return defaultSpec;
    }

    /**
     * Returns true if the card type is known to the corresponding Middleware SAL, otherwise false.
     *
     * @param cardType Type of the card.
     * @return {@code true} if card is known to Middleware SAL.
     */
    public boolean isCardTypeKnown(String cardType) {
        for (CardSpecType cardSpec : mwSpec.getCardConfig().getCardSpecs()) {
            if (cardSpec.getObjectIdentifier().equals(cardType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the CardInfoFile to a Card identified by the object identifier.
     *
     * @param oIdentifier
     * @return CardInfo-File or {@code null} if card is not known to Middleware SAL.
     */
    @Nullable
    public CardInfoType getCardInfo(String oIdentifier) {
	for (CardSpecType cardSpec : mwSpec.getCardConfig().getCardSpecs()) {
	    if (cardSpec.getObjectIdentifier().equals(oIdentifier)) {
		return mwConfig.getCardInfoByCardSpec(cardSpec);
	    }
	}
        return null;
    }

    /**
     * Returns the Card-Image to a Card identifier by the object identifier.
     *
     * @param oIdentifier
     * @return Card Image as InputStream or {@code null} if card is not known by Middleware SAL.
     */
    @Nullable
    public InputStream getCardImage(String oIdentifier) {
        for (CardSpecType cardSpec : mwSpec.getCardConfig().getCardSpecs()) {
            if (cardSpec.getObjectIdentifier().equals(oIdentifier)) {
                return mwConfig.getCardImage(cardSpec.getCardImageName());
            }
        }

        // return nothing
        return null;
    }

    /**
     * Maps the Middleware name to the object identifier of the card.
     *
     * @param middlewareCardType
     * @return Object identifier of card, mapped by middleware name.
     */
    @Nullable
    public String mapMiddlewareType(@Nonnull String middlewareCardType) {
	for (CardSpecType spec : mwSpec.getCardConfig().getCardSpecs()) {
	    String mwName = spec.getMiddlewareName();
	    if (middlewareCardType.equals(mwName)) {
		return spec.getObjectIdentifier();
	    }
	}

	// nothing found
	return null;
    }

    /**
     * Returns true if the ATR is known, otherwise false.
     *
     * @param atr
     * @return {@code true} if ATR is known, otherwise {@code false}.
     */
    public boolean isATRKnown(@Nullable byte[] atr) {
	if (atr != null) {
	    for (CardSpecType spec : mwSpec.getCardConfig().getCardSpecs()) {
		boolean matches = compareATR(atr, spec.getAtr(), spec.getMask());
		if (matches) {
		    return true;
		}
	    }
	}

	// no match found
	return false;
    }

    private boolean compareATR(@Nonnull byte[] atr, @Nullable byte[] refAtr, @Nullable byte[] mask) {
	if (refAtr != null) {
	    // create mask value if it is missing
	    if (mask == null) {
		mask = new byte[refAtr.length];
		Arrays.fill(mask, (byte) 0xFF);
	    }

	    // we can only match shorter strings
	    if (atr.length > refAtr.length) {
		return false;
	    }

	    // walk over each byte and compare masked value
	    for (int i=0; i < refAtr.length; i++) {
		// we have something left to compare
		if (i < atr.length) {
		    byte orig = (byte) (atr[i] & mask[i]);
		    byte ref = (byte) (refAtr[i] & mask[i]);
		    if (ref != orig) {
			return false;
		    }
		} else {
		    // accept only mask ignore values
		    if (mask[i] != 0) {
			return false;
		    }
		}
	    }

	    // we have a match
	    return true;
	} else {
	    return false;
	}
    }

}
