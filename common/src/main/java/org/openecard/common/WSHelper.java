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

package org.openecard.common;

import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.ResponseBaseType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.apdu.common.CardCommandStatus;
import org.openecard.common.apdu.common.CardResponseAPDU;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSHelper {

    public static class WSException extends ECardException {
	private static final long serialVersionUID = 1L;
	protected WSException(Result r) {
	    makeException(this, r);
	}
	protected WSException(String msg) {
	    makeException(this, msg);
	}
    }

    public static ResponseBaseType checkResult(ResponseBaseType response) throws WSException {
	Result r = response.getResult();
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    if (response instanceof TransmitResponse) {
		TransmitResponse tr = (TransmitResponse) response;
		List<byte[]> rApdus = tr.getOutputAPDU();

		if(rApdus.size() < 1){
		    throw new WSException(r);
		} else {
		    byte[] apdu = CardResponseAPDU.getTrailer(rApdus.get(rApdus.size()-1));
		    String msg = CardCommandStatus.getMessage(apdu);
		    throw new WSException(msg);
		}
	    } else {
		throw new WSException(r);
	    }
	}
	return response;
    }


    ///
    /// functions to create OASIS Result messages
    ///

    public static Result makeResultOK() {
	Result result = makeResult(ECardConstants.Major.OK, null, null, null);
	return result;
    }

    public static Result makeResultUnknownError(String msg) {
	Result result = makeResult(ECardConstants.Major.ERROR, ECardConstants.Minor.App.UNKNOWN_ERROR, msg);
	return result;
    }

    public static Result makeResult(String major, String minor, String message) {
	Result result = makeResult(major, minor, message, "en");
	return result;
    }

    public static Result makeResultError(String minor, String message) {
	Result result = makeResult(ECardConstants.Major.ERROR, minor, message, "en");
	return result;
    }

    public static Result makeResult(Throwable exc) {
	if (exc instanceof ECardException) {
	    ECardException e = (ECardException) exc;
	    Result result = e.getResult();
	    return result;
	} else {
	    Result result = makeResultUnknownError(exc.getMessage());
	    return result;
	}
    }

    public static Result makeResult(String major, String minor, String message, String lang) {
	Result r = new Result();
	r.setResultMajor(major);
	r.setResultMinor(minor);
	if (message != null) {
	    InternationalStringType msg = new InternationalStringType();
	    msg.setValue(message);
	    msg.setLang(lang);
	    r.setResultMessage(msg);
	}
	return r;
    }

    public static <C extends Class<T>, T extends ResponseBaseType> T makeResponse(C c, Result r) {
	try {
	    T t = c.getConstructor().newInstance();
	    t.setProfile(ECardConstants.Profile.ECARD_1_1);
	    t.setResult(r);
	    return t;
	} catch (Exception ignore) {
	    return null;
	}
    }

}
