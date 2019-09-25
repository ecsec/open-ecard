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

import org.openecard.binding.tctoken.ex.InvalidTCTokenElement;
import org.openecard.binding.tctoken.ex.InvalidTCTokenUrlException;
import org.openecard.binding.tctoken.ex.InvalidTCTokenException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.binding.tctoken.ex.AuthServerException;
import org.openecard.binding.tctoken.ex.MissingActivationParameterException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
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
import org.openecard.binding.tctoken.ex.InvalidAddressException;
import org.openecard.common.util.Pair;
import org.openecard.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.binding.tctoken.ex.UserCancellationException;
import org.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.AppVersion;
import org.openecard.common.DynamicContext;
import org.openecard.common.I18n;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.sal.util.InsertCardDialog;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.UrlBuilder;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;


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

    private TCToken token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;
    private String cardType = "http://bsi.bund.de/cif/npa.xml";
    private List<Pair<URL, TlsServerCertificate>> certificates;
    private URL tcTokenURL;
    private TCTokenContext tokenCtx;


    /**
     * Check and evaluate the request parameters and wrap the result in a {@code TCTokenRequest} class.
     *
     * @param parameters The request parameters.
     * @param ctx Addon {@link Context} used for the communication with the core.
     * @return A TCTokenRequest wrapping the parameters.
     * @throws InvalidTCTokenException
     * @throws MissingActivationParameterException
     * @throws AuthServerException
     * @throws InvalidRedirectUrlException
     * @throws InvalidTCTokenElement
     * @throws InvalidTCTokenUrlException
     * @throws SecurityViolationException
     * @throws InvalidAddressException
     * @throws UserCancellationException
     */
    public static TCTokenRequest convert(Map<String, String> parameters, Context ctx) throws
	    InvalidTCTokenException, MissingActivationParameterException, AuthServerException,
	    InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException, SecurityViolationException,
	    InvalidAddressException, UserCancellationException {
	TCTokenRequest result;
	if (parameters.containsKey("tcTokenURL")) {
	    result = parseTCTokenRequestURI(parameters, ctx);
	    return result;
	}

	throw new MissingActivationParameterException(NO_PARAMS);
    }

    private static TCTokenRequest parseTCTokenRequestURI(Map<String, String> queries, Context ctx)
	    throws InvalidTCTokenException, MissingActivationParameterException, AuthServerException,
	    InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException, SecurityViolationException,
	    InvalidAddressException, UserCancellationException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();
	try {
	    if (queries.containsKey("cardTypes") || queries.containsKey("cardType")) {
		String[] types;
		if (queries.containsKey("cardType")) {
		    types = new String[]{queries.get("cardType")};
		} else {
		    types = queries.get("cardTypes").split(",");
		}

		ConnectionHandleType handle = findCard(types, ctx);
		setIfdName(queries, handle.getIFDName());
		setContextHandle(queries, handle.getContextHandle());
		setSlotIndex(queries, handle.getSlotIndex());
		addTokenUrlParameter(queries, handle.getRecognitionInfo());
	    } else {
		String[] types = new String[]{tcTokenRequest.cardType};
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

	String activationTokenUrl = null;
	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    k = k == null ? "" : k;
	    String v = next.getValue();

	    if (v == null || v.isEmpty()) {
		LOG.info("Skipping query parameter '{}' because it does not contain a value.", k);
	    } else {
		switch (k) {
		    case "tcTokenURL":
			activationTokenUrl = v;
			break;
		    case "ifdName":
			tcTokenRequest.ifdName = v;
			break;
		    case "contextHandle":
			tcTokenRequest.contextHandle = StringUtils.toByteArray(v);
			break;
		    case "slotIndex":
			tcTokenRequest.slotIndex = new BigInteger(v);
			break;
		    case "cardType":
			tcTokenRequest.cardType = v;
			break;
		    default:
			LOG.info("Unknown query element: {}", k);
			break;
		}
	    }
	}

	// cardType determined! set in dynamic context, so the information is available in ResourceContext
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	dynCtx.put(TR03112Keys.ACTIVATION_CARD_TYPE, tcTokenRequest.cardType);

	if (activationTokenUrl != null) {
	    try {
		URL tokenUrl = new URL(activationTokenUrl);
		TCTokenContext tokenCtx = TCTokenContext.generateTCToken(tokenUrl);
		tcTokenRequest.tokenCtx = tokenCtx;
		tcTokenRequest.token = tokenCtx.getToken();
		tcTokenRequest.certificates = tokenCtx.getCerts();
		tcTokenRequest.tcTokenURL = tokenUrl;
	    } catch (MalformedURLException ex) {
		// TODO: check if the error type is correct, was WRONG_PARAMETER before
		throw new InvalidTCTokenUrlException(INVALID_TCTOKEN_URL, ex, activationTokenUrl);
	    }
	}

	if (tcTokenRequest.token == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}

	return tcTokenRequest;
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
		new InsertCardDialog(ctx.getUserConsent(), ctx.getCardStates(), namesAndType, ctx.getEventDispatcher());
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
	    queries.put("ifdName", ifdName);
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
	    queries.put("contextHandle", ByteUtils.toHexString(contextHandle));
	}
    }

    /**
     * Sets the SlotIndex in the given Map.
     *
     * @param queries Map which shall contain the new SlotIndex.
     * @param index The new SlotIndex to set.
     */
    private static void setSlotIndex(@Nonnull Map<String, String> queries, @Nonnull BigInteger index) {
	queries.put("slotIndex", index.toString());
    }

    /**
     * Adds the card type given in the given RecognitionInfo object as type to the tcTokenURL contained in the given map.
     *
     * @param queries Map which contains the tcTokenURL and shall contain the new cardType.
     * @param recInfo RecognitionInfo object containing the cardType or type parameter.
     */
    private static void addTokenUrlParameter(@Nonnull Map<String, String> queries, @Nonnull RecognitionInfo recInfo) {
	if (queries.containsKey("tcTokenURL")) {
	    String tcTokenURL = queries.get("tcTokenURL");
	    try {
		UrlBuilder builder = UrlBuilder.fromUrl(tcTokenURL);
		// url encoding is done by the builder
		builder = builder.queryParam("type", recInfo.getCardType(), true);
		queries.put("tcTokenURL", builder.build().toString());
		queries.put("cardType", recInfo.getCardType());
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
	if (queries.containsKey("tcTokenURL")) {
	    String tcTokenURL = queries.get("tcTokenURL");
	    try {
		UrlBuilder builder = UrlBuilder.fromUrl(tcTokenURL);
		// url encoding is done by the builder
		builder = builder.queryParam("type", selectedCardType, true);
		queries.put("tcTokenURL", builder.build().toString());
		queries.put("cardType", selectedCardType);
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

}
