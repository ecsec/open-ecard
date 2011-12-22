package org.openecard.client.common.ifd.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEInputType {

    private final AuthDataMap authMap;
    private final byte pinID;
    private final byte[] chat;
    private final String pin;
    private final byte[] certDesc;

    public PACEInputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException {
        authMap = new AuthDataMap(baseType);

        pinID = authMap.getContentAsBytes("PinID")[0];
        // optional elements
        chat = authMap.getContentAsBytes("CHAT");
        pin = authMap.getContentAsString("PIN");
        certDesc = authMap.getContentAsBytes("CertificateDescription");
    }

    public byte getPINID() {
        return pinID;
    }

    public byte[] getCHAT() {
        return chat;
    }

    public String getPIN() {
        return pin;
    }

    public byte[] getCertificateDescription() {
        return certDesc;
    }

    public PACEOutputType getOutputType() {
        return new PACEOutputType(authMap);
    }
}
