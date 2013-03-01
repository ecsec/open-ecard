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

package org.openecard.gui.android.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import org.openecard.gui.definition.OutputInfoUnit;


/**
 * Implementation of a simple image view for use in a {@link StepActivity}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ImageBox implements StepView {

    private ImageView imageView;

    public ImageBox(org.openecard.gui.definition.ImageBox imageBox, Context ctx) {
	this.imageView = new ImageView(ctx);
	byte[] imageData = imageBox.getImageData();
	Bitmap bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
	this.imageView.setImageBitmap(bmp);
    }


    @Override
    public View getView() {
	return imageView;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }

}
