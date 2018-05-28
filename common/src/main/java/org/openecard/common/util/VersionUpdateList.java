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

package org.openecard.common.util;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 *
 * @author Sebastian Schuberth
 */
@Immutable
public class VersionUpdateList {
    
    private final URL downloadPage;
    private final List<VersionUpdate> versionUpdates;

    public VersionUpdateList(@Nonnull List<VersionUpdate> versionUpdates, @Nonnull URL downloadPage) {
	this.versionUpdates = versionUpdates;
	this.downloadPage = downloadPage;
    }

    @Nonnull    
    public List<VersionUpdate> getVersionUpdates() {
	return Collections.unmodifiableList(versionUpdates);
    }

    @Nonnull
    public URL getDownloadPage() {
	return downloadPage;
    }
    
}
