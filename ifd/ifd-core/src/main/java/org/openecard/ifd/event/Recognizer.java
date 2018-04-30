/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.ifd.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.interfaces.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper to start the card recognition easily as a thread.
 *
 * @author Tobias Wich
 */
public class Recognizer implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(Recognizer.class);

    private final Environment env;
    private final ConnectionHandleType handle;
    private final String ifaceProtocol;

    public Recognizer(Environment env, ConnectionHandleType handle, String ifaceProtocol) {
	this.env = env;
	this.handle = handle;
	this.ifaceProtocol = ifaceProtocol;
    }

    @Override
    public void run() {
	ConnectionHandleType newHandle = recognizeSlot();
	if (newHandle != null) {
	    LOG.debug("Found a recognized card event ({}).", handle.getIFDName());
	    env.getEventDispatcher().notify(EventType.CARD_RECOGNIZED, new IfdEventObject(newHandle, ifaceProtocol));
	}
    }

    private ConnectionHandleType recognizeSlot() {
	RecognitionInfo rInfo = null;
	RecognitionInfo currentInfo = handle.getRecognitionInfo();
	if (currentInfo == null || currentInfo.getCardIdentifier() == null) {
	    LOG.error("Current recognition info or CardIdentifier is null.");
	} else if (env.getCIFProvider().needsRecognition(handle.getRecognitionInfo().getCardIdentifier())) {
	    try {
		CardRecognition cr = env.getRecognition();
		rInfo = cr.recognizeCard(handle.getContextHandle(), handle.getIFDName(), handle.getSlotIndex());
	    } catch (RecognitionException ex) {
		// ignore, card is just unknown
	    }
	}

	if (rInfo != null) {
	    ConnectionHandleType newHandle = HandlerUtils.copyHandle(handle);
	    newHandle.getRecognitionInfo().setCardType(rInfo.getCardType());
	    // Remove card identifier (ATR/ATS) as TR-03112-4 states that this should contain the ATR/ATS for unknown
	    // cards and the ICCSN or something similar for known cards. Until we extract the ICCSN just remove the ATR.
	    newHandle.getRecognitionInfo().setCardIdentifier(null);
	    return newHandle;
	} else {
	    return null;
	}
    }

}
