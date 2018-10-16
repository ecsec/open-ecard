/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.gui.swing.components;

import java.nio.charset.Charset;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JPanel;


/**
 * Panel implementation which is able to render HTML and PDF by using a Java FX WebView and pdf.js.
 * <br>
 * <br>
 * NOTE: <br>
 * This class uses JavaFX components. JavaFX is available in Oracles JRE sind version 7 and in OpenJDK/JRE since version
 * 8. Because of the version differences this class should be access just by the reflection API until we have a minimum
 * requirement of Java 8 for the execution.
 *
 * @author Hans-Martin Haase
 */
public class HTMLPanel {

    private static JFXPanel jfxPane;
    private static WebView browser;
    private static JPanel contentPane;

    /**
     * Create a new JPanel containing the Java FX components to render HTML.
     *
     * @param mimeType The MimeType of the {@code content} to display.
     * @param content The content to display.
     * @return A JPanel displaying the content.
     */
    public static JPanel createPanel(final String mimeType, final byte[] content) {
	contentPane = new JPanel();
	jfxPane = new JFXPanel();
	contentPane.add(jfxPane);
	Platform.runLater(() -> {
	    initFx(mimeType, content);
	});

	return contentPane;
    }

    /**
     * Initializes the Java FX components and fills them with content.
     *
     * @param mimeType The MimeType of the {@code content} to display.
     * @param content The content to display.
     */
    private static void initFx(String mimeType, byte[] content) {
	BorderPane borderPane = new BorderPane();
	browser = new WebView();
	WebEngine engine = browser.getEngine();
	engine.loadContent(new String(content, Charset.forName("UTF-8")));
	borderPane.setCenter(browser);
	Scene scene = new Scene(borderPane, 400, 200);
	jfxPane.setScene(scene);
    }
    
}
