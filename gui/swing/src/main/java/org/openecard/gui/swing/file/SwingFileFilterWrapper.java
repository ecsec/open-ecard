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

package org.openecard.gui.swing.file;

import java.io.File;
import org.openecard.gui.file.FileFilter;


/**
 * Wrapper class for a Open eCard FileFilter.
 * It wraps the Open eCard FileFilter in Swing FileFilter, so that it can be used in a JFileChooser.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingFileFilterWrapper extends javax.swing.filechooser.FileFilter {

    private final FileFilter wrappedFilter;

    /**
     * Create a FileFilter wrapper instance for the given FileFilter.
     *
     * @param wrappedFilter The FileFilter that needs to be wrapped.
     */
    public SwingFileFilterWrapper(FileFilter wrappedFilter) {
	this.wrappedFilter = wrappedFilter;
    }


    @Override
    public boolean accept(File f) {
	return wrappedFilter.accept(f);
    }

    @Override
    public String getDescription() {
	return wrappedFilter.getDescription();
    }

}
