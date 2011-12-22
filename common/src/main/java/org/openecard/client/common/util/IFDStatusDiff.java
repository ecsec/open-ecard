package org.openecard.client.common.util;

import org.openecard.client.common.logging.LogManager;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IFDStatusDiff {

    private static final Logger _logger = LogManager.getLogger(IFDStatusDiff.class.getName());
    
    private final List<IFDStatusType> expected;
    private List<IFDStatusType> result;
    private List<IFDStatusType> deleted;

    public IFDStatusDiff(List<IFDStatusType> expected) {
        this.expected = expected;
    }

    
    public static IFDStatusDiff diff(List<IFDStatusType> expected, List<IFDStatusType> others, boolean withNew) {
        IFDStatusDiff diff = new IFDStatusDiff(expected);
        diff.diff(others, withNew);
        return diff;
    }

    public void diff(List<IFDStatusType> others, boolean withNew) {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "diff(List<IFDStatusType> others, boolean withNew)", new Object[]{others, withNew});
        } // </editor-fold>
        result = new LinkedList<IFDStatusType>();
        deleted = new LinkedList<IFDStatusType>(expected);

        for (IFDStatusType next : others) {
            String nextName = next.getIFDName();
            if (expectedContains(nextName)) {
                // check for terminal connection
                IFDStatusType other = expectedGet(nextName);
                Boolean otherCard = other.isConnected();
                boolean otherCardB = (otherCard == null) ? false : otherCard.booleanValue();
                Boolean thisCard = next.isConnected();
                boolean thisCardB = (thisCard == null) ? false : thisCard.booleanValue();
                if (thisCardB != otherCardB) {
                    result.add(next);
                } else {
                    // check for card insertions or removals
                    if (other.getSlotStatus().size() == next.getSlotStatus().size()) {
                        for (SlotStatusType nextSlot : next.getSlotStatus()) {
                            BigInteger thisSlotIdx = nextSlot.getIndex();
                            SlotStatusType otherSlot = null;
                            for (SlotStatusType nextOtherSlot : other.getSlotStatus()) {
                                if (thisSlotIdx.equals(nextOtherSlot.getIndex())) {
                                    otherSlot = nextOtherSlot;
                                    break;
                                }
                            }
                            // no equivalent slot, only occurs on evolving terminals ;-)
                            if (otherSlot == null) {
                                result.add(next);
                                break;
                            }
                            // compare card status of slot
                            if (nextSlot.isCardAvailable() != otherSlot.isCardAvailable()) {
                                result.add(next);
                                break;
                            }
                            // compare if there are different cards in slot
                            byte[] nextATR = nextSlot.getATRorATS();
                            byte[] otherATR = otherSlot.getATRorATS();
                            if (!(nextATR == null && otherATR == null) && !(IFDStatusDiff.arrayEquals(nextATR, otherATR))) {
                                result.add(next);
                                break;
                            }
                        }
                    }
                }
                // delete card from deleted list
                deleted.remove(expectedGet(nextName));
            } else if (withNew) {
                result.add(next);
            }
        }

        merge(deleted);

        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "diff(List<IFDStatusType> others, boolean withNew)");
        } // </editor-fold>
    }

    private static boolean arrayEquals(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return false;
        } else {
            boolean result = Arrays.equals(a, b);
            return result;
        }
    }

    private IFDStatusType expectedGet(String ifdName) {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "expectedGet(String ifdName)", ifdName);
        } // </editor-fold>
        for (IFDStatusType s : expected) {
            if (s.getIFDName().equals(ifdName)) {
                // <editor-fold defaultstate="collapsed" desc="log trace">
                if (_logger.isLoggable(Level.FINER)) {
                    _logger.exiting(this.getClass().getName(), "expectedGet(String ifdName)", s);
                } // </editor-fold>
                return s;
            }
        }
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "expectedGet(String ifdName)", null);
        } // </editor-fold>
        return null;
    }

    private boolean expectedContains(String ifdName) {
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "expectedContains(String ifdName)", ifdName);
        } // </editor-fold>
        Boolean b = expectedGet(ifdName) != null;
        // <editor-fold defaultstate="collapsed" desc="log trace">
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "expectedContains(String ifdName)", b);
        } // </editor-fold>
        return b.booleanValue();
    }

    
    private List<IFDStatusType> merge(List<IFDStatusType> deleted) {
        ArrayList<IFDStatusType> states = new ArrayList<IFDStatusType>(result.size() + deleted.size());
        states.addAll(result);
        // transform all deleted terminals so it is clear they disappeared
        for (IFDStatusType next : deleted) {
            IFDStatusType s = new IFDStatusType();
            s.setIFDName(next.getIFDName());
            s.setConnected(Boolean.FALSE);
            // slots must be present too
            for (SlotStatusType nextST : next.getSlotStatus()) {
                SlotStatusType st = new SlotStatusType();
                st.setIndex(nextST.getIndex());
                st.setCardAvailable(Boolean.FALSE);
                s.getSlotStatus().add(st);
            }
            // add new deleted terminal type to states list
            states.add(s);
        }
        this.result = states;
        return states;
    }

    public boolean hasChanges() {
        if (this.result != null && !this.result.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public List<IFDStatusType> result() {
        return result;
    }
}
