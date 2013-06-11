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

package org.openecard.addon.manifest;

import java.util.ArrayList;


/**
 * 
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AddonBundleDescription {

    private Object _id;
    private Object _localizedName;
    private Object _localizedDescription;
    private Object _logo;
    private Object _version;
    private Object _about;
    private Object _license;
    public ArrayList<AppPluginActionDescription> _bindingActions = new ArrayList<AppPluginActionDescription>();
    public ArrayList<ProtocolPluginDescription> _iFDActions = new ArrayList<ProtocolPluginDescription>();
    public ArrayList<AppExtensionActionDescription> _applicationActions = new ArrayList<AppExtensionActionDescription>();
    public ArrayList<ProtocolPluginDescription> _sALActions = new ArrayList<ProtocolPluginDescription>();
    public Configuration _configDescription;

    public void loadFromManifest(Object aXml) {
	throw new UnsupportedOperationException();
    }

}
