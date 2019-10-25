/****************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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

import org.openecard.httpcore.ValidationError;
import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.IOException;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonRegistry;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import org.openecard.binding.tctoken.ex.NonGuiException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
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
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.openecard.transport.paos.PAOSException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.binding.tctoken.ex.ResultMinor;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.interfaces.DocumentSchemaValidator;
import org.openecard.common.interfaces.DocumentValidatorException;
import org.openecard.common.util.JAXPSchemaValidator;
import org.openecard.common.util.FuturePromise;
import org.openecard.common.util.Promise;
import org.openecard.common.util.TR03112Utils;
import org.openecard.httpcore.HttpResourceException;
import org.openecard.httpcore.InvalidProxyException;
import org.openecard.httpcore.InvalidUrlException;
import org.openecard.httpcore.ResourceContext;
import org.openecard.transport.paos.PAOSConnectionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * Transport binding agnostic TCToken handler. <br>
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

    private static final Logger LOG = LoggerFactory.getLogger(TCTokenHandler.class);

    private static final I18n LANG_TR = I18n.getTranslation("tr03112");
    private static final I18n LANG_TOKEN = I18n.getTranslation("tctoken");
    private static final I18n LANG_PIN = I18n.getTranslation("pinplugin");
    private static final I18n LANG_PACE = I18n.getTranslation("pace");

    // Translation constants
    private static final String ERROR_CARD_REMOVED = "action.error.card.removed";

    private final String pin;
    private final String puk;
    private final CardStateMap cardStates;
    private final Dispatcher dispatcher;
    private final UserConsent gui;
    private final AddonManager manager;
    private final Promise<DocumentSchemaValidator> schemaValidator;

    /**
     * Creates a TCToken handler instances and initializes it with the given parameters.
     *
     * @param ctx Context containing instances to the core modules.
     */
    public TCTokenHandler(Context ctx) {
	this.cardStates = ctx.getCardStates();
	this.dispatcher = ctx.getDispatcher();
	this.gui = ctx.getUserConsent();
	this.manager = ctx.getManager();
	pin = LANG_PACE.translationForKey("pin");
	puk = LANG_PACE.translationForKey("puk");

	schemaValidator = new FuturePromise<>(() -> {
	    boolean noValid = Boolean.valueOf(OpenecardProperties.getProperty("legacy.invalid_schema"));
	    if (! noValid) {
		try {
		    return JAXPSchemaValidator.load("Management.xsd");
		} catch (SAXException ex) {
		    LOG.warn("No Schema Validator available, skipping schema validation.", ex);
		}
	    }
	    // always valid
	    LOG.warn("Schema validation is disabled.");
	    return new DocumentSchemaValidator() {
		@Override
		public void validate(Document doc) throws DocumentValidatorException {
		}
		@Override
		public void validate(Element doc) throws DocumentValidatorException {
		}
	    };
	});
    }

    private ConnectionHandleType prepareHandle(ConnectionHandleType connectionHandle) throws WSException {
	// Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	CardApplicationPath appPath = new CardApplicationPath();
	appPath.setCardAppPathRequest(connectionHandle);
	CardApplicationPathResponse appPathRes = (CardApplicationPathResponse) dispatcher.safeDeliver(appPath);

	// Check CardApplicationPathResponse
	WSHelper.checkResult(appPathRes);

	CardApplicationConnect appConnect = new CardApplicationConnect();
	List<CardApplicationPathType> pathRes;
	pathRes = appPathRes.getCardAppPathResultSet().getCardApplicationPathResult();
	appConnect.setCardApplicationPath(pathRes.get(0));
	CardApplicationConnectResponse appConnectRes;
	appConnectRes = (CardApplicationConnectResponse) dispatcher.safeDeliver(appConnect);
	// Update ConnectionHandle. It now includes a SlotHandle.
	connectionHandle = appConnectRes.getConnectionHandle();

	// Check CardApplicationConnectResponse
	WSHelper.checkResult(appConnectRes);

	return connectionHandle;
    }

    private ConnectionHandleType ensureHandleIsUsable(ConnectionHandleType connectionHandle) throws WSException {
	connectionHandle = prepareHandle(connectionHandle);

	// save handle for later use
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	dynCtx.put(TR03112Keys.CONNECTION_HANDLE, HandlerUtils.copyHandle(connectionHandle));

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
    private TCTokenResponse processBinding(TCTokenRequest tokenRequest, @Nullable ConnectionHandleType connectionHandle)
	    throws PAOSException, DispatcherException {
	TCToken token = tokenRequest.getTCToken();
	try {
	    TCTokenResponse response = new TCTokenResponse();
	    response.setTCToken(token);
	    response.setResult(WSHelper.makeResultOK());

	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    dynCtx.put(TR03112Keys.ACTIVATION_THREAD, Thread.currentThread());

	    String binding = token.getBinding();
	    switch (binding) {
	    	case "urn:liberty:paos:2006-08": {
		    // send StartPAOS
		    connectionHandle = ensureHandleIsUsable(connectionHandle);
		    List<String> supportedDIDs = getSupportedDIDs();
		    PAOSTask task = new PAOSTask(dispatcher, connectionHandle, supportedDIDs, tokenRequest, schemaValidator);
		    FutureTask<StartPAOSResponse> paosTask = new FutureTask<>(task);
		    Thread paosThread = new Thread(paosTask, "PAOS");
		    paosThread.start();
		    // wait for computation to finish
		    waitForTask(paosTask);
		    response.setBindingTask(paosTask);
		    break;
		}
		case "urn:ietf:rfc:2616": {
		    // no actual binding, just connect via tls and authenticate the user with that connection
		    connectionHandle = ensureHandleIsUsable(connectionHandle);
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
	    LOG.error(msg, ex);

	    if(ECardConstants.Minor.IFD.CANCELLATION_BY_USER.equals(ex.getResultMinor())) {
		throw new PAOSException(ex);
	    }

	    throw new DispatcherException(msg, ex);
	}
    }

    public static void disconnectHandle(Dispatcher dispatcher, ConnectionHandleType connectionHandle) {
	// disconnect card after authentication
	CardApplicationDisconnect appDis = new CardApplicationDisconnect();
	appDis.setConnectionHandle(connectionHandle);
	appDis.setAction(ActionType.RESET);
	dispatcher.safeDeliver(appDis);
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
	if (LOG.isDebugEnabled()) {
	    try {
		WSMarshaller m = WSMarshallerFactory.createInstance();
		LOG.debug("TCToken:\n{}", m.doc2str(m.marshal(token)));
	    } catch (TransformerException | WSMarshallerException ex) {
		// it's no use
	    }
	}

	final DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	boolean performChecks = isPerformTR03112Checks(request);
	if (! performChecks) {
	    LOG.warn("Checks according to BSI TR03112 3.4.2, 3.4.4 (TCToken specific) and 3.4.5 are disabled.");
	}
	dynCtx.put(TR03112Keys.TCTOKEN_CHECKS, performChecks);
	dynCtx.put(TR03112Keys.TCTOKEN_SERVER_CERTIFICATES, request.getCertificates());

	ConnectionHandleType connectionHandle = null;
	TCTokenResponse response = new TCTokenResponse();
	response.setTCToken(token);

	byte[] requestedContextHandle = request.getContextHandle();
	String ifdName = request.getIFDName();
	BigInteger requestedSlotIndex = request.getSlotIndex();

	// we know exactly which card we want
	ConnectionHandleType requestedHandle = new ConnectionHandleType();
	requestedHandle.setContextHandle(requestedContextHandle);
	requestedHandle.setIFDName(ifdName);
	requestedHandle.setSlotIndex(requestedSlotIndex);

	Set<CardStateEntry> matchingHandles = cardStates.getMatchingEntries(requestedHandle);
	if (!matchingHandles.isEmpty()) {
	    connectionHandle = matchingHandles.toArray(new CardStateEntry[]{})[0].handleCopy();
	}


	if (connectionHandle == null) {
	    String msg = LANG_TOKEN.translationForKey("cancel");
	    LOG.error(msg);
	    response.setResult(WSHelper.makeResultError(ResultMinor.CANCELLATION_BY_USER, msg));
	    // fill in values, so it is usuable by the transport module
	    response = determineRefreshURL(request, response);
	    response.finishResponse();
	    return response;
	}

	try {
	    // process binding and follow redirect addresses afterwards
	    response = processBinding(request, connectionHandle);
	    // fill in values, so it is usuable by the transport module
	    response = determineRefreshURL(request, response);
	    response.finishResponse();
	    return response;
	} catch (DispatcherException w) {
	    LOG.error(w.getMessage(), w);

	    response.setResultCode(BindingResultCode.INTERNAL_ERROR);
	    response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, w.getMessage()));
	    showErrorMessage(w.getMessage());
	    throw new NonGuiException(response, w.getMessage(), w);
	} catch (PAOSException w) {
	    LOG.error(w.getMessage(), w);

	    // find actual error to display to the user
	    Throwable innerException = w.getCause();
	    if (innerException == null) {
		innerException = w;
	    } else if (innerException instanceof ExecutionException) {
		innerException = innerException.getCause();
	    }

	    String errorMsg = innerException.getLocalizedMessage();
	    errorMsg = errorMsg == null ? "" : errorMsg; // fix NPE when null is returned instead of a message
	    switch (errorMsg) {
		case "The target server failed to respond":
		    errorMsg = LANG_TR.translationForKey(NO_RESPONSE_FROM_SERVER);
		    break;
		case ECardConstants.Minor.App.INT_ERROR + " ==> Unknown eCard exception occurred.":
		    errorMsg = LANG_TR.translationForKey(UNKNOWN_ECARD_ERROR);
		    break;
		case "Internal TLS error, this could be an attack":
		    errorMsg = LANG_TR.translationForKey(INTERNAL_TLS_ERROR);
		    break;
	    }

	    LOG.debug("Processing InnerException.", innerException);
	    if (innerException instanceof WSException) {
		WSException ex = (WSException) innerException;
		errorMsg = createResponseFromWsEx(ex, response);
	    } else if (innerException instanceof PAOSConnectionException) {
		response.setResult(WSHelper.makeResultError(ResultMinor.TRUSTED_CHANNEL_ESTABLISHMENT_FAILED,
			w.getLocalizedMessage()));
		response.setAdditionalResultMinor(ECardConstants.Minor.Disp.COMM_ERROR);
	    } else if (innerException instanceof InterruptedException) {
		response.setResultCode(BindingResultCode.INTERRUPTED);
		response.setResult(WSHelper.makeResultError(ResultMinor.CANCELLATION_BY_USER, errorMsg));
		response.setAdditionalResultMinor(ECardConstants.Minor.App.SESS_TERMINATED);
	    } else if (innerException instanceof DocumentValidatorException) {
		errorMsg = LANG_TR.translationForKey(SCHEMA_VALIDATION_FAILED);
		// it is ridiculous, that this should be a client error, but the test spec demands this
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, w.getMessage()));
		response.setAdditionalResultMinor(ECardConstants.Minor.SAL.Support.SCHEMA_VAILD_FAILED);
	    } else {
		errorMsg = createMessageFromUnknownError(w);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, w.getMessage()));
		response.setAdditionalResultMinor(ECardConstants.Minor.App.UNKNOWN_ERROR);
	    }

	    String paosAdditionalMinor = w.getAdditionalResultMinor();
	    if (paosAdditionalMinor != null) {
		LOG.debug("Replacing minor from inner exception with minor from PAOSException.");
		LOG.debug("InnerException minor: {}", response.getAuxResultData().get(AuxDataKeys.MINOR_PROCESS_RESULT));
		LOG.debug("PAOSException minor: {}", paosAdditionalMinor);
		response.setAdditionalResultMinor(paosAdditionalMinor);
	    }

	    showErrorMessage(errorMsg);

	    try {
		// fill in values, so it is usuable by the transport module
		response = determineRefreshURL(request, response);
		response.finishResponse();
	    } catch (InvalidRedirectUrlException ex) {
		LOG.error(ex.getMessage(), ex);
		// in case we were interrupted before, use INTERRUPTED as result status
		if (innerException instanceof InterruptedException) {
		    response.setResultCode(BindingResultCode.INTERRUPTED);
		    response.setResult(WSHelper.makeResultError(ResultMinor.CANCELLATION_BY_USER, errorMsg));
		} else {
		    response.setResultCode(BindingResultCode.INTERNAL_ERROR);
		    response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, ex.getLocalizedMessage()));
		    throw new NonGuiException(response, ex.getMessage(), ex);
		}
	    } catch (SecurityViolationException ex) {
		String msg2 = "The RefreshAddress contained in the TCToken is invalid. Redirecting to the "
			+ "CommunicationErrorAddress.";
		LOG.error(msg2, ex);
		response.setResultCode(BindingResultCode.REDIRECT);
		response.setResult(WSHelper.makeResultError(ResultMinor.COMMUNICATION_ERROR, msg2));
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
	    LOG.info("Waiting for PAOS Task to finish has been interrupted. Cancelling authentication.");
	    task.cancel(true);
	    throw new PAOSException(ex);
	} catch (ExecutionException ex) {
	    LOG.error(ex.getMessage(), ex);
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

	    // disable certificate checks according to BSI TR03112-7 in some situations
	    boolean redirectChecks = isPerformTR03112Checks(request);
	    RedirectCertificateValidator verifier = new RedirectCertificateValidator(redirectChecks);
	    ResourceContext ctx = new TrResourceContextLoader().getStream(endpoint, verifier);
	    ctx.closeStream();

	    // using this verifier no result must be present, meaning no status code different than a redirect occurred
//	    if (result.p1 != null) {
//		// TODO: this error is expected according the spec, handle it in a different way
//		String msg = "Return-To-Websession yielded a non-redirect response.";
//		throw new IOException(msg);
//	    }

	    // determine redirect
	    List<Pair<URL, TlsServerCertificate>> resultPoints = ctx.getCerts();
	    Pair<URL, TlsServerCertificate> last = resultPoints.get(resultPoints.size() - 1);
	    endpoint = last.p1;
	    dynCtx.put(TR03112Keys.IS_REFRESH_URL_VALID, true);
	    LOG.debug("Setting redirect address to '{}'.", endpoint);
	    response.setRefreshAddress(endpoint.toString());
	    return response;
	} catch (MalformedURLException ex) {
	    throw new IllegalStateException(LANG_TR.translationForKey(REFRESH_URL_ERROR), ex);
	} catch (HttpResourceException | InvalidUrlException | InvalidProxyException | ValidationError | IOException ex) {
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
	} else if (TR03112Utils.DEVELOPER_MODE) {
	    activationChecks = false;
	    LOG.warn("DEVELOPER_MODE: All TR-03124-1 security checks are disabled.");
	}
	return activationChecks;
    }

    private void showBackgroundMessage(final String msg, final String title, final DialogType dialogType) {
	new Thread(() -> {
	    gui.obtainMessageDialog().showMessageDialog(msg, title, dialogType);
	}, "Background_MsgBox").start();
    }

    private void showErrorMessage(String errMsg) {
	String title = LANG_TR.translationForKey(ERROR_TITLE);
	String baseHeader = LANG_TR.translationForKey(ERROR_HEADER);
	String exceptionPart = LANG_TR.translationForKey(ERROR_MSG_IND);
	String removeCard = LANG_TR.translationForKey(REMOVE_CARD);
	String msg = String.format("%s\n\n%s\n%s\n\n%s", baseHeader, exceptionPart, errMsg, removeCard);
	showBackgroundMessage(msg, title, DialogType.ERROR_MESSAGE);
    }

    private String createResponseFromWsEx(WSException ex, TCTokenResponse response) {
	String errorMsg;
	String minor = ex.getResultMinor();

	switch (minor) {
	    case ECardConstants.Minor.Disp.TIMEOUT:
	    case ECardConstants.Minor.SAL.CANCELLATION_BY_USER:
	    case ECardConstants.Minor.IFD.CANCELLATION_BY_USER:
		errorMsg = LANG_TOKEN.translationForKey("cancel");
		response.setResult(WSHelper.makeResultError(ResultMinor.CANCELLATION_BY_USER, errorMsg));
		break;
	    case ECardConstants.Minor.SAL.EAC.DOC_VALID_FAILED:
		errorMsg = LANG_TR.translationForKey(CERT_ERROR);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.App.INCORRECT_PARM:
		errorMsg = LANG_TR.translationForKey(MESSAGE_CONTENT_INVALID);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.App.INT_ERROR:
		errorMsg = LANG_TR.translationForKey(INTERNAL_ERROR);
		response.setResult(WSHelper.makeResultError(ResultMinor.SERVER_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED:
		errorMsg = LANG_TR.translationForKey(CERT_DESCRIPTION_CHECK_FAILED);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.App.UNKNOWN_ERROR:
		errorMsg = LANG_TR.translationForKey(ERROR_WHILE_AUTHENTICATION);
		response.setResult(WSHelper.makeResultError(ResultMinor.SERVER_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.SAL.UNKNOWN_HANDLE:
		errorMsg = LANG_TR.translationForKey(UNKNOWN_CONNECTION_HANDLE);
		response.setResult(WSHelper.makeResultError(ResultMinor.SERVER_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE:
		errorMsg = LANG_PIN.translationForKey(ERROR_CARD_REMOVED);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.IFD.PASSWORD_BLOCKED:
		errorMsg = LANG_PACE.translationForKey("step_error_pin_blocked", pin, pin, puk, pin);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, errorMsg));
		break;
	    case ECardConstants.Minor.IFD.UNKNOWN_ERROR:
		errorMsg = LANG_TR.translationForKey(ERROR_WHILE_AUTHENTICATION);
		response.setResult(WSHelper.makeResultError(ResultMinor.CLIENT_ERROR, errorMsg));
		break;
	    default:
		errorMsg = LANG_TR.translationForKey(ERROR_WHILE_AUTHENTICATION);
		response.setResult(WSHelper.makeResultError(ResultMinor.SERVER_ERROR, errorMsg));
	}

	response.setAdditionalResultMinor(minor);

	return errorMsg;
    }

    /**
     * Creates an error message from an PAOSException which contains a not handled  inner exception.
     *
     * @param w An PAOSException containing a not handled inner exception.
     * @return A sting containing an error message.
     */
    private String createMessageFromUnknownError(@Nonnull PAOSException w) {
	String errorMsg = "\n";
	errorMsg += LANG_TR.translationForKey(UNHANDLED_INNER_EXCEPTION);
	errorMsg += "\n";
	errorMsg += w.getMessage();
	return errorMsg;
    }

}
