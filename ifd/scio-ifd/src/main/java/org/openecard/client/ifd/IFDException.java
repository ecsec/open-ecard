package org.openecard.client.ifd;

import org.openecard.client.common.ECardException;
import oasis.names.tc.dss._1_0.core.schema.Result;


/**
 * Exception class for IFD layer.
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IFDException extends ECardException {

    public IFDException(String msg) {
	makeException(this, msg);
    }

    public IFDException(String minor, String msg) {
	makeException(this, minor, msg);
    }

    public IFDException(Result r) {
	makeException(this, r);
    }

    public IFDException(Throwable cause) {
	makeException(this, cause);
    }

}
