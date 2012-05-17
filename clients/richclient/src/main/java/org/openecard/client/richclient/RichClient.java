package org.openecard.client.richclient;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.net.BindException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.ifd.scio.wrapper.SCChannel;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.richclient.activation.Activation;
import org.openecard.client.richclient.activation.common.ActivationConstants;
import org.openecard.client.richclient.activation.messages.ActivationApplicationRequest;
import org.openecard.client.richclient.activation.messages.ActivationApplicationResponse;
import org.openecard.client.richclient.activation.tctoken.TCToken;
import org.openecard.client.richclient.activation.tctoken.TCTokenConverter;
import org.openecard.client.richclient.activation.tctoken.TCTokenException;
import org.openecard.client.richclient.activation.tctoken.TCTokenGrabber;
import org.openecard.client.richclient.activation.tctoken.TCTokenParser;
import org.openecard.client.richclient.activation.tctoken.TCTokenVerifier;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class RichClient extends Thread {

    private static final Logger logger = LogManager.getLogger(RichClient.class.getName());
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    // TCToken
    private static RichClient client;
    // Only for development
    static List<TCToken> tokens;

    public static void main(String[] args) {
	try {
	    ConsoleHandler ch = new ConsoleHandler();
	    ch.setLevel(Level.FINEST);
	    logger.addHandler(ch);
	    logger.setLevel(Level.FINEST);
//
//	    LogManager.getLogger("org.openecard.client.richclient.activation").setLevel(Level.FINE);
//	    LogManager.getLogger("org.openecard.client.richclient.activation").addHandler(ch);
	    LogManager.getLogger("org.openecard.client.transport.paos").setLevel(Level.FINEST);
	    LogManager.getLogger("org.openecard.client.transport.paos").addHandler(ch);
//	    LogManager.getLogger("org.openecard.client.ifd.scio").addHandler(ch);
//	    LogManager.getLogger("org.openecard.client.ifd.scio").setLevel(Level.ALL);
	    LogManager.getLogger(SCChannel.class.getName()).addHandler(ch);
	    LogManager.getLogger("org.openecard.client.ifd.scio-backend").addHandler(ch);
	    LogManager.getLogger("org.openecard.client.ifd.scio-backend").setLevel(Level.FINEST);
	    LogManager.getLogger(SCChannel.class.getName()).setLevel(Level.FINEST);
//	    LogManager.getLogger("org.openecard.client.sal").setLevel(Level.ALL);
//	    LogManager.getLogger("org.openecard.client.sal").addHandler(ch);
//	    LogManager.getLogger("org.openecard.client.ifd").addHandler(ch);
//	    LogManager.getLogger("org.openecard.client.ifd").setLevel(Level.ALL);
//	    LogManager.getLogger("org.openecard.bouncycastle.crypto.tls").setLevel(Level.ALL);
//	    LogManager.getLogger("org.openecard.bouncycastle.crypto.tls").addHandler(ch);


	    client = new RichClient();
	    client.parseTCToken("https://willow.mtg.de/eid-server-demo-app/result/request.html");
//	    client.parseTCToken("https://eid.services.ageto.net/gw/login");
//	    client.parseTCToken("https://willow.mtg.de/eidavs/myaccount/createAnonymousUser.do?request.nextPage=http%3A%2F%2Fwillow.mtg.de%2Feidavs%2Fpopup%2FdownloadProduct.do%3FproductId%3D115064%26savedRequestAttributes%3D1337073818411");
//	    client.parseTCToken("https://test.governikus-eid.de/Autent-DemoApplication/RequestServlet?provider=demo_epa");
//
	    ActivationApplicationRequest applicationRequest = new ActivationApplicationRequest();
	    applicationRequest.setTCToken(tokens.get(0));
	    ActivationApplicationResponse applicationReponse = client.activate(applicationRequest);

	} catch (Exception ex) {
	    ex.printStackTrace();
	    Logger.getLogger(RichClient.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    private void parseTCToken(String tokenURI) throws TCTokenException {
	// Get TCToken from the given URL
	TCTokenGrabber grabber = new TCTokenGrabber();
	String data = grabber.getResource(tokenURI);

	//FIXME Remove me
	TCTokenConverter converter = new TCTokenConverter();
	data = converter.convert(data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ActivationConstants.ActivationError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens);
	ver.verify();
    }

    public static RichClient getInstance() throws Exception {
	if (client == null) {
	    client = new RichClient();
	}
	return client;
    }

    public RichClient() throws Exception {
	ConsoleHandler ch = new ConsoleHandler();
	ch.setLevel(Level.FINEST);
	logger.addHandler(ch);
	logger.setLevel(Level.FINEST);
//
//	    LogManager.getLogger("org.openecard.client.richclient.activation").setLevel(Level.FINE);
//	    LogManager.getLogger("org.openecard.client.richclient.activation").addHandler(ch);
	LogManager.getLogger("org.openecard.client.transport.paos").setLevel(Level.FINEST);
	LogManager.getLogger("org.openecard.client.transport.paos").addHandler(ch);
//	    LogManager.getLogger("org.openecard.client.ifd.scio").addHandler(ch);
//	    LogManager.getLogger("org.openecard.client.ifd.scio").setLevel(Level.ALL);
	LogManager.getLogger(SCChannel.class.getName()).addHandler(ch);
	LogManager.getLogger("org.openecard.client.ifd.scio-backend").addHandler(ch);
	LogManager.getLogger("org.openecard.client.ifd.scio-backend").setLevel(Level.FINEST);
	LogManager.getLogger(SCChannel.class.getName()).setLevel(Level.FINEST);

	try {
	    Activation.getInstance();
	} catch (BindException e) {
	    throw new Exception("Application is running.");
	}
	setup();
    }

    /**
     * Activate the client.
     *
     * @param request ActivationApplicationRequest
     * @return ActivationApplicationResponse
     */
    public ActivationApplicationResponse activate(ActivationApplicationRequest request) {
	ActivationApplicationResponse response = new ActivationApplicationResponse();

	TCToken token = request.getTCToken();
	ConnectionHandleType connectionHandle = request.getConnectionHandle();

	//FIXME ConnectionHandle kommt nachher vom ActivationApplicationRequest
	connectionHandle = sal.getConnectionHandles().get(0);

	try {
	    // Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	    CardApplicationPath cardApplicationPath = new CardApplicationPath();
	    cardApplicationPath.setCardAppPathRequest(connectionHandle);
	    CardApplicationPathResponse cardApplicationPathResponse = sal.cardApplicationPath(cardApplicationPath);

	    try {
		// Check CardApplicationPathResponse
		WSHelper.checkResult(cardApplicationPathResponse);
	    } catch (WSException ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
//	    logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		response.setErrorMessage(ex.getMessage());
		return response;
	    }

	    CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	    cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	    CardApplicationConnectResponse cardApplicationConnectResponse = sal.cardApplicationConnect(cardApplicationConnect);
	    // Update ConnectionHandle. It now includes a SlotHandle.
	    connectionHandle = cardApplicationConnectResponse.getConnectionHandle();

	    try {
		// Check CardApplicationConnectResponse
		WSHelper.checkResult(cardApplicationConnectResponse);
	    } catch (WSException ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
//	    logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		response.setErrorMessage(ex.getMessage());
		return response;
	    }

	    // Collect parameters for PSK based TLS
	    byte[] psk = token.getPathSecurityParameter().getPSK();
	    String sessionIdentifier = token.getSessionIdentifier();
	    String serverAddress = token.getServerAddress();

	    //FIXME
	    String endpoint = "https://" + serverAddress + "/?sessionid=" + sessionIdentifier;

	    // Set up TLS connection
	    PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk, serverAddress);

	    // Set up PAOS connection
	    PAOS p = new PAOS(endpoint, env.getDispatcher(), new TlsClientSocketFactory(tlsClient));

	    // Send StartPAOS
	    StartPAOS sp = new StartPAOS();
	    sp.getConnectionHandle().add(connectionHandle);
	    sp.setSessionIdentifier(sessionIdentifier);
	    p.sendStartPAOS(sp);

	    response.setRefreshAddress(token.getRefreshAddress());

	} catch (Throwable w) {
	    logger.log(Level.SEVERE, "Exception", w);
	    response.setErrorMessage(w.getMessage());
	}

	return response;
    }

    public void setup() throws Exception {
	// Set up client environment
	env = new ClientEnv();

	// Set up Management
	TinyManagement management = new TinyManagement(env);
	env.setManagement(management);

	// Set up the IFD
	ifd = new IFD();
	// Add PACE protocol to IFD
	ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	// Add IFD to client environment
	env.setIFD(ifd);

	// Set up the Dispatcher
	MessageDispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	ifd.setDispatcher(dispatcher);

	// Perform an EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	contextHandle = ifd.establishContext(establishContext).getContextHandle();

	recognition = new CardRecognition(ifd, contextHandle);

	// Set up EventManager
	EventManager em = new EventManager(recognition, env, contextHandle, ValueGenerators.generateSecureSessionID());
	env.setEventManager(em);

	cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
//	em.registerAllEvents(salCallback);

	// Set up SAL
	sal = new TinySAL(env, cardStates);
	// Add EAC protocol to SAL
	sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	// Add SAL to client environment
	env.setSAL(sal);

	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	sal.setGUI(gui);
	ifd.setGUI(gui);

	CardEventHandler cardEventHandler = new CardEventHandler();
	em.registerAllEvents(cardEventHandler);
	em.registerAllEvents(salCallback);

	// Initialize the EventManager
	em.initialize();
    }
}
