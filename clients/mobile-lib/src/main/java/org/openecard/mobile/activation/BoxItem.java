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
package org.openecard.mobile.activation;

import org.openecard.robovm.annotations.FrameworkInterface;

/**
 *
 * @author Neil Crossley
 */
@FrameworkInterface
public interface BoxItem {

    /**
     * Gets the name of the item. The name is used to identify the item and thus should be unique in the surrounding
     * selection box group.
     *
     * @return The name of the item.
     */
    public String getName();

    /**
     * Gets the display text of the item. The text is displayed on the GUI to indicate the meaning of the option to the
     * user.
     *
     * @return The text of the item.
     */
    public String getText();

    /**
     * Gets the selection value of the box item.
     *
     * @return {@code true} if the item is checked/selected, {@code false} otherwise.
     */
    public boolean isChecked();

    /**
     * Sets the selection value of the box item. This function is used to preselect items and to set the value when the
     * step displaying this item is finished.
     *
     * @param checked {@code true} if the item is checked/selected, {@code false} otherwise.
     */
    public void setChecked(boolean checked);

    /**
     * Gets whether the item is enabled, or disabled. Disabled items can be used to show a preselected value to the
     * user, but do not allow modification of the value.
     *
     * @return {@code true} if the item is disabled, {@code false} otherwise.
     */
    public boolean isDisabled() ;

}
