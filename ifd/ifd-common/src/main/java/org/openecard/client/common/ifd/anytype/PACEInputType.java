package org.openecard.client.common.ifd.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEInputType {

    private final AuthDataMap authMap;
    private final byte passwordType;
    private final byte[] chat;
    private final String password;
    private final byte[] certDesc;

    public PACEInputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException {
        authMap = new AuthDataMap(baseType);

        passwordType = authMap.getContentAsBytes("PasswordType")[0];
        // optional elements
        chat = authMap.getContentAsBytes("CHAT");
        password = authMap.getContentAsString("Password");
        certDesc = authMap.getContentAsBytes("CertificateDescription");
    }

    public byte getPasswordType() {
        return passwordType;
    }

    public byte[] getCHAT() {
        return chat;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getCertificateDescription() {
        return certDesc;
    }

    public PACEOutputType getOutputType() {
        return new PACEOutputType(authMap);
    }
}
