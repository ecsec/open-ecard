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

package org.openecard.mobile.system;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.openecard.addon.AddonManager;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.event.EventDispatcherImpl;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.sal.CombinedCIFProvider;
import org.openecard.common.util.ByteUtils;
import org.openecard.gui.UserConsent;
import org.openecard.gui.definition.ViewController;
import org.openecard.mobile.ui.EacNavigatorFactory;
import org.openecard.mobile.ui.InsertCardNavigatorFactory;
import org.openecard.mobile.ui.CompositeUserConsent;
import org.openecard.mobile.ui.PINManagementNavigatorFactory;
import org.openecard.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.ifd.scio.IFD;
import org.openecard.ifd.scio.wrapper.IFDTerminalFactory;
import org.openecard.management.TinyManagement;
import org.openecard.mobile.activation.ActivationInteraction;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import static org.openecard.mobile.system.ServiceMessages.*;
import org.openecard.mobile.ui.MessageDialogStub;
import org.openecard.mobile.ui.UserConsentNavigatorFactory;
import org.openecard.mobile.utils.ClasspathRegistry;
import org.openecard.recognition.CardRecognitionImpl;
import org.openecard.recognition.RepoCifProvider;
import org.openecard.sal.TinySAL;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.openecard.ws.SAL;
import org.openecard.ws.marshal.WsdefProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context object containing references to all internal objects of the Open eCard Stack. This object can be obtained by
 * either {@link OpeneCardServiceClient} or {@link OpeneCardServiceClientHandler}. Instances of this class must not be
 * used after the the Open eCard stack has been stopped.
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 */
public class OpeneCardContext {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardContext.class);

    public static final String IFD_FACTORY_KEY = "org.openecard.ifd.scio.factory.impl";
    public static final String WSDEF_MARSHALLER_KEY = "org.openecard.ws.marshaller.impl";

    private ClientEnv env;

    // Interface Device Layer (IFD)
    private IFD ifd;

    private AddonManager manager;
    private EventDispatcher eventDispatcher;
    private CardRecognitionImpl recognition;
    private Dispatcher dispatcher;
    private TinyManagement management;
    private SAL sal;

    private UserConsent gui;
    private HashMap<String, UserConsentNavigatorFactory<? extends ActivationInteraction>> realFactories;

    // true if already initialized
    private boolean initialized = false;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;

    private final NFCCapabilities nfcCapabilities;
    private final OpeneCardContextConfig config;
    private final NFCDialogMsgSetter msgSetter;

    // package private so that only this package can use it
    public OpeneCardContext(NFCCapabilities nfcCapabilities, OpeneCardContextConfig config, NFCDialogMsgSetter msgSetter) {
	if (nfcCapabilities == null) {
	    throw new IllegalStateException(NO_NFC_CONTEXT);
	}
	this.nfcCapabilities = nfcCapabilities;
	this.config = config;
	this.msgSetter = msgSetter;
    }

    ///
    /// Initialization & Shutdown
    ///
    public void initialize() throws UnableToInitialize, NfcUnavailable, NfcDisabled, ApduExtLengthNotSupported {
	String errorMsg = SERVICE_RESPONSE_FAILED;

	if (initialized) {
	    throw new UnableToInitialize(SERVICE_ALREADY_INITIALIZED);
	}


	// set up nfc and mobile marshaller
	WsdefProperties.setProperty(WSDEF_MARSHALLER_KEY, this.config.getWsdefMarshallerClass());

	boolean nfcAvailable = this.nfcCapabilities.isAvailable();
	boolean nfcEnabled = this.nfcCapabilities.isEnabled();
	NfcCapabilityResult nfcExtendedLengthSupport = this.nfcCapabilities.checkExtendedLength();
	if (!nfcAvailable) {
	    throw new NfcUnavailable();
	} else if (!nfcEnabled) {
	    throw new NfcDisabled();
	} else if (nfcExtendedLengthSupport == NfcCapabilityResult.NOT_SUPPORTED) {
	    throw new ApduExtLengthNotSupported(NFC_NO_EXTENDED_LENGTH_SUPPORT);
	} else {
	    LOG.info("NFC extended length capability: {}", nfcExtendedLengthSupport);
	}
	// TODO: initialize terminal factory
	LOG.info("Terminal factory initialized.");

	try {
	    // set up client environment
	    env = new ClientEnv();

	    // set up dispatcher
	    dispatcher = new MessageDispatcher(env);
	    env.setDispatcher(dispatcher);
	    LOG.info("Message Dispatcher initialized.");


	    // set up management
	    management = new TinyManagement(env);
	    env.setManagement(management);
	    LOG.info("Management initialized.");

	    // set up event dispatcher
	    eventDispatcher = new EventDispatcherImpl();
	    // Initialize and start the Event Dispatcher
	    eventDispatcher.start();
	    LOG.info("Event dispatcher started.");
	    env.setEventDispatcher(eventDispatcher);


	    gui = createUserConsent(dispatcher, eventDispatcher);

	    // set up card recognition
	    try {
		recognition = new CardRecognitionImpl(env);
		recognition.setGUI(gui);
		env.setRecognition(recognition);
		LOG.info("CardRecognition initialized.");
	    } catch (Exception ex) {
		errorMsg = CARD_REC_INIT_FAILED;
		throw ex;
	    }

	    // set up ifd
	    ifd = new IFD();
	    ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	    ifd.setGUI(gui);
	    ifd.setEnvironment(env);
	    ifd.setTerminalFactoryBuilder(new IFDTerminalFactory(this.config.getTerminalFactoryBuilder()));
	    env.setIFD(ifd);
	    LOG.info("IFD initialized.");


	    CombinedCIFProvider cifProv = new CombinedCIFProvider();
	    env.setCIFProvider(cifProv);
	    cifProv.addCifProvider(new RepoCifProvider(recognition));

	    // set up SAL
	    TinySAL mainSAL = new TinySAL(env);
	    mainSAL.setGUI(gui);

	    sal = mainSAL;
	    env.setSAL(sal);
	    LOG.info("SAL prepared.");

	    ViewController viewController = new ViewController() {
		@Override
		public void showSettingsUI() {
		}

		@Override
		public void showDefaultViewUI() {
		}
	    };

	    // set up addon manager
	    try {
		manager = new AddonManager(env, gui, viewController, new ClasspathRegistry(),
			mainSAL.getSalStateView());
		mainSAL.setAddonManager(manager);

		LOG.info("Addon manager initialized.");
	    } catch (Exception ex) {
		errorMsg = ADD_ON_INIT_FAILED;
		throw ex;
	    }

	    // establish context
	    try {
		EstablishContext establishContext = new EstablishContext();
		EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);
		LOG.info("Established context.");
		WSHelper.checkResult(establishContextResponse);
		contextHandle = establishContextResponse.getContextHandle();
		LOG.info("ContextHandle: {}", ByteUtils.toHexString(contextHandle));
		mainSAL.setIfdCtx(contextHandle);
	    } catch (WSHelper.WSException ex) {
		errorMsg = ESTABLISH_IFD_CONTEXT_FAILED;
		throw ex;
	    }

	    // initialize SAL
	    try {
		WSHelper.checkResult(sal.initialize(new Initialize()));
		LOG.info("SAL initialized.");
	    } catch (WSHelper.WSException ex) {
		errorMsg = ex.getMessage();
		throw ex;
	    }


	    // TODO: Hack to ensure registry is loaded before use.
	    manager.getRegistry().listAddons();

	    initialized = true;
	} catch (Exception ex) {
	    LOG.error(errorMsg, ex);
	    throw new UnableToInitialize(errorMsg, ex);
	}
    }

    private CompositeUserConsent createUserConsent(Dispatcher dispatcher, EventDispatcher eventDispatcher) {
	// initialize gui
	realFactories = new HashMap<>();
	// the key type must match the generic. This can't be enforced so watch it here.
	// TODO: introduce factory method for the new instance of EacNavigatorFactory.
	EacNavigatorFactory eacNavFac = EacNavigatorFactory.create(msgSetter, dispatcher);

	realFactories.put(eacNavFac.getProtocolType(), eacNavFac);

	PINManagementNavigatorFactory pinMngFac = new PINManagementNavigatorFactory(
		dispatcher, eventDispatcher);
	realFactories.put(pinMngFac.getProtocolType(), pinMngFac);

	InsertCardNavigatorFactory insertFac = new InsertCardNavigatorFactory(msgSetter);
	realFactories.put(insertFac.getProtocolType(), insertFac);

	List<UserConsentNavigatorFactory<?>> allFactories = Arrays.asList(
		eacNavFac,
		pinMngFac,
		insertFac);

	return new CompositeUserConsent(
		allFactories,
		new MessageDialogStub());
    }

    public boolean shutdown() {
	LOG.info("Shutting down.");
	initialized = false;
	try {
	    if (ifd != null && contextHandle != null) {
		ReleaseContext releaseContext = new ReleaseContext();
		releaseContext.setContextHandle(contextHandle);
		ifd.releaseContext(releaseContext);
	    }
	    if (eventDispatcher != null) {
		eventDispatcher.terminate();
	    }
	    if (manager != null) {
		manager.shutdown();
	    }
	    if (sal != null) {
		Terminate terminate = new Terminate();
		sal.terminate(terminate);
	    }

	    return true;
	} catch (Exception ex) {
	    LOG.error("Failed to terminate Open eCard instances...", ex);
	    return false;
	}
    }

    ///
    /// Get-/Setter Methods
    ///
    public IFD getIFD() {
	return ifd;
    }

    public SAL getSAL() {
	return sal;
    }

    public EventDispatcher getEventDispatcher() {
	return eventDispatcher;
    }

    public byte[] getContextHandle() {
	return contextHandle;
    }

    public TinyManagement getTinyManagement() {
	return management;
    }

    public Dispatcher getDispatcher() {
	return dispatcher;
    }

    public CardRecognitionImpl getRecognition() {
	return recognition;
    }

    public ClientEnv getEnv() {
	return env;
    }

    public UserConsent getGUI() {
	return gui;
    }

    public <T extends ActivationInteraction> UserConsentNavigatorFactory<T> getGuiNavigatorFactory(String protocolType) {
	UserConsentNavigatorFactory fac = realFactories.get(protocolType);
	if (fac == null) {
	    throw new IllegalArgumentException("The requested GUI class is not handled by any of the factory objects.");
	} else {
	    return fac;
	}
    }

    public AddonManager getManager() {
	return manager;
    }

}
