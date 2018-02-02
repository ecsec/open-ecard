/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.recognition;

import org.openecard.common.interfaces.RecognitionException;
import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.CardCall;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.DataMaskType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACD;
import iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACDResponse;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.MatchingDataType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.ResponseAPDUType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.AppVersion;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.IntegerUtils;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.openecard.recognition.staticrepo.LocalCifRepo;
import org.openecard.recognition.statictree.LocalFileTree;
import org.openecard.ws.GetRecognitionTree;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Interface to use the card recognition.
 * This implementation provides card recognition based on a static tree.
 *
 * @author Tobias Wich
 */
public class CardRecognitionImpl implements CardRecognition {

    private static final Logger LOG = LoggerFactory.getLogger(CardRecognitionImpl.class);
    private static final I18n LANG = I18n.getTranslation("recognition");
    private static final String IMAGE_PROPERTIES = "/card-images/card-images.properties";

    private final FutureTask<RecognitionTree> tree;
    private final FutureTask<org.openecard.ws.GetCardInfoOrACD> cifRepo;

    private Set<String> supportedCards;

    private final Properties cardImagesMap = new Properties();

    private final Environment env;
    private UserConsent gui;

    /**
     * Create recognizer with tree from local (file based) repository.
     *
     * @param env
     * @throws Exception
     */
    public CardRecognitionImpl(Environment env) throws Exception {
        this(env, null, null);
    }

    public CardRecognitionImpl(Environment env, final GetRecognitionTree treeRepo,
	    final org.openecard.ws.GetCardInfoOrACD cifRepo) throws Exception {
	this.env = env;

	cardImagesMap.load(FileUtils.resolveResourceAsStream(CardRecognitionImpl.class, IMAGE_PROPERTIES));

	this.cifRepo = new FutureTask<>(new Callable<org.openecard.ws.GetCardInfoOrACD>() {
	    @Override
	    public org.openecard.ws.GetCardInfoOrACD call() throws Exception {
		final WSMarshaller cifMarshaller = WSMarshallerFactory.createInstance();
		org.openecard.ws.GetCardInfoOrACD cifRepoTmp = cifRepo;
		if (cifRepoTmp == null) {
		    cifRepoTmp = new LocalCifRepo(cifMarshaller);
		}

		// request all cifs to fill list of supported cards
		prepareSupportedCards(cifRepoTmp);

		return cifRepoTmp;
	    }
	});
	new Thread(this.cifRepo, "Init-CardInfo-Repo").start();

	this.tree = new FutureTask<>(new Callable<RecognitionTree>() {
	    @Override
	    public RecognitionTree call() throws Exception {
		final WSMarshaller treeMarshaller = WSMarshallerFactory.createInstance();
		GetRecognitionTree treeRepoTmp = treeRepo;
		if (treeRepoTmp == null) {
		    treeRepoTmp = new LocalFileTree(treeMarshaller);
		}
		// request tree from service
		iso.std.iso_iec._24727.tech.schema.GetRecognitionTree req;
		req = new iso.std.iso_iec._24727.tech.schema.GetRecognitionTree();
		req.setAction(RecognitionProperties.getAction());
		GetRecognitionTreeResponse resp = treeRepoTmp.getRecognitionTree(req);
		checkResult(resp.getResult());

		return resp.getRecognitionTree();
	    }
	});
	new Thread(this.tree, "Init-RecognitionTree-Repo").start();
    }

    private void prepareSupportedCards(org.openecard.ws.GetCardInfoOrACD repo) {
	try {
	    try {
		InputStream in = FileUtils.resolveResourceAsStream(getClass(), "cif-repo/supported_cards");
		if (in == null) {
		    throw new IOException("File with supported cards not found.");
		}
		List<String> oids = FileUtils.readLinesFromConfig(in, "UTF-8");

		if (oids.size() == 1 && oids.get(0).equals("*")) {
		    oids = getAllTypesFromRepo(repo);
		}

		this.supportedCards = Collections.unmodifiableSet(new HashSet<>(oids));
	    } catch (IOException ex) {
		// no file loaded falling back to using everything from the repo
		List<String> oids = getAllTypesFromRepo(repo);
		this.supportedCards = Collections.unmodifiableSet(new HashSet<>(oids));
	    }
	} catch (WSHelper.WSException ex) {
	    LOG.error("Failed to retrieve CIFs from repo, don't support any card.");
	    this.supportedCards = Collections.emptySet();
	}
    }

