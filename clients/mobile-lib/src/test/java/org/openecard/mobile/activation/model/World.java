/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.mobile.activation.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import static org.openecard.mobile.activation.model.Timeout.MIN_WAIT_TIMEOUT;
import static org.openecard.mobile.activation.model.Timeout.WAIT_TIMEOUT;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.scio.AbstractNFCCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 *
 * @author Neil Crossley
 */
public class World implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private final CommonActivationUtils activationUtils;
    private final MockNFCCapabilitiesConfigurator capabilities;
    private final MobileTerminalConfigurator terminalConfigurator;
    public final ContextWorld contextWorld;
    public final PinManagementWorld pinManagementWorld;
    private AbstractNFCCard currentNfcCard;

    public World(CommonActivationUtils activationUtils, MockNFCCapabilitiesConfigurator capabilities, MobileTerminalConfigurator terminalConfigurator) {
	this.activationUtils = activationUtils;
	this.capabilities = capabilities;
	this.terminalConfigurator = terminalConfigurator;
	this.contextWorld = new ContextWorld();
	this.pinManagementWorld = new PinManagementWorld();
    }

    public void microSleep() {
	try {
	    LOG.debug("Sleeping.");
	    Thread.sleep(MIN_WAIT_TIMEOUT);
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void givenNpaCardInserted() {
	LOG.debug("NPA card inserted.");
	AbstractNFCCard spyCard = mock(AbstractNFCCard.class, withSettings()
		.useConstructor(this.terminalConfigurator.terminal).defaultAnswer(CALLS_REAL_METHODS));

	this.currentNfcCard = spyCard;
	doReturn(true).when(currentNfcCard).isCardPresent();
	doReturn(new SCIOATR(new byte[]{59, -118, -128, 1, -128, 49, -72, 115, -124, 1, -32, -126, -112, 0, 6})).when(currentNfcCard).getATR();
	try {
	    doReturn(new byte[]{59, -118, -128, 1, -128, 49, -72, 115, -124, 1, -32, -126, -112, 0, 6}).when(currentNfcCard).transceive(new byte[]{0, -92, 0, 12, 2, 63, 0});
	    doReturn(new byte[]{-112, 0}).when(currentNfcCard).transceive(new byte[]{0, -92, 0, 12, 2, 63, 0});
	    doReturn(new byte[]{106, -126}).when(currentNfcCard).transceive(new byte[]{0, -92, 2, 12, 2, 0, 3});
	    doReturn(new byte[]{-112, 0}).when(currentNfcCard).transceive(new byte[]{0, -92, 2, 12, 2, 47, 0});
	    doReturn(new byte[]{109, 0}).when(currentNfcCard).transceive(new byte[]{0, -78, 4, 4, -1});

	    doReturn(new byte[]{109, 0}).when(currentNfcCard).transceive(new byte[]{0, -78, 3, 4, -1});
	    doReturn(new byte[]{106, -126}).when(currentNfcCard).transceive(new byte[]{0, -92, 4, 12, 15, -16, 69, 115, 116, 69, 73, 68, 32, 118, 101, 114, 32, 49, 46, 48});
	    doReturn(new byte[]{106, -126}).when(currentNfcCard).transceive(new byte[]{0, -92, 4, 12, 15, -46, 51, 0, 0, 0, 69, 115, 116, 69, 73, 68, 32, 118, 51, 53});
	    doReturn(new byte[]{106, -126}).when(currentNfcCard).transceive(new byte[]{0, -92, 2, 12, 2, 0, 3});
	    doReturn(new byte[]{97, 50, 79, 15, -24, 40, -67, 8, 15, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 80, 15, 67, 73, 65, 32, 122, 117, 32, 68, 70, 46, 101, 83, 105, 103, 110, 81, 0, 115, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 97, 9, 79, 7, -96, 0, 0, 2, 71, 16, 1, 97, 11, 79, 9, -24, 7, 4, 0, 127, 0, 7, 3, 2, 97, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 98, -126}).when(currentNfcCard).transceive(new byte[]{0, -80, 0, 0, -1});

	    doReturn(new byte[]{-112, 0}).when(currentNfcCard).transceive(new byte[]{0, 34, -63, -92, 15, -128, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 2, -125, 1, 3});
//	    doReturn(new byte[]{}).when(currentNfcCard).transceive(new byte[]{});

	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}

	this.terminalConfigurator.terminal.setNFCCard(currentNfcCard);
    }

    @Override
    public void close() throws Exception {
	LOG.debug("Closing.");
	this.pinManagementWorld.close();
	this.contextWorld.close();
    }

    public class PinManagementWorld implements AutoCloseable {

	private PinManagementControllerFactory _pinManagementFactory;
	private Set<String> supportedCards;
	private Promise<ActivationResult> promisedActivationResult;
	private Promise<Void> promisedStarted;
	private Promise<Void> promisedRequestCardInsertion;
	private Promise<String> promisedRecognizeCard;
	private PinManagementInteraction interaction;
	private ActivationController activationController;

	private PinManagementControllerFactory pinManagementFactory() {
	    if (_pinManagementFactory == null) {
		_pinManagementFactory = activationUtils.pinManagementFactory();
	    }
	    return _pinManagementFactory;
	}

	public void startSimplePinManagement() {
	    LOG.debug("Start simple pin management.");
	    supportedCards = new HashSet<>();
	    promisedActivationResult = new Promise<>();
	    promisedStarted = new Promise<>();
	    promisedRequestCardInsertion = new Promise();
	    promisedRecognizeCard = new Promise();
	    interaction = mock(PinManagementInteraction.class);
	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRequestCardInsertion.isDelivered()) {
		    promisedRequestCardInsertion = new Promise();
		}
		promisedRequestCardInsertion.deliver(null);
		return null;
	    }).when(interaction).requestCardInsertion();
	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRecognizeCard.isDelivered()) {
		    promisedRecognizeCard = new Promise();
		}
		promisedRecognizeCard.deliver((String) arg0.getArguments()[0]);
		return null;
	    }).when(interaction).onCardRecognized(anyString());
	    activationController = pinManagementFactory().create(
		    supportedCards,
		    PromiseDeliveringFactory.controllerCallback.deliverStartedCompletion(promisedStarted, promisedActivationResult),
		    interaction);
	    activationController.start();
	}

	public void expectActivationResult(ActivationResultCode code) {
	    LOG.debug("Expect activation result {}.", code);
	    try {
		ActivationResult result = promisedActivationResult.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
		Assert.assertEquals(result.getResultCode(), code);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectOnStarted() {
	    LOG.debug("Expect on started.");
	    try {
		promisedStarted.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectCardInsertionRequest() {
	    LOG.debug("Expect on started.");
	    try {
		promisedRequestCardInsertion.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectRecognitionOfNpaCard() {
	    LOG.debug("Expect on started.");
	    try {
		String type = promisedRecognizeCard.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
		Assert.assertEquals(type, "http://bsi.bund.de/cif/npa.xml");
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void cancelPinManagement() {
	    LOG.debug("Cancel pin management.");
	    this.activationController.cancelAuthentication();
	}

	@Override
	public void close() throws Exception {
	    releasePromise(promisedActivationResult);
	    promisedActivationResult = null;
	    releasePromise(promisedStarted);
	    promisedStarted = null;

	    ActivationController oldActivationController = activationController;
	    if (oldActivationController != null) {
		oldActivationController.cancelAuthentication();
		activationController = null;
	    }
	    if (this._pinManagementFactory != null && oldActivationController != null) {
		this._pinManagementFactory.destroy(activationController);
	    }
	}

    }

    public class ContextWorld implements AutoCloseable {

	private ContextManager _contextManager;

	private ContextManager contextManager() {
	    if (_contextManager == null) {
		_contextManager = activationUtils.context(capabilities.build());
	    }
	    return _contextManager;
	}

	public ContextWorld startSuccessfully() {
	    LOG.debug("Start successfully.");
	    Promise<ServiceErrorResponse> resultStart = new Promise<>();
	    try {
		contextManager().start(PromiseDeliveringFactory.createContextServiceDelivery(resultStart));
	    } catch (UnableToInitialize | NfcUnavailable | NfcDisabled | ApduExtLengthNotSupported ex) {
		throw new RuntimeException(ex);
	    }

	    try {
		Assert.assertNull(resultStart.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	    return this;
	}

	public ContextWorld stopSuccessfully() {
	    LOG.debug("Stop successfully.");
	    Promise<ServiceErrorResponse> resultStart = new Promise<>();
	    contextManager().stop(PromiseDeliveringFactory.createContextServiceDelivery(resultStart));

	    try {
		Assert.assertNull(resultStart.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	    return this;
	}

	@Override
	public void close() throws Exception {
	    if (_contextManager != null) {
		try {
		    stopSuccessfully();
		} catch (Exception | AssertionError ex) {
		    // Suppress all exceptions.
		}
	    }
	}

    }

    private static <T> void releasePromise(Promise<T> promise) {
	if (promise != null) {
	    if (!promise.isCancelled() && !promise.isDelivered()) {
		promise.cancel();
	    }
	}
    }
}
