/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.ifd.scio.wrapper;

import iso.std.iso_iec._24727.tech.schema.BioSensorCapabilityType;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOTerminal;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.ECardConstants;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.util.ByteUtils;
import org.openecard.ifd.scio.reader.ExecutePACERequest;
import org.openecard.ifd.scio.reader.ExecutePACEResponse;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class TerminalInfo {

    private static final Logger logger = LoggerFactory.getLogger(TerminalInfo.class);

    private final ChannelManager cm;
    private final SCIOTerminal term;
    private final boolean externalChannel;
    private HandledChannel channel = null;

    private Map<Integer, Integer> featureCodes;

    // capabilities entries
    private Boolean acoustic = null;
    private Boolean optic    = null;
    private boolean slotCapRead = false;
    private SlotCapabilityType slotCap = null;
    private boolean dispCapRead = false;
    private DisplayCapabilityType dispCap = null;
    private boolean keyCapRead = false;
    private KeyPadCapabilityType keyCap = null;
    private boolean bioCapRead = false;
    private BioSensorCapabilityType bioCap = null;
    private List<PACECapabilities.PACECapability> PACECapabilities = null;

    public TerminalInfo(ChannelManager cm, SCIOTerminal term) {
	this.cm = cm;
	this.term = term;
	this.externalChannel = false;
    }

    public TerminalInfo(ChannelManager cm, HandledChannel channel) {
	this.cm = cm;
	this.term = channel.getChannel().getCard().getTerminal();
	this.externalChannel = true;
	this.channel = channel;
    }


    public String getName() {
	return term.getName();
    }

    public boolean isCardPresent() {
	try {
	    return term.isCardPresent();
	} catch (SCIOException ex) {
	    return false;
	}
    }

    public boolean isConnected() {
	return channel != null;
    }

    private boolean tryConnect() throws SCIOException {
	if (! isConnected()) {
	    // try to connect in any case, if no card is present IllegalStateException is thrown and false is returned
	    try {
		byte[] slotHandle = cm.openChannel(term.getName());
		this.channel = cm.getChannel(slotHandle);
		return true;
	    } catch (NoSuchTerminal | NoSuchChannel | IllegalStateException ex) {
		logger.warn("Channel could not be established due to missing card.");
		return false;
	    }
	} else {
	    return true;
	}
    }

    public void disconnect() {
	if (! externalChannel && isConnected()) {
	    try {
		cm.closeChannel(channel.getSlotHandle());
	    } catch (SCIOException ex) {
		logger.warn("Failed to close channel.");
	    }
	}
    }


    @Nonnull
    public IFDStatusType getStatus() throws SCIOException {
	IFDStatusType status = new IFDStatusType();
	status.setIFDName(getName());
	status.setConnected(true);

	// set slot status type
	SlotStatusType stype = new SlotStatusType();
	status.getSlotStatus().add(stype);
	boolean cardPresent = isCardPresent();
	stype.setCardAvailable(cardPresent);
	stype.setIndex(BigInteger.ZERO);
	// get card status and stuff
	if (cardPresent) {
	    if (tryConnect()) {
		SCIOATR atr = channel.getChannel().getCard().getATR();
		stype.setATRorATS(atr.getBytes());
	    }
	}
	// ifd status completely constructed
	return status;
    }


    public SlotCapabilityType getSlotCapability() throws SCIOException {
	if (! slotCapRead) {
	    SlotCapabilityType cap = new SlotCapabilityType();
	    cap.setIndex(BigInteger.ZERO);

	    if (supportsPace()) {
		List<PACECapabilities.PACECapability> capabilities = getPACECapabilities();
		List<String> protos = buildPACEProtocolList(capabilities);
		cap.getProtocol().addAll(protos);
	    }
	    if (supportsPinCompare()) {
		cap.getProtocol().add(ECardConstants.Protocol.PIN_COMPARE);
	    }

	    slotCap = cap;
	    slotCapRead = true;
	}

	return slotCap;
    }

    public static List<String> buildPACEProtocolList(List<PACECapabilities.PACECapability> paceCapabilities) {
	List<String> supportedProtos = new LinkedList<>();
	for (PACECapabilities.PACECapability next : paceCapabilities) {
	    supportedProtos.add(next.getProtocol());
	}
	return supportedProtos;
    }


    public boolean isAcousticSignal() throws SCIOException {
	if (acoustic == null) {
	    // no way to ask PCSC this question
	    acoustic = false;
	}
	return acoustic;
    }

    public boolean isOpticalSignal() throws SCIOException {
	if (optic == null) {
	    // no way to ask PCSC this question
	    optic = false;
	}
	return optic;
    }

    @Nullable
    public DisplayCapabilityType getDisplayCapability() throws SCIOException {
	if (! dispCapRead) {
	    if (tryConnect()) {
		Map<Integer,Integer> features = getFeatureCodes();
		if (features.containsKey(PCSCFeatures.IFD_DISPLAY_PROPERTIES)) {
		    Integer displayFeature = features.get(PCSCFeatures.IFD_DISPLAY_PROPERTIES);
		    byte[] data = channel.transmitControlCommand(displayFeature, new byte[0]);
		    if (data.length == 4) {
			int lineLength = ByteUtils.toInteger(Arrays.copyOfRange(data, 0, 2));
			int numLines   = ByteUtils.toInteger(Arrays.copyOfRange(data, 2, 4));
			if (lineLength > 0 && numLines > 0) {
			    dispCap = new DisplayCapabilityType();
			    dispCap.setIndex(BigInteger.ZERO);
			    dispCap.setColumns(BigInteger.valueOf(lineLength));
			    dispCap.setLines(BigInteger.valueOf(numLines));
			}
		    }
		}
		// regardless whether the data has been successfully extracted, or not, the data has been read
		dispCapRead = true;
	    }
	}
	return dispCap;
    }

    @Nullable
    public KeyPadCapabilityType getKeypadCapability() throws SCIOException {
	if (! keyCapRead) {
	    if (tryConnect()) {
		// try to get the properties from the reader
		Map<Integer,Integer> features = getFeatureCodes();
		if (features.containsKey(PCSCFeatures.IFD_PIN_PROPERTIES)) {
		    Integer pinFeature = features.get(PCSCFeatures.IFD_PIN_PROPERTIES);
		    byte[] data = channel.transmitControlCommand(pinFeature, new byte[0]);
		    if (data.length == 4) {
			int wcdLayout = ByteUtils.toInteger(Arrays.copyOfRange(data, 0, 2));
			byte entryValidation = data[2];
			byte timeOut2 = data[3];
			// TODO: extract number of keys somehow

			// write our data structure
			keyCap = new KeyPadCapabilityType();
			keyCap.setIndex(BigInteger.ZERO);
			keyCap.setKeys(BigInteger.valueOf(16));
		    }
		}

		// regardless whether the data has been successfully extracted, or not, the data has been read
		keyCapRead = true;
	    }
	}
	return keyCap;
    }


    @Nullable
    public BioSensorCapabilityType getBiosensorCapability() {
	if (! bioCapRead) {
	    // TODO: read actual biosensor capability
	    bioCap = null;
	    bioCapRead = true;
	}

	return bioCap;
    }


    private Integer getPaceCtrlCode() throws SCIOException {
	if (tryConnect()) {
	    Map<Integer,Integer> features = getFeatureCodes();
	    return features.get(PCSCFeatures.EXECUTE_PACE);
	}
	return null;
    }

    public boolean supportsPace() throws SCIOException {
	return getPaceCtrlCode() != null;
    }

    public List<PACECapabilities.PACECapability> getPACECapabilities() throws SCIOException {
	List<PACECapabilities.PACECapability> result = new LinkedList<>();

	if (PACECapabilities == null) {
	    if (tryConnect()) {
		if (supportsPace()) {
		    int ctrlCode = getPaceCtrlCode();
		    ExecutePACERequest.Function paceFunc = ExecutePACERequest.Function.GetReaderPACECapabilities;
		    byte[] getCapabilityRequest = new ExecutePACERequest(paceFunc).toBytes();
		    byte[] response = channel.transmitControlCommand(ctrlCode, getCapabilityRequest);
		    ExecutePACEResponse paceResponse = new ExecutePACEResponse(response);
		    if (paceResponse.isError()) {
			String msg = "PACE is advertised but the result iss errornous.\n";
			msg += paceResponse.getResult().getResultMessage().getValue();
			throw new SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR);
		    }
		    PACECapabilities cap = new PACECapabilities(paceResponse.getData());
		    PACECapabilities = cap.getFeaturesEnum();
		    result.addAll(PACECapabilities);
		}
	    }
	} else {
	    result.addAll(PACECapabilities);
	}

	return Collections.unmodifiableList(result);
    }


    private Integer getPinCompareCtrlCode() throws SCIOException {
	if (tryConnect()) {
	    Map<Integer,Integer> features = getFeatureCodes();
	    return features.get(PCSCFeatures.VERIFY_PIN_DIRECT);
	}
	return null;
    }

    public boolean supportsPinCompare() throws SCIOException {
	return getPinCompareCtrlCode() != null;
    }

    @Nonnull
    public Map<Integer, Integer> getFeatureCodes() throws SCIOException {
	if (tryConnect()) {
	    if (featureCodes == null) {
		int code = PCSCFeatures.GET_FEATURE_REQUEST_CTLCODE();
		try {
		    byte[] response = channel.transmitControlCommand(code, new byte[0]);
		    featureCodes = PCSCFeatures.featureMapFromRequest(response);
		} catch (SCIOException ex) {
		    // TODO: remove this workaround by supporting feature requests under all systems and all readers
		    logger.warn("Unable to request features from reader.", ex);
		    featureCodes = new HashMap<>();
		} catch (IllegalStateException ex) {
		    logger.warn("Transmit control command failed due to missing card connection.", ex);
		    return Collections.emptyMap();
		}
	    }

	    return featureCodes;
	} else {
	    return Collections.emptyMap();
	}
    }

}
