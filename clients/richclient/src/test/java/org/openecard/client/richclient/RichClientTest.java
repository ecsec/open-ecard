package org.openecard.client.richclient;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.ifd.scio.wrapper.SCChannel;
import org.openecard.client.richclient.activation.common.ActivationConstants;
import org.openecard.client.richclient.activation.messages.ActivationApplicationRequest;
import org.openecard.client.richclient.activation.messages.ActivationApplicationResponse;
import org.openecard.client.richclient.activation.tctoken.TCToken;
import org.openecard.client.richclient.activation.tctoken.TCTokenConverter;
import org.openecard.client.richclient.activation.tctoken.TCTokenException;
import org.openecard.client.richclient.activation.tctoken.TCTokenGrabber;
import org.openecard.client.richclient.activation.tctoken.TCTokenParser;
import org.openecard.client.richclient.activation.tctoken.TCTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class RichClientTest {

    private static final Logger logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static List<TCToken> tokens;
    private static String tokenURI = "https://willow.mtg.de/eid-server-demo-app/result/request.html";

    public RichClientTest() {
    }

    @Before
    public void setUp() {
	try {
	    ConsoleHandler ch = new ConsoleHandler();
	    ch.setLevel(Level.FINEST);
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
	} catch (Exception e) {
	    logger.error(e.getMessage());
	    fail(e.getMessage());
	}
    }

    @Test
    public void testMain() {
	try {
	    RichClient client = RichClient.getInstance();
	    // Wait some seconds until the client comes up
	    Thread.sleep(2500);

	    ActivationApplicationRequest applicationRequest = new ActivationApplicationRequest();
	    applicationRequest.setTCToken(tokens.get(0));
	    ActivationApplicationResponse applicationReponse = client.activate(applicationRequest);

	    System.out.println("RICH CLIENT RESULT");
	    System.out.println(applicationReponse.getErrorMessage());
	    System.out.println(applicationReponse.getRefreshAddress());
	    System.out.println(applicationReponse.getErrorPage());


	} catch (Exception e) {
	    e.printStackTrace();
	    logger.error(e.getMessage());
	    fail(e.getMessage());
	}
    }
}
