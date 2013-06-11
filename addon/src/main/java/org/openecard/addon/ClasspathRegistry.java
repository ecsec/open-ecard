/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon;

import java.util.Set;
import org.openecard.addon.manifest.AddonBundleDescription;


/**
 * Addon registry serving add-ons from the classpath of the base app.
 * This type of registry works for JNLP and integrated plugins.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ClasspathRegistry implements AddonRegistry {

    @Override
    public Set<AddonBundleDescription> listPlugins() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AddonBundleDescription search(String aId) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<AddonBundleDescription> searchByName(String aName) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<AddonBundleDescription> searchProtocol(String aUri) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClassLoader downloadPlugin(String aId) {
	throw new UnsupportedOperationException("Not supported yet.");
    }


}
