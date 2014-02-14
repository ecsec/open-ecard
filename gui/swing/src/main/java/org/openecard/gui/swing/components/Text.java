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

package org.openecard.gui.swing.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a simple text component for use in a {@link org.openecard.gui.swing.StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Text implements StepComponent {

    private final JTextPane textArea;

    public Text(org.openecard.gui.definition.Text text) {
	String textValue = text.getText();
	if (! (textValue.startsWith("<html>") && textValue.endsWith("</html>"))) {
	    textValue = "<html><body>" + textValue + "</body></html>";
	}

	textArea = new JTextPane();
	textArea.setContentType("text/html");
	textArea.setMargin(new Insets(0, 0, 0, 0));
	textArea.setEditable(false);

	Font font = UIManager.getFont("Label.font");
	String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
	HTMLDocument doc = (HTMLDocument) textArea.getDocument();
	doc.getStyleSheet().addRule(bodyRule);

	textArea.setText(textValue);
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
