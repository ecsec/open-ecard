/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.recognition;

import iso.std.iso_iec._24727.tech.schema.*;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.apdu.common.CardResponseAPDU;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.FileUtils;
import org.openecard.client.recognition.staticrepo.LocalCifRepo;
import org.openecard.client.recognition.statictree.LocalFileTree;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerFactory;
import org.openecard.ws.GetRecognitionTree;
import org.openecard.ws.IFD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardRecognition {

    private static final Logger _logger = LoggerFactory.getLogger(CardRecognition.class);

    private final RecognitionTree tree;

    private final org.openecard.ws.GetCardInfoOrACD cifRepo;
    private final TreeMap<String, CardInfoType> cifCache = new TreeMap<String, CardInfoType>();

    private final Properties cardImagesMap = new Properties();

    private final IFD ifd;
    private final byte[] ctx;


    /**
     * Create recognizer with tree from local (file based) repository.
     * @param ifd
     * @param ctx
     * @throws Exception
     */
    public CardRecognition(IFD ifd, byte[] ctx) throws Exception {
	this(ifd, ctx, null, null);
    }

    public CardRecognition(IFD ifd, byte[] ctx, GetRecognitionTree treeRepo, org.openecard.ws.GetCardInfoOrACD cifRepo) throws Exception {
	this.ifd = ifd;
	this.ctx = ctx;

	// load alternative tree service if needed
	WSMarshaller marshaller = WSMarshallerFactory.createInstance();
	if (treeRepo == null) {
	    treeRepo = new LocalFileTree(marshaller);
	}
	if (cifRepo == null) {
	    cifRepo = new LocalCifRepo(marshaller);
	}
	this.cifRepo = cifRepo;

	cardImagesMap.load(FileUtils.resolveResourceAsStream(CardRecognition.class, "/card-images/card-images.properties"));

	// request tree from service
	iso.std.iso_iec._24727.tech.schema.GetRecognitionTree req = new iso.std.iso_iec._24727.tech.schema.GetRecognitionTree();
	req.setAction(RecognitionProperties.getAction());
	GetRecognitionTreeResponse resp = treeRepo.getRecognitionTree(req);
	checkResult(resp.getResult());
	this.tree = resp.getRecognitionTree();
    }


    public CardInfoType getCardInfo(String type) {
	// only do something when a repo is specified
	if (cifRepo != null) {
	    if (cifCache.containsKey(type)) {
		return cifCache.get(type);
	    } else {
		GetCardInfoOrACD req = new GetCardInfoOrACD();
		req.setAction(ECardConstants.CIF.GET_SPECIFIED);
		req.getCardTypeIdentifier().add(type);
		GetCardInfoOrACDResponse res = cifRepo.getCardInfoOrACD(req);
		// checkout response if it contains our cardinfo
		List<Object> cifs = res.getCardInfoOrCapabilityInfo();
		for (Object next : cifs) {
		    if (next instanceof CardInfoType) {
			cifCache.put(type, (CardInfoType) next);
			return (CardInfoType) next;
		    }
		}
		// no valid cardinfo save null to the map to prevent fetching the nonexistant cif again
		cifCache.put(type, null);
		return null;
	    }
	} else {
	    return null;
	}
    }

    /**
     * Gets image stream of the given card or a no card image if the object identifier is unknown.
     * @param objectid iso:ObjectIdentifier as defined in the CardInfo file.
     * @return InputStream of the card image.
     */
    public InputStream getCardImage(String objectid) {
	String fname = cardImagesMap.getProperty(objectid);
	InputStream fs = null;
	if (fname != null) {
	    fs = loadCardImage(fname);
	}
	if (fs == null) {
	    fs = getUnknownCardImage();
	}
	return fs;
    }

    /**
     * @see #getCardImage(java.lang.String)
     */
    public InputStream getUnknownCardImage() {
	return loadCardImage("unknown_card.png");
    }
    /**
     * @see #getCardImage(java.lang.String)
     */
    public InputStream getNoCardImage() {
	return loadCardImage("no_card.png");
    }
    /**
     * @see #getCardImage(java.lang.String)
     */
    public InputStream getNoTerminalImage() {
	return loadCardImage("no_terminal.png");
    }

    /**
     * Gets stream of the given image in the directory card-images.
     * @param filename
     * @return Stream of the image or null, if none is found.
     */
    private static InputStream loadCardImage(String filename) {
	try {
	    return FileUtils.resolveResourceAsStream(CardRecognition.class, "/card-images/" + filename);
	} catch (IOException ex) {
	    _logger.info("Failed to load card image '" + filename + "'.", ex);
	    return null;
	}
    }


    public RecognitionInfo recognizeCard(String ifdName, BigInteger slot) throws RecognitionException {
	// connect card
	byte[] slotHandle = connect(ifdName, slot);
	// recognise card
	String type = treeCalls(slotHandle, tree.getCardCall());
	// disconnect and return
	disconnect(slotHandle);
	// build result or throw exception if it is null
	if (type == null) {
	    return null;
	}
	RecognitionInfo info = new RecognitionInfo();
	info.setCardType(type);
	return info;
    }


    private void checkResult(Result r) throws RecognitionException {
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    throw new RecognitionException(r);
	}
    }
    /**
     * Special transmit check determining only whether a response is present or not and it contains at least a trailer.<br/>
     * Unexpected result may be the wrong cause, because the command could represent multiple commands.
     *
     * @param r The response to check
     * @return True when result present, false otherwise.
     */
    private boolean checkTransmitResult(TransmitResponse r) {
	if (! r.getOutputAPDU().isEmpty() && r.getOutputAPDU().get(0).length >= 2) {
	    return true;
	} else {
	    return false;
	}
    }


    private byte[] connect(String ifdName, BigInteger slot) throws RecognitionException {
	Connect c = new Connect();
	c.setContextHandle(ctx);
	c.setIFDName(ifdName);
	c.setSlot(slot);

	ConnectResponse r = ifd.connect(c);
	checkResult(r.getResult());

	return r.getSlotHandle();
    }

    private void disconnect(byte[] slotHandle) throws RecognitionException {
	Disconnect c = new Disconnect();
	c.setSlotHandle(slotHandle);

	DisconnectResponse r = ifd.disconnect(c);
	checkResult(r.getResult());
    }

    private byte[] transmit(byte[] slotHandle, byte[] input, List<ResponseAPDUType> results) {
	Transmit t = new Transmit();
	t.setSlotHandle(slotHandle);
	InputAPDUInfoType apdu = new InputAPDUInfoType();
	apdu.setInputAPDU(input);
	for (ResponseAPDUType result : results) {
	    apdu.getAcceptableStatusCode().add(result.getTrailer());
	}
	t.getInputAPDUInfo().add(apdu);

	TransmitResponse r = ifd.transmit(t);
	if (checkTransmitResult(r)) {
	    return r.getOutputAPDU().get(0);
	} else {
	    return null;
	}
    }


    private List<CardCall> branch2list(CardCall first) {
	LinkedList<CardCall> calls = new LinkedList<CardCall>();
	calls.add(first);

	CardCall next = first;
	// while next is a select call
	while (next.getResponseAPDU().get(0).getBody() == null) {
	    // a select only has one call in its conclusion
	    next = next.getResponseAPDU().get(0).getConclusion().getCardCall().get(0);
	    calls.add(next);
	}

	return calls;
    }


    private String treeCalls(byte[] slotHandle, List<CardCall> calls) throws RecognitionException {
	for (CardCall c : calls) {
	    // make list of next feature (aka branch)
	    List<CardCall> branch = branch2list(c);
	    // execute selects and then matcher, matcher decides over success
	    for (CardCall next : branch) {
		boolean matcher = (next.getResponseAPDU().get(0).getBody() != null) ? true : false;
		byte[] resultBytes = transmit(slotHandle, next.getCommandAPDU(), next.getResponseAPDU());
		// break when outcome is wrong
		if (resultBytes == null) {
		    break;
		}
		// get command bytes and trailer
		byte[] result = CardResponseAPDU.getData(resultBytes);
		byte[] trailer = CardResponseAPDU.getTrailer(resultBytes);
		// if select, only one response exists
		if (!matcher && ! Arrays.equals(next.getResponseAPDU().get(0).getTrailer(), trailer)) {
		    // break when outcome is wrong
		    break;
		} else if (!matcher) {
		    // trailer matches expected response from select, continue
		    continue;
		} else {
		    // matcher command, loop through responses
		    for (ResponseAPDUType r : next.getResponseAPDU()) {
			// next response, when outcome is wrong
			if (! Arrays.equals(r.getTrailer(), trailer)) {
			    continue;
			}
			// check internals for match
			if (checkDataObject(r.getBody(), result)) {
			    if (r.getConclusion().getRecognizedCardType() != null) {
				// type recognised
				return r.getConclusion().getRecognizedCardType();
			    } else {
				// type dependent on subtree
				return treeCalls(slotHandle, r.getConclusion().getCardCall());
			    }
			}
		    }
		}
	    }
	}

	return null;
    }


    private boolean checkDataObject(DataMaskType matcher, byte[] result) {
	// check if we have a tag and data object
	if (matcher.getTag() != null && matcher.getDataObject() != null) {
	    try {
		TLV tlv = TLV.fromBER(result);
		return checkDataObject(matcher, tlv);
	    } catch (TLVException ex) {
	    }
	    // no TLV structure or fallthrough after tag not found
	    return false;
	}

	// we have a matcher
	return checkMatchingData(matcher.getMatchingData(), result);
    }

    private boolean checkDataObject(DataMaskType matcher, TLV result) {
	byte[] tag = matcher.getTag();
	DataMaskType nextMatcher = matcher.getDataObject();

	// this function only works with tag and dataobject
	if (tag == null || nextMatcher == null) {
	    return false;
	}

	long tagNum = ByteUtils.toLong(tag);

	List<TLV> chunks = result.findNextTags(tagNum);
	for (TLV next : chunks) {
	    boolean outcome;
	    if (nextMatcher.getMatchingData() != null) {
		outcome = checkMatchingData(nextMatcher.getMatchingData(), next.getValue());
	    } else {
		outcome = checkDataObject(nextMatcher, next.getChild());
	    }
	    // evaluate outcome
	    if (outcome == true) {
		return true;
	    }
	}
	// no match
	return false;
    }

    private boolean checkMatchingData(MatchingDataType matcher, byte[] result) {
	// get values
	byte[] offsetBytes = matcher.getOffset();
	byte[] lengthBytes = matcher.getLength();
	byte[] valueBytes  = matcher.getMatchingValue();
	byte[] maskBytes   = matcher.getMask();

	// convert values for convenience
	if (offsetBytes == null) {
	    offsetBytes = new byte[] {(byte) 0x00, (byte) 0x00};
	}
	int offset = ByteUtils.toInteger(offsetBytes);
	int length = ByteUtils.toInteger(lengthBytes);
	if (maskBytes == null) {
	    maskBytes = new byte[valueBytes.length];
	    for (int i = 0; i < maskBytes.length; i++) {
		maskBytes[i] = (byte) 0xFF;
	    }
	}

	// some basic integrity checks
	if (maskBytes.length != valueBytes.length) {
	    return false;
	}
	if (valueBytes.length != length) {
	    return false;
	}
	if (result.length < length + offset) {
	    return false;
	}

	// check
	for (int i = offset; i < length + offset; i++) {
	    if ((maskBytes[i - offset] & result[i]) != valueBytes[i - offset]) {
		return false;
	    }
	}

	return true;
    }

}
