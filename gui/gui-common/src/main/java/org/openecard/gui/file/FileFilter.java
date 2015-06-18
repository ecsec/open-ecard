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
import java.io.Serializable;


/**
 * Class to filter out files in a {@link org.openecard.gui.FileDialog}.
 * Filters have two usages in the file dialog.
 * <ol>
 * <li>Restrict the visibility of files in the dialog.</li>
 * <li>Present a selectable preset of visible files.</li>
 * </ol>
 *
 * @author Tobias Wich
 */
public interface FileFilter extends Serializable {

    /**
     * Decide whether a file is accepted or not.
     *
     * @param file File to check whether it is accepted or not.
     * @return <code>true</code> if the file is accepted, <code>false</code> otherwise.
     */
    boolean accept(File file);

    /**
     * Gets the description text of this filter.
     * The description may be shown to the user, if a selection of filters is desirable.
     *
     * @return The description text. <code>null</code> is not permitted.
     */
    String getDescription();

}
