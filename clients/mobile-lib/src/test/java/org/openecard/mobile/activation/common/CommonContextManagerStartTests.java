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

import java.lang.management.ManagementFactory;
import java.security.Provider;
import java.security.Security;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mockito.Mock;
 import org.mockito.Mockito;
 import static org.mockito.Mockito.*;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.openecard.common.ifd.scio.TerminalWatcher.StateChangeEvent;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.activation.fakes.FakeMobileNfcTerminalFactory;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.ws.jaxb.JAXBMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManagerStartTests {

    private static Logger log = LoggerFactory.getLogger(CommonContextManagerStartTests.class);

    private static final int WAIT_TIMEOUT = isDebug() ? 999000 : 1000;

    @Mock
    NFCCapabilities mockNfc;

    @Mock
    OpeneCardContextConfig config;

    @Mock
    OpeneCardServiceHandler handler;

    MockitoSession mockito;

    @BeforeMethod()
    void setup() {
	mockito = Mockito.mockitoSession()
		.initMocks(this)
		.strictness(Strictness.STRICT_STUBS)
		.startMocking();

    }

    @AfterMethod()
    void teardown() {
	mockito.finishMocking();
    }

    @Mocked
    FakeMobileNfcTerminalFactory mockTerminalFactory;

    @Mock
    SCIOTerminals mockSCIOTerminals;

    @Mock
    TerminalWatcher mockTerminalWatcher;

    private String providerName;

    @BeforeClass
    void initEnvironment() {
	// HACK: too much parallel class loading will cause deadlocks! https://github.com/spring-projects/spring-boot/issues/16744
	Provider provider = new BouncyCastleProvider();
	Security.addProvider(provider);
	this.providerName = provider.getName();
    }

    @AfterClass()
    void removeEnvironment() {
	if (providerName != null) {
	    Security.removeProvider(providerName);
	}
    }

    CommonContextManager createSut() {
	return new CommonContextManager(mockNfc, config);
    }

    @Test
    void sutStartsCorrectly() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported, SCIOException {
	initializationExpectations();

	CommonContextManager sut = this.createSut();
	sut.start(startHandler);
	
	new Verifications() {
	    {
		handler.onSuccess();
	    }
	};
    }

    @Test
    void sutStartsThreaded() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported, SCIOException, InterruptedException {

	Object lock = new Object();
	StateChangeEvent[] events = new StateChangeEvent[]{
	    new StateChangeEvent(TerminalWatcher.EventType.TERMINAL_ADDED, "FakeTerminal"),
	    new StateChangeEvent()
	};

	initialExpectations();

	new MockUp<TerminalWatcher>() {

	    private int eventIndex = 0;

	    @Mock
	    public SCIOTerminals getTerminals() {
		return mockSCIOTerminals;
	    }

	    @Mock
	    public StateChangeEvent waitForChange() throws InterruptedException {
		log.debug("XXXXXX - also here");

		Thread.sleep(100);

		log.debug("XXXXXX - here");

		synchronized (lock) {
		    lock.notifyAll();

		    return events[eventIndex++];
		}
	    }
	    @Mock
	    public StateChangeEvent waitForChange(int delay) throws InterruptedException {
		log.debug("XXXXXX - also here");

		Thread.sleep(delay);

		log.debug("XXXXXX - here");

		synchronized (lock) {
		    lock.notifyAll();

		    return events[eventIndex++];
		}
	    }

	};

	synchronized (lock) {
	    log.debug("XXXXXX - starting");
	    sut.start(handler);

	    log.debug("XXXXXX - waiting");
	    lock.wait(WAIT_TIMEOUT);
	}

	new Verifications() {
	    {
		handler.onSuccess();
	    }
	};
    }

    @Test
    void sutStartStopCorrectly() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported, SCIOException {

	initialExpectations();

	sut.start(handler);
	sut.stop(handler);
	new Verifications() {
	    {
		handler.onSuccess();
		handler.onSuccess();
	    }
	};
    }

 
	    
	
    

    @Test
    void sutStartsStopsCorrectly() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported, SCIOException {
	initializationExpectations();
	CommonContextManager sut = this.createSut();

	new Expectations() {
	    {
		stopHandler.onSuccess();
	    }
	};
    }

    static boolean isDebug() {
	for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
	    if (arg.contains("jdwp=")) {
		return true;
	    }
	};
    }

    @Test(expectedExceptions = NfcUnavailable.class)
    void sutCannotStartWithoutNfc() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported, SCIOException {
	initializationExpectations();
	CommonContextManager sut = this.createSut();

	sut.start(startHandler);
    }

    private void initializationExpectations() {
	when(this.config.getIfdFactoryClass()).thenReturn("org.openecard.mobile.activation.fakes.FakeMobileNfcTerminalFactory");
	when(this.config.getWsdefMarshallerClass()).thenReturn("org.openecard.ws.jaxb.JAXBMarshaller");

	when(this.mockNfc.isAvailable()).thenReturn(Boolean.TRUE);
	when(this.mockNfc.isEnabled()).thenReturn(Boolean.TRUE);
	when(this.mockNfc.checkExtendedLength()).thenReturn(NfcCapabilityResult.SUPPORTED);

	new Expectations() {
	    {
		mockTerminalFactory.terminals();
		result = mockSCIOTerminals;
	    }
	}
	return false;
    }
}
