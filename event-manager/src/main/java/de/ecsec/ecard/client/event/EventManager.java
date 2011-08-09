package de.ecsec.ecard.client.event;

import de.ecsec.core.common.ECardConstants;
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
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import oasis.names.tc.dss._1_0.core.schema.Result;

/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class EventManager implements de.ecsec.core.common.interfaces.EventManager {

    private static final Logger _logger = LogManager.getLogger(EventManager.class.getPackage().getName());
    
    private CardRecognition cr;
    private Environment env;
    private String sessionId;
    private byte[] ctx;
    private volatile ConcurrentSkipListMap<String, IFDStatusType> statusList;
    private EnumMap<EventType, Event> events;
    
    public EventManager(CardRecognition cr, Environment env, String sessionId, byte[] ctx) {
        this.cr = cr;
        this.env = env;
        this.sessionId = sessionId;
        this.ctx = ctx;
        statusList = new ConcurrentSkipListMap<String, IFDStatusType>();
        events = new EnumMap<EventType, Event>(EventType.class);
        for (EventType type : EventType.values()) {
            events.put(type, new Event(type));
        }
    }
    
    public List<ConnectionHandleType> getInitialStatus() {
        List<ConnectionHandleType> cHandles = new LinkedList<ConnectionHandleType>();
        try {
            // get current status from IFD
            GetStatus gsRequest = new GetStatus();
            gsRequest.setContextHandle(ctx);
            GetStatusResponse gsResponse = env.getIFD().getStatus(gsRequest);
            checkResult(gsResponse.getResult());
            // start looking for changes
            doWait();
            // process GetStatusResponse
            List<IFDStatusType> statuses = gsResponse.getIFDStatus();
            if (statuses != null && !statuses.isEmpty()) {
                String ifdName;
                RecognitionInfo info;
                List<SlotStatusType> slotStatuses;
                for (IFDStatusType status : statuses) {
                    slotStatuses = status.getSlotStatus();
                    ifdName = status.getIFDName();
                    statusList.put(ifdName, status);
                    for (SlotStatusType slotStatus : slotStatuses) {
                        if (slotStatus.isCardAvailable()) {
                            try {
                                info = cr.recognizeCard(ifdName, slotStatus.getIndex());
                                if (info != null) {
                                    cHandles.add(makeConnectionHandle(ifdName, slotStatus.getIndex(), info));
                                } else {
                                    cHandles.add(makeConnectionHandle(ifdName, slotStatus.getIndex(), makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, null)));
                                }
                            } catch (RecognitionException ex) {
                                _logger.logp(Level.WARNING, this.getClass().getName(), "getCurrentStatus()", ex.getMessage(), ex);
                                cHandles.add(makeConnectionHandle(ifdName, slotStatus.getIndex(), makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, null)));
                            }
                        } else {
                            cHandles.add(makeConnectionHandle(ifdName));
                        }
                    }
                }
                return cHandles;
            } else {
                // no terminals connected, return empty list
                return cHandles;
            }
        } catch (EventException ex) {
            _logger.logp(Level.WARNING, this.getClass().getName(), "getCurrentStatus()", ex.getResultMessage(), ex);
            return cHandles;
        }
    }
    
    public CardRecognition getCardRecognition() {
        return cr;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public byte[] getContext() {
        return ctx;
    }
    
    protected void doWait() {
        Thread t = new Thread(new WaitHandler(this, env));
        t.start();
    }

    protected IFDStatusType getStatus(String ifdName) {
        return statusList.get(ifdName);
    }
    
    protected void updateStatus(String ifdName, IFDStatusType status) {
        updateStatus(ifdName, status, false);
    }
    
    protected void updateStatus(String ifdName, IFDStatusType status, boolean remove) {
        if (remove) {
            statusList.remove(ifdName);
        } else {
            statusList.put(ifdName, status);
        }
    }
    
    protected RecognitionInfo makeRecognitionInfo(String cardType, byte[] cardIdentifier) {
        RecognitionInfo info = new RecognitionInfo();
        XMLGregorianCalendar cal = null;
        try {
            DatatypeFactory factory = DatatypeFactory.newInstance();
            cal = factory.newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException ex) {
            // do nothing here
        }
        info.setCaptureTime(cal);
        info.setCardType(cardType);
        info.setCardIdentifier(cardIdentifier);
        return info;
    }

    protected ConnectionHandleType makeConnectionHandle(String ifdName) {
        return makeConnectionHandle(ifdName, null, null);
    }
    
    protected ConnectionHandleType makeConnectionHandle(String ifdName, RecognitionInfo info) {
        return makeConnectionHandle(ifdName, null, info);
    }
    
    protected ConnectionHandleType makeConnectionHandle(String ifdName, BigInteger slotIdx, RecognitionInfo info) {
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
    
    protected void checkResult(Result r) throws EventException {
        if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
            throw new EventException(r);
        }
    }
    
    protected void notify(EventType eventType, Object eventData) {
        Event event = events.get(eventType);
        event.notify(eventData);
    }

    @Override
    public Object initialize() {
        return getInitialStatus();
    }
    
    @Override
    public void register(EventType type, EventCallback callback) {
        Event event = events.get(type);
        event.addListener(callback);
    }

    @Override
    public void register(List<EventType> types, EventCallback callback) {
        Event event;
        for (EventType type : types) {
            event = events.get(type);
            event.addListener(callback);
        }
    }

    @Override
    public void registerAllEvents(EventCallback callback) {
        Event event;
        for (EventType type : EventType.values()) {
            event = events.get(type);
            event.addListener(callback);
        }
    }
}
