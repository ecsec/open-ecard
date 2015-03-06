/****************************************************************************
 * Copyright (C) 2012-2015 HS Coburg.
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

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.xml.transform.TransformerException;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonRegistry;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.binding.tctoken.ex.InvalidAddressException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import org.openecard.binding.tctoken.ex.NonGuiException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.util.Pair;
import org.openecard.common.sal.util.InsertCardDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.message.DialogType;
import org.openecard.recognition.CardRecognition;
import org.openecard.transport.paos.PAOSException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.common.util.HandlerUtils;
import org.openecard.transport.paos.PAOSConnectionException;


/**
 * Transport binding agnostic TCToken handler. <br/>
 * This handler supports the following transports:
 * <ul>
 * <li>PAOS</li>
 * </ul>
 * <p>
 * This handler supports the following security protocols:
 * <ul>
 * <li>TLS</li>
 * <li>TLS-PSK</li>
 * <li>PLS-PSK-RSA</li>
 * </ul>
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class TCTokenHandler {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenHandler.class);

    private static final I18n langTr03112 = I18n.getTranslation("tr03112");

    private final I18n lang = I18n.getTranslation("tctoken");

    private final CardStateMap cardStates;
    private final Dispatcher dispatcher;
    private final UserConsent gui;
    private final CardRecognition rec;
    private final AddonManager manager;

    /**
     * Creates a TCToken handler instances and initializes it with the given parameters.
     *
     * @param ctx Context containing instances to the core modules.
     */
    public TCTokenHandler(Context ctx) {
	this.cardStates = ctx.getCardStates();
	this.dispatcher = ctx.getDispatcher();
	this.gui = ctx.getUserConsent();
	this.rec = ctx.getRecognition();
	this.manager = ctx.getManager();
    }


    /**
     * Gets the first handle of the given card type.
     *
     * @param type The card type to get the first handle for.
     * @return Handle describing the given card type or null if none is present.
     */
    private ConnectionHandleType getFirstHandle(String type) {
	String cardName = rec.getTranslatedCardName(type);
	ConnectionHandleType conHandle = new ConnectionHandleType();
	ConnectionHandleType.RecognitionInfo recInfo = new ConnectionHandleType.RecognitionInfo();
	recInfo.setCardType(type);
	conHandle.setRecognitionInfo(recInfo);
	Set<CardStateEntry> entries = cardStates.getMatchingEntries(conHandle);
	if (entries.isEmpty()) {
	    InsertCardDialog uc = new InsertCardDialog(gui, cardStates, type, cardName);
	    return uc.show();
	} else {
	    return entries.iterator().next().handleCopy();
	}
    }

    private ConnectionHandleType prepareHandle(ConnectionHandleType connectionHandle) throws DispatcherException, InvocationTargetException, WSException {
	// Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	CardApplicationPath appPath = new CardApplicationPath();
	appPath.setCardAppPathRequest(connectionHandle);
	CardApplicationPathResponse appPathRes = (CardApplicationPathResponse) dispatcher.deliver(appPath);

	// Check CardApplicationPathResponse
	WSHelper.checkResult(appPathRes);

	CardApplicationConnect appConnect = new CardApplicationConnect();
	List<CardApplicationPathType> pathRes;
	pathRes = appPathRes.getCardAppPathResultSet().getCardApplicationPathResult();
	appConnect.setCardApplicationPath(pathRes.get(0));
	CardApplicationConnectResponse appConnectRes;
	appConnectRes = (CardApplicationConnectResponse) dispatcher.deliver(appConnect);
	// Update ConnectionHandle. It now includes a SlotHandle.
	connectionHandle = appConnectRes.getConnectionHandle();

	// Check CardApplicationConnectResponse
	WSHelper.checkResult(appConnectRes);

	return connectionHandle;
    }

    /**
     * Performs the actual PAOS procedure.
     * Connects the given card, establishes the HTTP channel and talks to the server. Afterwards disconnects the card.
     *
     * @param token The TCToken containing the connection parameters.
     * @param connectionHandle The handle of the card that will be used.
     * @return A TCTokenResponse indicating success or failure.
     * @throws DispatcherException If there was a problem dispatching a request from the server.
     * @throws PAOSException If there was a transport error.
     */
    private TCTokenResponse processBinding(TCTokenRequest tokenRequest, ConnectionHandleType connectionHandle)
	    throws PAOSException, DispatcherException {
	TCToken token = tokenRequest.getTCToken();
	try {
	    connectionHandle = prepareHandle(connectionHandle);

	    // save handle for later use
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    dynCtx.put(TR03112Keys.CONNECTION_HANDLE, HandlerUtils.copyHandle(connectionHandle));

	    TCTokenResponse response = new TCTokenResponse();
	    response.setTCToken(token);
	    response.setResult(WSHelper.makeResultOK());

	    String binding = token.getBinding();
	    switch (binding) {
	    	case "urn:liberty:paos:2006-08": {
		    // send StartPAOS
		    List<String> supportedDIDs = getSupportedDIDs();
		    PAOSTask task = new PAOSTask(dispatcher, connectionHandle, supportedDIDs, tokenRequest, gui);
		    FutureTask<StartPAOSResponse> paosTask = new FutureTask<>(task);
		    Thread paosThread = new Thread(paosTask, "PAOS");
		    paosThread.start();
		    if (! tokenRequest.isTokenFromObject()) {
			// wait for computation to finish
			waitForTask(paosTask);
		    }
		    response.setBindingTask(paosTask);
		    break;
		}
		case "urn:ietf:rfc:2616":{
		    // no actual binding, just connect via tls and authenticate the user with that connection
		    HttpGetTask task = new HttpGetTask(dispatcher, connectionHandle, tokenRequest);
		    FutureTask<StartPAOSResponse> tlsTask = new FutureTask<>(task);
		    Thread tlsThread = new Thread(tlsTask, "TLS Auth");
		    tlsThread.start();
		    waitForTask(tlsTask);
		    response.setBindingTask(tlsTask);
		    break;
		}
		default:
		    // unknown binding
		    throw new RuntimeException("Unsupported binding in TCToken.");
	    }

	    return response;
	} catch (WSException ex) {
	    String msg = "Failed to connect to card.";
	    logger.error(msg, ex);
	    throw new DispatcherException(msg, ex);
	} catch (InvocationTargetException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new DispatcherException(ex);
	}
    }

    public static void disconnectHandle(Dispatcher dispatcher, ConnectionHandleType connectionHandle)
	   throws DispatcherException {
	try {
	    // disconnect card after authentication
	    CardApplicationDisconnect appDis = new CardApplicationDisconnect();
	    appDis.setConnectionHandle(connectionHandle);
	    dispatcher.deliver(appDis);
	} catch (InvocationTargetException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new DispatcherException(ex);
	}
    }

    public static void killUserConsent() {
	// kill any open dialog
	DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	Object navObj = ctx.get(TR03112Keys.OPEN_USER_CONSENT_NAVIGATOR);
	if (navObj instanceof UserConsentNavigator) {
	    UserConsentNavigator nav = (UserConsentNavigator) navObj;
	    nav.close();
	}
    }

    /**
     * Activates the client according to the received TCToken.
     *
     * @param request The activation request containing the TCToken.
     * @return The response containing the result of the activation process.
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws SecurityViolationException
     * @throws NonGuiException
     */
    public TCTokenResponse handleActivate(TCTokenRequest request) throws InvalidRedirectUrlException,
	    SecurityViolationException, NonGuiException {
	TCToken token = request.getTCToken();
	if (logger.isDebugEnabled()) {
	    try {
		WSMarshaller m = WSMarshallerFactory.createInstance();
		logger.debug("TCToken:\n{}", m.doc2str(m.marshal(token)));
	    } catch (TransformerException | WSMarshallerException ex) {
		// it's no use
	    }
	}

	final DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	boolean performChecks = isPerformTR03112Checks(request);
	if (! performChecks) {
	    logger.warn("Checks according to BSI TR03112 3.4.2, 3.4.4 (TCToken specific) and 3.4.5 are disabled.");
	}
	boolean isObjectActivation = request.getTCTokenURL() == null;
	if (isObjectActivation) {
	    logger.warn("Checks according to BSI TR03112 3.4.4 (TCToken specific) are disabled.");
	}
	dynCtx.put(TR03112Keys.TCTOKEN_CHECKS, performChecks);
	dynCtx.put(TR03112Keys.OBJECT_ACTIVATION, isObjectActivation);
	dynCtx.put(TR03112Keys.TCTOKEN_SERVER_CERTIFICATES, request.getCertificates());

	ConnectionHandleType connectionHandle = null;
	TCTokenResponse response = new TCTokenResponse();
	response.setTCToken(token);

	byte[] requestedContextHandle = request.getContextHandle();
	String ifdName = request.getIFDName();
	BigInteger requestedSlotIndex = request.getSlotIndex();

	if (requestedContextHandle == null || ifdName == null || requestedSlotIndex == null) {
	    // use dumb activation without explicitly specifying the card and terminal
	    // see TR-03112-7 v 1.1.2 (2012-02-28) sec. 3.2
	    connectionHandle = getFirstHandle(request.getCardType());
	} else {
	    // we know exactly which card we want
	    ConnectionHandleType requestedHandle = new ConnectionHandleType();
	    requestedHandle.setContextHandle(requestedContextHandle);
	    requestedHandle.setIFDName(ifdName);
	    requestedHandle.setSlotIndex(requestedSlotIndex);

	    Set<CardStateEntry> matchingHandles = cardStates.getMatchingEntries(requestedHandle);
	    if (! matchingHandles.isEmpty()) {
		connectionHandle = matchingHandles.toArray(new CardStateEntry[] {})[0].handleCopy();
	    }
	}

	if (connectionHandle == null) {
	    String msg = lang.translationForKey("cancel");
	    logger.error(msg);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.SAL.CANCELLATION_BY_USER, msg));
	    // fill in values, so it is usuable by the transport module
	    response = determineRefreshURL(request, response);
	    response.finishResponse(true);
	    return response;
	}

	try {
	    // process binding and follow redirect addresses afterwards
	    response = processBinding(request, connectionHandle);
	    // fill in values, so it is usuable by the transport module
	    response = determineRefreshURL(request, response);
	    response.finishResponse(isObjectActivation);
	    return response;
	} catch (DispatcherException w) {
	    logger.error(w.getMessage(), w);

	    response.setResultCode(BindingResultCode.INTERNAL_ERROR);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, w.getMessage()));
	    showErrorMessage(w.getMessage());
	    throw new NonGuiException(response, w.getMessage(), w);
	} catch (PAOSException w) {
	    logger.error(w.getMessage(), w);

	    // find actual error to display to the user
	    Throwable innerException = w.getCause();
	    if (innerException == null) {
		innerException = w;
	    } else if (innerException instanceof ExecutionException) {
		innerException = innerException.getCause();
	    }

	    String errorMsg = innerException.getLocalizedMessage();
	    switch (errorMsg) {
		case "The target server failed to respond":
		    errorMsg = langTr03112.translationForKey(NO_RESPONSE_FROM_SERVER);
		    break;
		case ECardConstants.Minor.App.INT_ERROR + " ==> Unknown eCard exception occurred.":
		    errorMsg = langTr03112.translationForKey(UNKNOWN_ECARD_ERROR);
		    break;
		case "Internal TLS error, this could be an attack":
		    errorMsg = langTr03112.translationForKey(INTERNAL_TLS_ERROR);
		    break;
	    }

	    if (innerException instanceof WSException) {
		errorMsg = langTr03112.translationForKey(ERROR_WHILE_AUTHENTICATION);
		response.setResult(WSHelper.makeResultError(((WSException) innerException).getResultMinor(), errorMsg));
		
	    } else if (innerException instanceof PAOSConnectionException) {
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.COMMUNICATION_ERROR, w.getLocalizedMessage()));
	    } else {
		// TODO: check for better matching minor type
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, w.getLocalizedMessage()));
	    }

	    showErrorMessage(errorMsg);

	    try {
		// fill in values, so it is usuable by the transport module
		response = determineRefreshURL(request, response);
		response.finishResponse(true);
	    } catch (InvalidRedirectUrlException ex) {
		logger.error(ex.getMessage(), ex);
		response.setResultCode(BindingResultCode.INTERNAL_ERROR);
		throw new NonGuiException(response, ex.getMessage(), ex);
	    } catch (SecurityViolationException ex) {
		String msg2 = "The RefreshAddress contained in the TCToken is invalid. Redirecting to the "
			+ "CommunicationErrorAddress.";
		logger.error(msg2, ex);
		response.setResultCode(BindingResultCode.REDIRECT);
		response.addAuxResultData(AuxDataKeys.REDIRECT_LOCATION, ex.getBindingResult().getAuxResultData().get(
			AuxDataKeys.REDIRECT_LOCATION));
	    }

	    return response;
	}
    }

    private static void waitForTask(Future<?> task) throws PAOSException, DispatcherException {
	try {
	    task.get();
	} catch (InterruptedException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new PAOSException(ex);
	} catch (ExecutionException ex) {
	    logger.error(ex.getMessage(), ex);
	    // perform conversion of ExecutionException from the Future to the really expected exceptions
	    if (ex.getCause() instanceof PAOSException) {
		throw (PAOSException) ex.getCause();
	    } else if (ex.getCause() instanceof DispatcherException) {
		throw (DispatcherException) ex.getCause();
	    } else {
		throw new PAOSException(ex);
	    }
	}
    }

    /**
     * Follow the URL in the RefreshAddress and update it in the response.
     * The redirect is followed as long as the response is a redirect (302, 303 or 307) AND is a
     * https-URL AND the hash of the retrieved server certificate is contained in the CertificateDescrioption, else
     * return 400. If the URL and the subjectURL in the CertificateDescription conform to the SOP we reached our final
     * destination.
     *
     * @param request TCToken request used to determine which security checks to perform.
     * @param response The TCToken response in which the original refresh address is defined and where it will be
     *	 updated.
     * @return Modified response with the final address the browser should be redirected to.
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     */
    private static TCTokenResponse determineRefreshURL(TCTokenRequest request, TCTokenResponse response)
	    throws InvalidRedirectUrlException, SecurityViolationException {
	try {
	    String endpointStr = response.getRefreshAddress();
	    URL endpoint = new URL(endpointStr);
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);

	    // omit checks completely if this is an object tag activation
	    Object objectActivation = dynCtx.get(TR03112Keys.OBJECT_ACTIVATION);
	    if (objectActivation instanceof Boolean && ((Boolean) objectActivation) == true) {
		return response;
	    }

	    // disable certificate checks according to BSI TR03112-7 in some situations
	    boolean redirectChecks = isPerformTR03112Checks(request);
	    RedirectCertificateValidator verifier = new RedirectCertificateValidator(redirectChecks);
	    ResourceContext ctx = ResourceContext.getStream(endpoint, verifier);
	    ctx.closeStream();

	    // using this verifier no result must be present, meaning no status code different than a redirect occurred
//	    if (result.p1 != null) {
//		// TODO: this error is expected according the spec, handle it in a different way
//		String msg = "Return-To-Websession yielded a non-redirect response.";
//		throw new IOException(msg);
//	    }

	    // determine redirect
	    List<Pair<URL, Certificate>> resultPoints = ctx.getCerts();
	    Pair<URL, Certificate> last = resultPoints.get(resultPoints.size() - 1);
	    endpoint = last.p1;
	    dynCtx.put(TR03112Keys.IS_REFRESH_URL_VALID, true);
	    logger.debug("Setting redirect address to '{}'.", endpoint);
	    response.setRefreshAddress(endpoint.toString());
	    return response;
	} catch (MalformedURLException ex) {
	    throw new IllegalStateException(langTr03112.translationForKey(REFRESH_URL_ERROR), ex);
	} catch (ResourceException | InvalidAddressException | ValidationError | IOException ex) {
	    String code = ECardConstants.Minor.App.COMMUNICATION_ERROR;
	    String communicationErrorAddress = response.getTCToken().getComErrorAddressWithParams(code);
	    
	    if (communicationErrorAddress != null && ! communicationErrorAddress.isEmpty()) {
		throw new SecurityViolationException(communicationErrorAddress, REFRESH_DETERMINATION_FAILED, ex);
	    }
	    throw new InvalidRedirectUrlException(REFRESH_DETERMINATION_FAILED, ex);
	}
    }

    private List<String> getSupportedDIDs() {
	TreeSet<String> result = new TreeSet<>();

	// check all sal protocols in the
	AddonRegistry registry = manager.getRegistry();
	Set<AddonSpecification> addons = registry.listAddons();
	for (AddonSpecification addon : addons) {
	    for (ProtocolPluginSpecification proto : addon.getSalActions()) {
		result.add(proto.getUri());
	    }
	}

	return new ArrayList<>(result);
    }

    /**
     * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
     *
     * @param tcTokenRequest TC Token request.
     * @return {@code true} if checks should be performed, {@code false} otherwise.
     */
    private static boolean isPerformTR03112Checks(TCTokenRequest tcTokenRequest) {
	boolean activationChecks = true;
	// disable checks when not using the nPA
	if (! tcTokenRequest.getCardType().equals("http://bsi.bund.de/cif/npa.xml")) {
	    activationChecks = false;
	}
	return activationChecks;
    }

    private void showBackgroundMessage(final String msg, final String title, final DialogType dialogType) {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		gui.obtainMessageDialog().showMessageDialog(msg, title, dialogType);
	    }
	}, "Background_MsgBox").start();
    }

    private void showErrorMessage(String errMsg) {
	String title = langTr03112.translationForKey(ERROR_TITLE);
	String baseHeader = langTr03112.translationForKey(ERROR_HEADER);
	String exceptionPart = langTr03112.translationForKey(ERROR_MSG_IND);
	String removeCard = langTr03112.translationForKey(REMOVE_CARD);
	String msg = String.format("%s\n\n%s\n%s\n\n%s", baseHeader, exceptionPart, errMsg, removeCard);
	showBackgroundMessage(msg, title, DialogType.ERROR_MESSAGE);
    }

}
