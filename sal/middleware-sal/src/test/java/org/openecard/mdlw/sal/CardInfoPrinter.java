/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import org.openecard.mdlw.sal.config.MiddlewareConfigLoader;
import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import javax.xml.bind.JAXBException;
import org.openecard.mdlw.sal.enums.UserType;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author Jan Mannsbart
 */
public class CardInfoPrinter {

    private static final String PIN_VALUE = "123123";

    private MiddlewareSALConfig mwConfig;

    @BeforeClass
    public void init() throws IOException, FileNotFoundException, JAXBException {
        mwConfig = new MiddlewareConfigLoader().getMiddlewareSALConfigs().get(0);
    }

    @Test
    public void test() throws CryptokiException, InterruptedException {

        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(true);

        for (int i = 0; i < list.size(); i++) {
            MwSlot s = list.get(i);
            System.out.println(s.getSlotInfo().getSlotDescription());
            System.out.println("SlotID: " + i);
        }

        System.out.println("");

        System.out.println("SlotID eingeben: ");
        Scanner in = new Scanner(System.in);
        int num = in.nextInt();

        MwSlot selectedSlot = list.get(num);

        System.out.println("Manufactor: "+selectedSlot.getSlotInfo().getManufactor());
        System.out.println("SlotDescription: "+selectedSlot.getSlotInfo().getSlotDescription());

	MwToken token = selectedSlot.getTokenInfo();
        System.out.println("");
        System.out.println("#######################Infos########################");
        System.out.println("");
        System.out.println("PinLabel: " + token.getLabel());
        System.out.println("");
	System.out.println(String.format("ObjectIdentifier: %s_%s", token.getManufacturerID(), token.getModel()));
	System.out.println("");

        MwSession session = selectedSlot.openSession();

        session.login(UserType.User, PIN_VALUE.toCharArray());

        List<MwData> datas = session.getData();

        List<MwPrivateKey> keys = session.getPrivateKeys();

        for (MwPrivateKey key : keys) {
            System.out.println("Private KeyType: " + key.getKeyTypeName());
            System.out.println("Private KeyLabel: " + key.getKeyLabel());
            System.out.println("");
        }
        System.out.println("");
        List<MwPublicKey> pubKeys = session.getPublicKeys();

        for (MwPublicKey pubKey : pubKeys) {
            System.out.println("Public KeyType " + pubKey.getKeyTypeName());
            System.out.println("Public KeyLabel: " + pubKey.getKeyLabel());
            System.out.println("");
        }
        System.out.println("");
        List<MwCertificate> certs = session.getCertificates();

        for (MwCertificate cert : certs) {
            System.out.println("CertType: " + cert.getCertificateType());
            System.out.println("CertLabel: " + cert.getLabel());
            System.out.println("CertVal: " + cert.getValue());
            System.out.println("");
        }

        module.destroy();

        System.out.println("");
        System.out.println("####################################################");
        System.out.println("Finished!");
    }
}
