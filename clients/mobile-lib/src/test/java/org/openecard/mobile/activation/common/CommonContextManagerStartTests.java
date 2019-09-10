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

import java.security.Provider;
import java.security.Security;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.activation.fakes.FakeMobileNfcTerminalFactory;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManagerStartTests {

    @Injectable
    NFCCapabilities mockNfc;

    @Injectable()
    OpeneCardContextConfig config;

    @Tested
    CommonContextManager sut;
    @Mocked
    OpeneCardServiceHandler handler;
    @Mocked
    FakeMobileNfcTerminalFactory mockTerminalFactory;
    @Mocked
    SCIOTerminals mockSCIOTerminals;
    @Mocked
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

    @Test
    void sutStartsCorrectly() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported, SCIOException {

	new Expectations() {
	    {
		config.getIfdFactoryClass();
		result = "org.openecard.mobile.activation.fakes.FakeMobileNfcTerminalFactory";
		config.getWsdefMarshallerClass();
		result = "org.openecard.ws.jaxb.JAXBMarshaller";
		mockNfc.isAvailable();
		result = true;
		mockNfc.isEnabled();
		result = true;
		mockNfc.checkExtendedLength();
		result = NfcCapabilityResult.SUPPORTED;
		mockTerminalFactory.terminals();
		result = mockSCIOTerminals;

		handler.onSuccess();
	    }
	};

	sut.start(handler);
	new Verifications() {
	    {
		handler.onSuccess();
	    }
	};
    }

}
