package skid.mob.ios;

import java.security.Provider;
import java.security.Security;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.common.util.SysUtils;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import org.openecard.scio.CachingTerminalFactoryBuilder;
import org.openecard.scio.IOSNFCFactory;
import skid.mob.lib.SamlClient;
import java.security.KeyStore;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.openecard.bouncycastle.jcajce.provider.keystore.BC;
import org.openecard.bouncycastle.jcajce.provider.keystore.BC.Mappings;
import org.openecard.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
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

	try {
	    for (Provider p : Security.getProviders()) {
		Security.removeProvider(p.getName());
	    }
	    BouncyCastleProvider provider = new BouncyCastleProvider();
	    BouncyCastleJsseProvider bcjp = new BouncyCastleJsseProvider();

	    Mappings map = new BC.Mappings();
	    map.configure(provider);

	    Security.insertProviderAt(provider, 0);
	    Security.insertProviderAt(bcjp, 0);
	    //Security.setProperty("ssl.SocketFactory.provider", "org.openecard.bouncycastle.jsse.provider.SSLSocketFactoryImpl");
		//	    Security.setProperty("ssl.SocketFactory.provider", "");

	    for (Provider p : Security.getProviders()) {
		System.out.println("Avail Provider:" + p.getName());
	    }
	    SSLContext ctx = SSLContext.getInstance("TLS");

	    KeyStore trustAnchors = AppleTrustStore.getTrustStore();
	    //here we have to use our BC not the other
	    TrustManagerFactory tmFac = TrustManagerFactory.getInstance("PKIX");
	    tmFac.init(trustAnchors);
	    TrustManager[] tms = tmFac.getTrustManagers();
	    ctx.init(null, tms, null);
	    


	    SSLSocketFactory fact = ctx.getSocketFactory();

	    System.out.println("sslfact:" + fact.getClass().toString());
	    System.out.println("sslctx:" + ctx.getClass().toString());

	    HttpsURLConnection.setDefaultSSLSocketFactory(fact);
	} catch (Throwable t) {
	    System.out.println("Message from throwable in static block:" + t.getMessage());
	    t.printStackTrace();
	}

//	LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//	Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
//	rootLogger.setLevel(Level.ERROR);
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
    public PinManagementControllerFactory pinManagementFactory(NFCConfig nfcConfig) {
	this.nfcConfig = nfcConfig;
	return this.utils.pinManagementFactory();
    }

    @Override
    public PinManagementControllerFactory pinManagementFactory(String defaultNFCDialogMsg, String defaultNFCCardRecognizedMessage) {
	this.nfcConfig = new NFCConfig() {
	    @Override
	    public String getProvideCardMessage() {
		return defaultNFCDialogMsg;
	    }

	    @Override
	    public String getDefaultNFCCardRecognizedMessage() {
		return defaultNFCCardRecognizedMessage;
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
    public PinManagementControllerFactory pinManagementFactory() {
	return pinManagementFactory("Please provide card", "Card recognized");
    }

    @Override
    public AuthModuleCallbackBuilder createAuthModuleBuilder() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
