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

module openecard.clients.richclient {
    requires openecard.sal.tiny;
    requires openecard.sal.common;
    requires openecard.common;
    requires openecard.i18n;
    requires openecard.addon;
    requires openecard.recognition;
    requires openecard.gui.common;
    requires openecard.cifs;
    requires openecard.sal.middleware;
    requires openecard.ifd.pace;
    requires openecard.wsdef.client;
    requires openecard.crypto.common;
    requires openecard.ifd.core;
    requires openecard.pcsc;
    requires openecard.dispatcher;
    requires openecard.ifd.common;
    requires openecard.management;
    requires openecard.gui.swing;
    requires openecard.about;
    requires openecard.graphics;
    requires openecard.bindings.http;
    requires openecard.tls;
    requires openecard.tr03112;
    requires openecard.http.core;
    requires openecard.status;
    requires openecard.pin.management;
    requires openecard.genericcryptography;
    requires openecard.chipgateway;
    requires openecard.jaxb.marshaller;
    requires openecard.wsdef.common;

    requires org.json;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.tls;
    requires org.bouncycastle.pkix;
    requires javafx.swingEmpty;
    requires javafx.swing;
    requires javafx.graphicsEmpty;
    requires javafx.graphics;
    requires javafx.baseEmpty;
    requires javafx.base;
    requires javafx.webEmpty;
    requires javafx.web;
    requires javafx.controlsEmpty;
    requires javafx.controls;
    requires javafx.mediaEmpty;
    requires javafx.media;
    requires proxy.vole;
    requires ini4j;
    requires org.apache.httpcomponents.httpcore;
    requires java.xml.ws;
    requires java.xml.bind;
    requires java.activation;
    requires java.xml.soap;
    requires javax.jws;
    requires com.fasterxml.jackson.module.jaxb;
    requires jackson.annotations;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires jose4j;
    requires logback.classic;
    requires logback.core;
    requires slf4j.api;
    requires annotations;
}
