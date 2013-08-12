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

package org.openecard.control.binding.javascript;

import java.util.List;
import java.util.Map;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.HighestVersionSelector;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class JavaScriptBinding {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptBinding.class);

    private final AddonSelector selector;

    public JavaScriptBinding(AddonManager manager) {
	logger.debug("Starting JavaScriptBinding");
	selector = new AddonSelector(manager);
	selector.setStrategy(new HighestVersionSelector());
    }

    public Object[] handle(String id, Map<?, ?> data) {
	try {
	    AppPluginAction action = selector.getAppPluginAction(id);
	    if (action != null) {
		// convert data to something suitable to be processed by the execute function
		Body body = extractBody(data);
		Map<String, String> params = extractParams(data);
		List<Attachment> attachments = extractAttachments(data);

		BindingResult result = action.execute(null, null, null);
		Object[] jsonResult = convertResult(result);
		return jsonResult;
	    } else {
		logger.error("Cannot init add-on for the ID [{}]", id);
	    }
	} catch (AddonNotFoundException ex) {
	    logger.error("Cannot find an add-on for the ID [{}]", id);
	}
	return null;
    }

    private Object[] convertResult(BindingResult result) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    private Body extractBody(Map<?, ?> data) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    private Map<String, String> extractParams(Map<?, ?> data) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<Attachment> extractAttachments(Map<?, ?> data) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
