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

package org.openecard.gui.definition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Definition class for a text element which can fold its content.
 * The ToggleText has a title which is always displayed and a text which can be folded.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ToggleText extends IDTrait implements InputInfoUnit {

    private static final Logger logger = LoggerFactory.getLogger(Text.class);

    private String title;
    private String text;
    private boolean collapsed;


    /**
     * Gets whether the text is collapsed or not.
     * In the collapsed state, the element's text is not visible.
     *
     * @return {@code true} if the text is collapsed, {@code false} otherwise.
     */
    public boolean isCollapsed() {
	return collapsed;
    }
    /**
     * Sets whether the text is collapsed or not.
     * In the collapsed state, the element's text is not visible.
     *
     * @param collapsed {@code true} if the text is collapsed, {@code false} otherwise.
     */
    public void setCollapsed(boolean collapsed) {
	this.collapsed = collapsed;
    }

    /**
     * Gets the title of this instance.
     *
     * @return The title of this instance.
     */
    public String getTitle() {
	return title;
    }
    /**
     * Sets the title of this instance.
     *
     * @param title The title of this instance.
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * Gets the text of this instance.
     *
     * @return The text of this instance.
     */
    public String getText() {
	return text;
    }
    /**
     * Sets the text of this instance.
     *
     * @param text The text of this instance.
     */
    public void setText(String text) {
	this.text = text;
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.TOGGLE_TEXT;
    }

    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (! (this.getClass().equals(origin.getClass()))) {
	    logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	ToggleText other = (ToggleText) origin;
	// do copy
	this.title = other.title;
	this.text = other.text;
	this.collapsed = other.collapsed;
    }

}
