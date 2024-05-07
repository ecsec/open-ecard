/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.StringUtils;


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
    private final boolean internal;

    public MiddlewareSALConfig(MiddlewareConfig mwConfig, MiddlewareSpecType mwSpec, boolean internal) {
	this.mwConfig = mwConfig;
        this.mwSpec = mwSpec;
	this.internal = internal;
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

    public boolean isDisabled() {
	String key = mwSpec.getMiddlewareName() + ".enabled";
	boolean enabledProp = Boolean.parseBoolean(OpenecardProperties.properties().getProperty(key, "false"));
	boolean forceEnable = MiddlewareProperties.isForceLoadInternalModules() && internal;
	boolean enabled = enabledProp || forceEnable;
	return mwSpec.isDisabled() || ! enabled;
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
    public MiddlewareSpecType getMwSpec() {
	return mwSpec;
    }

    @Nonnull
    protected LibSpecType getLibSpec() throws NullPointerException {
	LibSpecType defaultSpec = null, fullMatch = null, nameMatch = null, archMatch = null;
	String osName = System.getProperty("os.name", "");
	String arch = System.getProperty("os.arch", "");

	for (LibSpecType spec : mwSpec.getLibSpec()) {
	    String specOs = spec.getOperatingSystem();
	    String specArch = spec.getArch();

	    if (specOs == null && specArch == null) {
		defaultSpec = spec;
	    } else if (specOs != null && osName.startsWith(specOs) && specArch != null && arch.equals(specArch)) {
		fullMatch = spec;
	    } else if (specOs != null && osName.startsWith(specOs) && specArch == null) {
		nameMatch = spec;
	    } else if (specOs == null && specArch != null && arch.equals(specArch)) {
		archMatch = spec;
	    }
	}

	if (fullMatch != null) {
	    return fullMatch;
	} else if (nameMatch != null) {
	    return nameMatch;
	} else if (archMatch != null) {
	    return archMatch;
	} else if (defaultSpec != null) {
	    return defaultSpec;
	} else {
	    throw new NullPointerException("No default LibSpec defined in the XML config.");
	}
    }

    public boolean hasBuiltinPinDialog() {
	return mwSpec.isBuiltinPinDialog();
    }

    /**
     * This method checks if a context specific login can be skipped for a given card type and DID. An empty optional
     * is returned, if there is no information if the context specific login should be skipped. If there are more elements
     * with the same DID name, then the last one is used!
     *
     * @param cardType This argument represents the given card type.
     * @param didName This argument represents the name of the DID.
     * @return
     */
    public Optional<Boolean> canSkipContextSpecificLogin(String cardType, String didName) {
	Optional<Boolean> canSkipContextSpecificLogin = Optional.empty();

	for (CardSpecType cardSpec : mwSpec.getCardConfig().getCardSpecs()) {
	    if (cardSpec.getObjectIdentifier().equals(cardType)) {
		// iterate over all context specific login elements and check if DID name is equals to the given one.
		// If there is no DID name given for the context specific login element, then it applies for all DIDs.
		for (CardSpecType.ContextSpecificLoginType skippableCtxLoginElem : cardSpec.getContextSpecificLogins()) {
		    Optional<String> didNameOfSkippableCtxLogin = Optional.ofNullable(skippableCtxLoginElem.getDidName());

		    // if DID name is present, then it applies only for this name!
		    if (didNameOfSkippableCtxLogin.isPresent()) {
			// if DID name is equals to the given one, skip CTX-Login based on the set value.
			if (didNameOfSkippableCtxLogin.get().equals(didName)) {
			    canSkipContextSpecificLogin = Optional.of(skippableCtxLoginElem.canSkipContextSpecificLogin());
			}
		    // if no DID name is present, then it works for each DID.
		    } else {
			canSkipContextSpecificLogin = Optional.of(skippableCtxLoginElem.canSkipContextSpecificLogin());
		    }
		}
	    }
	}

	return canSkipContextSpecificLogin;
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
    public InputStream getCardImage(@Nonnull String oIdentifier) {
        CardSpecType cardSpec = getCardSpecType(oIdentifier);
	if (cardSpec != null) {
	    return mwConfig.getCardImage(cardSpec.getCardImageName());
	}

        // return nothing
        return null;
    }

    @Nullable
    public CardSpecType getCardSpecType(@Nonnull String oIdentifier) {
        for (CardSpecType cardSpec : mwSpec.getCardConfig().getCardSpecs()) {
            if (cardSpec.getObjectIdentifier().equals(oIdentifier)) {
                return cardSpec;
            }
        }

        // return nothing
        return null;
    }


    /**
     * Maps the token identifier values to the object identifier of the card.
     * All parameters handle the empty string as null values.
     *
     * @param manufacturer Manufacturer field obtained from the PKCS11 middleware.
     * @param model Model field obtained from the PKCS11 middleware.
     * @param label Label field obtained from the PKCS11 middleware.
     * @return Object identifier of card, mapped by middleware name.
     */
    @Nullable
    public String mapMiddlewareType(@Nullable String manufacturer, @Nullable String model, @Nullable String label) {
	// make sure empty strings are null values, so the if statements work correctly
	manufacturer = StringUtils.emptyToNull(manufacturer);
	model = StringUtils.emptyToNull(model);
	label = StringUtils.emptyToNull(label);

	// only check if there is at least one value that needs to be checked
	if (! (manufacturer == null && model == null && label == null)) {
	    for (CardSpecType spec : mwSpec.getCardConfig().getCardSpecs()) {
		String tMan = spec.getManufacturer();
		String tMod = spec.getModel();
		String tLab = spec.getLabel();

		if ((tMan == null || tMan.equals(manufacturer))
			&& (tMod == null || tMod.equals(model))
			&& (tLab == null || tLab.equals(label))) {
		    return spec.getObjectIdentifier();
		}
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
		for (byte[] nextAtr : spec.getAtr()) {
		    boolean matches = compareATR(atr, nextAtr, spec.getMask());
		    if (matches) {
			return true;
		    }
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
