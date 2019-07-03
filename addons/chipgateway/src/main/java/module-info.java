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

module openecard.chipgateway {
    requires openecard.tls;
    requires openecard.crypto.common;
    requires openecard.ifd.common;
    requires openecard.common;
    requires openecard.wsdef.common;
    requires openecard.i18n;
    requires openecard.gui.common;
    requires openecard.sal.common;
    requires openecard.http.core;
    requires openecard.addon;
    requires openecard.wsdef.client;

    requires org.bouncycastle.tls;
    requires proxy.vole;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires org.json;
    requires org.apache.httpcomponents.httpcore;
    requires com.fasterxml.jackson.module.jaxb;
    requires jackson.annotations;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires jose4j;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
    requires java.xml.ws;
    requires java.xml.bind;
    requires java.activation;
    requires java.xml.soap;
    requires javax.jws;
    requires slf4j.api;
    requires annotations;
}
