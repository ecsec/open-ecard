package de.ecsec.ecard.client.event;

import de.ecsec.core.common.ECardConstants;
import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.interfaces.Environment;
import de.ecsec.core.common.interfaces.EventCallback;
import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.recognition.CardRecognition;
import de.ecsec.core.recognition.RecognitionException;
import de.ecsec.core.ws.WSMarshaller;
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
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    
    private WSMarshaller m = new WSMarshaller();
    
    private CardRecognition cr;
    private Environment env;
    private String sessionId;
    private byte[] ctx;
    private boolean recognize;
    
    private ConcurrentSkipListMap<String, IFDStatusType> statusList;
    private EnumMap<EventType, Event> events;
    
    public EventManager(CardRecognition cr, Environment env, String sessionId, byte[] ctx, boolean recognize) {
        this.cr = cr;
        this.env = env;
        this.sessionId = sessionId;
        this.ctx = ctx;
        this.recognize = recognize;
        statusList = new ConcurrentSkipListMap<String, IFDStatusType>();
        events = new EnumMap<EventType, Event>(EventType.class);
        for (EventType type : EventType.values()) {
            events.put(type, new Event(type));
        }
    }
    
    protected List<ConnectionHandleType> process() {
        List<ConnectionHandleType> cHandles = new LinkedList<ConnectionHandleType>();
        Map<String, IFDStatusType> tmpStatusList = copy(statusList);
        try {
            // start looking for changes
            doWait();
            // get current status from IFD
            GetStatus gsRequest = new GetStatus();
            gsRequest.setContextHandle(ctx);
            GetStatusResponse gsResponse = env.getIFD().getStatus(gsRequest);
            checkResult(gsResponse.getResult());
            // process GetStatusResponse
            List<IFDStatusType> currStatuses = gsResponse.getIFDStatus();
            if (statusList.isEmpty()) {
                for (IFDStatusType status : currStatuses) {
                    cHandles.addAll(newTerminal(status));
                }
                return cHandles;
            } else {
                IFDStatusType oldStatus;
                for (IFDStatusType currStatus : currStatuses) {
                    oldStatus = tmpStatusList.get(currStatus.getIFDName());
                    if (oldStatus != null) {
                        cHandles.addAll(diff(currStatus, oldStatus));
                        tmpStatusList.remove(oldStatus.getIFDName());
                    } else {
                        // new terminal added
                        cHandles.addAll(newTerminal(currStatus));
                    }
                }
                if (!tmpStatusList.isEmpty()) {
                    clear(tmpStatusList);
                }
                return cHandles;
            }
        } catch (EventException ex) {
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "process()", ex.getResultMessage(), ex);
            }
            return cHandles;
        }
    }
    
    private void clear(Map<String, IFDStatusType> map) {
        for (IFDStatusType status : map.values()) {
            notify(EventType.TERMINAL_REMOVED, makeConnectionHandle(status.getIFDName()));
            updateStatus(status.getIFDName(), status, true);
        }
    }
    
    private List<ConnectionHandleType> diff(IFDStatusType currStatus, IFDStatusType oldStatus) {
        List<ConnectionHandleType> cHandles = new LinkedList<ConnectionHandleType>();
        List<SlotStatusType> currSlotStatuses = currStatus.getSlotStatus();
        List<SlotStatusType> oldSlotStatuses = oldStatus.getSlotStatus();
        SlotStatusType oldSlotStatus = null;
        for (SlotStatusType currSlotStatus : currSlotStatuses) {
            oldSlotStatus = match(currSlotStatus.getIndex(), oldSlotStatuses);
            // since slots can not be added to or removed from a terminal,
            // the slot status should never be null
            if (oldSlotStatus == null) {
                continue;
            }
            if (currSlotStatus.isCardAvailable() && oldSlotStatus.isCardAvailable()) {
                byte[] currATR = currSlotStatus.getATRorATS();
                byte[] oldATR = oldSlotStatus.getATRorATS();
                if (currATR != null && oldATR != null) {
                    // old card removed, new card inserted
                    if (!Arrays.equals(currATR, oldATR)) {
                        notify(EventType.CARD_REMOVED, makeConnectionHandle(currStatus.getIFDName()));
                        notify(EventType.CARD_INSERTED, makeConnectionHandle(currStatus.getIFDName(), currSlotStatus.getIndex()));
                        updateStatus(currStatus.getIFDName(), currStatus);
                        cHandles.add(recognize(currStatus.getIFDName(), currSlotStatus));
                    } 
                }
            } else if (currSlotStatus.isCardAvailable() && !oldSlotStatus.isCardAvailable()) {
                notify(EventType.CARD_INSERTED, makeConnectionHandle(currStatus.getIFDName(), currSlotStatus.getIndex()));
                updateStatus(currStatus.getIFDName(), currStatus);
                cHandles.add(recognize(currStatus.getIFDName(), currSlotStatus));
            } else if (!currSlotStatus.isCardAvailable() && oldSlotStatus.isCardAvailable()) {
                notify(EventType.CARD_REMOVED, makeConnectionHandle(currStatus.getIFDName()));
                updateStatus(currStatus.getIFDName(), currStatus, true);
            } else {
                // nothing changed
            }
        }
        return cHandles;
    }
    
    private SlotStatusType match(BigInteger slotIdx, List<SlotStatusType> slotStatuses) {
        for (SlotStatusType slotStatus : slotStatuses) {
            if (slotStatus.getIndex().equals(slotIdx)) {
                return slotStatus;
            }
        }
        if (_logger.isLoggable(Level.WARNING)) {
            _logger.logp(Level.WARNING, this.getClass().getName(), "match(BigInteger slotIdx, List<SlotStatusType> slotStatuses)", "No matching slot index found.");
        }
        // no matching slot index found
        return null;
    }
    
    private List<ConnectionHandleType> newTerminal(IFDStatusType status) {
        notify(EventType.TERMINAL_ADDED, makeConnectionHandle(status.getIFDName()));
        updateStatus(status.getIFDName(), status);
        List<SlotStatusType> slotStatuses = status.getSlotStatus();
        List<ConnectionHandleType> cHandles = new ArrayList<ConnectionHandleType>(slotStatuses.size());
        for (SlotStatusType slotStatus : slotStatuses) {
            if (slotStatus.isCardAvailable()) {
                notify(EventType.CARD_INSERTED, makeConnectionHandle(status.getIFDName(), slotStatus.getIndex()));
                cHandles.add(recognize(status.getIFDName(), slotStatus));
            } else {
                cHandles.add(makeConnectionHandle(status.getIFDName()));
            }
        }
        return cHandles;
    }
    
    private ConnectionHandleType recognize(String ifdName, SlotStatusType slotStatus) {
        ConnectionHandleType cHandle = null;
        if (recognize) {
            try {
                RecognitionInfo info = cr.recognizeCard(ifdName, slotStatus.getIndex());
                cHandle = makeConnectionHandle(ifdName, slotStatus.getIndex(), info);
                notify(EventType.CARD_RECOGNIZED, cHandle);
                return cHandle;
            } catch (RecognitionException ex) {
                // do nothing here...
            }
        } 
        RecognitionInfo info = makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, slotStatus.getATRorATS());
        cHandle = makeConnectionHandle(ifdName, slotStatus.getIndex(), info);
        notify(EventType.CARD_RECOGNIZED, cHandle);
        return cHandle;
    }
    
    private Map<String, IFDStatusType> copy(Map<String, IFDStatusType> original) {
        ConcurrentSkipListMap<String, IFDStatusType> copy = new ConcurrentSkipListMap<String, IFDStatusType>();
        if (!original.isEmpty()) {
            for (Entry<String, IFDStatusType> entry : original.entrySet()) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        return copy;
    }
    
    private void doWait() {
        Thread t = new Thread(new WaitHandler(this, env));
        t.start();
    }

    private void updateStatus(String ifdName, IFDStatusType status) {
        updateStatus(ifdName, status, false);
    }
    
    private void updateStatus(String ifdName, IFDStatusType status, boolean remove) {
        if (remove) {
            statusList.remove(ifdName);
        } else {
            statusList.put(ifdName, status);
        }
    }
    
    private RecognitionInfo makeRecognitionInfo(String cardType, byte[] cardIdentifier) {
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
    
    private void notify(EventType eventType, Object eventData) {
        Event event = events.get(eventType);
        Thread t = new Thread(new EventHandler(event, eventData));
        t.start();
    }
    
    protected void checkResult(Result r) throws EventException {
        if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
            throw new EventException(r);
        }
    }
    
    public byte[] getContext() {
        return ctx;
    }

    @Override
    public Object initialize() {
        return process();
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
