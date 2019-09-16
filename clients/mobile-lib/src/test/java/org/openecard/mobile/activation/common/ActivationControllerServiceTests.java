/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
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
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonRegistry;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.manifest.AddonSpecification;
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

    private static final int WAIT_TIMEOUT = Timeout.WAIT_TIMEOUT;

    MockitoSession mockito;

    @BeforeMethod()
    void setup() {
	mockito = Mockito.mockitoSession()
		.initMocks(this)
		.strictness(Strictness.LENIENT)
		.startMocking();

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

    @Test
    void canCreateSut() {
	this.createSut();
    }

    @Test
    void sutCanCompleteActivation() throws InterruptedException, TimeoutException {
	Promise<ActivationResult> outcome = new Promise();
	ControllerCallback mockCallback = PromiseDeliveringFactory.createControllerCallbackDelivery(outcome);

	ActivationControllerService sut = this.withMinimumAddons("eID-Client").withSuccessActivation(WAIT_TIMEOUT / 10).createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(), mockCallback);

	ActivationResult result = outcome.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	Assert.assertNotNull(result);
	Assert.assertEquals(result.getResultCode(), ActivationResultCode.OK);
    }

    @Test
    void canCancelSut() throws InterruptedException, TimeoutException {
	Promise<ActivationResult> outcome = new Promise();
	ControllerCallback mockCallback = PromiseDeliveringFactory.createControllerCallbackDelivery(outcome);
	ActivationControllerService sut = this.withMinimumAddons("eID-Client").withSuccessActivation(WAIT_TIMEOUT / 2).createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(), mockCallback);

	Thread.sleep(WAIT_TIMEOUT / 10);

	sut.cancelAuthentication(mockCallback);

	ActivationResult result = outcome.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	Assert.assertNotNull(result);
	Assert.assertEquals(result.getResultCode(), ActivationResultCode.INTERRUPTED);
    }

    @Test
    void sutCancelsActivationOnlyOnce() throws InterruptedException, TimeoutException {
	ControllerCallback mockCallback = mock(ControllerCallback.class);
	ActivationControllerService sut = this.withMinimumAddons("eID-Client").withSuccessActivation(WAIT_TIMEOUT / 2).createSut();

	sut.start(ActivationUrlFactory.fromResource("eID-Client").create(), mockCallback);

	Thread.sleep(WAIT_TIMEOUT / 10);
	sut.cancelAuthentication(mockCallback);
	Thread.sleep(WAIT_TIMEOUT / 10);
	sut.cancelAuthentication(mockCallback);
	Thread.sleep(WAIT_TIMEOUT / 10);
	sut.cancelAuthentication(mockCallback);

	verify(mockCallback).onAuthenticationCompletion(argThat(r -> r.getResultCode() == ActivationResultCode.INTERRUPTED));
    }

    public ActivationControllerService createSut() {
	return new ActivationControllerService(mockOpeneCardContextProvider);
    }

    public ActivationControllerServiceTests withMinimumAddons(String addonName) {
	AddonSpecification mockAddon = mock(AddonSpecification.class);
	when(mockAddon.getVersion()).thenReturn("1.1.1");
	Set<AddonSpecification> addons = new HashSet<AddonSpecification>() {
	    {
		add(mockAddon);
	    }
	};
	return this.withMockContext().withMockAddonManager().withAddonRegistry().withAddons(addonName, addons).withAppPluginAction(mockAddon, addonName);
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

    private ActivationControllerServiceTests withAddons(String name, Set<AddonSpecification> addons) {
	when(this.mockAddonRegistry.searchByResourceName(name)).thenReturn(addons);
	return this;
    }

    private ActivationControllerServiceTests withAppPluginAction(@Nonnull AddonSpecification addonSpec, @Nonnull String resourceName) {
	when(this.mockAddonManager.getAppPluginAction(addonSpec, resourceName)).thenReturn(mockPluginAction);
	return this;
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

    private ActivationControllerServiceTests withSuccessActivation(int sleepDelay) {
	return this.withActivation(new BindingResult(), sleepDelay);
    }
}
