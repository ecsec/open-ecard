package org.openecard.client.recognition;

import org.openecard.client.common.ECardConstants;
import oasis.names.tc.dss._1_0.core.schema.Result;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class RecognitionException extends Exception {

    private final String major;
    private final String minor;
    private final String message;
    private final String lang;

    public RecognitionException(Result r) {
	major = r.getResultMajor();
	if (r.getResultMinor() != null) {
	    minor = r.getResultMinor();
	    if (r.getResultMessage() != null) {
		message = r.getResultMessage().getValue();
		lang = r.getResultMessage().getLang();
	    } else {
		message = "Unknown error";
		lang = "EN";
	    }
	} else {
	    minor = ECardConstants.Minor.App.UNKNOWN_ERROR;
	    message = "Unknown error";
	    lang = "EN";
	}
    }

}
