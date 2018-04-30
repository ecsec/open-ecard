/****************************************************************************
 * Copyright (C) 2015-2016 ecsec GmbH.
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
import javax.xml.bind.JAXBException;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.FinalizationException;
import org.openecard.mdlw.sal.exceptions.InitializationException;
import org.openecard.mdlw.sal.enums.Flag;
import org.openecard.mdlw.sal.enums.TokenState;
import org.openecard.mdlw.sal.enums.UserType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author Jan Mannsbart
 */
@Test(groups = { "luxtrust" })
public class TestMwModule {

    final byte[] dummyData = "test".getBytes();

    private MiddlewareSALConfig mwConfig;

    @BeforeClass
    public void init() throws IOException, FileNotFoundException, JAXBException {
        mwConfig = new MiddlewareConfigLoader().getMiddlewareSALConfigs().get(0);
    }

    @Test
    public void testInit() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        module.destroy();
    }

    @Test
    public void testGetInfo() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        module.getInfo();
        module.destroy();
    }

    @Test
    public void testFinalize() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        module.destroy();
    }

    @Test
    public void testGetSlots() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(false);
        module.destroy();
    }

    @Test
    public void testGetSlotToken() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(true);
        MwSession session = list.get(0).openSession();

        if (list.get(0).getTokenInfo().containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH)) {
            System.out.println("Terminal bereit!");
            session.loginExternal(UserType.User);
        } else {
            System.out.println("Kein Terminal bereit!");
            session.login(UserType.User, "123123".toCharArray());
        }

        MwPublicKey key = session.getPublicKeys().get(0);

        // System.out.println("type: " +
        // session.getCertificates().get(0).getCertificateType());

        session.logout();
        session.closeSession();
        module.destroy();
    }

    @Test
    public void testMechanismsInfos() throws CryptokiException, IOException, UnsupportedAlgorithmException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(true);
        MwSlot slot = list.get(0);
        MwSession session = slot.openSession();

        List<MwMechanism> mechanisms = slot.getMechanismList();

        module.destroy();

        for (MwMechanism m : mechanisms) {
            System.out.println(m.getSignatureAlgorithm());
        }
    }

    @Test
    public void testSign() throws CryptokiException, IOException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(true);
        MwSlot slot = list.get(0);
        MwSession session = slot.openSession();

        MwToken token = slot.getTokenInfo();

        if (token.containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH)) {
            System.out.println("Terminal bereit!");
            session.loginExternal(UserType.User);
        } else {
            System.out.println("Kein Terminal bereit!");
            session.login(UserType.User, "123123".toCharArray());
        }

        List<MwPrivateKey> privateKeys = session.getPrivateKeys();

        byte[] dat = "testtest".getBytes();
        byte[] signData = privateKeys.get(0).sign(SignatureAlgorithms.CKM_RSA_PKCS, dat);

        session.logout();
        session.closeSession();
        module.destroy();
    }

    @Test
    public void testTokenInfo() throws CryptokiException, IOException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(true);
        MwSlot slot = list.get(0);

        MwToken token = slot.getTokenInfo();

        System.out.println("label " + token.getLabel());
        System.out.println("manid " + token.getManufacturerID());
        System.out.println("model " + token.getModel());
        System.out.println("serialnumb " + token.getSerialNumber());

        module.destroy();
    }

    @Test
    public void testKeys() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        List<MwSlot> list = module.getSlotList(true);
        MwSlot slot = list.get(0);
        MwToken token = slot.getTokenInfo();

        System.out.println("Token-/PinLabel: " + slot.getTokenInfo().getLabel());
        System.out.println("ManufacturerID: " + slot.getTokenInfo().getManufacturerID());
        System.out.println("Model: " + slot.getTokenInfo().getModel());

        MwSession session = slot.openSession();

        if (token.containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH)) {
            System.out.println("Terminal bereit!");
            session.loginExternal(UserType.User);
        } else {
            System.out.println("Kein Terminal bereit!");
            session.login(UserType.User, "123123".toCharArray());
        }

        List<MwPrivateKey> keys = session.getPrivateKeys();

        for (MwPrivateKey key : keys) {
            System.out.println("Private KeyData: " + key.getKeyTypeName());
            System.out.println("Private KeyLabel: " + key.getKeyLabel());

        }

        List<MwPublicKey> pubKeys = session.getPublicKeys();

        for (MwPublicKey pubKey : pubKeys) {
            System.out.println("Public KeyData " + pubKey.getKeyTypeName());
            System.out.println("Public KeyLabel: " + pubKey.getKeyLabel());

        }

        List<MwCertificate> certs = session.getCertificates();

        for (MwCertificate cert : certs) {
            System.out.println("Certtype: " + cert.getCertificateType());
            System.out.println("Certlabel: " + cert.getLabel());

        }

        module.destroy();

    }

    /*
     *
     * NEW TESTS
     *
     */

    @Test
    public void testMwBasic() throws InitializationException, FinalizationException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();
        module.destroy();
    }

    @Test
    public void testMwSlots() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();

        List<MwSlot> list = module.getSlotList(TokenState.Present);
        list = module.getSlotList(TokenState.Present);

        MwSlot slot = list.get(0);

        slot.getSlotInfo();

        module.destroy();
    }

    @Test
    public void testMwToken() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();

        List<MwSlot> list = module.getSlotList(TokenState.Present);

        MwSlot slot = list.get(0);

        MwToken token = slot.getTokenInfo();

        module.destroy();
    }

    @Test
    public void testMwSession() throws CryptokiException, IOException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();

        List<MwSlot> list = module.getSlotList(TokenState.Present);

        MwSlot slot = list.get(0);

        MwSession session = slot.openSession();

        session.closeSession();

        module.destroy();
    }

    @Test
    public void testReadCertificate() throws InitializationException, CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();

        List<MwSlot> list = module.getSlotList(TokenState.Present);
        MwSlot slot = list.get(0);
        MwSession session = slot.openSession();

        List<MwCertificate> certs = session.getCertificates();

        session.closeSession();
        module.destroy();
    }

    @Test
    public void testTokenFlags() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();

        List<MwSlot> list = module.getSlotList(TokenState.Present);
        MwSlot slot = list.get(0);
        System.out.println("SlotFlag: " + slot.getSlotInfo().getFlags());

        slot.openSession();

        list = module.getSlotList(TokenState.Present);
        slot = list.get(0);
        System.out.println("SlotFlag: " + slot.getSlotInfo().getFlags());

        module.destroy();
    }

    @Test
    public void testFlags() throws CryptokiException {
        MwModule module = new MwModule(mwConfig);
        module.initialize();

        List<MwSlot> list = module.getSlotList(TokenState.Present);
        MwSlot slot = list.get(0);

        MwToken token = slot.getTokenInfo();

        System.out.println("Token abfrage neu: " + token.containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH));

        module.destroy();
    }

}