    private List<String> getAllTypesFromRepo(org.openecard.ws.GetCardInfoOrACD repo) throws WSHelper.WSException {
	// read list of all cifs from the repo
	GetCardInfoOrACD req = new GetCardInfoOrACD();
	req.setAction(ECardConstants.CIF.GET_OTHER);
	GetCardInfoOrACDResponse res = repo.getCardInfoOrACD(req);
	WSHelper.checkResult(res);

	ArrayList<String> oids = new ArrayList<>();
	for (Object cif : res.getCardInfoOrCapabilityInfo()) {
	    if (cif instanceof CardInfoType) {
		String type = ((CardInfoType) cif).getCardType().getObjectIdentifier();
		oids.add(type);
	    }
	}

	return oids;
    }

    public void setGUI(UserConsent gui) {
	this.gui = gui;
    }

    private RecognitionTree getTree() {
	try {
	    return tree.get();
	} catch (InterruptedException ex) {
	    String msg = "Initialization of the RecognitionTree repository has been interrupted.";
	    LOG.warn(msg);
	    throw new RuntimeException(msg);
	} catch (ExecutionException ex) {
	    String msg = "Initialization of the RecognitionTree repository yielded an error.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex.getCause());
	}
    }

    private org.openecard.ws.GetCardInfoOrACD getCifRepo() {
	try {
	    return cifRepo.get();
	} catch (InterruptedException ex) {
	    String msg = "Initialization of the CardInfo repository has been interrupted.";
	    LOG.warn(msg);
	    throw new RuntimeException(msg);
	} catch (ExecutionException ex) {
	    String msg = "Initialization of the CardInfo repository yielded an error.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex.getCause());
	}
    }

    @Override
    public List<CardInfoType> getCardInfos() {
	// TODO: add caching
	GetCardInfoOrACD req = new GetCardInfoOrACD();
	req.setAction(ECardConstants.CIF.GET_OTHER);
	GetCardInfoOrACDResponse res = getCifRepo().getCardInfoOrACD(req);
	// checkout response if it contains our cardinfo
	List<Serializable> cifs = res.getCardInfoOrCapabilityInfo();
	ArrayList<CardInfoType> result = new ArrayList<>();
	for (Serializable next : cifs) {
	    if (next instanceof CardInfoType) {
		result.add((CardInfoType) next);
	    }
	}
	return result;
    }

    @Override
    public CardInfoType getCardInfo(String type) {
        CardInfoType cif = env.getCIFProvider().getCardInfo(type);
	if (cif == null) {
	    cif = getCardInfoFromRepo(type);
	}
	return cif;
    }

    @Override
    public CardInfoType getCardInfoFromRepo(String type) {
        CardInfoType cif = null;
        // only do something when a repo is specified
	if (cif == null && cifRepo != null) {
	    GetCardInfoOrACD req = new GetCardInfoOrACD();
	    req.setAction(ECardConstants.CIF.GET_SPECIFIED);
	    req.getCardTypeIdentifier().add(type);
	    GetCardInfoOrACDResponse res = getCifRepo().getCardInfoOrACD(req);
	    // checkout response if it contains our cardinfo
	    List<Serializable> cifs = res.getCardInfoOrCapabilityInfo();
	    for (Serializable next : cifs) {
		if (next instanceof CardInfoType) {
		    return (CardInfoType) next;
		}
	    }
	}
	return cif;
    }

    private boolean isSupportedCard(String type) {
	getCifRepo(); // makes sure supportedCards is initialized
	return supportedCards.contains(type);
    }

