/*
 * Copyright 2011 Moritz Horsch.
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
package org.openecard.client.crypto.common.asn1.eac;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openecard.bouncycastle.jce.spec.ElGamalParameterSpec;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.crypto.common.asn1.eac.oid.CAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;
import org.openecard.client.crypto.common.asn1.eac.oid.PACEObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EFCardAccessTest {

    private EFCardAccess efcaA;
    private EFCardAccess efcaB;

    private void init() throws Exception {

	ConsoleHandler ch = new ConsoleHandler();
	ch.setLevel(Level.ALL);
	Logger.getLogger("ASN1").addHandler(ch);

	// Standardized Domain Parameters
	byte[] data = loadTestFile("EF_CardAccess.bin");
	SecurityInfos sis = SecurityInfos.getInstance(data);
	efcaA = new EFCardAccess(sis);

	// Proprietary Domain Parameters
	data = loadTestFile("EF_CardAccess_pdp.bin");
	sis = SecurityInfos.getInstance(data);
	efcaB = new EFCardAccess(sis);
    }

    private byte[] loadTestFile(String file) throws Exception {
	String path = "/" + file;
	InputStream is = EFCardAccessTest.class.getResourceAsStream(path);
	ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
	try {
	    int b;
	    while ((b = is.read()) != -1) {
		baos.write((byte) b);
	    }
	} catch (Exception e) {
	    Logger.getLogger(EFCardAccessTest.class.getName()).log(Level.SEVERE, "Exception", e);
	}
	return baos.toByteArray();
    }

    @Test
    public void testPACESecurityInfos() throws Exception {
	init();
	PACESecurityInfos psi = efcaA.getPACESecurityInfos();
	PACEInfo pi = psi.getPACEInfo();
	PACEDomainParameterInfo pdp = psi.getPACEDomainParameterInfo();

	assertEquals(pi.getProtocol(), "0.4.0.127.0.7.2.2.4.2.2");
	assertEquals(pi.getProtocol(), PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128);
	assertEquals(pi.getVersion(), 2);
	assertEquals(pi.getParameterID(), 13);
	assertNull(pdp);

	psi = efcaB.getPACESecurityInfos();
	pi = psi.getPACEInfo();
	pdp = psi.getPACEDomainParameterInfo();

	assertEquals(pi.getProtocol(), "0.4.0.127.0.7.2.2.4.2.2");
	assertEquals(pi.getProtocol(), PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128);
	assertEquals(pi.getVersion(), 1);
	assertEquals(pi.getParameterID(), -1);

	assertEquals(pdp.getProtocol(), "0.4.0.127.0.7.2.2.4.2");
	assertEquals(pdp.getProtocol(), PACEObjectIdentifier.id_PACE_ECDH_GM);
	assertEquals(pdp.getParameterID(), 0);
	assertEquals(pdp.getDomainParameter().getObjectIdentifier(), "0.4.0.127.0.7.1.1.5.2.2.2");
    }

    @Test
    public void testCASecurityInfos() throws Exception {
	init();
	CASecurityInfos csi = efcaA.getCASecurityInfos();
	CAInfo ci = csi.getCAInfo();
	CADomainParameterInfo cdp = csi.getCADomainParameterInfo();

	assertEquals(ci.getProtocol(), "0.4.0.127.0.7.2.2.3.2.2");
	assertEquals(ci.getProtocol(), CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128);
	assertEquals(ci.getVersion(), 2);
	assertEquals(ci.getKeyID(), 69);

	assertEquals(cdp.getProtocol().toString(), "0.4.0.127.0.7.2.2.3.2");
	assertEquals(cdp.getProtocol().toString(), CAObjectIdentifier.id_CA_ECDH.toString());
	assertEquals(cdp.getDomainParameter().getObjectIdentifier().toString(), "0.4.0.127.0.7.1.2");
	assertEquals(cdp.getDomainParameter().getObjectIdentifier().toString(), EACObjectIdentifier.standardizedDomainParameters.toString());
	assertEquals(cdp.getDomainParameter().getParameters().toString(), "13");
	assertEquals(cdp.getKeyID(), 69);

	csi = efcaB.getCASecurityInfos();
	ci = csi.getCAInfo();
	cdp = csi.getCADomainParameterInfo();

	assertEquals(ci.getProtocol(), "0.4.0.127.0.7.2.2.3.2.2");
	assertEquals(ci.getProtocol(), CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128);
	assertEquals(ci.getVersion(), 2);
	assertEquals(ci.getKeyID(), 0);

	assertEquals(cdp.getProtocol().toString(), "0.4.0.127.0.7.2.2.3.2");
	assertEquals(cdp.getProtocol().toString(), CAObjectIdentifier.id_CA_ECDH.toString());
	assertEquals(cdp.getDomainParameter().getObjectIdentifier().toString(), "0.4.0.127.0.7.1.1.5.2.2.2");
    }

    @Test
    public void testTASecurityInfos() throws Exception {
	init();
	TASecurityInfos tsi = efcaA.getTASecurityInfos();
	TAInfo ti = tsi.getTAInfo();

	assertEquals(ti.getProtocol().toString(), "0.4.0.127.0.7.2.2.2");
	assertEquals(ti.getProtocol().toString(), EACObjectIdentifier.id_TA);
	assertEquals(ti.getVersion(), 2);

	tsi = efcaB.getTASecurityInfos();
	ti = tsi.getTAInfo();

	assertEquals(ti.getProtocol().toString(), "0.4.0.127.0.7.2.2.2");
	assertEquals(ti.getProtocol().toString(), EACObjectIdentifier.id_TA);
	assertEquals(ti.getVersion(), 2);
    }

    @Test
    public void testCardInfoLocator() throws Exception {
	init();
	CardInfoLocator cil = efcaA.getCardInfoLocator();

	assertEquals(cil.getProtocol().toString(), "0.4.0.127.0.7.2.2.6");
	assertEquals(cil.getProtocol().toString(), EACObjectIdentifier.id_CI.toString());
	assertEquals(cil.getURL(), "http://bsi.bund.de/cif/npa.xml");
	assertNull(cil.getEFCardInfo());

	cil = efcaB.getCardInfoLocator();

	assertEquals(cil.getProtocol().toString(), "0.4.0.127.0.7.2.2.6");
	assertEquals(cil.getProtocol().toString(), EACObjectIdentifier.id_CI.toString());
	assertEquals(cil.getURL(), "AwT ePA - BDr GmbH - Testkarte v1.0");
	assertNull(cil.getEFCardInfo());
    }

}
