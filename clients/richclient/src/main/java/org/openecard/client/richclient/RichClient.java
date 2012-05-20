package org.openecard.client.richclient;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.net.BindException;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.richclient.activation.Activation;
import org.openecard.client.richclient.activation.messages.StatusRequest;
import org.openecard.client.richclient.activation.messages.StatusResponse;
import org.openecard.client.richclient.activation.messages.TCTokenRequest;
import org.openecard.client.richclient.activation.messages.TCTokenResponse;
import org.openecard.client.richclient.activation.messages.common.ClientRequest;
import org.openecard.client.richclient.activation.messages.common.ClientResponse;
import org.openecard.client.richclient.activation.tctoken.TCToken;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class RichClient {

    private static final Logger logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static RichClient client;
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;

    public static void main(String args[]) {
	try {
	    RichClient.getInstance();
	} catch (Exception e) {
	    logger.warn(e.getMessage());
	}
    }

    public static RichClient getInstance() throws Exception {
	if (client == null) {
	    client = new RichClient();
	}
	return client;
    }

    private RichClient() throws Exception {
	try {
	    Activation.getInstance();
	} catch (BindException e) {
	    throw new Exception("Client activation is running.");
	}
	setup();
    }

    public ClientResponse request(ClientRequest request) {
	if (request instanceof TCTokenRequest) {
	    return handleActivate((TCTokenRequest) request);
	} else if (request instanceof StatusRequest) {
	    return handleStatus((StatusRequest) request);
	}
	return null;
    }

    private StatusResponse handleStatus(StatusRequest statusRequest) {
	StatusResponse response = new StatusResponse();
	

	return response;
    }

    /**
     * Activate the client.
     *
     * @param request ActivationApplicationRequest
     * @return ActivationApplicationResponse
     */
    private TCTokenResponse handleActivate(TCTokenRequest request) {
	TCTokenResponse response = new TCTokenResponse();

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
	    //TODO Change to support different protocols
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
	    logger.error(LoggingConstants.THROWING, "Exception", w);
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
	ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	env.setIFD(ifd);

	// Set up the Dispatcher
	MessageDispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	ifd.setDispatcher(dispatcher);

	// Perform an EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	contextHandle = ifd.establishContext(establishContext).getContextHandle();

	// Set up CardRecognition
	recognition = new CardRecognition(ifd, contextHandle);

	// Set up EventManager
	EventManager em = new EventManager(recognition, env, contextHandle, ValueGenerators.generateSecureSessionID());
	env.setEventManager(em);

	// Set up SALStateCallback
	cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	em.registerAllEvents(salCallback);

	// Set up SAL
	sal = new TinySAL(env, cardStates);
	sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	env.setSAL(sal);

	// Set up GUI
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	sal.setGUI(gui);
	ifd.setGUI(gui);

	// Initialize the EventManager
	em.initialize();
    }
}
