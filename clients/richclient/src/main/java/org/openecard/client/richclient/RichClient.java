package org.openecard.client.richclient;

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.event.EventManager;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.richclient.activation.messages.ActivationApplicationRequest;
import org.openecard.client.richclient.activation.messages.ActivationApplicationResponse;
import org.openecard.client.richclient.activation.tctoken.TCToken;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.paos.PAOSCallback;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TLSClientSocketFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class RichClient extends Thread implements EventCallback, PAOSCallback {

    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;
    private CardRecognition recognition;
    CardStateMap cardStates;
    // TCToken
    private TCToken token;
    private static final Logger logger = LogManager.getLogger(RichClient.class.getName());
    // Warum muss das eine liste sein?
    private ArrayList<ConnectionHandleType> connectionHandles = new ArrayList<ConnectionHandleType>();
    private static RichClient client;

    public static void main(String[] args) {
	try {
	    ConsoleHandler ch = new ConsoleHandler();
	    ch.setLevel(Level.FINEST);
	    logger.addHandler(ch);

	    new RichClient();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    Logger.getLogger(RichClient.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public static RichClient getInstance() throws Exception {
	if (client == null) {
	    client = new RichClient();
	}
	return client;
    }

    public RichClient() throws Exception {
//        try {
//            Activation.getInstance();
//        } catch (BindException e) {
//            throw new Exception("Application is running.");
//        }
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

	try {
	    token = request.getTCToken();

	    byte[] psk = token.getPathSecurityParameter().getPSK();
	    System.out.println(ByteUtils.toHexString(psk));
	    String sessionIdentifier = token.getSessionIdentifier();
	    String serverAddress = token.getServerAddress();
	    //FIXME
	    String endpoint = "https://" + serverAddress + "/?sessionid=" + sessionIdentifier;

	    // Set up TLS connection
	    PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk, serverAddress);

	    // Set up PAOS connection
	    PAOS p = new PAOS(endpoint, env.getDispatcher(), RichClient.this, new TLSClientSocketFactory(tlsClient));

	    // Send StartPAOS
	    StartPAOS sp = new StartPAOS();
	    sp.getConnectionHandle().addAll(connectionHandles);
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

	setUpIFD();
	setUpSAL();

	// Set up Management
	TinyManagement management = new TinyManagement(env);
	env.setManagement(management);

	// Set up Dispatcher
	MessageDispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);


	cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);


	recognition = new CardRecognition(ifd, contextHandle);

	// Set up EventManager
	EventManager em = new EventManager(recognition, env, contextHandle, "1");

//        em.registerAllEvents(salCallback);
	CardEventHandler tmp = new CardEventHandler();
	tmp.start();
	em.registerAllEvents(tmp);
	em.registerAllEvents(this);

	em.initialize();

//        while (true) {
//            Thread.sleep(1000);
//            waitForInput();
//        }
//
    }

    private void waitForInput() {
	synchronized (this) {
	    try {
		wait();
	    } catch (InterruptedException ex) {
		// Oh oh,...
		ex.printStackTrace();
	    }
	}
    }

    private void setUpIFD() {
	ifd = new IFD();

	// Perform EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	contextHandle = ifd.establishContext(establishContext).getContextHandle();

	// Add PACE protocol to IFD
	ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	// Add IFD to client environment
	env.setIFD(ifd);
    }

    private void setUpSAL() {
	sal = new TinySAL(env, cardStates);

	// Add EAC protocol to SAL
	sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	// Add SAL to client environment
	env.setSAL(sal);
    }

    private void setupClientEnvironment() throws Exception {
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	try {
	    logger.log(Level.INFO, "Event: {0} {1}", new Object[]{eventType.name(), eventData.getClass()});

	    if (eventType.equals(EventType.CARD_RECOGNIZED)) {
		if (eventData instanceof ConnectionHandleType) {
		    ConnectionHandleType connectionHandle = (ConnectionHandleType) eventData;
		    String ifdName = connectionHandle.getIFDName();
		    BigInteger slotIndex = connectionHandle.getSlotIndex();


		    // Muss ich das machen?
//                    ListIFDs listIFDs = new ListIFDs();
//                    listIFDs.setContextHandle(contextHandle);


		    logger.log(Level.FINE, "Found card at {0}", ifdName);

		    // Establish a connection to the card.
		    Connect connect = new Connect();
		    connect.setContextHandle(contextHandle);
		    connect.setExclusive(false);
		    connect.setIFDName(ifdName);
		    connect.setSlot(slotIndex);
		    ConnectResponse connectResponse = ifd.connect(connect);

		    // Create a ConnecationHandle which represents the established connection to a card application.
		    //TODO Muss ich das machen oder macht der Server das?
		    connectionHandle.setSlotHandle(connectResponse.getSlotHandle());
		    connectionHandles.add(connectionHandle);
		}
	    }
	} catch (Throwable e) {
	    logger.log(Level.SEVERE, e.toString());
	}
    }

    @Override
    public void loadRefreshAddress() {
	//TODO delete me
	System.out.println("loadRefreshAddress");
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
