/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
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

package org.openecard.client.gui.android;

import android.content.Context;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.UserConsentDescription;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class AndroidUserConsent implements org.openecard.client.gui.UserConsent{

	private Context context;
	
	public AndroidUserConsent(Context context){
		this.context = context;
	}

	@Override
	public UserConsentNavigator obtainNavigator(UserConsentDescription arg0) {
		return new AndroidNavigator(arg0.getSteps(), this.context);
	}

}
