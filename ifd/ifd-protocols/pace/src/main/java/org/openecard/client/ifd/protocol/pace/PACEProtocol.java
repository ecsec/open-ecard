/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace;

import iso.std.iso_iec._24727.tech.schema.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.ifd.anytype.PACEInputType;
import org.openecard.client.common.ifd.anytype.PACEOutputType;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.InfoUnitElementType;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Passwordfield;
import org.openecard.client.gui.definition.Radiobox;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.Textfield;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.ifd.protocol.exception.ProtocolException;
import org.openecard.ws.IFD;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class PACEProtocol implements Protocol {

    private static final Logger logger = Logger.getLogger("PACE");
    private SecureMessaging sm;

    @Override
    public EstablishChannelResponse establish(EstablishChannel req, IFD ifd, UserConsent gui) {
        DIDAuthenticationDataType parameter = req.getAuthenticationProtocolData();
        EstablishChannelResponse response = new EstablishChannelResponse();

        try {
            // Get parameters
            PACEInputType paceParam = new PACEInputType(parameter);
            byte passwordType = paceParam.getPINID();
            byte[] chat = paceParam.getCHAT();
            byte[] pin = null;
            if(paceParam.getPIN()==null||paceParam.getPIN().trim().isEmpty()){
        	pin = this.getPINFromUser(gui);	
            } else {
        	pin = paceParam.getPIN().getBytes("ISO-8859-1");
            }
            byte[] slotHandle = req.getSlotHandle();

            // Read EF.CardAccess
            Transmit t = CardCommands.Select.makeTransmit(slotHandle, CardCommands.Select.MF());
            TransmitResponse tr = ifd.transmit(t);
            WSHelper.checkResult(tr);

            t = CardCommands.Select.makeTransmit(slotHandle, CardCommands.Select.EF(StringUtils.toByteArray("011C")));
            tr = ifd.transmit(t);
            WSHelper.checkResult(tr);

            int i = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte length = (byte) 0xDF;
            while (true) {
                t = CardCommands.Read.makeTransmit(slotHandle, CardCommands.Read.binary((short) (i * (length & 0xFF)), length));
                tr = ifd.transmit(t);
                try {
                    byte[] data = tr.getOutputAPDU().get(0);

                    baos.write(CardCommands.getDataFromResponse(data));

                    WSHelper.checkResult(tr);

                    //FIXME
                    if (Arrays.equals(CardCommands.getResultFromResponse(data), new byte[]{(byte) 0x62, (byte) 0x82})) {
                        break;
                    }
                } catch (IOException ex) {
                    //TODO
                } catch (WSException ex) {
                    baos.close();
                    break;
                }
                i++;
            }

            byte[] efcadata = baos.toByteArray();

            // Parse SecurityInfos
            SecurityInfos sis = SecurityInfos.getInstance(efcadata);
            EFCardAccess efca = new EFCardAccess(sis);
            PACESecurityInfos psi = efca.getPACESecurityInfos();

            // Start PACE
            PACEImplementation pace = new PACEImplementation(ifd, slotHandle, psi);
            pace.execute(pin, chat, passwordType);

            // Establish Secure Messaging channel
            sm = new SecureMessaging(pace.getKeyMAC(), pace.getKeyENC());

            // Create response
            PACEOutputType authDataResponse = paceParam.getOutputType();
            authDataResponse.setEFCardAccess(efcadata);
            authDataResponse.setCurrentCAR(pace.getCurrentCAR());
            authDataResponse.setPreviousCAR(pace.getPreviousCAR());
            authDataResponse.setIDPICC(pace.getIDPICC());
            authDataResponse.setRetryCounter(pace.getRetryCounter());

            response.setResult(WSHelper.makeResultOK());
            response.setAuthenticationProtocolData(authDataResponse.getAuthDataType());

            return response;
        } catch (UnsupportedEncodingException ex) {
            // Cannot encode the PIN in ISO-8859-1 charset
            return WSHelper.makeResponse(
                    EstablishChannelResponse.class,
                    WSHelper.makeResultError(ECardConstants.Minor.IFD.UNKNOWN_PIN_FORMAT, "Cannot encode the PIN in ISO-8859-1 charset"));
        } catch (ProtocolException ex) {
            //TODO
            return WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResult(ex));
        } catch (Throwable t) {
            //TODO
            return WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResult(t));
        }
    }

    private byte[] getPINFromUser(UserConsent gui) {
	//TODO localization
	UserConsentDescription uc = new UserConsentDescription("openecard");
	Step s3 = new Step("PIN-Eingabe");
	
	Text i9 = new Text();
	i9.setText("Wenn Sie mit der Übermittlung der ausgewählten Daten einverstanden sind, geben Sie bitte Ihre 6-stellige Personalausweis-PIN ein\n");
	s3.getInputInfoUnits().add(i9);
	
	Text i10 = new Text();
	i10.setText("Erfolderliche Eingabe: Personalausweis-PIN\nNoch offene Versuche: 3\nKartentyp: Personalausweis\nKartenlesegerät: NFC\n");
	s3.getInputInfoUnits().add(i10);
	
	Textfield f1 = new Textfield();
	f1.setName("PIN:\t");
	s3.getInputInfoUnits().add(f1);
	
	uc.getSteps().add(s3);
	
	UserConsentNavigator navi = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navi);
        exec.process();
        
       Textfield t =  (Textfield) exec.getResults().get("PIN-Eingabe").getResults().get(0);
       //TODO error handling
       return t.getValue().getBytes();
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
        try {
            return sm.encrypt(commandAPDU);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception", ex);
        }
        return null;
    }

    @Override
    public byte[] removeSM(byte[] responseAPDU) {
        try {
            return sm.decrypt(responseAPDU);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception", ex);
        }
        return null;
    }
}