    /**
     * Gets the translated card name for a card type.
     *
     * @param cardType The card type to get the card name for.
     * @return A card name matching the users locale or the English name as default. If the card is not supported, the
     *   string {@code Unknown card type} is returned.
     */
    @Override
    public String getTranslatedCardName(String cardType) {
	CardInfoType info = getCardInfo(cardType);

	Locale userLocale = Locale.getDefault();
	String langCode = userLocale.getLanguage();
	String enFallback = "Unknown card type.";

	if (info == null) {
	    // we can identify the card but do not have a card info file for it
	    return enFallback;
	}

	for (InternationalStringType typ : info.getCardType().getCardTypeName()) {
	    if (typ.getLang().equalsIgnoreCase("en")) {
		enFallback = typ.getValue();
	    }
	    if (typ.getLang().equalsIgnoreCase(langCode)) {
		return typ.getValue();
	    }
	}
	return enFallback;
    }

    /**
     * Gets image stream of the given card or a no card image if the object identifier is unknown.
     * @param objectid iso:ObjectIdentifier as defined in the CardInfo file.
     * @return InputStream of the card image.
     */
    @Override
    public InputStream getCardImage(String objectid) {
	String fname = cardImagesMap.getProperty(objectid);
	// Get the card image as inputstream from the responsible Middleware SAL
	InputStream fs = env.getCIFProvider().getCardImage(objectid);
	// If null is returned, load the Image from the local cif repo
	if (fs == null && fname != null) {
	    fs = loadCardImage(fname);
	}
	if (fs == null) {
	    fs = getUnknownCardImage();
	}
	return fs;
    }

