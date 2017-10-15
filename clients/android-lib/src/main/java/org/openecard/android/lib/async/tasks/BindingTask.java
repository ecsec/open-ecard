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

package org.openecard.android.lib.async.tasks;

import android.os.AsyncTask;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.BindingResult;
import org.openecard.android.lib.intent.binding.IntentBinding;
import org.openecard.android.lib.intent.binding.IntentBindingConstants;
import org.openecard.common.util.HttpRequestLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Mike Prechtl
 */
public class BindingTask extends AsyncTask<Void, Void, BindingResult> implements IntentBindingConstants {

	private static final Logger LOG = LoggerFactory.getLogger(BindingTask.class);

	private final IntentBinding binding;
	private final String uri;

	public BindingTask(IntentBinding binding, String uri) {
		this.binding = binding;
		this.uri = uri;
	}

	@Override
	protected BindingResult doInBackground(Void... voids) {
		URI requestURI = URI.create(uri);
		String path = requestURI.getPath();
		String resourceName = path.substring(1, path.length()); // remove leading '/'

		// find suitable addon
		try {
			AppPluginAction action = binding.getAddonSelector().getAppPluginAction(resourceName);
			if (binding.getAddonManager() == null) {
				throw new IllegalStateException(ADDON_INIT_FAILED);
			} else {
				String rawQuery = requestURI.getRawQuery();
				Map<String, String> queries = new HashMap<>(0);
				if (rawQuery != null) {
					queries = HttpRequestLineUtils.transform(rawQuery);
				}
				return action.execute(null, queries, null, null);
			}
		} catch (AddonNotFoundException ex) {
			LOG.info(ADDON_NOT_FOUND, ex);
		} catch (UnsupportedEncodingException ex) {
			LOG.warn("Unsupported encoding.", ex);
		} catch (Exception ex) {
			LOG.warn(ex.getMessage(), ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(BindingResult bindingResult) {
		BindingTaskResponse response = new BindingTaskResponse(bindingResult);
		binding.getContextWrapper().setResultOfBindingTask(response);
	}
}
