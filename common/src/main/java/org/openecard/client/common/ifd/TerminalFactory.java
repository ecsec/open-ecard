package org.openecard.client.common.ifd;

import javax.smartcardio.CardTerminals;


/**
 * TerminalFactory interface similar to javax.smartcardio.TerminalFactory, but without
 * the static factory elements which are not present in systems like Android.<br/>
 * The ecsec IFD contains a generic loader class which takes a class name from a config file
 * and executes a method with the following signature:<br/>
 * <code>public static de.ecsec.core.common.ifd.TerminalFactory getInstance();</code>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface TerminalFactory {

    /**
     * Returns the type of this TerminalFactory. Examples would be PC/SC or AndroidNFC.
     *
     * @return the type of this TerminalFactory
     */
    public String getType();

    /**
     * Returns a new CardTerminals object encapsulating the terminals
     * supported by this factory.
     * See the class comment of the {@linkplain CardTerminals} class
     * regarding how the returned objects can be shared and reused.
     *
     * @return a new CardTerminals object encapsulating the terminals
     * supported by this factory.
     */
    public CardTerminals terminals();

}
