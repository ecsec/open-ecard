/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.gui.android.eac.types;

import android.os.Parcel;
import android.os.Parcelable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.gui.android.AbstractParcelable;
import org.openecard.gui.android.ParcelableCreator;
import org.openecard.gui.android.Serialize;


/**
 *
 * @author Tobias Wich
 */
public class BoxItem extends AbstractParcelable<BoxItem> {

    public static final Parcelable.Creator<BoxItem> CREATOR = new ParcelableCreator<>(BoxItem.class);

    public BoxItem(@Nonnull String name, boolean selected, boolean disabled, @Nullable String additionalText) {
	this.name = name;
	this.selected = selected;
	this.disabled = disabled;
	this.additionalText = additionalText;
    }

    public BoxItem(Parcel src) {
	readFromParcel(src);
    }


    /**
     * Enum name of the Access Right.
     *
     * @see CHAT.DataGroup
     * @see CHAT.SpecialFunction
     */
    @Serialize
    private String name;
    /**
     * Indicates the selection state of the item.
     */
    @Serialize
    private boolean selected;
    /**
     * Indicates that this item may not be changed, meaning the attribute is required.
     */
    @Serialize
    private boolean disabled;
    /**
     * Additional data such as verification age.
     */
    @Serialize
    @Nullable
    private String additionalText;

    public String getName() {
	return name;
    }

    public boolean isSelected() {
	return selected;
    }

    public void setSelected(boolean selected) {
	this.selected = selected;
    }

    public boolean isDisabled() {
	return disabled;
    }

    @Nullable
    public String getAdditionalText() {
	return additionalText;
    }

}
