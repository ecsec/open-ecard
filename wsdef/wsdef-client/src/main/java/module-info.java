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

module openecard.wsdef.client {
    requires java.logging;
    requires java.xml.ws;
    requires java.xml.bind;
    requires java.activation;
    requires java.xml.soap;
    requires javax.jws;
    requires slf4j.api;
    requires annotations;

    exports de.bund.bsi.ecard.api._1;
    exports iso.std.iso_iec._24727.tech.schema;
    exports oasis.names.tc.dss._1_0.core.schema;
    exports oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_;
    exports oasis.names.tc.saml._1_0.assertion;
    exports oasis.names.tc.saml._2_0.assertion;
    exports org.etsi.uri._01903.v1_3;
    exports org.etsi.uri._02231.v3_1;
    exports org.openecard.ws;
    exports org.openecard.ws.chipgateway;
    exports org.openecard.ws.schema;
    exports org.setcce.schemas.ers;
    exports org.w3._2000._09.xmldsig_;
    exports org.w3._2001._04.xmldsig_more_;
    exports org.w3._2001._04.xmlenc_;
    exports org.w3._2007._05.xmldsig_more_;
    exports org.w3._2009.xmlenc11_;
    exports generated;

    opens de.bund.bsi.ecard.api._1 to java.xml.bind;
    opens iso.std.iso_iec._24727.tech.schema to java.xml.bind;
    opens oasis.names.tc.dss._1_0.core.schema to java.xml.bind;
    opens oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_ to java.xml.bind;
    opens oasis.names.tc.saml._1_0.assertion to java.xml.bind;
    opens oasis.names.tc.saml._2_0.assertion to java.xml.bind;
    opens org.etsi.uri._01903.v1_3 to java.xml.bind;
    opens org.etsi.uri._02231.v3_1 to java.xml.bind;
    opens org.openecard.ws to java.xml.bind;
    opens org.openecard.ws.chipgateway to java.xml.bind;
    opens org.openecard.ws.schema to java.xml.bind;
    opens org.setcce.schemas.ers to java.xml.bind;
    opens org.w3._2000._09.xmldsig_ to java.xml.bind;
    opens org.w3._2001._04.xmldsig_more_ to java.xml.bind;
    opens org.w3._2001._04.xmlenc_ to java.xml.bind;
    opens org.w3._2007._05.xmldsig_more_ to java.xml.bind;
    opens org.w3._2009.xmlenc11_ to java.xml.bind;
    opens generated to java.xml.bind;
}
