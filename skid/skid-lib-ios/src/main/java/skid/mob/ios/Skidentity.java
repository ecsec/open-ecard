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
import skid.mob.lib.SamlClient;
import skid.mob.lib.SkidLib;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.openecard.common.util.Promise;
import org.openecard.ios.activation.DeveloperOptions;
import org.openecard.ios.activation.DeveloperOptionsImpl;
import org.openecard.ios.activation.IOSNFCCapabilities;
import org.openecard.ios.activation.IOSNFCDialogMsgSetter;
import org.openecard.ios.activation.NFCConfig;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.activation.StopServiceHandler;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.robovm.annotations.FrameworkObject;
import org.openecard.scio.IOSConfig;
import org.openecard.ws.android.AndroidMarshaller;
import org.slf4j.LoggerFactory;
import skid.mob.impl.SkidResultImpl;
import skid.mob.impl.fs.SamlClientImpl;
import skid.mob.lib.AuthModuleCallbackBuilder;
import skid.mob.lib.SkidErrorCodes;
import skid.mob.lib.SkidResult;


/**
 *
 * @author Florian Otto
 */
@FrameworkObject(factoryMethod = "createSkidLib")
public class Skidentity implements IOSSkidLib {

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

    private final CommonActivationUtils utils;
//    private /* final */ CachingTerminalFactoryBuilder<IOSNFCFactory> builder;

    private final ContextManager oecCtx;
    private ActivationSource oecActivationSource;

    private final DeveloperOptions developerOptions;
    private NFCConfig nfcConfig;

    public Skidentity() {
	this.developerOptions = new DeveloperOptionsImpl();
	IOSNFCCapabilities capabilities = new IOSNFCCapabilities();
	IOSConfig currentConfig = new IOSConfig() {
	    @Override
	    public String getDefaultProvideCardMessage() {

		return nfcConfig.getProvideCardMessage();
	    }

	    @Override
	    public String getDefaultCardRecognizedMessage() {

		return nfcConfig.getDefaultNFCCardRecognizedMessage();
	    }

	    @Override
	    public String getDefaultNFCErrorMessage() {
		return nfcConfig.getDefaultNFCErrorMessage();
	    }

	    @Override
	    public String getAquireNFCTagTimeoutErrorMessage() {
		return nfcConfig.getAquireNFCTagTimeoutMessage();
	    }

	    @Override
	    public String getNFCCompletionMessage() {
		return nfcConfig.getNFCCompletionMessage();
	    }

	    @Override
	    public String getTagLostErrorMessage() {
		return nfcConfig.getTagLostErrorMessage();
	    }

	    @Override
	    public String getDefaultCardConnectedMessage() {
		return nfcConfig.getDefaultCardConnectedMessage();
	    }
	};

	CachingTerminalFactoryBuilder<IOSNFCFactory> builder = new CachingTerminalFactoryBuilder(() -> new IOSNFCFactory(currentConfig));
	OpeneCardContextConfig config = new OpeneCardContextConfig(builder, AndroidMarshaller.class.getCanonicalName());
	CommonActivationUtils activationUtils;
	activationUtils = new CommonActivationUtils(config, new IOSNFCDialogMsgSetter(builder));
	this.utils = activationUtils;
	this.oecCtx = this.utils.context(capabilities);

    }

    @Override
    public SkidResult initialize() {

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

    @Override
    public SamlClient createSamlClient() {
	SamlClientImpl samlClient = new SamlClientImpl(oecActivationSource);
	return samlClient;
    }

    @Override
    public PinManagementControllerFactory pinManagementFactory() {
	this.nfcConfig = new NFCConfig() {
	    @Override
	    public String getProvideCardMessage() {
		return "defaultNFCDialgoMsg";
	    }

	    @Override
	    public String getDefaultNFCCardRecognizedMessage() {
		return "defaultNFCCardRecognizedMessage";
	    }

	    @Override
	    public String getDefaultNFCErrorMessage() {
		return "Communication with the card ended.";
	    }

	    @Override
	    public String getAquireNFCTagTimeoutMessage() {
		return "Could not connect to a card.";
	    }

	    @Override
	    public String getNFCCompletionMessage() {
		return "Finished communicating with the card.";
	    }

	    @Override
	    public String getTagLostErrorMessage() {
		return "Lost communication with the card.";
	    }

	    @Override
	    public String getDefaultCardConnectedMessage() {
		return "Connected to the card.";
	    }
	};
	return this.utils.pinManagementFactory();
    }

    @Override
    public AuthModuleCallbackBuilder createAuthModuleBuilder() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
