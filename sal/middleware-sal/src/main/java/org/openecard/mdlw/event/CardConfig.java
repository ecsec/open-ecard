/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class CardConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CardConfig.class);
    private static CardConfig INST;

    private final CardConfigType cardConf;

    private CardConfig() {
	this.cardConf = new CardConfigType();
    }

    private CardConfig(@Nonnull String propsResource) throws IOException {
	CardConfigType cfg = new CardConfigType();
	try (InputStream is = getClass().getResourceAsStream(propsResource)) {
	    JAXBContext ctx = JAXBContext.newInstance(CardConfigType.class);
	    cfg = (CardConfigType) ctx.createUnmarshaller().unmarshal(is);
	} catch (IOException | JAXBException ex) {
	    LOG.error("Failed to load middleware card config.");
	}

	this.cardConf = cfg;
    }

    private static synchronized CardConfig getInstance() {
	if (INST == null) {
	    try {
		INST = new CardConfig("/middleware/cardspec.xml");
	    } catch (IOException ex) {
		LOG.error("The bundled properties file could not be loaded.", ex);
		INST = new CardConfig();
	    }
	}
	return INST;
    }

    @Nullable
    public static String mapMiddlewareType(@Nonnull String middlewareCardType) {
	List<CardConfigType.CardSpec> specs = getInstance().cardConf.getCardSpecs();
	for (CardConfigType.CardSpec spec : specs) {
	    if (middlewareCardType.equals(spec.getMiddlewareName())) {
		return spec.getType();
	    }
	}
	
	// nothing found
	return null;
    }

    public static boolean isATRKnown(@Nullable byte[] atr) {
	if (atr != null) {
	    List<CardConfigType.CardSpec> specs = getInstance().cardConf.getCardSpecs();
	    for (CardConfigType.CardSpec spec : specs) {
		boolean matches = compareATR(atr, spec.getAtr(), spec.getMask());
		if (matches) {
		    return true;
		}
	    }
	}

	// no match found
	return false;
    }

    private static boolean compareATR(@Nonnull byte[] atr, @Nullable byte[] refAtr, @Nullable byte[] mask) {
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
