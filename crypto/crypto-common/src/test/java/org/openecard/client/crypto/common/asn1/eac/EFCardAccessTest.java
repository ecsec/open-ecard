/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.crypto.common.asn1.eac;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.openecard.client.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.client.crypto.common.asn1.eac.oid.CAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;
import org.openecard.client.crypto.common.asn1.eac.oid.PACEObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EFCardAccessTest {

    private static final Logger _logger = LoggerFactory.getLogger(EFCardAccessTest.class);

    private EFCardAccess efcaA;
    private EFCardAccess efcaB;

    @BeforeTest
    public void init() throws Exception {
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
	    _logger.error(e.getMessage(), e);
	}
	return baos.toByteArray();
    }

    @Test
    public void testPACESecurityInfos() throws Exception {
	PACESecurityInfos psi = efcaA.getPACESecurityInfos();
	PACEInfo pi = psi.getPACEInfos().get(0);
	PACEDomainParameter pdp = new PACEDomainParameter(psi);

	assertEquals(pi.getProtocol(), "0.4.0.127.0.7.2.2.4.2.2");
	assertEquals(pi.getProtocol(), PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128);
	assertEquals(pi.getVersion(), 2);
	assertEquals(pi.getParameterID(), 13);

	psi = efcaB.getPACESecurityInfos();
	pi = psi.getPACEInfos().get(0);
	PACEDomainParameterInfo pdpi = psi.getPACEDomainParameterInfos().get(0);
//	pdp = new PACEDomainParameter(psi);

	assertEquals(pi.getProtocol(), "0.4.0.127.0.7.2.2.4.2.2");
	assertEquals(pi.getProtocol(), PACEObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128);
	assertEquals(pi.getVersion(), 1);
	assertEquals(pi.getParameterID(), -1);

	assertEquals(pdpi.getProtocol(), "0.4.0.127.0.7.2.2.4.2");
	assertEquals(pdpi.getProtocol(), PACEObjectIdentifier.id_PACE_ECDH_GM);
	assertEquals(pdpi.getParameterID(), 0);
	assertEquals(pdpi.getDomainParameter().getObjectIdentifier(), "0.4.0.127.0.7.1.1.5.2.2.2");
    }

    @Test
    public void testCASecurityInfos() throws Exception {
	CASecurityInfos csi = efcaA.getCASecurityInfos();
	CADomainParameter cdp = new CADomainParameter(csi);

	CAInfo ci = csi.getCAInfos().get(0);
	assertEquals(ci.getProtocol(), "0.4.0.127.0.7.2.2.3.2.2");
	assertEquals(ci.getProtocol(), CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128);
	assertEquals(ci.getVersion(), 2);
	assertEquals(ci.getKeyID(), 65);

	ci = csi.getCAInfos().get(1);
	assertEquals(ci.getProtocol(), "0.4.0.127.0.7.2.2.3.2.2");
	assertEquals(ci.getProtocol(), CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128);
	assertEquals(ci.getVersion(), 2);
	assertEquals(ci.getKeyID(), 69);

	CADomainParameterInfo cdpi = csi.getCADomainParameterInfos().get(0);
	assertEquals(cdpi.getProtocol().toString(), "0.4.0.127.0.7.2.2.3.2");
	assertEquals(cdpi.getProtocol().toString(), CAObjectIdentifier.id_CA_ECDH.toString());
	assertEquals(cdpi.getDomainParameter().getObjectIdentifier().toString(), "0.4.0.127.0.7.1.2");
	assertEquals(cdpi.getDomainParameter().getObjectIdentifier().toString(), EACObjectIdentifier.standardized_Domain_Parameters.toString());
	assertEquals(cdpi.getDomainParameter().getParameters().toString(), "13");
	assertEquals(cdpi.getKeyID(), 65);

	cdpi = csi.getCADomainParameterInfos().get(1);
	assertEquals(cdpi.getProtocol().toString(), "0.4.0.127.0.7.2.2.3.2");
	assertEquals(cdpi.getProtocol().toString(), CAObjectIdentifier.id_CA_ECDH.toString());
	assertEquals(cdpi.getDomainParameter().getObjectIdentifier().toString(), "0.4.0.127.0.7.1.2");
	assertEquals(cdpi.getDomainParameter().getObjectIdentifier().toString(), EACObjectIdentifier.standardized_Domain_Parameters.toString());
	assertEquals(cdpi.getDomainParameter().getParameters().toString(), "13");
	assertEquals(cdpi.getKeyID(), 69);

	csi = efcaB.getCASecurityInfos();
	ci = csi.getCAInfos().get(0);
	cdpi = csi.getCADomainParameterInfos().get(0);
//	cdp = new CADomainParameter(csi);

	assertEquals(ci.getProtocol(), "0.4.0.127.0.7.2.2.3.2.2");
	assertEquals(ci.getProtocol(), CAObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128);
	assertEquals(ci.getVersion(), 2);
	assertEquals(ci.getKeyID(), 0);

	assertEquals(cdpi.getProtocol().toString(), "0.4.0.127.0.7.2.2.3.2");
	assertEquals(cdpi.getProtocol().toString(), CAObjectIdentifier.id_CA_ECDH.toString());
	assertEquals(cdpi.getDomainParameter().getObjectIdentifier().toString(), "0.4.0.127.0.7.1.1.5.2.2.2");
    }

    @Test
    public void testTASecurityInfos() throws Exception {
	TASecurityInfos tsi = efcaA.getTASecurityInfos();
	TAInfo ti = tsi.getTAInfos().get(0);

	assertEquals(ti.getProtocol().toString(), "0.4.0.127.0.7.2.2.2");
	assertEquals(ti.getProtocol().toString(), EACObjectIdentifier.id_TA);
	assertEquals(ti.getVersion(), 2);

	tsi = efcaB.getTASecurityInfos();
	ti = tsi.getTAInfos().get(0);

	assertEquals(ti.getProtocol().toString(), "0.4.0.127.0.7.2.2.2");
	assertEquals(ti.getProtocol().toString(), EACObjectIdentifier.id_TA);
	assertEquals(ti.getVersion(), 2);
    }

    @Test
    public void testCardInfoLocator() throws Exception {
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
