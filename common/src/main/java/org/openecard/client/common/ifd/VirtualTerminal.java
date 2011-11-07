package org.openecard.client.common.ifd;

import java.math.BigInteger;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface VirtualTerminal {

    /**
     * Write a message to the underlying device with infinite timeout.
     *
     * @param msg Message to be displayed
     * @return Number associated with the asynchronous process, must be used in wait
     */
    public int displayMessage(String msg);
    
    /**
     * Write a message to the underlying device. Don't block.
     *
     * @param msg Message to be displayed
     * @param timeout Value in milliseconds, -1 for infinite.
     * @return Number associated with the asynchronous process, must be used in wait
     */
    public int displayMessage(String msg, BigInteger timeout);

    /**
     * Finish when the last message returned
     *
     * @param procNum Wait for specified process number to finish message display
     */
    public void waitForMsg(int procNum);
    
    /**
     * Cancel running action identified by procNum
     *
     * @param procNum Number associated with the asynchronous process
     */
    public void cancel(int procNum);


    /**
     * Beep once. This must not block.
     */
    public void beep();


    /**
     * Blink once. This must not block.
     */
    public void blink();


    /**
     * Capture pin from user and return it if entered successfully. This must block.
     * 
     * @param msg Message to display
     * @param firstTimeout Timeout until first key. Null for default.
     * @param otherTimeout Timeout after first key. Null for default.
     * @return captured pin or appropriate error type
     */
    public VirtualPinResult requestPIN(String msg, BigInteger firstTimeout, BigInteger otherTimeout);

}
