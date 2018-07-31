/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import org.openecard.crypto.common.sal.did.TokenCache;
import org.openecard.addons.cg.activate.TlsConnectionHandler;
import org.openecard.addons.cg.ex.VersionTooOld;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.openecard.addon.Context;
import org.openecard.addons.cg.ex.AuthServerException;
import org.openecard.addons.cg.ex.ConnectionError;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.entity.ContentType;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.impl.DefaultConnectionReuseStrategy;
import org.openecard.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestExecutor;
import org.openecard.addons.cg.tctoken.TCToken;
import static org.openecard.addons.cg.ex.ErrorTranslations.*;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import org.openecard.addons.cg.ex.ChipGatewayDataError;
import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.addons.cg.ex.ParameterInvalid;
import org.openecard.addons.cg.ex.PinBlocked;
import org.openecard.addons.cg.ex.RemotePinException;
import org.openecard.addons.cg.ex.ResultMinor;
import org.openecard.addons.cg.ex.SlotHandleInvalid;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.common.AppVersion;
import org.openecard.common.I18n;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.SemanticVersion;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteComparator;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.UrlBuilder;
import org.openecard.common.util.ValueGenerators;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.NoSuchDid;
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.openecard.transport.httpcore.HttpRequestHelper;
import org.openecard.transport.httpcore.HttpUtils;
import org.openecard.transport.httpcore.StreamHttpClientConnection;
import org.openecard.ws.chipgateway.CertificateInfoType;
import org.openecard.ws.chipgateway.CommandType;
import org.openecard.ws.chipgateway.GetCommandType;
import org.openecard.ws.chipgateway.HelloRequestType;
import org.openecard.ws.chipgateway.HelloResponseType;
import org.openecard.ws.chipgateway.ListCertificatesRequestType;
import org.openecard.ws.chipgateway.ListCertificatesResponseType;
import org.openecard.ws.chipgateway.ListTokensRequestType;
import org.openecard.ws.chipgateway.ListTokensResponseType;
import org.openecard.ws.chipgateway.ResponseType;
import org.openecard.ws.chipgateway.SignRequestType;
import org.openecard.ws.chipgateway.SignResponseType;
import org.openecard.ws.chipgateway.TokenInfoType;
import org.openecard.ws.chipgateway.TerminateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ChipGateway {

    private static final Logger LOG = LoggerFactory.getLogger(ChipGateway.class);

    private static final long WAIT_MAX_MILLIS = 60 * 60 * 1000; // 60 min
    private static final I18n LANG = I18n.getTranslation("chipgateway");
    private static final AtomicInteger TASK_THREAD_NUM = new AtomicInteger(1);
    private static final AtomicInteger HTTP_THREAD_NUM = new AtomicInteger(1);
    private static final boolean LOG_HTTP_MESSAGES = true;

    private final TlsConnectionHandler tlsHandler;
    private final TCToken token;
    private final JsonWebKey pinKey;
    private final Context addonCtx;
    private final UserConsent gui;
    private final Dispatcher dispatcher;
    private final ObjectMapper mapper;
    private final String sessionId;
    private final UrlBuilder addrBuilder;

    private final URI helloUrl;
    private final URI getCommandUrl;
    private final URI listTokensUrl;
    private final URI listCertsUrl;
    private final URI signUrl;
    private final URI terminateUrl;

    // connection specific values
    private final HttpContext httpCtx = new BasicHttpContext();
    private final HttpRequestExecutor httpExecutor = new HttpRequestExecutor();
    private final DefaultConnectionReuseStrategy reuseStrategy = new DefaultConnectionReuseStrategy();
    private StreamHttpClientConnection conn;
    private boolean canReuse = false;
    private volatile boolean isInterrupted = false;

    private HelloRequestType helloReq;
    private Thread showDialogThread;

    private final TreeSet<byte[]> connectedSlots;
    private final TokenCache tokenCache;

    public ChipGateway(TlsConnectionHandler handler, TCToken token, Context addonCtx)
	    throws InvalidTCTokenElement {
        try {
            this.tlsHandler = handler;
            this.token = token;
	    this.addonCtx = addonCtx;
            this.gui = addonCtx.getUserConsent();
            this.dispatcher = addonCtx.getDispatcher();
            this.sessionId = token.getSessionIdentifier();

            this.addrBuilder = UrlBuilder.fromUrl(token.getServerAddress());
            this.helloUrl = addrBuilder.addPathSegment("HelloRequest").build();
            this.getCommandUrl = addrBuilder.addPathSegment("GetCommand").build();
            this.listTokensUrl = addrBuilder.addPathSegment("ListTokensResponse").build();
            this.listCertsUrl = addrBuilder.addPathSegment("ListCertificatesResponse").build();
            this.signUrl = addrBuilder.addPathSegment("SignResponse").build();
	    this.terminateUrl = addrBuilder.addPathSegment("Terminate").build();

            this.mapper = new ObjectMapper();
            this.mapper.registerModule(new JaxbAnnotationModule());

	    this.connectedSlots = new TreeSet<>(new ByteComparator());
	    this.tokenCache = new TokenCache(dispatcher);

	    JsonWebKey webKey = null;
	    if ("http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption".equals(token.getPathSecurityProtocol())) {
		String jwkStr = token.getPathSecurityParameters().getJWK();
		try {
		    webKey = JsonWebKey.Factory.newJwk(jwkStr);
		} catch (JoseException ex) {
		    LOG.error("Failed to convert JWK.", ex);
		}
	    }
	    pinKey = webKey;

        } catch (URISyntaxException ex) {
            throw new InvalidTCTokenElement(MALFORMED_URL, ex, "ServerAddress");
        }
    }

    private void openHttpStream() throws ConnectionError, InvalidRedirectUrlException {
	try {
	    LOG.debug("Opening connection to ChipGateway server.");
	    TlsClientProtocol handler = tlsHandler.createTlsConnection();
	    conn = new StreamHttpClientConnection(handler.getInputStream(), handler.getOutputStream());
	    LOG.debug("Connection to ChipGateway server established.");
	} catch (IOException | URISyntaxException ex) {
	    throw new ConnectionError(token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
                    CONNECTION_OPEN_FAILED, ex);
	}
    }

    /**
     * Check the status code returned from the server.
     * If the status code indicates an error, a ChipGatewayException will be thrown.
     *
     * @param statusCode The status code we received from the server
     * @throws ConnectionError If the server returned a HTTP error code
     */
    private void checkHTTPStatusCode(int statusCode) throws ConnectionError, InvalidRedirectUrlException {
	if (statusCode != 200) {
	    throw new ConnectionError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
                    INVALID_HTTP_STATUS, statusCode);
	}
    }


    private void checkProcessCancelled() {
	if (Thread.currentThread().isInterrupted()) {
	    throw performProcessCancelled();
	}
    }

    private ThreadTerminateException performProcessCancelled() {
	TerminateType resp = new TerminateType();
	resp.setSessionIdentifier(sessionId);
	resp.setResult(ChipGatewayStatusCodes.STOPPED);
	return performProcessCancelled(getResource(terminateUrl), resp);
    }

    private ThreadTerminateException performProcessCancelled(final String resource, final ResponseType msg) {
	LOG.debug("Sending terminate message due to process cancellation.");
	this.isInterrupted = true;
	Thread t = new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    sendMessage(resource, mapper.writeValueAsString(msg), CommandType.class);
		} catch (JsonProcessingException | ConnectionError | InvalidRedirectUrlException | ChipGatewayDataError ex) {
		    LOG.debug("Error sending terminating message.", ex);
		} finally {
		    // close connection as nobody will send a message after termination
		    try {
			if (conn != null) {
			    conn.close();
			}
		    } catch (IOException ex) {
			LOG.error("Failed to close connection to server.", ex);
		    }
		}
	    }
	}, "ChipGateway-terminate");
	t.start();

	return new ThreadTerminateException("ChipGateway protocol interrupted.");
    }

    private CommandType sendMessageInterruptableAndCheckTermination(final String resource, final ResponseType resp)
	    throws ConnectionError, InvalidRedirectUrlException, ChipGatewayDataError, ThreadTerminateException, JsonProcessingException {
	// stop messages are sent in the background
	if (ChipGatewayStatusCodes.STOPPED.equals(resp.getResult())) {
	    throw performProcessCancelled(resource, resp);
	}
	// all other messages are sent normally and if an interrupt is hit, send terminate in background thread
	try {
	    String msg = mapper.writeValueAsString(resp);
	    return sendMessageInterruptable(resource, msg, CommandType.class);
	} catch (ThreadTerminateException ex) {
	    LOG.info("Sending message {} interrupted. Shutting down.", resp.getClass().getSimpleName());
	    throw performProcessCancelled();
	}
    }

    private <T> T sendMessageInterruptable(final String resource, final String msg, final Class<T> resClass)
	    throws ConnectionError, InvalidRedirectUrlException, ChipGatewayDataError, ThreadTerminateException {
	FutureTask<T> task = new FutureTask<>(new Callable<T>() {
	    @Override
	    public T call() throws Exception {
		return sendMessage(resource, msg, resClass);
	    }
	});
	new Thread(task, "HTTP-Client-" + HTTP_THREAD_NUM.getAndIncrement()).start();

	try {
	    return task.get();
	} catch (ExecutionException ex) {
	    Throwable cause = ex.getCause();
	    if (cause instanceof ConnectionError) {
		throw (ConnectionError) cause;
	    } else if (cause instanceof InvalidRedirectUrlException) {
		throw (InvalidRedirectUrlException) cause;
	    } else if (cause instanceof ChipGatewayDataError) {
		throw (ChipGatewayDataError) cause;
	    } else if (cause instanceof RuntimeException) {
		throw (RuntimeException) cause;
	    } else {
		throw new RuntimeException("Unexpected exception raised by HTTP message sending thread.", cause);
	    }
	} catch (InterruptedException ex) {
	    LOG.debug("Sending HTTP message interrupted.");
	    task.cancel(true);

	    // force new connection because this one may be unfinished and thus unusable
	    try {
		conn.shutdown();
	    } catch (IOException ignore) {
	    }

	    throw new ThreadTerminateException("Interrupt received while sending HTTP message.");
	}
    }

    private <T> T sendMessage(String resource, String msg, Class<T> resClass) throws ConnectionError,
            InvalidRedirectUrlException, ChipGatewayDataError {
	return sendMessage(resource, msg, resClass, true);
    }

    private <T> T sendMessage(String resource, String msg, Class<T> resClass, boolean tryAgain)
	    throws ConnectionError, InvalidRedirectUrlException, ChipGatewayDataError {
	try {
	    // open initial connection
	    if (conn == null || ! canReuse || (! conn.isOpen() && canReuse)) {
		openHttpStream();
	    }

	    // prepare request
	    BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", resource);
	    HttpRequestHelper.setDefaultHeader(req, tlsHandler.getServerAddress());
	    req.setHeader("Accept", "application/json");

	    ContentType reqContentType = ContentType.create("application/json", "UTF-8");
	    if (LOG_HTTP_MESSAGES) {
		HttpUtils.dumpHttpRequest(LOG, "before adding content", req);
	    }
	    StringEntity reqMsg = new StringEntity(msg, reqContentType);
	    req.setEntity(reqMsg);
	    req.setHeader(reqMsg.getContentType());
	    req.setHeader("Content-Length", Long.toString(reqMsg.getContentLength()));
	    if (LOG_HTTP_MESSAGES) {
		LOG.debug(msg);
	    }

	    // send request and receive response
	    LOG.debug("Sending HTTP request.");
	    HttpResponse response = httpExecutor.execute(req, conn, httpCtx);
	    canReuse = reuseStrategy.keepAlive(response, httpCtx);
	    LOG.debug("HTTP response received.");
	    int statusCode = response.getStatusLine().getStatusCode();
	    checkHTTPStatusCode(statusCode);

	    conn.receiveResponseEntity(response);
	    HttpEntity entity = response.getEntity();
	    byte[] entityData = FileUtils.toByteArray(entity.getContent());
	    if (LOG_HTTP_MESSAGES) {
		HttpUtils.dumpHttpResponse(LOG, response, entityData);
	    }

	    // convert entity and return it
	    T resultObj = parseResultObj(entityData, resClass);
	    return resultObj;
	} catch (IOException ex) {
	    if (! Thread.currentThread().isInterrupted() && tryAgain) {
		String errorMsg = "ChipGateway server closed the connection. Trying to connect again.";
		if (LOG.isDebugEnabled()) {
		    LOG.debug(errorMsg, ex);
		} else {
		    LOG.info(errorMsg);
		}
		canReuse = false;
		return sendMessage(resource, msg, resClass, false);
	    } else {
		throw new ConnectionError(token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
                    CONNECTION_OPEN_FAILED, ex);
	    }
	} catch (HttpException ex) {
	    throw new ConnectionError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
                    HTTP_ERROR, ex);
	}
    }

    private <T> T parseResultObj(byte[] msg, Class<T> msgClass) throws ChipGatewayDataError,
            InvalidRedirectUrlException {
	try {
	    T obj = mapper.readValue(msg, msgClass);
	    return obj;
	} catch (IOException ex) {
	    String errorMsg = "Failed to convert response to JSON data type.";
            LOG.warn(errorMsg);
	    throw new ChipGatewayDataError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
                    INVALID_CHIPGATEWAY_MSG, ex);
	}
    }

    private String getResource(URI uri) {
	String path = uri.getPath();
	String query = uri.getQuery();
	// correct and combine path
	path = (path == null) ? "/" : path;
	String resource = (query == null) ? path : path + "?" + query;
	return resource;
    }

    public TerminateType sendHello() throws VersionTooOld, ChipGatewayDataError, ConnectionError,
            InvalidRedirectUrlException, AuthServerException {
	try {
	    byte[] challenge = ValueGenerators.generateRandom(32);
	    helloReq = new HelloRequestType();
	    helloReq.setSessionIdentifier(sessionId);
	    helloReq.setVersion(String.format("%s.%s.%s", AppVersion.getMajor(), AppVersion.getMinor(), AppVersion.getPatch()));
	    helloReq.setChallenge(challenge);

	    // send Hello
	    String helloReqMsg = mapper.writeValueAsString(helloReq);
	    HelloResponseType helloResp = sendMessageInterruptable(getResource(helloUrl), helloReqMsg, HelloResponseType.class);
	    processHelloResponse(helloResp);

	    // send GetCommand
	    GetCommandType cmdReq = createGetCommandRequest();
	    String cmdReqMsg = mapper.writeValueAsString(cmdReq);
	    CommandType cmdResp;
	    try {
		cmdResp = sendMessageInterruptable(getResource(getCommandUrl), cmdReqMsg, CommandType.class);
	    } catch (ThreadTerminateException ex) {
		performProcessCancelled();
		throw ex;
	    }

	    // send messages to the server as long as there is no termination response
	    while (cmdResp.getTerminate() == null) {
		ListTokensRequestType tokensReq = cmdResp.getListTokensRequest();
		ListCertificatesRequestType certReq = cmdResp.getListCertificatesRequest();
		SignRequestType signReq = cmdResp.getSignRequest();

		if (tokensReq != null) {
		    cmdResp = processTokensRequest(tokensReq);
		} else if (certReq != null) {
		    cmdResp = processCertificatesRequest(certReq);
		} else if (signReq != null) {
		    cmdResp = processSignRequest(signReq);
		} else {
                    throw new ChipGatewayDataError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
                            INVALID_CHIPGATEWAY_MSG);
		}
	    }

	    // return the last message (terminate type)
	    return cmdResp.getTerminate();

	} catch (JsonProcessingException ex) {
            throw new ChipGatewayDataError(token.finalizeErrorAddress(ResultMinor.CLIENT_ERROR),
                    INVALID_CHIPGATEWAY_MSG, ex);
	} finally {
	    // clear token cache and delete all pins in it
	    tokenCache.clearPins();

	    // display GUI if needed
	    if (showDialogThread != null) {
		showDialogThread.start();
	    }

	    try {
		// in case we are interrupted, terminate is sent in the background, so don't close just yet
		if (conn != null && ! isInterrupted) {
		    conn.close();
		}
	    } catch (IOException ex) {
		LOG.error("Failed to close connection to server.", ex);
	    }

	    // disconnect all slots which have been connected in the process
	    for (byte[] nextSlot : connectedSlots) {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Disconnecting card with slotHandle={}.", ByteUtils.toHexString(nextSlot));
		}
		CardApplicationDisconnect req = new CardApplicationDisconnect();
		//req.setAction(ActionType.RESET);
		ConnectionHandleType handle = HandlerBuilder.create()
			.setSlotHandle(nextSlot)
			.buildConnectionHandle();
		req.setConnectionHandle(handle);
		dispatcher.safeDeliver(req);
	    }
	}
    }

    private void processHelloResponse(HelloResponseType helloResp) throws AuthServerException,
            InvalidRedirectUrlException, VersionTooOld, ChipGatewayDataError {
	// check if we have been interrupted
	checkProcessCancelled();

	String rCode = helloResp.getResult();

	// check for codes which don't break the process immediately
	if (ChipGatewayStatusCodes.OK.equals(rCode) ||
		ChipGatewayStatusCodes.UPDATE_RECOMMENDED.equals(rCode) ||
		ChipGatewayStatusCodes.UPDATE_REQUIRED.equals(rCode)) {
	    // validate hello response (e.i. challenge and server signature)
	    if (ChipGatewayProperties.isValidateChallengeResponse()) {
		LOG.debug("Validating challenge-response signature.");
		validateSignature(helloResp);
	    } else {
		LOG.warn("Skipping the validation of the challenge-response signature.");
	    }

	    // check version and propose to update the client
	    String dlUrl = helloResp.getDownloadAddress();
	    boolean mustUpdate = ChipGatewayStatusCodes.UPDATE_REQUIRED.equals(rCode);
	    if (dlUrl != null) {
		// create dialog thread which will be executed when the protocol is finished
		createUpdateDialog(dlUrl, mustUpdate);
	    }
	    if (mustUpdate) {
		// stop protocol
		throw new VersionTooOld(token.finalizeErrorAddress(ResultMinor.CLIENT_ERROR), VERSION_OUTDATED);
	    }

	    // TODO: check WebOrigin
	} else {
	    LOG.error("Received an error form the ChipGateway server ({}).", rCode);
	    throw new ChipGatewayDataError(rCode, "Received an error form the ChipGateway server.");
//	    switch (rCode) {
//		// TODO: evaluate result
//	    }
	}
    }

    private GetCommandType createGetCommandRequest() {
	GetCommandType cmd = new GetCommandType();
	cmd.setSessionIdentifier(sessionId);

	// add token info
	try {
	    ListTokens helper = new ListTokens(Collections.EMPTY_LIST, addonCtx);
	    List<TokenInfoType> matchedTokens = helper.findTokens();
	    cmd.getTokenInfo().addAll(matchedTokens);
	} catch (UnsupportedAlgorithmException ex) {
	    throw new RuntimeException("Unexpected error in empty token filter.", ex);
	} catch (WSHelper.WSException ex) {
	    LOG.error("Error requesting initial list of tokens.");
	}

	return cmd;
    }

    private CommandType processTokensRequest(ListTokensRequestType tokensReq) throws ConnectionError,
	    JsonProcessingException, InvalidRedirectUrlException, ChipGatewayDataError {
	// check if we have been interrupted
	checkProcessCancelled();

	ListTokensResponseType tokensResp = new ListTokensResponseType();
	tokensResp.setSessionIdentifier(sessionId);

	try {
	    tokensResp = waitForTokens(tokensReq);
	} catch (UnsupportedAlgorithmException ex) {
	    LOG.error("Unsupported algorithm used.", ex);
	    tokensResp.setResult(ChipGatewayStatusCodes.INCORRECT_PARAMETER);
	} catch (WSHelper.WSException ex) {
	    LOG.error("Unknown error.", ex);
	    tokensResp.setResult(ChipGatewayStatusCodes.OTHER);
	} catch (ThreadTerminateException | InterruptedException ex) {
	    LOG.info("Chipgateway process interrupted.", ex);
	    tokensResp.setResult(ChipGatewayStatusCodes.STOPPED);
	} catch (TimeoutException ex) {
	    LOG.info("Waiting for new tokens timed out.", ex);
	    tokensResp.setResult(ChipGatewayStatusCodes.TIMEOUT);
	}

	return sendMessageInterruptableAndCheckTermination(getResource(listTokensUrl), tokensResp);
    }

    private ListTokensResponseType waitForTokens(ListTokensRequestType tokensReq) throws UnsupportedAlgorithmException,
	    WSHelper.WSException, InterruptedException, TimeoutException {
	BigInteger waitSecondsBig = tokensReq.getMaxWaitSeconds();
	long waitMillis = getWaitMillis(waitSecondsBig);

	Date startTime = new Date();

	ListTokens helper = new ListTokens(tokensReq.getTokenInfo(), addonCtx);
	do {
	    // build list of matching tokens
	    List<TokenInfoType> matchedTokens = helper.findTokens();

	    // save handles of connected cards
	    connectedSlots.addAll(helper.getConnectedSlots());

	    // return if tokens have been found or no specific set of tokens has been requested
	    if (! matchedTokens.isEmpty() || tokensReq.getTokenInfo().isEmpty()) {
		ListTokensResponseType tokensResp = new ListTokensResponseType();
		tokensResp.setSessionIdentifier(sessionId);
		tokensResp.setResult(ChipGatewayStatusCodes.OK);
		tokensResp.getTokenInfo().addAll(matchedTokens);
		return tokensResp;
	    }

	    // TODO: use real wait mechanism on the SAL implementation
	    Thread.sleep(1000);
	} while ((new Date().getTime() - startTime.getTime()) < waitMillis);

	throw new TimeoutException("Waiting for ListTokens timed out.");
    }


    private CommandType processCertificatesRequest(final ListCertificatesRequestType certReq) throws ConnectionError,
	    JsonProcessingException, InvalidRedirectUrlException, ChipGatewayDataError {
	// check if we have been interrupted
	checkProcessCancelled();

	BigInteger waitSecondsBig = certReq.getMaxWaitSeconds();
	long waitMillis = getWaitMillis(waitSecondsBig);

	// run the actual stuff in the background, so we can wait and terminate if needed
	FutureTask<ListCertificatesResponseType> action = new FutureTask<>(new Callable<ListCertificatesResponseType>() {
	    @Override
	    public ListCertificatesResponseType call() throws Exception {
		ListCertificatesResponseType certResp = new ListCertificatesResponseType();
		certResp.setSessionIdentifier(sessionId);

		char[] pin = null;
		try {
		    pin = getPin(certReq.getPIN());
		    byte[] slotHandle = certReq.getSlotHandle();
		    ListCertificates helper = new ListCertificates(tokenCache, slotHandle, certReq.getCertificateFilter(), pin);
		    List<CertificateInfoType> certInfos = helper.getCertificates();

		    certResp.getCertificateInfo().addAll(certInfos);
		    certResp.setResult(ChipGatewayStatusCodes.OK);
		    return certResp;
		} finally {
		    if (pin != null) {
			Arrays.fill(pin, ' ');
		    }
		}
	    }
	});
	Thread t = new Thread(action, "CertificatesRequest-Task-" + TASK_THREAD_NUM.getAndIncrement());
	t.setDaemon(true);
	t.start();

	ListCertificatesResponseType certResp = new ListCertificatesResponseType();
	certResp.setSessionIdentifier(sessionId);

	try {
	    // wait for thread to finish
	    certResp = action.get(waitMillis, TimeUnit.MILLISECONDS);
	} catch (TimeoutException ex) {
	    LOG.info("Background task took longer than the timeout value permitted.", ex);
	    action.cancel(true); // cancel task
	    // wait for task to finish, so the SC stack can not get confused
	    try {
		t.join();
		certResp.setResult(ChipGatewayStatusCodes.TIMEOUT);
	    } catch (InterruptedException ignore) {
		// send stop message
		certResp.setResult(ChipGatewayStatusCodes.STOPPED);
	    }
	} catch (ExecutionException ex) {
	    LOG.error("Background task produced an exception.", ex);
	    Throwable cause = ex.getCause();
	    if (cause instanceof RemotePinException) {
		LOG.error("Error getting encrypted PIN.", ex);
		certResp.setResult(ChipGatewayStatusCodes.INCORRECT_PARAMETER);
	    } else if (cause instanceof ParameterInvalid) {
		LOG.error("Error while processing the certificate filter parameters.", ex);
		certResp.setResult(ChipGatewayStatusCodes.INCORRECT_PARAMETER);
	    } else if (cause instanceof SlotHandleInvalid) {
		LOG.error("No token for the given slot handle found.", cause);
		certResp.setResult(ChipGatewayStatusCodes.UNKNOWN_SLOT);
	    } else if (cause instanceof NoSuchDid) {
		LOG.error("DID does not exist.", cause);
		certResp.setResult(ChipGatewayStatusCodes.UNKNOWN_DID);
	    } else if (cause instanceof SecurityConditionUnsatisfiable) {
		LOG.error("DID can not be authenticated.", cause);
		certResp.setResult(ChipGatewayStatusCodes.SECURITY_NOT_SATISFIED);
	    } else if (cause instanceof CertificateException) {
		LOG.error("Certificate could not be processed.", cause);
		certResp.setResult(ChipGatewayStatusCodes.OTHER);
	    } else if (cause instanceof WSHelper.WSException) {
		LOG.error("Unknown error.", cause);
		certResp.setResult(ChipGatewayStatusCodes.OTHER);
	    } else if (cause instanceof ThreadTerminateException) {
		LOG.error("Chipgateway process interrupted.", cause);
		certResp.setResult(ChipGatewayStatusCodes.STOPPED);
	    } else {
		LOG.error("Unknown error during list certificate operation.", cause);
		certResp.setResult(ChipGatewayStatusCodes.OTHER);
	    }
	} catch (InterruptedException ex) {
	    String msg = "Interrupted while waiting for background task.";
	    if (LOG.isDebugEnabled()) {
		LOG.debug(msg, ex);
	    } else {
		LOG.info(msg);
	    }
	    action.cancel(true); // cancel task
	    // send stop message
	    certResp.setResult(ChipGatewayStatusCodes.STOPPED);
	}

	return sendMessageInterruptableAndCheckTermination(getResource(listCertsUrl), certResp);
    }

    private CommandType processSignRequest(final SignRequestType signReq) throws ConnectionError,
	    JsonProcessingException, InvalidRedirectUrlException, ChipGatewayDataError {
	// check if we have been interrupted
	checkProcessCancelled();

	BigInteger waitSecondsBig = signReq.getMaxWaitSeconds();
	long waitMillis = getWaitMillis(waitSecondsBig);

	// run the actual stuff in the background, so we can wait and terminate if needed
	FutureTask<SignResponseType> action = new FutureTask<>(new Callable<SignResponseType>() {
	    @Override
	    public SignResponseType call() throws Exception {
		SignResponseType signResp = new SignResponseType();
		signResp.setSessionIdentifier(sessionId);

		byte[] slotHandle = signReq.getSlotHandle();
		String didName = signReq.getDIDName();

		char[] pin = null;
		try {
		    pin = getPin(signReq.getPIN());
		    Signer signer = new Signer(tokenCache, slotHandle, didName, pin);
		    byte[] signature = signer.sign(signReq.getMessage());

		    signResp.setSignature(signature);
		    signResp.setResult(ChipGatewayStatusCodes.OK);

		    return signResp;
		} finally {
		    if (pin != null) {
			Arrays.fill(pin, ' ');
		    }
		}
	    }
	});
	Thread t = new Thread(action, "SignRequest-Task-" + TASK_THREAD_NUM.getAndIncrement());
	t.setDaemon(true);
	t.start();

	SignResponseType signResp = new SignResponseType();
	signResp.setSessionIdentifier(sessionId);

	try {
	    // wait for thread to finish
	    signResp = action.get(waitMillis, TimeUnit.MILLISECONDS);
	} catch (TimeoutException ex) {
	    LOG.info("Background task took longer than the timeout value permitted.", ex);
	    action.cancel(true); // cancel task
	    // wait for task to finish, so the SC stack can not get confused
	    try {
		t.join();
		signResp.setResult(ChipGatewayStatusCodes.TIMEOUT);
	    } catch (InterruptedException ignore) {
		// send stop message
		signResp.setResult(ChipGatewayStatusCodes.STOPPED);
	    }
	} catch (ExecutionException ex) {
	    LOG.error("Background task produced an exception.", ex);
	    Throwable cause = ex.getCause();
	    if (cause instanceof RemotePinException) {
		LOG.error("Error getting encrypted PIN.", cause);
		signResp.setResult(ChipGatewayStatusCodes.INCORRECT_PARAMETER);
	    } else if (cause instanceof ParameterInvalid) {
		LOG.error("Error while processing the certificate filter parameters.", cause);
		signResp.setResult(ChipGatewayStatusCodes.INCORRECT_PARAMETER);
	    } else if (cause instanceof SlotHandleInvalid) {
		LOG.error("No token for the given slot handle found.", cause);
		signResp.setResult(ChipGatewayStatusCodes.UNKNOWN_SLOT);
	    } else if (cause instanceof NoSuchDid) {
		LOG.error("DID does not exist.", cause);
		signResp.setResult(ChipGatewayStatusCodes.UNKNOWN_DID);
	    } else if (cause instanceof PinBlocked) {
		LOG.error("PIN is blocked.", ex);
		signResp.setResult(ChipGatewayStatusCodes.PIN_BLOCKED);
	    } else if (cause instanceof SecurityConditionUnsatisfiable) {
		LOG.error("DID can not be authenticated.", cause);
		signResp.setResult(ChipGatewayStatusCodes.SECURITY_NOT_SATISFIED);
	    } else if (cause instanceof WSHelper.WSException) {
		LOG.error("Unknown error.", cause);
		signResp.setResult(ChipGatewayStatusCodes.OTHER);
	    } else if (cause instanceof ThreadTerminateException) {
		LOG.error("Chipgateway process interrupted.", cause);
		signResp.setResult(ChipGatewayStatusCodes.STOPPED);
	    } else {
		LOG.error("Unknown error during sign operation.", cause);
		signResp.setResult(ChipGatewayStatusCodes.OTHER);
	    }
	} catch (InterruptedException ex) {
	    String msg = "Interrupted while waiting for background task.";
	    if (LOG.isDebugEnabled()) {
		LOG.debug(msg, ex);
	    } else {
		LOG.info(msg);
	    }
	    action.cancel(true); // cancel task
	    // send stop message
	    signResp.setResult(ChipGatewayStatusCodes.STOPPED);
	}

	return sendMessageInterruptableAndCheckTermination(getResource(signUrl), signResp);
    }

    private void validateSignature(HelloResponseType helloResp) throws AuthServerException,
            InvalidRedirectUrlException {
	try {
	    byte[] challenge = helloReq.getChallenge();
	    byte[] signature = helloResp.getSignature();
	    signature = signature == null ? new byte[0] : signature; // prevent null value

	    SignatureVerifier sigVerif = new SignatureVerifier(challenge);
	    sigVerif.validate(signature);
	} catch (IOException ex) {
	    String msg = "Failed to load ChipGateway truststore from bundled truststore file.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex);
	} catch (KeyStoreException ex) {
	    String msg = "ChipGateway truststore is inoperable.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex);
	} catch (NoSuchAlgorithmException ex) {
	    String msg = "Invalid algorithm used during signature verification.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex);
	} catch (CertificateException ex) {
	    String msg = "Invalid certificate used in signature.";
	    LOG.warn(msg, ex);
	    throw new RuntimeException(msg, ex);
	} catch (SignatureInvalid ex) {
            throw new AuthServerException(token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
                    SIGNATURE_INVALID, ex);
        }
    }

    private boolean isUpdateNecessary(String minimumVersion) {
	SemanticVersion requiredVersion = new SemanticVersion(minimumVersion);
	SemanticVersion appVersion = AppVersion.getVersion();
	return appVersion.isOlder(requiredVersion);
    }

    private void showErrorMessage(String msg) {
	String title = LANG.translationForKey("error.dialog.title");
	String subMsg = LANG.translationForKey("error.dialog.submessage");
	String fullMsg = String.format("%s%n%n%s", msg, subMsg);
	gui.obtainMessageDialog().showMessageDialog(fullMsg, title, DialogType.ERROR_MESSAGE);
    }

    private void createUpdateDialog(String dlUrl, boolean updateRequired) throws ChipGatewayDataError,
	    InvalidRedirectUrlException {
	// stop here when hide dialog system property is set and the update is optional
	if (! updateRequired && ChipGatewayProperties.isHideUpdateDialog()) {
	    return;
	}

	// check that dlUrl conforms to the spec

	// only show if we have a download URL
	if (dlUrl != null && ! dlUrl.isEmpty()) {
	    try {
		URI uri = new URI(dlUrl);
		if (! "https".equalsIgnoreCase(uri.getScheme())) {
		    showErrorMessage(LANG.translationForKey("error.server_wrong_config"));
		    throw new MalformedURLException("Download URL is not an https URL.");
		}
		String dlHost = uri.getHost();
		if (ChipGatewayProperties.isUseUpdateDomainWhitelist()
			&& ! AllowedUpdateDomains.instance().isAllowedDomain(dlHost)) {
		    String msg = String.format("Update host name (%s) does not match allowed domain names.", dlHost);
		    LOG.error(msg);

		    showErrorMessage(LANG.translationForKey("error.server_wrong_config"));
		    throw new MalformedURLException(String.format("Download URL host (%s) is not in whitelist.", dlHost));
		}

		final UpdateDialog dialog = new UpdateDialog(gui, dlUrl, updateRequired);
		showDialogThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
			dialog.display();
		    }
		}, "Update-Dialog-" + TASK_THREAD_NUM.getAndIncrement());
		showDialogThread.setDaemon(true);
	    } catch (MalformedURLException | URISyntaxException ex) {
		String msg = "Received malformed download URL from server.";
		LOG.error(msg, ex);
		throw new ChipGatewayDataError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR), msg, ex);
	    }
	}
    }

    private long getWaitMillis(@Nullable BigInteger waitSecondsBig) {
	long waitMillis;
	// limit timeout to WAIT_MAX_MILLIS
	if (waitSecondsBig == null || waitSecondsBig.compareTo(BigInteger.valueOf(WAIT_MAX_MILLIS)) >= 0) {
	    waitMillis = WAIT_MAX_MILLIS;
	} else {
	    waitMillis = waitSecondsBig.longValue() * 1000;
	}
	return waitMillis;
    }

    @Nullable
    private char[] getPin(@Nullable String encryptedPin) throws RemotePinException {
	if (ChipGatewayProperties.isRemotePinAllowed() && encryptedPin != null) {
	    if (pinKey != null) {
		try {
		    // decrypt PIN
		    JsonWebEncryption jwe = new JsonWebEncryption();

		    // specify algorithmic constraints
		    AlgorithmConstraints algConstraints = new AlgorithmConstraints(
			    AlgorithmConstraints.ConstraintType.WHITELIST, KeyManagementAlgorithmIdentifiers.DIRECT);
		    jwe.setAlgorithmConstraints(algConstraints);
		    AlgorithmConstraints encConstraints = new AlgorithmConstraints(
			    AlgorithmConstraints.ConstraintType.WHITELIST,
			    ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
			    ContentEncryptionAlgorithmIdentifiers.AES_192_CBC_HMAC_SHA_384,
			    ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512,
			    ContentEncryptionAlgorithmIdentifiers.AES_128_GCM,
			    ContentEncryptionAlgorithmIdentifiers.AES_192_GCM,
			    ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
		    jwe.setContentEncryptionAlgorithmConstraints(encConstraints);

		    // perform decryption
		    jwe.setCompactSerialization(encryptedPin);
		    jwe.setKey(pinKey.getKey());
		    byte[] pinBytes = jwe.getPlaintextBytes();

		    // check if PIN is a sane value
		    char[] pin;
		    if (pinBytes == null || pinBytes.length == 0) {
			String msg = "No or empty PIN received from ChipGateway server, despite a key being present.";
			LOG.warn(msg);
			pin = null;
		    } else {
			CharBuffer charBuf = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(pinBytes));
			pin = new char[charBuf.remaining()];
			charBuf.get(pin);
			if (charBuf.hasArray()) {
			    Arrays.fill(charBuf.array(), ' ');
			}
		    }
		    return pin;
		} catch (JoseException ex) {
		    throw new RemotePinException("Error decrypting PIN.", ex);
		}
	    } else {
		// PIN sent but no key provided, raise error for the server
		throw new RemotePinException("Encrypted PIN received, but no key for decryption is available.");
	    }
	} else {
	    // no pin sent, let user supply the pin
	    return null;
	}
    }

}
