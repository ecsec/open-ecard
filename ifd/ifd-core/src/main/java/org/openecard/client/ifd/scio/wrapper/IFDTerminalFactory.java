package org.openecard.client.ifd.scio.wrapper;

import org.openecard.client.common.GenericFactoryException;
import org.openecard.client.ifd.scio.IFDException;
import org.openecard.client.ifd.scio.IFDProperties;
import org.openecard.client.common.GenericFactory;
import org.openecard.client.common.ifd.TerminalFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IFDTerminalFactory {

    private GenericFactory<TerminalFactory> factory;

    public IFDTerminalFactory() throws IFDException {
        try {
            factory = new GenericFactory<TerminalFactory>(IFDProperties.properties(), "org.openecard.ifd.scio.factory.impl");
        } catch (GenericFactoryException ex) {
            throw new IFDException(ex);
        }
    }


    private static IFDTerminalFactory factoryInst = null;

    public static TerminalFactory getInstance() throws IFDException {
        if (factoryInst == null) {
            factoryInst = new IFDTerminalFactory();
        }

        try {
            return factoryInst.factory.getInstance();
        } catch (GenericFactoryException ex) {
            throw new IFDException(ex);
        }
    }

}
