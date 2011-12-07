package org.openecard.client.common.ifd.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import org.openecard.client.common.util.Helper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEOutputType {

    private final AuthDataMap authMap;

    private byte[] statusBytes;
    private byte[] ef_cardaccess;
    private byte[] car;
    private byte[] carPrev;
    private byte[] idicc;

    protected PACEOutputType(AuthDataMap authMap) {
        this.authMap = authMap;
    }


    public void setStatusbytes(byte[] statusBytes) {
        this.statusBytes = statusBytes;
    }
    public void setEF_CardAccess(byte[] ef_cardaccess) {
        this.ef_cardaccess = ef_cardaccess;
    }
    public void setCAR(byte[] car) {
        this.car = car;
    }
    public void setCARprev(byte[] carPrev) {
        this.carPrev = carPrev;
    }
    public void setIDicc(byte[] idicc) {
        this.idicc = idicc;
    }

    public DIDAuthenticationDataType getAuthDataType() {
        AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PACEOutputType());
        authResponse.addElement("StatusBytes", Helper.convByteArrayToString(statusBytes));
        authResponse.addElement("EF_CardAccess", Helper.convByteArrayToString(ef_cardaccess));
        if (car != null) {
            authResponse.addElement("CAR", Helper.convByteArrayToString(car));
        }
        if (carPrev != null) {
            authResponse.addElement("CARprev", Helper.convByteArrayToString(carPrev));
        }
        if (idicc != null) {
            authResponse.addElement("IDicc", Helper.convByteArrayToString(idicc));
        }
        return authResponse.getResponse();
    }

}
