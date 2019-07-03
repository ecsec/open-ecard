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

module openecard.gui.swing {
    requires openecard.gui.common;
    requires openecard.common;
    requires openecard.wsdef.common;
    requires openecard.i18n;
    requires openecard.wsdef.client;

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
    requires org.json;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.tls;
    requires java.xml.ws;
    requires java.xml.bind;
    requires java.activation;
    requires java.xml.soap;
    requires javax.jws;
    requires slf4j.api;
    requires annotations;

    exports org.openecard.gui.swing;
    exports org.openecard.gui.swing.common;
    exports org.openecard.gui.swing.components;
    exports org.openecard.gui.swing.file;
    exports org.openecard.gui.swing.steplayout;
}
