/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
package org.openecard.mobile.activation.common;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.MockitoSession;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonRegistry;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.model.ActivationUrlFactory;
import org.openecard.mobile.activation.model.PromiseDeliveringFactory;
import org.openecard.mobile.activation.model.Timeout;
import org.openecard.mobile.system.OpeneCardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class ActivationControllerServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationControllerServiceTests.class);

    private static final int SCALING_FACTOR = 15;
    private static final int SLEEP_WAIT = Timeout.MIN_WAIT_TIMEOUT / 2 / SCALING_FACTOR;
    private static final int MICRO_WAIT = SLEEP_WAIT / 2;
    private static final int WAIT_TIMEOUT = Timeout.WAIT_TIMEOUT / SCALING_FACTOR;

    MockitoSession mockito;
    private boolean hasSlowEacStack;

    @BeforeMethod()
    void setup() {
	mockito = Mockito.mockitoSession()
		.initMocks(this)
		.strictness(Strictness.LENIENT)
		.startMocking();
	hasSlowEacStack = false;
    }

    @AfterMethod()
    void teardown() {
	try {
	    mockito.finishMocking();

	} catch (Exception e) {
	    LOG.warn("Error occured during cleanup.", e);
	}
    }

    @Mock
    OpeneCardContextProvider mockOpeneCardContextProvider;

    @Mock
    OpeneCardContext mockContext;

    @Mock
    AddonManager mockAddonManager;

    @Mock
    AddonRegistry mockAddonRegistry;

    @Mock
    AppPluginAction mockPluginAction;

    @Mock
    EventDispatcher mockEventDispatcher;

    @Test
    void canCreateSut() {
	this.createSut();
    }

    @Test
    void sutCanCompleteActivation() throws InterruptedException, TimeoutException {
	Promise<ActivationResult> outcome = new Promise();
	ControllerCallback mockControllerCallback = PromiseDeliveringFactory.controllerCallback.deliverCompletion(outcome);
	ActivationControllerService sut = this.withMinimumAddons("eID-Client").withSuccessActivation().createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(),
		mockControllerCallback,
		anyInteractionPreperationFactory());

	ActivationResult result = outcome.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	Assert.assertNotNull(result);
	Assert.assertEquals(result.getResultCode(), ActivationResultCode.OK);
    }

    @Test
    void sutNotifiesWhenStarted() throws InterruptedException, TimeoutException {
	Promise<Void> outcome = new Promise();
	ControllerCallback mockControllerCallback = PromiseDeliveringFactory.controllerCallback.deliverStarted(outcome);
	ActivationControllerService sut = this.withMinimumAddons("eID-Client").withSuccessActivation().createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(),
		mockControllerCallback,
		anyInteractionPreperationFactory());

	outcome.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    void sutDoesNotNotifyStartedWhenCancelled() throws InterruptedException, TimeoutException {
	Promise<Void> outcome = new Promise();
	ControllerCallback mockControllerCallback = PromiseDeliveringFactory.controllerCallback.deliverStarted(outcome);
	ActivationControllerService sut = this
		.withSlowEacStack()
		.withMinimumAddons("eID-Client")
		.withSuccessActivation().createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(),
		mockControllerCallback,
		anyInteractionPreperationFactory());
	sut.cancelAuthentication(mockControllerCallback);

	Assert.assertThrows(() -> outcome.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void sutDoesNotNotifyStartedAfterDelayedCancel() throws InterruptedException, TimeoutException {
	Promise<Void> outcome = new Promise();
	ControllerCallback mockControllerCallback = PromiseDeliveringFactory.controllerCallback.deliverStarted(outcome);
	ActivationControllerService sut = this
		.withSlowEacStack()
		.withMinimumAddons("eID-Client")
		.withSuccessActivation(SLEEP_WAIT).createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(),
		mockControllerCallback,
		anyInteractionPreperationFactory());
	Thread.sleep(SLEEP_WAIT / 2);
	sut.cancelAuthentication(mockControllerCallback);

	Assert.assertThrows(() -> outcome.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void sutCancelsActivationOnlyOnce() throws InterruptedException, TimeoutException {
	ControllerCallback mockControllerCallback = mock(ControllerCallback.class);

	ActivationControllerService sut = this
		.withMinimumAddons("eID-Client")
		.withSuccessActivation(SLEEP_WAIT)
		.createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(),
		mockControllerCallback,
		anyInteractionPreperationFactory());
	Thread.sleep(MICRO_WAIT);
	sut.cancelAuthentication(mockControllerCallback);
	Thread.sleep(MICRO_WAIT);
	sut.cancelAuthentication(mockControllerCallback);
	Thread.sleep(MICRO_WAIT);
	sut.cancelAuthentication(mockControllerCallback);

	verify(mockControllerCallback, times(1)).onStarted();
	verify(mockControllerCallback).onAuthenticationCompletion(argThat(r -> {
	    return r.getResultCode() == ActivationResultCode.INTERRUPTED;
	}));
	verify(mockControllerCallback, times(1)).onAuthenticationCompletion(any());
    }

    public ActivationControllerService createSut() {
	return new ActivationControllerService(mockOpeneCardContextProvider);
    }

    public ActivationControllerServiceTests withSlowEacStack() {
	this.hasSlowEacStack = true;
	return this;
    }

    public ActivationControllerServiceTests withMinimumAddons(String addonName) {
	AddonSpecification mockAddon = mock(AddonSpecification.class);
	when(mockAddon.getVersion()).thenReturn("1.1.1");
	Set<AddonSpecification> addons = new HashSet<AddonSpecification>() {
	    {
		add(mockAddon);
	    }
	};
	return this.withMockContext()
		.withMockAddonManager()
		.withAddonRegistry()
		.withEventDispatcher()
		.withAddons(addonName, addons)
		.withAppPluginAction(mockAddon, addonName);
    }

    public ActivationControllerServiceTests withMockContext() {
	when(this.mockOpeneCardContextProvider.getContext()).thenReturn(mockContext);
	return this;
    }

    public ActivationControllerServiceTests withMockAddonManager() {
	when(this.mockContext.getManager()).thenReturn(mockAddonManager);
	return this;
    }

    private ActivationControllerServiceTests withAddonRegistry() {
	when(this.mockAddonManager.getRegistry()).thenReturn(this.mockAddonRegistry);
	return this;
    }

    private ActivationControllerServiceTests withEventDispatcher() {
	when(this.mockContext.getEventDispatcher()).thenReturn(this.mockEventDispatcher);
	return this;
    }

    private ActivationControllerServiceTests withAddons(String name, Set<AddonSpecification> addons) {
	withValue(when(this.mockAddonRegistry.searchByResourceName(name)), addons);
	return this;
    }

    private ActivationControllerServiceTests withAppPluginAction(@Nonnull AddonSpecification addonSpec, @Nonnull String resourceName) {
	withValue(when(this.mockAddonManager.getAppPluginAction(addonSpec, resourceName)), mockPluginAction);
	return this;
    }

    private <T> OngoingStubbing<T> withValue(OngoingStubbing<T> stubbing, T value) {
	if (this.hasSlowEacStack) {
	    return stubbing.thenAnswer(new Answer<T>() {
		@Override
		public T answer(InvocationOnMock arg0) throws Throwable {
		    Thread.sleep(SLEEP_WAIT);
		    return value;
		}
	    });
	} else {
	    return stubbing.thenReturn(value);
	}
    }

    private ActivationControllerServiceTests withActivation(BindingResult result, int sleepDelay) {
	when(this.mockPluginAction.execute(any(), any(), any(), any())).thenAnswer(new Answer<BindingResult>() {
	    @Override
	    public BindingResult answer(InvocationOnMock arg0) throws Throwable {
		Thread.sleep(sleepDelay);

		return result;
	    }
	});
	return this;
    }

    private ActivationControllerServiceTests withSuccessActivation() {
	return this.withSuccessActivation(MICRO_WAIT);
    }

    private ActivationControllerServiceTests withSuccessActivation(int sleepDelay) {
	return this.withActivation(new BindingResult(), sleepDelay);
    }

    private InteractionPreperationFactory anyInteractionPreperationFactory() {
	return mock(InteractionPreperationFactory.class);
    }

}
