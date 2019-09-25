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
	doReturn(new SCIOATR(new byte[0])).when(currentNfcCard).getATR();

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
	    interaction = mock(PinManagementInteraction.class);
	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRequestCardInsertion.isDelivered()) {
		    promisedRequestCardInsertion = new Promise();
		}
		promisedRequestCardInsertion.deliver(null);
		return null;
	    }).when(interaction).requestCardInsertion();
	    activationController = pinManagementFactory().create(
		    supportedCards,
		    PromiseDeliveringFactory.controllerCallback.deliverStartedCompletion(promisedStarted, promisedActivationResult), interaction);
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
