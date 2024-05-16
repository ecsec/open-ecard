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

package org.openecard.gui.swing.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.nio.charset.Charset;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.openecard.gui.definition.Document;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a simple text component for use in a {@link org.openecard.gui.swing.StepFrame}.
 *
 * @author Tobias Wich
 */
public class Text implements StepComponent {

    private final JTextPane textArea;

    public Text(org.openecard.gui.definition.Text text) {
	Document textValue = text.getDocument();
	String textString;

	switch (textValue.getMimeType()) {
	    case "text/plain":
		textString = "<html><body>" + new String(textValue.getValue(), Charset.forName("UTF-8")) + "</body></html>";
		break;
	    case "text/html":
		// pray that the code is HTML 3.2 compliant
		textString = new String(textValue.getValue(), Charset.forName("UTF-8"));
		break;
	    default:
		throw new IllegalArgumentException("Content with the MimeType " + textValue.getMimeType() + " is not supported by the Swing Text implementation.");
	}

	textArea = new JTextPane();
	textArea.setEditorKitForContentType("text/html", new HTMLEditorKit());
	textArea.setContentType("text/html");
	textArea.setMargin(new Insets(0, 0, 0, 0));
	textArea.setEditable(false);

	Font font = UIManager.getFont("Label.font");
	String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
	HTMLDocument doc = (HTMLDocument) textArea.getDocument();
	doc.getStyleSheet().addRule(bodyRule);

	textArea.setText(textString);
    }

    @Override
    public Component getComponent() {
	return textArea;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }

}
