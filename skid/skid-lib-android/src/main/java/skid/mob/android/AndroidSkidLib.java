/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.android;

import android.content.Intent;
import android.nfc.Tag;
import java.io.IOException;
import org.openecard.android.activation.AndroidContextManager;
import org.openecard.android.activation.DelegatingAndroidContextManager;
import org.openecard.common.util.Promise;
import org.openecard.common.util.SysUtils;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ActivationUtils;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.activation.StopServiceHandler;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NFCTagNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.scio.AndroidNFCFactory;
import org.openecard.scio.CachingTerminalFactoryBuilder;
import org.openecard.ws.android.AndroidMarshaller;
import skid.mob.impl.SkidResultImpl;
import skid.mob.impl.fs.SamlClientImpl;
import skid.mob.impl.auth.AuthModuleCallbackBuilderImpl;
import skid.mob.lib.AuthModuleCallbackBuilder;
import skid.mob.lib.SamlClient;
import skid.mob.lib.SkidErrorCodes;
import skid.mob.lib.SkidLib;
import skid.mob.lib.SkidResult;


/**
 *
 * @author Tobias Wich
 */
public class AndroidSkidLib implements SkidLib {

    static {
	// define that this system is Android
	SysUtils.setIsAndroid();
    }

    private final ActivationUtils utils;
    private final CachingTerminalFactoryBuilder<AndroidNFCFactory> builder;

    private AndroidContextManager oecCtx;
    private ActivationSource oecActivationSource;

    AndroidSkidLib(CommonActivationUtils utils, CachingTerminalFactoryBuilder<AndroidNFCFactory> builder) {
	this.utils = utils;
	this.builder = builder;
    }

    public static AndroidSkidLib createSkidLib() {
	CachingTerminalFactoryBuilder<AndroidNFCFactory> factory = new CachingTerminalFactoryBuilder<>(() -> new AndroidNFCFactory());

	OpeneCardContextConfig config = new OpeneCardContextConfig(factory, AndroidMarshaller.class.getCanonicalName());
	CommonActivationUtils activationUtils = new CommonActivationUtils(config, new NFCDialogMsgSetter() {
	    @Override
	    public void setText(String msg) {
	    }

	    @Override
	    public boolean isSupported() {
		return false;
	    }
	});

	return new AndroidSkidLib(activationUtils, factory);
    }


    @Override
    public SkidResult initialize() {
	// pretend NFC is fully functional, the actual check must be done by the application beforehand and eid actions must be prevented if necessary
	oecCtx = new DelegatingAndroidContextManager(utils.context(new NFCCapabilities() {
	    @Override
	    public boolean isAvailable() {
		return true;
	    }

	    @Override
	    public boolean isEnabled() {
		return true;
	    }

	    @Override
	    public NfcCapabilityResult checkExtendedLength() {
		return NfcCapabilityResult.SUPPORTED;
	    }
	}), builder);

	Promise<ActivationSource> finished = new Promise<>();
	Promise<ServiceErrorResponse> error = new Promise<>();

	try {
	    oecCtx.initializeContext(new StartServiceHandler() {
		@Override
		public void onSuccess(ActivationSource source) {
		    error.cancel();
		    finished.deliver(source);
		}

		@Override
		public void onFailure(ServiceErrorResponse response) {
		    error.deliver(response);
		    finished.cancel();
		}
	    });

	    oecActivationSource = finished.deref();
	    return new SkidResultImpl();
	} catch (InterruptedException ex) {
	    if (error.isDelivered()) {
		ServiceErrorResponse res = error.derefNonblocking();
		SkidResult skidRes = SkidResultImpl.fromOecServiceError(res);
		return skidRes;
	    } else {
		return new SkidResultImpl(SkidErrorCodes.INTERRUPTED, "Initialization has been interrupted.");
	    }
	} catch (ApduExtLengthNotSupported | NfcDisabled | NfcUnavailable | UnableToInitialize ex) {
	    return new SkidResultImpl(SkidErrorCodes.INTERNAL_ERROR, "Failed to initialize Open eCard Stack.");
	}
    }

    @Override
    public SkidResult terminate() {
	Promise<Object> finished = new Promise<>();
	Promise<ServiceErrorResponse> error = new Promise<>();

	try {
	    oecCtx.terminateContext(new StopServiceHandler() {
		@Override
		public void onSuccess() {
		    error.cancel();
		    finished.deliver(true);
		}

		@Override
		public void onFailure(ServiceErrorResponse response) {
		    error.deliver(response);
		    finished.cancel();
		}
	    });
	    
	    finished.deref();
	    return new SkidResultImpl();
	} catch (InterruptedException ex) {
	    if (error.isDelivered()) {
		ServiceErrorResponse res = error.derefNonblocking();
		SkidResult skidRes = SkidResultImpl.fromOecServiceError(res);
		return skidRes;
	    } else {
		return new SkidResultImpl(SkidErrorCodes.INTERRUPTED, "Termination has been interrupted.");
	    }
	}
    }

    public void onNewIntent(Intent intent) throws ApduExtLengthNotSupported, NFCTagNotSupported, IOException {
	oecCtx.onNewIntent(intent);
    }

    public void onNewIntent(Tag tag) throws ApduExtLengthNotSupported, NFCTagNotSupported, IOException {
	oecCtx.onNewIntent(tag);
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
	return new AuthModuleCallbackBuilderImpl();
    }

}
