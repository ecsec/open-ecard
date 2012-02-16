/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.EAC2OutputType;
import org.openecard.client.common.sal.anytype.EACAdditionalInputType;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.iso7816.FCP;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.ifd.protocol.pace.NPACardCommands;
import org.openecard.ws.IFD;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class ChipAuthenticationStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private IFD ifd;
    private byte[] slotHandle;
    
    public ChipAuthenticationStep(IFD ifd) {
	this.ifd = ifd;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }
    
    private ResponseAPDU transmitSingleAPDU(byte[] apdu) throws WSException {
        ArrayList<byte[]> responses = new ArrayList<byte[]>() {
            {
                add(new byte[]{(byte) 0x90, (byte) 0x00});
            }
        };

        Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
        TransmitResponse tr = (TransmitResponse) WSHelper.checkResult(ifd.transmit(t));
        return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }
    
    
    private byte[] readFile(byte[] FID, byte[] slotHandle) throws IOException, TLVException, WSException {
	// 1. READ FCP and get length, 2. SELECT FILE, 3. READ IN LOOP

	ResponseAPDU rapdu = this.transmitSingleAPDU(CardCommands.Select.EF_FCP(FID));
	FCP fcp = new FCP(rapdu.getData());
	long length = fcp.getNumBytes();
	this.transmitSingleAPDU(CardCommands.Select.EF(FID));
	ByteArrayOutputStream fileContent = new ByteArrayOutputStream((int) length);

	for (short offset = 0; length > 0; offset += 255, length -= 255) {
	    byte[] apdu = CardCommands.Read.binary(offset, (short) ((length >= 255) ? 255 : length));
	    rapdu = this.transmitSingleAPDU(apdu);
	    fileContent.write(rapdu.getData());
	}

	return fileContent.toByteArray();
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	try {
	    EACAdditionalInputType eacAddInput = new EACAdditionalInputType(didAuthenticate.getAuthenticationProtocolData());
	    this.slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle();
	    
	    this.transmitSingleAPDU(NPACardCommands.externalAuthentication(eacAddInput.getSignature()));    
	    
	    byte[] FID_EFCARDSECURITY = new byte[] { 0x01, 0x1D }; //TODO should be stored somewhere else
	    byte[] efCardSecurity = readFile(FID_EFCARDSECURITY, didAuthenticate.getConnectionHandle().getSlotHandle());

	    this.transmitSingleAPDU(NPACardCommands.mseSetAT(null, new byte[] { 0x41 }));

	    ResponseAPDU rapdu = this.transmitSingleAPDU(NPACardCommands
		    .generalAuthenticate((byte) 0x00, (byte) 0x80, StringUtils.toByteArray("04" + internalData.get("pubkey"))));

	    TLV tlv = TLV.fromBER(rapdu.getData());
	    byte[] nonce = tlv.findChildTags(0x81).get(0).getValue();
	    byte[] token = tlv.findChildTags(0x82).get(0).getValue();

	    DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();

	    Result r = new Result();
	    r.setResultMajor(ECardConstants.Major.OK);
	    didAuthenticateResponse.setResult(r);

	    EAC2OutputType eacadditionaloutput = new EAC2OutputType(didAuthenticate, ByteUtils.toHexString(efCardSecurity),
		    ByteUtils.toHexString(token), ByteUtils.toHexString(nonce));

	    didAuthenticateResponse.setAuthenticationProtocolData(eacadditionaloutput.getAuthDataType());

	    DestroyChannel destroyChannel = new DestroyChannel(); //disable SecureMessaging
	    destroyChannel.setSlotHandle(didAuthenticate.getConnectionHandle().getSlotHandle());
	    ifd.destroyChannel(destroyChannel);
	    return didAuthenticateResponse;
	} catch (Exception e) {
	    e.printStackTrace();
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultUnknownError(e.getMessage()));
	}
    }

}
