package org.openecard.client.richclient;

import java.util.logging.Logger;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.logging.LogManager;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardEventHandler extends Thread implements EventCallback {

    private static final Logger logger = LogManager.getLogger(RichClient.class.getName());

    public CardEventHandler() {
//        Thread t = new Thread(this);
//        t.run();
    }

    private void waitForInput() {
	synchronized (this) {
	    try {
		wait();
	    } catch (InterruptedException ex) {
		// Oh oh,...
	    }
	}
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
//        logger.log(Level.INFO, eventType.name() + " |  " + eventData.getClass());
//        if (eventData instanceof ConnectionHandleType) {
//            ConnectionHandleType c = (ConnectionHandleType) eventData;
//            logger.log(Level.INFO, "> " + c.getRecognitionInfo().getCaptureTime() + " " + ByteUtils.toHexString(c.getRecognitionInfo().getCardIdentifier()) + " " + c.getRecognitionInfo().getCardType());
//            logger.log(Level.INFO, "> " + c.getSlotIndex() + " " + ByteUtils.toHexString(c.getSlotHandle()));
////            System.out.println();
//        } else {
//            logger.log(Level.INFO, "error");
//        }
    }

}
