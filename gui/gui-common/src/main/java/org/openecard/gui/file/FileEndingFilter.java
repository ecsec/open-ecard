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

package org.openecard.gui.file;

import java.io.File;


/**
 * File filter checking only the ending (aka file type) of a file.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class FileEndingFilter implements FileFilter {

    private final String fileEnding;

    /**
     * Creates a FileEndingFilter.
     *
     * @param fileEnding File ending to filter.
     * @param withDot Whether to prepend a . to the file ending or not. The dot is only added if non is present yet.
     */
    public FileEndingFilter(String fileEnding, boolean withDot) {
	this.fileEnding = (withDot && ! fileEnding.startsWith(".") ? "." : "") + fileEnding;
    }

    /**
     * Creates a FileEndingFilter.
     * Calling this constructor is the same as calling <code>new FileEndingFilter(fileEnding, true);</code>.
     *
     * @param fileEnding File ending to filter.
     */
    public FileEndingFilter(String fileEnding) {
	this(fileEnding, true);
    }

    @Override
    public boolean accept(File f) {
	if (! f.isDirectory()) {
	    String name = f.getName();
	    return name.endsWith(fileEnding);
	} else {
	    return true;
	}
    }

    @Override
    public String getDescription() {
	return "*" + fileEnding;
    }

}
