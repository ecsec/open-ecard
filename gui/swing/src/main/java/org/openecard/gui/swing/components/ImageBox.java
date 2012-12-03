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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a simple image component for use in a {@link StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ImageBox implements StepComponent {

    private JLabel imageLabel;
    private ImageIcon image;

    public ImageBox(org.openecard.gui.definition.ImageBox imageBox) {
	this.image = new ImageIcon(imageBox.getImageData());
	this.imageLabel = new JLabel(image);
    }


    @Override
    public Component getComponent() {
	return imageLabel;
    }

    @Override
    public boolean isValueType() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean validate() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputInfoUnit getValue() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
