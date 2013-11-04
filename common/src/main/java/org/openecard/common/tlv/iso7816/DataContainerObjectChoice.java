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

package org.openecard.common.tlv.iso7816;

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class DataContainerObjectChoice extends TLVType {

    private GenericDataContainerObject<TLV> opaqueDO;
    private GenericDataContainerObject<TLV> iso7816DO;
    private GenericDataContainerObject<TLV> oidDO;
    private TLV extension;

    public DataContainerObjectChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    opaqueDO = new GenericDataContainerObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    iso7816DO = new GenericDataContainerObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    oidDO = new GenericDataContainerObject<TLV>(p.next(0), TLV.class);
	} else {
	    extension = p.next(0);
	}
    }

    public GenericDataContainerObject<TLV> getOpaqueDO() {
	return opaqueDO;
    }

    public GenericDataContainerObject<TLV> getIso7816DO() {
	return iso7816DO;
    }

    public GenericDataContainerObject<TLV> getOidDO() {
	return oidDO;
    }

    public TLV getExtension() {
	return extension;
    }

}
