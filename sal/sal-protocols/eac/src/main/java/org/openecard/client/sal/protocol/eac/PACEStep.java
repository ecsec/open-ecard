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
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.net.MalformedURLException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.EAC1InputType;
import org.openecard.client.common.sal.anytype.EAC1OutputType;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CHAT.DataGroup;
import org.openecard.client.crypto.common.asn1.cvc.CHAT.SpecialFunction;
import org.openecard.client.crypto.common.asn1.cvc.CHAT.TerminalType;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.Hyperlink;
import org.openecard.client.gui.definition.InfoUnitElementType;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.ws.IFD;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class PACEStep implements ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {

    private IFD ifd;
    private UserConsent gui;

    public PACEStep(IFD ifd, UserConsent gui) {
	this.ifd = ifd;
	this.gui = gui;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDAuthenticate;
    }

    @Override
    public DIDAuthenticateResponse perform(DIDAuthenticate didAuthenticate, Map<String, Object> internalData) {
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document d = builder.newDocument();

	    EAC1InputType eac1input = new EAC1InputType(didAuthenticate.getAuthenticationProtocolData());

	    CertificateDescription description = new CertificateDescription(TLV.fromBER(eac1input.getCertificateDescription()));
	    CHAT requiredCHAT = new CHAT(TLV.fromBER(eac1input.getReuiredCHAT()));
	    CHAT optionalCHAT = new CHAT(TLV.fromBER(eac1input.getOptionalCHAT()));
	    CHAT chosenCHAT = this.showUserConsentAndRetrieveCHAT(description, requiredCHAT, optionalCHAT,
		    eac1input.getCertificates().get(0));

	    EstablishChannel establishChannel = new EstablishChannel();
	    establishChannel.setSlotHandle(didAuthenticate.getConnectionHandle().getSlotHandle());

	    DIDAuthenticationDataType establishChannelInput = new DIDAuthenticationDataType();
	    establishChannelInput.setProtocol(ECardConstants.Protocol.PACE);

	    Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "PinID");
	    e.setTextContent("3"); // Personalausweis-PIN
	    establishChannelInput.getAny().add(e);

	    e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "CertificateDescription");
	    e.setTextContent(ByteUtils.toHexString(eac1input.getCertificateDescription()));
	    establishChannelInput.getAny().add(e);

	    e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "CHAT");
	    e.setTextContent(chosenCHAT.toString());
	    establishChannelInput.getAny().add(e);

	    establishChannel.setAuthenticationProtocolData(establishChannelInput);

	    internalData.put("eServiceCertificate", eac1input.getCertificates().get(0));
	    internalData.put("authenticatedAuxiliaryData", eac1input.getAuthenticatedAuxiliaryData());

	    EstablishChannelResponse establishChannelResponse = ifd.establishChannel(establishChannel);
	    DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();
	    didAuthenticateResponse.setResult(establishChannelResponse.getResult());

	    EAC1OutputType eac1out = new EAC1OutputType(didAuthenticate.getAuthenticationProtocolData(),establishChannelResponse.getAuthenticationProtocolData(),
		    chosenCHAT.getBytes());
	   

	    didAuthenticateResponse.setAuthenticationProtocolData(eac1out.getAuthDataType());
	    didAuthenticateResponse.getAuthenticationProtocolData().setProtocol(null);

	    internalData.put("CAR", eac1out.getCertificationAuthorityReference());

	    return didAuthenticateResponse;
	} catch (Exception e) {
	    e.printStackTrace();
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultUnknownError(e.getMessage()));
	}
    }

    private CHAT showUserConsentAndRetrieveCHAT(CertificateDescription description, CHAT requiredCHAT, CHAT optionalCHAT,
	    CardVerifiableCertificate cvc) {
	//TODO localisation
	UserConsentDescription ucd = new UserConsentDescription("test");
	Step s1 = new Step("Anbieterinformationen");
	ucd.getSteps().add(s1);
	Text i1 = new Text();
	i1.setText("Name des Diensteanbieters:\n" + description.getSubjectName() + "\n");
	s1.getInputInfoUnits().add(i1);

	Text i2 = new Text();
	i2.setText("Internetadresse des Diensteanbieters:");
	s1.getInputInfoUnits().add(i2);

	try {
	    Hyperlink h1 = new Hyperlink();
	    h1.setHref(description.getSubjectURL());
	    s1.getInputInfoUnits().add(h1);
	} catch (MalformedURLException e1) {
	    // Fallback to Textouput
	    Text h1 = new Text();
	    h1.setText(description.getSubjectURL() + "\n");
	    s1.getInputInfoUnits().add(h1);
	}

	Text i3 = new Text();
	i3.setText("\nAngaben des Diensteanbieters:\n" + description.getTermsOfUsage() + "\n");
	s1.getInputInfoUnits().add(i3);

	Text i6 = new Text();
	i6.setText("Die Berechtigung zur Abfrage von Daten ist gültig\nvon: " + cvc.getCertificateEffectiveDate() + "\nbis: "
		+ cvc.getCertificateExpirationDate() + "\n");
	s1.getInputInfoUnits().add(i6);

	Text i7 = new Text();
	i7.setText("Aussteller des Berechtigungszertifikats:\n" + description.getIssuerName() + "\n");
	s1.getInputInfoUnits().add(i7);

	Text i8 = new Text();
	i8.setText("Internetadresse des Ausstellers:");
	s1.getInputInfoUnits().add(i8);

	try {
	    Hyperlink h2 = new Hyperlink();
	    h2.setHref(description.getIssuerURL());
	    s1.getInputInfoUnits().add(h2);
	} catch (MalformedURLException e1) {
	    // Fallback to Textouput
	    Text h2 = new Text();
	    h2.setText(description.getIssuerURL() + "\n");
	    s1.getInputInfoUnits().add(h2);
	}

	Step s2 = new Step("Angefragte Daten");
	ucd.getSteps().add(s2);
	
	Text i9 = new Text();
	i9.setText("Für den genannten Zweck bitten wir Sie, die folgenden Daten aus Ihrem Personalausweis zu übermitteln:\n");
	s2.getInputInfoUnits().add(i9);
	
	
	Checkbox i4 = new Checkbox();
	s2.getInputInfoUnits().add(i4);
	
	for(int i = 0;i< requiredCHAT.getReadAccess().length;i++){
	    if(requiredCHAT.getReadAccess()[i]){
		BoxItem bi1 = new BoxItem();
		i4.getBoxItems().add(bi1);
		bi1.setName(DataGroup.values()[i].name());
		bi1.setChecked(true);
		bi1.setDisabled(false);
		bi1.setText(DataGroup.values()[i].toString());
	    }
	}
	
	for(int i = 0;i< requiredCHAT.getSpecialFunctions().length;i++){
	    
	    if(requiredCHAT.getSpecialFunctions()[i]){
		BoxItem bi1 = new BoxItem();
		i4.getBoxItems().add(bi1);
		bi1.setName(SpecialFunction.values()[i].name());
		bi1.setChecked(true);
		bi1.setDisabled(false);
		bi1.setText(SpecialFunction.values()[i].toString());
	    }
	}
	   
		
	UserConsentNavigator ucn = this.gui.obtainNavigator(ucd);
	ExecutionEngine exec = new ExecutionEngine(ucn);
	exec.process();
	
	CHAT chat = new CHAT(TerminalType.AuthenticationTerminal);
	boolean[] specialFunctions =new boolean[8];
	boolean[] readAccess =new boolean[21];
	
	for(OutputInfoUnit out : exec.getResults().get("Angefragte Daten").getResults()){
	    if (out.type().equals(InfoUnitElementType.Checkbox)){
			Checkbox c = (Checkbox) out;
			for(BoxItem bi : c.getBoxItems()){
			    if(bi.isChecked()){
				if(bi.getName().startsWith("DG")){
				    readAccess[DataGroup.valueOf(bi.getName()).ordinal()] = true;
				} else {
				    specialFunctions[SpecialFunction.valueOf(bi.getName()).ordinal()] = true;
				}
			    }
			    //else do nothing, false is default
			}
		} 
		  //else ignore  
	}
	
	chat.setSpecialFunctions(specialFunctions);
	chat.setReadAccess(readAccess);
	    	
	return chat;

    }

}
