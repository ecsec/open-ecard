package de.ecsec.ecard.client.event;

import de.ecsec.core.common.ECardConstants;
import de.ecsec.core.common.WSHelper;
import de.ecsec.core.common.WSHelper.WSException;
import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.interfaces.Environment;
import de.ecsec.core.common.interfaces.EventCallback;
import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.recognition.CardRecognition;
import de.ecsec.core.recognition.RecognitionException;
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
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventManager implements de.ecsec.core.common.interfaces.EventManager {

    private static final Logger _logger = LogManager.getLogger(EventManager.class.getPackage().getName());

    protected final CardRecognition cr;
    protected final Environment env;
    protected final String sessionId;
    protected final byte[] ctx;
    protected final boolean recognize;

    private final EnumMap<EventType, Event> events;

    protected ExecutorService threadPool;
    private Future watcher;


    public EventManager(CardRecognition cr, Environment env, String sessionId, byte[] ctx) {
	this.cr = cr;
	this.recognize = cr != null;
	this.env = env;
	this.sessionId = sessionId;
	this.ctx = ctx;
	this.events = new EnumMap<EventType, Event>(EventType.class);
	for (EventType type : EventType.values()) {
	    events.put(type, new Event(type));
	}
    }


    protected List<IFDStatusType> ifdStatus() {
	GetStatus status = new GetStatus();
	status.setContextHandle(ctx);
	GetStatusResponse statusResponse = env.getIFD().getStatus(status);
	List<IFDStatusType> result;
	try {
	    WSHelper.checkResult(statusResponse);
	    result = statusResponse.getIFDStatus();
	} catch (WSException ex) {
	    _logger.log(Level.WARNING, "GetStatus returned with error.", ex);
	    result = new LinkedList();
	}
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
	chan.setSessionIdentifier(sessionId);
	ConnectionHandleType cHandle = new ConnectionHandleType();
	cHandle.setChannelHandle(chan);
	cHandle.setContextHandle(ctx);
	cHandle.setIFDName(ifdName);
	cHandle.setSlotIndex(slotIdx);
	cHandle.setRecognitionInfo(info);
	return cHandle;
    }

    private ConnectionHandleType recognizeSlot(String ifdName, SlotStatusType status) {
	// build recognition info in any way
	RecognitionInfo rInfo = null;
	if (recognize) {
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
			ConnectionHandleType conHandle = recognizeSlot(next.getIFDName(), nextSlot);
			notify(EventType.CARD_INSERTED, conHandle);
			notify(EventType.CARD_RECOGNIZED, conHandle);
		    }
		} else {
		    // compare slot for difference
		    if (nextSlot.isCardAvailable() != counterPartSlot.isCardAvailable()) {
			if (nextSlot.isCardAvailable()) {
			    ConnectionHandleType conHandle = recognizeSlot(next.getIFDName(), nextSlot);
			    notify(EventType.CARD_INSERTED, conHandle);
			    notify(EventType.CARD_RECOGNIZED, conHandle);
			} else {
			    notify(EventType.CARD_REMOVED, makeConnectionHandle(next.getIFDName(), nextSlot.getIndex()));
			}
		    } else {
			// compare atr
			if (nextSlot.isCardAvailable()) {
			    if (! Arrays.equals(nextSlot.getATRorATS(), counterPartSlot.getATRorATS())) {
				ConnectionHandleType conHandle = recognizeSlot(next.getIFDName(), nextSlot);
				notify(EventType.CARD_RECOGNIZED, conHandle);
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

    private synchronized void notify(EventType eventType, Object eventData) {
	Event event = events.get(eventType);
	this.threadPool.submit(new EventHandler(event, eventData));
    }

    @Override
    public synchronized void register(EventType type, EventCallback callback) {
	Event event = events.get(type);
	event.addListener(callback);
    }

    @Override
    public synchronized void register(List<EventType> types, EventCallback callback) {
	for (EventType type : types) {
	    Event event = events.get(type);
	    event.addListener(callback);
	}
    }

    @Override
    public synchronized void registerAllEvents(EventCallback callback) {
	for (EventType type : EventType.values()) {
	    Event event = events.get(type);
	    event.addListener(callback);
	}
    }

}
