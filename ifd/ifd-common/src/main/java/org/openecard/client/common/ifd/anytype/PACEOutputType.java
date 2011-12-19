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
    private byte[] currentCAR;
    private byte[] previousCAR;
    private byte[] idpicc;

    protected PACEOutputType(AuthDataMap authMap) {
        this.authMap = authMap;
    }

    public void setStatusbytes(byte[] statusBytes) {
        this.statusBytes = statusBytes;
    }

    public void setEF_CardAccess(byte[] ef_cardaccess) {
        this.ef_cardaccess = ef_cardaccess;
    }

    public void setCurrentCAR(byte[] car) {
        this.currentCAR = car;
    }

    public void setPreviousCAR(byte[] car) {
        this.previousCAR = car;
    }

    public void setIDPICC(byte[] idicc) {
        this.idpicc = idicc;
    }

    public DIDAuthenticationDataType getAuthDataType() {
        AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PACEOutputType());
        authResponse.addElement("StatusBytes", Helper.convByteArrayToString(statusBytes));
        authResponse.addElement("EFCardAccess", Helper.convByteArrayToString(ef_cardaccess));
        if (currentCAR != null) {
            authResponse.addElement("CurrentCAR", Helper.convByteArrayToString(currentCAR));
        }
        if (previousCAR != null) {
            authResponse.addElement("PreviousCAR", Helper.convByteArrayToString(previousCAR));
        }
        if (idpicc != null) {
            authResponse.addElement("IDPICC", Helper.convByteArrayToString(idpicc));
        }
        return authResponse.getResponse();
    }
}
