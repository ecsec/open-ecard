package org.openecard.client.common;

import org.openecard.client.common.util.CardCommandStatus;
import org.openecard.client.common.util.CardCommands;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSHelper {

    public static class WSException extends ECardException {
	protected WSException(Result r) {
	    makeException(this, r);
	}
	protected WSException(String msg) {
	    makeException(this, msg);
	}
    }

    public static ResponseType checkResult(ResponseType response) throws WSException {
	Result r = response.getResult();
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    if (response instanceof TransmitResponse) {
		TransmitResponse tr = (TransmitResponse) response;
		List<byte[]> rApdus = tr.getOutputAPDU();
		byte[] apdu = (rApdus.size() < 1) ? null : CardCommands.getResultFromResponse(rApdus.get(rApdus.size()-1));
		if (apdu == null) {
		    throw new WSException(r);
		} else {
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

    public static <C extends Class<T>, T extends ResponseType> T makeResponse(C c, Result r) {
        try {
            T t = c.getConstructor().newInstance();
            t.setProfile(ECardConstants.Profile.ECARD_1_1);
            t.setResult(r);
            return t;
        } catch (Exception ex) {
            return null;
        }
    }

}
