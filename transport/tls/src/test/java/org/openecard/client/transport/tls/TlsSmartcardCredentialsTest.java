/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.transport.tls;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.x509.X509CertificateStructure;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.InfoUnitElementType;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.genericryptography.GenericCryptoProtocolFactory;
import org.openecard.client.sal.protocol.pincompare.PinCompareProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TlsSmartcardCredentialsTest {

    private static ClientEnv env;
    private static TinySAL instance;
    private static CardStateMap states;
    static ConnectionHandleType handle;
    private static Dispatcher dispatcher;
    byte[] appIdentifier_ESIGN = Hex.decode("A000000167455349474E");
    byte[] cardApplication_ROOT = Hex.decode("D2760001448000");

    @BeforeClass
    public static void setUpClass() throws Exception {
	env = new ClientEnv();

	IFD ifd = new IFD();
	env.setIFD(ifd);
	ifd.setGUI(new SwingUserConsent(new SwingDialogWrapper()));
	states = new CardStateMap();

	EstablishContextResponse ecr = env.getIFD().establishContext(new EstablishContext());
	CardRecognition cr = new CardRecognition(ifd, ecr.getContextHandle());
	ListIFDs listIFDs = new ListIFDs();

	listIFDs.setContextHandle(ecr.getContextHandle());
	ListIFDsResponse listIFDsResponse = ifd.listIFDs(listIFDs);
	RecognitionInfo recognitionInfo = cr.recognizeCard(listIFDsResponse.getIFDName().get(0), new BigInteger("0"));
	SALStateCallback salCallback = new SALStateCallback(cr, states);
	Connect c = new Connect();
	c.setContextHandle(ecr.getContextHandle());
	c.setIFDName(listIFDsResponse.getIFDName().get(0));
	c.setSlot(new BigInteger("0"));
	ConnectResponse connectResponse = env.getIFD().connect(c);

	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(ecr.getContextHandle());
	connectionHandleType.setRecognitionInfo(recognitionInfo);
	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
	connectionHandleType.setSlotIndex(new BigInteger("0"));
	connectionHandleType.setSlotHandle(connectResponse.getSlotHandle());
	salCallback.signalEvent(EventType.CARD_RECOGNIZED, connectionHandleType);
	instance = new TinySAL(env, states);
	dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	env.setSAL(instance);
	instance.addProtocol(ECardConstants.Protocol.PIN_COMPARE, new PinCompareProtocolFactory());
	instance.addProtocol(ECardConstants.Protocol.GENERIC_CRYPTO, new GenericCryptoProtocolFactory());
    }

    /**
     * @param args
     * @throws URISyntaxException
     * @throws WSException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    @Test(enabled=false) // works only with connected terminal+eGK with gematik labortest 02 certificate
    public void test() throws Exception {
	// Connect to ESIGN application
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = (CardApplicationPathResponse) dispatcher.deliver(cardApplicationPath);
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationPathType = cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0);
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathType);
	CardApplicationConnectResponse result1 = (CardApplicationConnectResponse) dispatcher.deliver(cardApplicationConnect);
	ConnectionHandleType connectionHandle = result1.getConnectionHandle();
	WSHelper.checkResult(result1);

	// List DIDs from ESIGN that support GenericCrypto and signature
	// computation
	DIDList didList = new DIDList();
	didList.setConnectionHandle(connectionHandle);
	DIDQualifierType didQualifier = new DIDQualifierType();
	didQualifier.setApplicationIdentifier(connectionHandle.getCardApplication());
	didQualifier.setObjectIdentifier("urn:oid:1.3.162.15480.3.0.25");
	didQualifier.setApplicationFunction("Compute-signature");
	didList.setFilter(didQualifier);
	DIDListResponse didListResponse = (DIDListResponse) dispatcher.deliver(didList);
	WSHelper.checkResult(didListResponse);

	String chosenDID = null;
	if (didListResponse.getDIDNameList().getDIDName().isEmpty()) {
	    Assert.fail("There were no usable DIDs on the smartcard");
	} else if (didListResponse.getDIDNameList().getDIDName().size() > 0) {
	    // more than one certificate could be used, we have to select one
	    List<Certificate> certificates = new ArrayList<Certificate>();
	    for (String didName : didListResponse.getDIDNameList().getDIDName()) {
		certificates.add(readCertificate(didName, connectionHandle));
	    }
	    chosenDID = showSelectCertificateDialog(certificates, didListResponse);
	    Assert.assertNotNull(chosenDID);
	} else {
	    // only one certificate could be used
	    chosenDID = didListResponse.getDIDNameList().getDIDName().get(0);
	}
	AuthenticateHelper.authenticateDID(states.getEntry(connectionHandle), chosenDID, CryptographicServiceActionName.SIGN, dispatcher);

	connectionHandle.setCardApplication(appIdentifier_ESIGN);

	// URL url = new URL("https://ftei-vm-073.hs-coburg.de:7777/");
	URL url = new URL("https://tls.skidentity.de/activate");
	String host = url.getHost();
	connectionHandle.setCardApplication(appIdentifier_ESIGN);
	DefaultTlsAuthentication tlsAuthentication = new DefaultTlsAuthentication(new TlsSmartcardCredentials(dispatcher, connectionHandle, chosenDID));
	DefaultTlsClientImpl tlsClient = new DefaultTlsClientImpl(host, tlsAuthentication);
	//TODO reenable when bouncycaslte artifact is updated
	//tlsClient.setClientVersion(ProtocolVersion.TLSv11);
	TlsClientSocketFactory tlsClientSocketFactory = new TlsClientSocketFactory(tlsClient);

	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	conn.setSSLSocketFactory(tlsClientSocketFactory);
	conn.connect();

	InputStream response = null;
	StringBuilder sb = new StringBuilder();
	try {
	    response = conn.getInputStream();
	    InputStreamReader isr = new InputStreamReader(response);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    while ((line = br.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    response.close();
	}
	System.out.println(sb.toString());
	Assert.assertTrue(sb.toString().contains("Activation requested without specifying valid parameters."));
	// Assert.assertTrue(sb.toString().contains("X.509 Certificate Information:"));
    }

    private Certificate readCertificate(String didName, ConnectionHandleType connectionHandle) throws Exception {
	// get the specified did
	DIDGet didGet = new DIDGet();
	didGet.setConnectionHandle(connectionHandle);
	didGet.setDIDName(didName);
	didGet.setDIDScope(DIDScopeType.LOCAL);
	DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	WSHelper.checkResult(didGetResponse);

	// get the name of the dataset for the certificate and select it
	CryptoMarkerType cryptoMarker = new CryptoMarkerType((iso.std.iso_iec._24727.tech.schema.CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());
	String dataSetName = cryptoMarker.getCertificateRef().getDataSetName();
	DataSetSelect dataSetSelect = new DataSetSelect();
	dataSetSelect.setConnectionHandle(connectionHandle);
	dataSetSelect.setDataSetName(dataSetName);
	DataSetSelectResponse dataSetSelectResponse = (DataSetSelectResponse) dispatcher.deliver(dataSetSelect);
	WSHelper.checkResult(dataSetSelectResponse);

	AuthenticateHelper.authenticateDataSet(states.getEntry(connectionHandle), dataSetName, NamedDataServiceActionName.DSI_READ, dispatcher);

	// read the contents of the certificate
	DSIRead dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(connectionHandle);
	connectionHandle.setCardApplication(appIdentifier_ESIGN);
	dsiRead.setDSIName(dataSetName);
	DSIReadResponse dsiReadResponse = (DSIReadResponse) dispatcher.deliver(dsiRead);
	WSHelper.checkResult(dsiReadResponse);

	// convert to bouncycastle certificate
	ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(dsiReadResponse.getDSIContent());
	X509CertificateStructure[] x509CertificateStructure = { new X509CertificateStructure(asn1Sequence) };
	return new org.openecard.bouncycastle.crypto.tls.Certificate(x509CertificateStructure);
    }

    private String showSelectCertificateDialog(List<Certificate> certificates, DIDListResponse didListResponse) {
	CertificateDialog certificateDialog = new CertificateDialog(certificates);

	SwingUserConsent ucEngine = new SwingUserConsent(new SwingDialogWrapper());
	UserConsentNavigator navigator = ucEngine.obtainNavigator(certificateDialog.getUserConsent());
	ExecutionEngine exec = new ExecutionEngine(navigator);
	ResultStatus result = exec.process();

	Assert.assertEquals(result, ResultStatus.OK);

	for (Map.Entry<String, ExecutionResults> entry : exec.getResults().entrySet()) {
	    for (OutputInfoUnit out : entry.getValue().getResults()) {
		if (out.type().equals(InfoUnitElementType.CHECK_BOX)) {
		    Checkbox c = (Checkbox) out;
		    for (BoxItem bi : c.getBoxItems()) {
			if (bi.isChecked()) {
			    return didListResponse.getDIDNameList().getDIDName().get(Integer.parseInt(bi.getName()));
			}

		    }
		}
	    }

	}
	return null;
    }

}
