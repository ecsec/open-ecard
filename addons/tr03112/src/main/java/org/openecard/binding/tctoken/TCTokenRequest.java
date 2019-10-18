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

package org.openecard.binding.tctoken;

import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.openecard.addon.Context;
import org.openecard.addons.tr03124.gui.CardMonitorTask;
import org.openecard.addons.tr03124.gui.CardSelectionAction;
import org.openecard.addons.tr03124.gui.CardSelectionStep;
import org.openecard.binding.tctoken.ex.AuthServerException;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.binding.tctoken.ex.InvalidAddressException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import org.openecard.binding.tctoken.ex.InvalidTCTokenElement;
import org.openecard.binding.tctoken.ex.InvalidTCTokenException;
import org.openecard.binding.tctoken.ex.InvalidTCTokenUrlException;
import org.openecard.binding.tctoken.ex.MissingActivationParameterException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.binding.tctoken.ex.UserCancellationException;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.AppVersion;
import org.openecard.common.DynamicContext;
import org.openecard.common.I18n;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.sal.util.InsertCardDialog;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.Pair;
import org.openecard.common.util.StringUtils;
import org.openecard.common.util.TR03112Utils;
import org.openecard.common.util.UrlBuilder;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class represents a TC Token request to the client. It contains the {@link TCTokenType} and situational parts
 * like the ifdName or the server certificates received while retrieving the TC Token.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class TCTokenRequest {

    private static final Logger LOG = LoggerFactory.getLogger(TCTokenRequest.class);
    private static final I18n LANG = I18n.getTranslation("tr03112");

    private static final String TC_TOKEN_URL_KEY = "tcTokenURL";
    private static final String CARD_TYPE_KEY = "cardType";
    private static final String SLOT_INDEX_KEY = "slotIndex";
    private static final String CONTEXT_HANDLE_KEY = "contextHandle";
    private static final String IFD_NAME_KEY = "ifdName";
    private static final String DEFAULT_NPA_CARD_TYPE = "http://bsi.bund.de/cif/npa.xml";

    private TCToken token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;
    private String cardType = DEFAULT_NPA_CARD_TYPE;
    private List<Pair<URL, TlsServerCertificate>> certificates;
    private URL tcTokenURL;
    private TCTokenContext tokenCtx;

    /**
     * Check and evaluate the request parameters and wrap the result in a {@code TCTokenRequest} class.
     *
     * @param parameters The request parameters.
     * @param ctx Addon {@link Context} used for the communication with the core.
     * @param tokenInfo The token.
     * @return A TCTokenRequest wrapping the parameters.
     * @throws MissingActivationParameterException
     */
    public static TCTokenRequest convert(Map<String, String> parameters, Context ctx, Pair<TCTokenContext, URL> tokenInfo)
	    throws MissingActivationParameterException {
	TCTokenRequest result = parseTCTokenRequestURI(parameters, ctx, tokenInfo);
	return result;
    }

    private static TCTokenRequest parseTCTokenRequestURI(Map<String, String> queries, Context ctx, Pair<TCTokenContext, URL> tokenInfo)
	    throws MissingActivationParameterException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	if (tokenInfo != null || tokenInfo.p1 == null || tokenInfo.p2 == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    k = k == null ? "" : k;
	    String v = next.getValue();

	    if (v == null || v.isEmpty()) {
		LOG.info("Skipping query parameter '{}' because it does not contain a value.", k);
	    } else {
		switch (k) {
		    case TC_TOKEN_URL_KEY:
			LOG.info("Skipping given query parameter '{}' because it was already extracted", TC_TOKEN_URL_KEY);
			break;
		    case IFD_NAME_KEY:
			tcTokenRequest.ifdName = v;
			break;
		    case CONTEXT_HANDLE_KEY:
			tcTokenRequest.contextHandle = extractContextHandle(v);
			break;
		    case SLOT_INDEX_KEY:
			tcTokenRequest.slotIndex = extractSlotIndex(v);
			break;
		    case CARD_TYPE_KEY:
			tcTokenRequest.cardType = v;
			break;
		    default:
			LOG.info("Unknown query element: {}", k);
			break;
		}
	    }
	}

	tcTokenRequest.tokenCtx = tokenInfo.p1;
	tcTokenRequest.token = tokenInfo.p1.getToken();
	tcTokenRequest.certificates = tokenInfo.p1.getCerts();
	tcTokenRequest.tcTokenURL = tokenInfo.p2;
	return tcTokenRequest;
    }

    public static String extractIFDName(Map<String, String> queries) {
	return queries.get(IFD_NAME_KEY);
    }

    public static String extractCardType(Map<String, String> queries) {
	return queries.get(CARD_TYPE_KEY);
    }

    public static BigInteger extractSlotIndex(Map<String, String> queries) {
	return extractSlotIndex(queries.get(SLOT_INDEX_KEY));
    }

    public static BigInteger extractSlotIndex(String rawSlotIndex) {
	if (rawSlotIndex == null || rawSlotIndex.isEmpty()) {
	    return null;
	}
	return new BigInteger(rawSlotIndex);
    }

    public static byte[] extractContextHandle(Map<String, String> queries) {
	return extractContextHandle(queries.get(CONTEXT_HANDLE_KEY));
    }

    public static byte[] extractContextHandle(String rawContextHandle) {
	if (rawContextHandle == null || rawContextHandle.isEmpty()) {
	    return null;
	}
	return StringUtils.toByteArray(rawContextHandle);
    }

    /**
     * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
     *
     * @param queries The query parameters of the TC token binding request.
     * @return {@code true} if checks should be performed, {@code false} otherwise.
     */
    public static boolean isPerformTR03112Checks(Map<String, String> queries) {
	return isPerformTR03112Checks(queries.get(CARD_TYPE_KEY));
    }

    /**
     * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
     *
     * @param cardType The card type.
     * @return {@code true} if checks should be performed, {@code false} otherwise.
     */
   public static boolean isPerformTR03112Checks(String cardType) {
       boolean activationChecks = true;
	// disable checks when not using the nPA
	if (cardType != null && !cardType.equals(DEFAULT_NPA_CARD_TYPE)) {
	    activationChecks = false;
	} else if (TR03112Utils.DEVELOPER_MODE) {
	    activationChecks = false;
	    LOG.warn("DEVELOPER_MODE: All TR-03124-1 security checks are disabled.");
	}
	return activationChecks;
   }

    /**
     * Evaluate and extract the TC Token context from the given parameters.
     * @param queries The request parameters.
     * @return The TC Token context and the URL from which it was derived.
     * @throws AuthServerException
     * @throws InvalidRedirectUrlException
     * @throws InvalidAddressException
     * @throws InvalidTCTokenElement
     * @throws SecurityViolationException
     * @throws UserCancellationException
     * @throws MissingActivationParameterException
     * @throws InvalidTCTokenException
     * @throws InvalidTCTokenUrlException
     */
    public static Pair<TCTokenContext, URL> extractTCTokenContext(Map<String, String> queries) throws AuthServerException,
	    InvalidRedirectUrlException, InvalidAddressException, InvalidTCTokenElement, SecurityViolationException, UserCancellationException,
	    MissingActivationParameterException, InvalidTCTokenException, InvalidTCTokenUrlException {
	String activationTokenUrl = queries.get(TC_TOKEN_URL_KEY);

	Pair<TCTokenContext, URL> result = extractTCTokenContext(activationTokenUrl);
	queries.remove(TC_TOKEN_URL_KEY);
	return result;
    }

    public static Pair<TCTokenContext, URL> removeTCTokenContext(Map<String, String> queries) throws AuthServerException,
	    InvalidRedirectUrlException, InvalidAddressException, InvalidTCTokenElement, SecurityViolationException, UserCancellationException,
	    MissingActivationParameterException, InvalidTCTokenException, InvalidTCTokenUrlException {
	Pair<TCTokenContext, URL> result = extractTCTokenContext(queries);
	queries.remove(TC_TOKEN_URL_KEY);
	return result;
    }

    private static Pair<TCTokenContext, URL> extractTCTokenContext(String activationTokenUrl) throws AuthServerException,
	    InvalidRedirectUrlException, InvalidAddressException, InvalidTCTokenElement, SecurityViolationException, UserCancellationException,
	    MissingActivationParameterException, InvalidTCTokenException, InvalidTCTokenUrlException {
	if (activationTokenUrl == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}

	URL tokenUrl;
	try {
	    tokenUrl = new URL(activationTokenUrl);
	} catch(MalformedURLException ex) {
	    // TODO: check if the error type is correct, was WRONG_PARAMETER before
	    throw new InvalidTCTokenUrlException(INVALID_TCTOKEN_URL, ex, activationTokenUrl);
	}
	TCTokenContext tokenCtx = TCTokenContext.generateTCToken(tokenUrl);
	return new Pair(tokenCtx, tokenUrl);
    }

    // TODO: migrate this logic and re-introduce the handling of card types. Currently not needed for NPA.
    public static void correctTCTokenRequestURI(Map<String, String> queries, Context ctx) throws MissingActivationParameterException {
	final String cardType = extractCardType(queries);
	final boolean hasCardType = cardType != null;
	try {
	    if (queries.containsKey("cardTypes") || hasCardType) {
		String[] types;
		if (hasCardType) {
		    types = new String[]{cardType};
		} else {
		    types = queries.get("cardTypes").split(",");
		}

		ConnectionHandleType handle = findCard(types, ctx);
		setIfdName(queries, handle.getIFDName());
		setContextHandle(queries, handle.getContextHandle());
		setSlotIndex(queries, handle.getSlotIndex());
		addTokenUrlParameter(queries, handle.getRecognitionInfo());
	    } else {
		String[] types = new String[]{DEFAULT_NPA_CARD_TYPE};
		ConnectionHandleType handle = findCard(types, ctx);
		setIfdName(queries, handle.getIFDName());
		setContextHandle(queries, handle.getContextHandle());
		setSlotIndex(queries, handle.getSlotIndex());
	    }
	} catch (UserCancellationException ex) {
	    if (queries.containsKey("cardTypes")) {
		addTokenUrlParameter(queries, queries.get("cardTypes").split(",")[0]);
	    }
	    LOG.warn("The user aborted the CardInsertion dialog.", ex);
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    dynCtx.put(TR03112Keys.CARD_SELECTION_CANCELLATION, ex);
	}

	// cardType determined! set in dynamic context, so the information is available in ResourceContext
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	dynCtx.put(TR03112Keys.ACTIVATION_CARD_TYPE, hasCardType ? cardType : DEFAULT_NPA_CARD_TYPE);
    }

    /**
     * Finds a card which matches one of the give types.
     *
     * @param types String array containing valid card types.
     * @param disp Dispatcher used to query cards and terminals.
     * @param gui User consent to display messages to the user.
     * @return ConnectionHandleType object of the chosen card.
     */
    private static ConnectionHandleType findCard(@Nonnull  String[] types, @Nonnull Context ctx) throws
	    MissingActivationParameterException, UserCancellationException {
	CardRecognition rec = ctx.getRecognition();
	Map<String, String> namesAndType = new HashMap<>();
	for (String type : types) {
	    namesAndType.put(rec.getTranslatedCardName(type), type);
	}

	InsertCardDialog insCardDiag =
		new InsertCardDialog(ctx.getUserConsent(), namesAndType, ctx.getEventDispatcher());
	List<ConnectionHandleType> usableCards = insCardDiag.show();

	if (usableCards == null) {
	    // user aborted the card insertion dialog
	    LOG.info("Waiting for cards has not returned a result, cancelling process.");
	    throw new UserCancellationException(null, LANG.translationForKey(CARD_INSERTION_ABORT));
	}

	ConnectionHandleType handle;
	if (usableCards.size() > 1) {
	    UserConsentDescription ucd = new UserConsentDescription(LANG.translationForKey("card.selection.heading.uc",
		    AppVersion.getName()));
	    String stepTitle = LANG.translationForKey("card.selection.heading.step");
	    CardSelectionStep step = new CardSelectionStep(stepTitle, usableCards, ctx.getRecognition());
	    ArrayList<String> types2 = new ArrayList<>();
	    types2.addAll(namesAndType.values());
	    CardMonitorTask task = new CardMonitorTask(types2, step);
	    ctx.getEventDispatcher().add(task, EventType.CARD_REMOVED, EventType.RECOGNIZED_CARD_ACTIVE);
	    step.setBackgroundTask(task);
	    CardSelectionAction action = new CardSelectionAction(step, usableCards, types2, ctx);
	    step.setAction(action);
	    ucd.getSteps().add(step);

	    UserConsent uc = ctx.getUserConsent();
	    UserConsentNavigator ucNav = uc.obtainNavigator(ucd);
	    ExecutionEngine exec = new ExecutionEngine(ucNav);
	    ResultStatus resStatus = exec.process();
	    if (resStatus != ResultStatus.OK) {
		throw new MissingActivationParameterException(CARD_SELECTION_ABORT);
	    }

	    handle = action.getResult();
	    ctx.getEventDispatcher().del(task);
	} else {
	    handle = usableCards.get(0);
	}

	return handle;
    }

    /**
     * Sets the IFDName in the given map.
     *
     * @param queries Map which shall contain the new IFDName value.
     * @param ifdName The new IFDName to set.
     */
    private static void setIfdName(@Nonnull Map<String, String> queries, @Nonnull String ifdName) {
	if (! ifdName.isEmpty()) {
	    queries.put(IFD_NAME_KEY, ifdName);
	}
    }

    /**
     * Sets the ContextHandle in the given Map.
     *
     * @param queries Map which shall contain the new ContextHandle value.
     * @param contextHandle The new ContextHandle to set.
     */
    private static void setContextHandle(@Nonnull Map<String, String> queries, @Nonnull byte[] contextHandle) {
	if (contextHandle.length > 0) {
	    queries.put(CONTEXT_HANDLE_KEY, ByteUtils.toHexString(contextHandle));
	}
    }

    /**
     * Sets the SlotIndex in the given Map.
     *
     * @param queries Map which shall contain the new SlotIndex.
     * @param index The new SlotIndex to set.
     */
    private static void setSlotIndex(@Nonnull Map<String, String> queries, @Nonnull BigInteger index) {
	queries.put(SLOT_INDEX_KEY, index.toString());
    }

    /**
     * Adds the card type given in the given RecognitionInfo object as type to the tcTokenURL contained in the given map.
     *
     * @param queries Map which contains the tcTokenURL and shall contain the new cardType.
     * @param recInfo RecognitionInfo object containing the cardType or type parameter.
     */
    private static void addTokenUrlParameter(@Nonnull Map<String, String> queries, @Nonnull RecognitionInfo recInfo) {
	if (queries.containsKey(TC_TOKEN_URL_KEY)) {
	    String tcTokenURL = queries.get(TC_TOKEN_URL_KEY);
	    try {
		UrlBuilder builder = UrlBuilder.fromUrl(tcTokenURL);
		// url encoding is done by the builder
		builder = builder.queryParam("type", recInfo.getCardType(), true);
		queries.put(TC_TOKEN_URL_KEY, builder.build().toString());
		queries.put(CARD_TYPE_KEY, recInfo.getCardType());
	    } catch (URISyntaxException ex) {
		// ignore if this happens the authentication will fail at all.
	    }
	}
    }

    /**
     * Adds the card type given in the given RecognitionInfo object as type to the tcTokenURL contained in the given map.
     *
     * @param queries Map which contains the tcTokenURL and shall contain the new cardType.
     * @param recInfo RecognitionInfo object containing the cardType or type parameter.
     */
    private static void addTokenUrlParameter(@Nonnull Map<String, String> queries, @Nonnull String selectedCardType) {
	if (queries.containsKey(TC_TOKEN_URL_KEY)) {
	    String tcTokenURL = queries.get(TC_TOKEN_URL_KEY);
	    try {
		UrlBuilder builder = UrlBuilder.fromUrl(tcTokenURL);
		// url encoding is done by the builder
		builder = builder.queryParam("type", selectedCardType, true);
		queries.put(TC_TOKEN_URL_KEY, builder.build().toString());
		queries.put(CARD_TYPE_KEY, selectedCardType);
	    } catch (URISyntaxException ex) {
		// ignore if this happens the authentication will fail at all.
	    }
	}
    }

    /**
     * Returns the TCToken.
     *
     * @return TCToken
     */
    public TCToken getTCToken() {
	return token;
    }

    /**
     * Returns the IFD name.
     *
     * @return IFD name
     */
    public String getIFDName() {
	return ifdName;
    }

    /**
     * Returns the context handle.
     *
     * @return Context handle
     */
    public byte[] getContextHandle() {
	return contextHandle;
    }

    /**
     * Returns the slot index.
     *
     * @return Slot index
     */
    public BigInteger getSlotIndex() {
	return slotIndex;
    }

    /**
     * Returns the card type selected for this authentication process.
     * Defaults to the nPA identifier to provide a fallback.
     *
     * @return Card type
     */
    public String getCardType() {
	return cardType;
    }

    /**
     * Gets the certificates of the servers that have been passed while the TCToken was retrieved.
     *
     * @return List of the X509 server certificates and the requested URLs. May be null under certain circumstances
     *   (e.g. legacy activation).
     */
    public List<Pair<URL, TlsServerCertificate>> getCertificates() {
	return certificates;
    }

    /**
     * Gets the TC Token URL.
     *
     * @return TC Token URL
     */
    public URL getTCTokenURL() {
	return tcTokenURL;
    }

    public TCTokenContext getTokenContext() {
	return tokenCtx;
    }

    /**
     * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
     *
     * @return {@code true} if checks should be performed, {@code false} otherwise.
     */
    public boolean  isPerformTR03112Checks() {
	return isPerformTR03112Checks(this.cardType);
    }
}
