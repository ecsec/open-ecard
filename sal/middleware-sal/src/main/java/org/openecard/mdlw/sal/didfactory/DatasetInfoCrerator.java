/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal.didfactory;

import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.PathType;


public class DatasetInfoCrerator {

    public final String datasetName;
    public static AccessControlListType acl;
    public static byte[] efIdOrPath;

    public DatasetInfoCrerator(String name) {
        this.datasetName = name;
    }

    public void setAcl(AccessControlListType acl) {
        this.acl = acl;
    }

    public void setEfIdOrPath(byte[] arr) {
        this.efIdOrPath = arr;
    }

    public DataSetInfoType create() {
        DataSetInfoType dsit = new DataSetInfoType();
        dsit.setDataSetName(datasetName);
        dsit.setDataSetACL(acl);

        PathType pt = new PathType();
        pt.setEfIdOrPath(efIdOrPath);

        dsit.setDataSetPath(pt);
        
        return dsit;
    }

}
