/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.mockito.Mockito.mock;
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
    public final ContextWorld contextWorld;
    public final PinManagementWorld pinManagementWorld;

    public World(CommonActivationUtils activationUtils, MockNFCCapabilitiesConfigurator capabilities, MockMobileTerminalConfigurator terminalConfigurator) {
	this.activationUtils = activationUtils;
	this.capabilities = capabilities;
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
	    interaction = mock(PinManagementInteraction.class);
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

	public void cancelPinManagement() {
	    LOG.debug("Cancel pin management.");
	    this.activationController.cancelAuthentication();
	}

	@Override
	public void close() throws Exception {
	    if (promisedActivationResult != null) {
		if (!promisedActivationResult.isCancelled() && !promisedActivationResult.isDelivered()) {
		    promisedActivationResult.cancel();
		}
		promisedActivationResult = null;
	    }

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
		} catch(Exception | AssertionError ex) {
		    // Suppress all exceptions.
		}
	    }
	}

    }
}
