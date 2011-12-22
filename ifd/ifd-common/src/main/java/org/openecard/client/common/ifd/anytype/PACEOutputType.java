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
    private byte[] efCardAccess;
    private byte[] currentCAR;
    private byte[] previousCAR;
    private byte[] idpicc;

    protected PACEOutputType(AuthDataMap authMap) {
        this.authMap = authMap;
    }

    public void setStatusbytes(byte[] statusBytes) {
        this.statusBytes = statusBytes;
    }

    public void setEFCardAccess(byte[] efCardAccess) {
        this.efCardAccess = efCardAccess;
    }

    public void setCurrentCAR(byte[] car) {
        this.currentCAR = car;
    }

    public void setPreviousCAR(byte[] car) {
        this.previousCAR = car;
    }

    public void setIDICC(byte[] idicc) {
        this.idpicc = idicc;
    }

    public DIDAuthenticationDataType getAuthDataType() {
        AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PACEOutputType());
        authResponse.addElement("StatusBytes", Helper.convByteArrayToString(statusBytes));
        authResponse.addElement("EF_CardAccess", Helper.convByteArrayToString(efCardAccess));
        if (currentCAR != null) {
            authResponse.addElement("CARcurr", Helper.convByteArrayToString(currentCAR));
        }
        if (previousCAR != null) {
            authResponse.addElement("CARprev", Helper.convByteArrayToString(previousCAR));
        }
        if (idpicc != null) {
            authResponse.addElement("IDicc", Helper.convByteArrayToString(idpicc));
        }
        return authResponse.getResponse();
    }
}
