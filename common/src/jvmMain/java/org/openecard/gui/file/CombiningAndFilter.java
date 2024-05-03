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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * File filter checking all wrapped FileFilters.
 * This FileFilter accepts a file only if all filters agree to accept the file.
 *
 * @author Tobias Wich
 */
public class CombiningAndFilter implements FileFilter {

    private final List<? extends FileFilter> filters;
    private final String description;

    /**
     * Creates a new CombiningAndFilter.
     * In case no filter is given, the {@link AcceptAllFilesFilter} is used as the only choice.
     *
     * @param filters Filters to be combined.
     */
    public CombiningAndFilter(FileFilter... filters) {
	this(Arrays.asList(filters));
    }

    /**
     * Creates a new CombiningAndFilter.
     * In case no filter is given, the {@link AcceptAllFilesFilter} is used as the only choice.
     *
     * @param filters Filters to be combined.
     */
    public CombiningAndFilter(List<FileFilter> filters) {
	if (filters.isEmpty()) {
	    this.filters = Arrays.asList(new AcceptAllFilesFilter());
	} else {
	    this.filters = filters;
	}

	StringBuilder sb = new StringBuilder(32);
	Iterator<? extends FileFilter> it = this.filters.iterator();
	while (it.hasNext()) {
	    sb.append(it.next().getDescription());
	    if (it.hasNext()) {
		sb.append("; ");
	    }
	}
	description = sb.toString();
    }


    @Override
    public boolean accept(File f) {
	for (FileFilter next : filters) {
	    if (! next.accept(f)) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public String getDescription() {
	return description;
    }

}
