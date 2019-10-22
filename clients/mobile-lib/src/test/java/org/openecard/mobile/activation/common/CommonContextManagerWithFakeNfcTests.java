/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import java.security.Provider;
import java.security.Security;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.MockitoSession;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.openecard.common.ifd.scio.TerminalWatcher.StateChangeEvent;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.model.DelegatingMobileNfcTerminalFactory;
import org.openecard.mobile.activation.model.NfcConfig;
import org.openecard.mobile.activation.model.OpeneCardContextConfigFactory;
import org.openecard.mobile.activation.model.PromiseDeliveringFactory;
import org.openecard.mobile.activation.model.Timeout;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.scio.NFCCardTerminal;
import org.openecard.scio.NFCCardTerminals;
import org.openecard.scio.NFCCardWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManagerWithFakeNfcTests {

    private static final Logger LOG = LoggerFactory.getLogger(CommonContextManagerWithFakeNfcTests.class);

    private static final int WAIT_TIMEOUT = Timeout.WAIT_TIMEOUT;

    private static final String DUMMY_MOBILE_TERMINAL = "DUMMY MOBILE TERMINAL";

    @BeforeClass
    void initEnvironment() {
	// HACK: too much parallel class loading will cause deadlocks! https://github.com/spring-projects/spring-boot/issues/16744
	boolean requireHack = false;
	if (requireHack) {
	    Provider provider = new BouncyCastleProvider();
	    Security.addProvider(provider);
	    this.providerName = provider.getName();
	}
    }

    @AfterClass()
    void removeEnvironment() {
	if (providerName != null) {
	    Security.removeProvider(providerName);
	}
    }

    MockitoSession mockito;

    @BeforeMethod()
    void setup() {
	mockito = Mockito.mockitoSession()
		.initMocks(this)
		.strictness(Strictness.LENIENT)
		.startMocking();

	configFactory = OpeneCardContextConfigFactory.mobile(mockTerminalFactory);

	nfcTerminal = new NFCCardTerminal();
	nfcTerminals = new NFCCardTerminals(nfcTerminal);
	nfcTerminalWatcher = new NFCCardWatcher(nfcTerminals, nfcTerminal);
    }

    @AfterMethod()
    void teardown() {
	try {
	    mockito.finishMocking();

	} catch (Exception e) {
	    LOG.warn("Error occured during cleanup.", e);
	}
	DelegatingMobileNfcTerminalFactory.setDelegate(null);
    }

    @Mock
    NFCCapabilities mockNfc;

    @Mock
    ActivationSource source;

    @Mock
    TerminalFactory mockTerminalFactory;

    OpeneCardContextConfigFactory configFactory;

    NFCCardTerminal nfcTerminal;

    NFCCardTerminals nfcTerminals;

    NFCCardWatcher nfcTerminalWatcher;

    private String providerName;

    CommonContextManager createSut() {
	return createSut(configFactory);
    }

    CommonContextManager createSut(OpeneCardContextConfigFactory factory) {
	return createSut(mockNfc, factory.create(), source);
    }

    CommonContextManager createSut(NFCCapabilities nfc, OpeneCardContextConfig config, ActivationSource source) {
	return new CommonContextManager(nfc, config, source);
    }

    @Test
    void sutStartsCorrectly() throws InterruptedException, TimeoutException, Exception {
	withNfcSupport(NfcConfig.create());
	withTerminalSupport();

	Promise<ActivationSource> result = new Promise();

	CommonContextManager sut = this.createSut();

	sut.start(PromiseDeliveringFactory.createStartServiceDelivery(result, null));

	Assert.assertNotNull(result.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    void sutStartsThreaded() throws InterruptedException, SCIOException, TimeoutException, Exception {
	withNfcSupport(NfcConfig.create());
	withTerminalSupport();

	Promise<ActivationSource> result = new Promise();

	Object lock = new Object();
	StateChangeEvent[] events = new StateChangeEvent[]{
	    new StateChangeEvent(TerminalWatcher.EventType.TERMINAL_ADDED, DUMMY_MOBILE_TERMINAL),
	    new StateChangeEvent()
	};
	Promise<Boolean> delivered = new Promise();
	Answer<StateChangeEvent> answerWaitForChange = new Answer<StateChangeEvent>() {

	    private int eventIndex = 0;
	    private static final long EVENT_DELAY = 100;

	    @Override
	    public StateChangeEvent answer(InvocationOnMock invocation) throws Throwable {
		Object[] args = invocation.getArguments();
		long time;
		if (args.length == 0) {
		    time = EVENT_DELAY;
		} else {
		    time = Math.min(EVENT_DELAY, invocation.getArgument(0, Long.class));
		}
		Thread.sleep(time);

		synchronized (lock) {
		    lock.notifyAll();
		    eventIndex = Math.min(eventIndex + 1, events.length - 1);

		    if (eventIndex == events.length - 1 && !delivered.isDelivered()) {
			delivered.deliver(Boolean.TRUE);
		    }

		    return events[eventIndex];
		}
	    }
	};
	// doAnswer(answerWaitForChange).when(nfcTerminalWatcher).waitForChange();
	// doAnswer(answerWaitForChange).when(nfcTerminalWatcher).waitForChange(anyLong());

	CommonContextManager sut = this.createSut();

	synchronized (lock) {
	    sut.start(PromiseDeliveringFactory.createStartServiceDelivery(result, null));
	    lock.wait(WAIT_TIMEOUT);
	    lock.wait(WAIT_TIMEOUT);
	}

	Assert.assertNotNull(result.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));

    }

    @Test
    void sutStartStopCorrectly() throws InterruptedException, TimeoutException, Exception {
	Promise<ActivationSource> startResult = new Promise();
	Promise<ServiceErrorResponse> stopResult = new Promise();
	withNfcSupport(NfcConfig.create());
	withTerminalSupport();

	CommonContextManager sut = this.createSut();

	sut.start(PromiseDeliveringFactory.createStartServiceDelivery(startResult, null));

	Assert.assertNotNull(startResult.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));

	sut.stop(PromiseDeliveringFactory.createStopServiceDelivery(stopResult));

	Assert.assertNull(stopResult.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test()
    void sutCannotStartWithoutNfc() throws InterruptedException, TimeoutException, Exception {
	when(this.mockNfc.isAvailable()).thenReturn(Boolean.FALSE);
	withNfcSupport(NfcConfig.createUnavailable());

	when(this.mockNfc.isAvailable()).thenReturn(Boolean.FALSE);
	Object lock = new Object();

	CommonContextManager sut = this.createSut();
	Promise<ServiceErrorResponse> result = new Promise();

	synchronized (lock) {
	    sut.start(PromiseDeliveringFactory.createStartServiceDelivery(null, result));
	}
	Assert.assertNotNull(result.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS), "To be null, the start process must have unexpectedly succeeded!");
    }

    private void withNfcSupport(NfcConfig nfcConfig) throws Exception {
	when(this.mockNfc.isAvailable()).thenReturn(nfcConfig.isAvailable);
	when(this.mockNfc.isEnabled()).thenReturn(nfcConfig.isEnabled);
	when(this.mockNfc.checkExtendedLength()).thenReturn(nfcConfig.checkExtendedLength);
    }

    private void withTerminalSupport() throws SCIOException, NoSuchTerminal {
	when(this.mockTerminalFactory.terminals()).thenReturn(nfcTerminals);
    }

}
