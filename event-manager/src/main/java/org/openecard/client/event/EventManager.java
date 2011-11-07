package org.openecard.client.event;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.interfaces.EventFilter;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.recognition.RecognitionException;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventManager implements org.openecard.client.common.interfaces.EventManager {

    private static final Logger _logger = LogManager.getLogger(EventManager.class.getPackage().getName());

    protected final CardRecognition cr;
    protected final Environment env;
    protected final byte[] ctx;
    protected final boolean recognize;

    private final Dispatcher dispatcher;

    protected ExecutorService threadPool;
    private Future watcher;


    public EventManager(CardRecognition cr, Environment env, byte[] ctx) {
	this.cr = cr;
	this.recognize = cr != null;
	this.env = env;
	this.ctx = ctx;
	this.dispatcher = new Dispatcher(this);
    }


    protected List<IFDStatusType> ifdStatus() throws WSException {
	GetStatus status = new GetStatus();
	status.setContextHandle(ctx);
	GetStatusResponse statusResponse = env.getIFD().getStatus(status);
	List<IFDStatusType> result;

        WSHelper.checkResult(statusResponse);
        result = statusResponse.getIFDStatus();
        return result;
    }


    private ConnectionHandleType makeConnectionHandle(String ifdName) {
	return makeConnectionHandle(ifdName, null, null);
    }

    private ConnectionHandleType makeConnectionHandle(String ifdName, BigInteger slotIdx) {
	return makeConnectionHandle(ifdName, slotIdx, null);
    }

    private ConnectionHandleType makeConnectionHandle(String ifdName, RecognitionInfo info) {
	return makeConnectionHandle(ifdName, null, info);
    }

    private ConnectionHandleType makeConnectionHandle(String ifdName, BigInteger slotIdx, RecognitionInfo info) {
	ChannelHandleType chan = new ChannelHandleType();
	chan.setSessionIdentifier(ValueGenerators.generateSessionID());
	ConnectionHandleType cHandle = new ConnectionHandleType();
	cHandle.setChannelHandle(chan);
	cHandle.setContextHandle(ctx);
	cHandle.setIFDName(ifdName);
	cHandle.setSlotIndex(slotIdx);
	cHandle.setRecognitionInfo(info);
	return cHandle;
    }

    protected ConnectionHandleType recognizeSlot(String ifdName, SlotStatusType status, boolean withRecognition) {
	// build recognition info in any way
	RecognitionInfo rInfo = null;
	if (recognize && withRecognition) {
	    try {
		rInfo = cr.recognizeCard(ifdName, status.getIndex());
	    } catch (RecognitionException ex) {
		// ignore, card is just unknown
	    }
	    // no card found build unknown structure
	}
	// in case recognition is off, or unkown card
	if (rInfo == null) {
	    rInfo = new RecognitionInfo();
	    rInfo.setCardType(ECardConstants.UNKNOWN_CARD);
	    rInfo.setCardIdentifier(status.getATRorATS());
	    XMLGregorianCalendar cal = null;
	    try {
		cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
	    } catch (DatatypeConfigurationException ex) {
		// ignore error
	    }
	    rInfo.setCaptureTime(cal);
	}
	return makeConnectionHandle(ifdName, status.getIndex(), rInfo);
    }


    private IFDStatusType getCorresponding(String ifdName, List<IFDStatusType> stati) {
	for (IFDStatusType next : stati) {
	    if (next.getIFDName().equals(ifdName)) {
		return next;
	    }
	}
	return null;
    }
    private SlotStatusType getCorresponding(BigInteger idx, List<SlotStatusType> stati) {
	for (SlotStatusType next : stati) {
	    if (next.getIndex().equals(idx)) {
		return next;
	    }
	}
	return null;
    }

    private void sendAsyncEvents(String ifdName, SlotStatusType nextSlot, EventType... types) {
        threadPool.submit(new Recognizer(this, ifdName, nextSlot, types));
    }

    protected void sendEvents(List<IFDStatusType> oldS, List<IFDStatusType> changed) {
	for (IFDStatusType next : changed) {
	    IFDStatusType counterPart = getCorresponding(next.getIFDName(), oldS);
	    // next is completely new
	    if (counterPart == null) {
		notify(EventType.TERMINAL_ADDED, makeConnectionHandle(next.getIFDName()));
		// create empty counterPart so all slots raise events
		counterPart = new IFDStatusType();
		counterPart.setIFDName(next.getIFDName());
	    }
	    // inspect every slot
	    for (SlotStatusType nextSlot : next.getSlotStatus()) {
		SlotStatusType counterPartSlot = getCorresponding(nextSlot.getIndex(), counterPart.getSlotStatus());
		if (counterPartSlot == null) {
		    // slot is new, send event when card is present
		    if (nextSlot.isCardAvailable()) {
                        sendAsyncEvents(next.getIFDName(), nextSlot, EventType.CARD_INSERTED, EventType.CARD_RECOGNIZED);
		    }
		} else {
		    // compare slot for difference
		    if (nextSlot.isCardAvailable() != counterPartSlot.isCardAvailable()) {
			if (nextSlot.isCardAvailable()) {
			    sendAsyncEvents(next.getIFDName(), nextSlot, EventType.CARD_INSERTED, EventType.CARD_RECOGNIZED);
			} else {
			    notify(EventType.CARD_REMOVED, makeConnectionHandle(next.getIFDName(), nextSlot.getIndex()));
			}
		    } else {
			// compare atr
			if (nextSlot.isCardAvailable()) {
			    if (! Arrays.equals(nextSlot.getATRorATS(), counterPartSlot.getATRorATS())) {
                                sendAsyncEvents(next.getIFDName(), nextSlot, EventType.CARD_RECOGNIZED);
			    }
			}
		    }
		}
	    }
	    // remove terminal
	    if (! next.isConnected()) {
		notify(EventType.TERMINAL_REMOVED, makeConnectionHandle(next.getIFDName()));
	    }
	}
    }


    @Override
    public synchronized Object initialize() {
	this.threadPool = Executors.newCachedThreadPool();
	// start watcher thread
	watcher = threadPool.submit(new EventRunner(this));
	// TODO: remove return value altogether
	return new ArrayList<ConnectionHandleType>();
    }

    @Override
    public synchronized void terminate() {
	watcher.cancel(true);
	this.threadPool.shutdown();
    }

    protected synchronized void notify(EventType eventType, Object eventData) {
        dispatcher.notify(eventType, eventData);
    }

    @Override
    public void register(EventCallback callback, EventFilter filter) {
        dispatcher.add(callback, filter);
    }

    @Override
    public void register(EventCallback callback, EventType type) {
        dispatcher.add(callback, type);
    }

    @Override
    public void register(EventCallback callback, List<EventType> types) {
        dispatcher.add(callback, (EventType[])types.toArray());
    }

    @Override
    public synchronized void registerAllEvents(EventCallback callback) {
        dispatcher.add(callback);
    }
    
    @Override
    public void unregister(EventCallback callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