    /**
     * @return Stream containing the requested image.
     * @see #getCardImage(java.lang.String)
     */
    @Override
    public InputStream getUnknownCardImage() {
	return loadCardImage("unknown_card.png");
    }
    /**
     * @return Stream containing the requested image.
     * @see #getCardImage(java.lang.String)
     */
    @Override
    public InputStream getNoCardImage() {
	return loadCardImage("no_card.jpg");
    }
    /**
     * @return Stream containing the requested image.
     * @see #getCardImage(java.lang.String)
     */
    @Override
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
	    return FileUtils.resolveResourceAsStream(CardRecognitionImpl.class, "/card-images/" + filename);
	} catch (IOException ex) {
	    LOG.info("Failed to load card image '" + filename + "'.", ex);
	    return null;
	}
    }


    /**
     * Recognizes the card in the defined reader.
     *
     * @param ctx Context handle of the IFD.
     * @param ifdName Name of the card reader.
     * @param slot Index of the slot in the reader.
     * @return RecognitionInfo structure containing the card type of the detected card or {@code null} if no card could
     *   be detected.
     * @throws RecognitionException Thrown in case there was an error in the recognition.
     */
    @Nullable
    @Override
    public RecognitionInfo recognizeCard(byte[] ctx, String ifdName, BigInteger slot) throws RecognitionException {
	// connect card
	byte[] slotHandle = connect(ctx, ifdName, slot);
	// recognise card
	String type = treeCalls(slotHandle, getTree().getCardCall());
	// disconnect and return
	disconnect(slotHandle);
	// build result or throw exception if it is null or unsupported
	if (type == null || ! isSupportedCard(type)) {
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
     * Special transmit check determining only whether a response is present or not and it contains at least a trailer.<br>
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

    /**
     * Returns the fibonacci number for a given index.
     *
     * @param idx index
     * @return the fibonacci number for the given index
     */
    private long fibonacci(int idx) {
	if (idx == 1 || idx == 2) {
	    return 1;
	} else {
	    return fibonacci(idx - 1) + fibonacci(idx - 2);
	}
    }

    private byte[] connect(byte[] ctx, String ifdName, BigInteger slot) throws RecognitionException {
	Connect c = new Connect();
	c.setContextHandle(ctx);
	c.setIFDName(ifdName);
	c.setSlot(slot);

	ConnectResponse r = (ConnectResponse) env.getDispatcher().safeDeliver(c);
	checkResult(r.getResult());

	waitForExclusiveCardAccess(r.getSlotHandle(), ifdName);

	return r.getSlotHandle();
    }

    /**
     * This method tries to get exclusive card access until it is granted.
     * The waiting delay between the attempts is determined by the fibonacci numbers.
     *
     * @param slotHandle slot handle specifying the card to get exclusive access for
     * @param ifdName Name of the IFD in which the card is inserted
     */
    private void waitForExclusiveCardAccess(byte[] slotHandle, String ifdName) throws RecognitionException {
	String resultMajor;
	int i = 2;
	do {
	    // try to get exclusive card access for the recognition run
	    BeginTransaction trans = new BeginTransaction();
	    trans.setSlotHandle(slotHandle);
	    BeginTransactionResponse resp = (BeginTransactionResponse) env.getDispatcher().safeDeliver(trans);
	    resultMajor = resp.getResult().getResultMajor();

	    if (! resultMajor.equals(ECardConstants.Major.OK)) {
		String resultMinor = resp.getResult().getResultMinor();
		if (ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE.equals(resultMinor)) {
		    throw new RecognitionException("Card is not available anymore.");
		}
		// could not get exclusive card access, wait in increasingly longer intervals and retry
		try {
		    long waitInSeconds = fibonacci(i);
		    i++;
		    LOG.debug("Could not get exclusive card access. Trying again in {} seconds.", waitInSeconds);
		    if (i == 6 && gui != null) {
			MessageDialog dialog = gui.obtainMessageDialog();
			String message = LANG.translationForKey("message", AppVersion.getName(), ifdName);
			String title = LANG.translationForKey("error", ifdName);
			dialog.showMessageDialog(message, title, DialogType.WARNING_MESSAGE);
		    }
		    Thread.sleep(1000 * waitInSeconds);
		} catch (InterruptedException e) {
		    // ignore
		}
	    }
	} while (! resultMajor.equals(ECardConstants.Major.OK));
    }

    private void disconnect(byte[] slotHandle) throws RecognitionException {
	// end exclusive card access
	EndTransaction end = new EndTransaction();
	end.setSlotHandle(slotHandle);
	EndTransactionResponse endTransactionResponse = (EndTransactionResponse) env.getDispatcher().safeDeliver(end);
	checkResult(endTransactionResponse.getResult());

	Disconnect d = new Disconnect();
	d.setSlotHandle(slotHandle);
	DisconnectResponse r = (DisconnectResponse) env.getDispatcher().safeDeliver(d);
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

	TransmitResponse r = (TransmitResponse) env.getDispatcher().safeDeliver(t);
	if (checkTransmitResult(r)) {
	    return r.getOutputAPDU().get(0);
	} else {
	    return null;
	}
    }


    private List<CardCall> branch2list(CardCall first) {
	LinkedList<CardCall> calls = new LinkedList<>();
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
		if (! matcher && ! Arrays.equals(next.getResponseAPDU().get(0).getTrailer(), trailer)) {
		    // break when outcome is wrong
		    break;
		} else if (! matcher) {
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
			if (checkBody(r.getBody(), result)) {
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

    private boolean checkBody(DataMaskType body, byte[] result) {
	// tag in body has a special meaning
	if (body.getTag() != null && body.getDataObject() != null) {
	    byte[] tag = body.getTag();
	    if (ByteUtils.isPrefix(tag, result)) {
		result = ByteUtils.copy(result, tag.length, result.length - tag.length);
		return checkDataObject(body.getDataObject(), result);
	    } else {
		return false;
	    }
	} else if (body.getDataObject() != null) {
	    return checkDataObject(body.getDataObject(), result);
	} else {
	    return checkMatchingData(body.getMatchingData(), result);
	}
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
	if (lengthBytes == null) {
	    lengthBytes = IntegerUtils.toByteArray(valueBytes.length);
	}
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
