package de.ecsec.ecard.client.event;

import de.ecsec.core.common.ECardConstants;
import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.interfaces.Environment;
import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.recognition.RecognitionException;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class WaitHandler implements Runnable {

    private static final Logger _logger = LogManager.getLogger(WaitHandler.class.getPackage().getName());
    private EventManager manager;
    private Environment env;

    public WaitHandler(EventManager manager, Environment env) {
        this.manager = manager;
        this.env = env;
    }

    @Override
    public void run() {
        try {
            Wait wRequest = new Wait();
            wRequest.setContextHandle(manager.getContext());
            WaitResponse wResponse = env.getIFD().wait(wRequest);
            
            manager.checkResult(wResponse.getResult());
            manager.doWait();
            List<IFDStatusType> statuses = wResponse.getIFDEvent();
            if (statuses != null && !statuses.isEmpty()) {
                IFDStatusType oldStatus;
                List<SlotStatusType> slotStatuses;
                List<SlotStatusType> oldSlotStatuses;
                for (IFDStatusType status : statuses) {
                    oldStatus = manager.getStatus(status.getIFDName());
                    slotStatuses = status.getSlotStatus();
                    // if null is returned, a new terminal has been added
                    if (oldStatus == null) {
                        manager.notify(EventType.TERMINAL_ADDED, manager.makeConnectionHandle(status.getIFDName()));
                        for (SlotStatusType slotStatus : slotStatuses) {
                            // check if a card is available
                            if (slotStatus.isCardAvailable()) {
                                manager.notify(EventType.CARD_INSERTED, manager.makeConnectionHandle(status.getIFDName(), slotStatus.getIndex(), manager.makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, null)));
                                recognize(status.getIFDName(), slotStatus.getIndex());
                            }
                        }
                        manager.updateStatus(status.getIFDName(), status);
                    } else {
                        if (status.isConnected()) {
                            BigInteger slotIdx;
                            oldSlotStatuses = oldStatus.getSlotStatus();
                            for (SlotStatusType slotStatus : slotStatuses) {
                                slotIdx = slotStatus.getIndex();
                                for (SlotStatusType oldSlotStatus : oldSlotStatuses) {
                                    if (slotIdx.equals(oldSlotStatus.getIndex())) {
                                        if (slotStatus.isCardAvailable() == true && oldSlotStatus.isCardAvailable() == false) {
                                            // card has been inserted
                                            manager.notify(EventType.CARD_INSERTED, manager.makeConnectionHandle(status.getIFDName(), slotStatus.getIndex(), manager.makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, null)));
                                            recognize(status.getIFDName(), slotIdx);
                                        } else if (slotStatus.isCardAvailable() == false && oldSlotStatus.isCardAvailable() == true) {
                                            // card has been removed
                                            manager.notify(EventType.CARD_REMOVED, manager.makeConnectionHandle(status.getIFDName()));
                                        } else {
                                            // nothing changed
                                        }
                                        break;
                                    }
                                }
                            }
                            manager.updateStatus(status.getIFDName(), status);
                        } else {
                            // terminal has been removed
                            manager.notify(EventType.TERMINAL_REMOVED, manager.makeConnectionHandle(status.getIFDName()));
                            manager.updateStatus(status.getIFDName(), status, true);
                        }
                    }
                }
            }
        } catch (EventException ex) {
            _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getResultMessage(), ex);
            manager.doWait();
        }
    }

    private void recognize(String ifdName, BigInteger slotIdx) {
        try {
            RecognitionInfo info = manager.getCardRecognition().recognizeCard(ifdName, slotIdx);
            if (info != null) {
                manager.notify(EventType.CARD_RECOGNIZED, manager.makeConnectionHandle(ifdName, slotIdx, info));
            } else {
                manager.notify(EventType.CARD_INSERTED, manager.makeConnectionHandle(ifdName, slotIdx, manager.makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, null)));
            }
        } catch (RecognitionException ex) {
            _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getMessage(), ex);
            manager.notify(EventType.CARD_INSERTED, manager.makeConnectionHandle(ifdName, slotIdx, manager.makeRecognitionInfo(ECardConstants.UNKNOWN_CARD, null)));
        }
    }
}
