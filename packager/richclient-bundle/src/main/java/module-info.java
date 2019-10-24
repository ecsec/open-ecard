/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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


module org.openecard.richclient {
    requires java.smartcardio;
    requires java.logging;
    requires java.desktop;
    requires java.sql; // for jackson serialization

    /* BouncyCastle modules */
    requires org.bouncycastle.provider;
    requires org.bouncycastle.tls;
    requires org.bouncycastle.pkix;

    /* JAXB module */
    requires java.xml.bind;

    /* JavaFX modules */
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    /* Open JAXB classes for reflection */
    opens de.bund.bsi.ecard.api._1;
    opens iso.std.iso_iec._24727.tech.schema;
    opens oasis.names.tc.dss._1_0.core.schema;
    opens oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_;
    opens oasis.names.tc.saml._1_0.assertion;
    opens oasis.names.tc.saml._2_0.assertion;
    opens org.etsi.uri._01903.v1_3;
    opens org.etsi.uri._02231.v3_1;
    opens org.openecard.ws;
    opens org.openecard.ws.chipgateway;
    opens org.openecard.ws.schema;
    opens org.w3._2000._09.xmldsig_;
    opens org.w3._2001._04.xmldsig_more_;
    opens org.w3._2001._04.xmlenc_;
    opens org.w3._2007._05.xmldsig_more_;
    opens org.w3._2009.xmlenc11_;
    opens generated;

    opens org.openecard.mdlw.sal.config to java.xml.bind;
    opens org.openecard.addon.manifest to java.xml.bind;

    opens jnasmartcardio to java.base;
}
