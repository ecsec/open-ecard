/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.recognition;

import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;


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
