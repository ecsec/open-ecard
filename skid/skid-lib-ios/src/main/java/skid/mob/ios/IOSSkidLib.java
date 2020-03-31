/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.ios;

import java.security.Provider;
import java.security.Security;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.common.util.SysUtils;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ActivationUtils;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.scio.CachingTerminalFactoryBuilder;
import org.openecard.scio.IOSNFCFactory;
import skid.mob.impl.SamlClientImpl;
import skid.mob.lib.SamlClient;
import skid.mob.lib.SkidLib;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.openecard.ios.activation.DeveloperOptions;
import org.openecard.ios.activation.NFCConfig;
import org.openecard.robovm.annotations.FrameworkObject;
import org.slf4j.LoggerFactory;
import skid.mob.lib.AuthModuleCallbackBuilder;
import skid.mob.lib.SkidResult;


/**
 *
 * @author Florian Otto
 */
@FrameworkObject(factoryMethod = "createSkidLib")
public class IOSSkidLib implements SkidLib {

    static {
	SysUtils.setIsIOS();

	Provider provider = new BouncyCastleProvider();
	try {
	    Security.removeProvider(provider.getName());
	    Security.removeProvider("BC");
	} catch (Exception e) {
	}
	Security.addProvider(provider);

	LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
	rootLogger.setLevel(Level.ERROR);
    }

    private /* final */ ActivationUtils utils;
    private /* final */ CachingTerminalFactoryBuilder<IOSNFCFactory> builder;

    private ContextManager oecCtx;
    private ActivationSource oecActivationSource;

    private /*final*/ DeveloperOptions developerOptions;
    private NFCConfig nfcConfig;

    public IOSSkidLib() {

    }

    IOSSkidLib(CommonActivationUtils utils, CachingTerminalFactoryBuilder<IOSNFCFactory> builder) {
	this.utils = utils;
	this.builder = builder;
    }
//in constructor cause create method gets done via roboface

//    public static IOSSkidLib createSkidLib() {
//	CachingTerminalFactoryBuilder<IOSNFCFactory> factory = new CachingTerminalFactoryBuilder<>(() -> new IOSNFCFactory());
//
//	OpeneCardContextConfig config = new OpeneCardContextConfig(factory, AndroidMarshaller.class.getCanonicalName());
//	CommonActivationUtils activationUtils = new CommonActivationUtils(config, new NFCDialogMsgSetter() {
//	    @Override
//	    public void setText(String msg) {
//	    }
//
//	    @Override
//	    public boolean isSupported() {
//		return false;
//	    }
//	});
//
//	return new IOSSkidLib(activationUtils, factory);
//    }


    @Override
    public SkidResult initialize() {
//	// pretend NFC is fully functional, the actual check must be done by the application beforehand and eid actions must be prevented if necessary
//	oecCtx = new DelegatingAndroidContextManager(utils.context(new NFCCapabilities() {
//	    @Override
//	    public boolean isAvailable() {
//		return true;
//	    }
//
//	    @Override
//	    public boolean isEnabled() {
//		return true;
//	    }
//
//	    @Override
//	    public NfcCapabilityResult checkExtendedLength() {
//		return NfcCapabilityResult.SUPPORTED;
//	    }
//	}), builder);
//
//	Promise<ActivationSource> finished = new Promise<>();
//	Promise<ServiceErrorResponse> error = new Promise<>();
//
//	try {
//	    oecCtx.initializeContext(new StartServiceHandler() {
//		@Override
//		public void onSuccess(ActivationSource source) {
//		    error.cancel();
//		    finished.deliver(source);
//		}
//
//		@Override
//		public void onFailure(ServiceErrorResponse response) {
//		    error.deliver(response);
//		    finished.cancel();
//		}
//	    });
//
//	    oecActivationSource = finished.deref();
//	    return new SkidResultImpl();
//	} catch (InterruptedException ex) {
//	    if (error.isDelivered()) {
//		ServiceErrorResponse res = error.derefNonblocking();
//		SkidResult skidRes = SkidResultImpl.fromOecServiceError(res);
//		return skidRes;
//	    } else {
//		return new SkidResultImpl(SkidErrorCodes.INTERRUPTED, "Initialization has been interrupted.");
//	    }
//	} catch (ApduExtLengthNotSupported | NfcDisabled | NfcUnavailable | UnableToInitialize ex) {
//	    return new SkidResultImpl(SkidErrorCodes.INTERNAL_ERROR, "Failed to initialize Open eCard Stack.");
//	}
	return null;
    }

    @Override
    public SkidResult terminate() {
//	Promise<Object> finished = new Promise<>();
//	Promise<ServiceErrorResponse> error = new Promise<>();
//
//	try {
//	    oecCtx.terminateContext(new StopServiceHandler() {
//		@Override
//		public void onSuccess() {
//		    error.cancel();
//		    finished.deliver(true);
//		}
//
//		@Override
//		public void onFailure(ServiceErrorResponse response) {
//		    error.deliver(response);
//		    finished.cancel();
//		}
//	    });
//
//	    finished.deref();
//	    return new SkidResultImpl();
//	} catch (InterruptedException ex) {
//	    if (error.isDelivered()) {
//		ServiceErrorResponse res = error.derefNonblocking();
//		SkidResult skidRes = SkidResultImpl.fromOecServiceError(res);
//		return skidRes;
//	    } else {
//		return new SkidResultImpl(SkidErrorCodes.INTERRUPTED, "Termination has been interrupted.");
//	    }
//	}
	return null;
    }

    @Override
    public SamlClient createSamlClient() {
	SamlClientImpl samlClient = new SamlClientImpl(oecActivationSource);
	return samlClient;
    }

    @Override
    public PinManagementControllerFactory pinManagementFactory() {
	return oecActivationSource.pinManagementFactory();
    }

    @Override
    public AuthModuleCallbackBuilder createAuthModuleBuilder() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
