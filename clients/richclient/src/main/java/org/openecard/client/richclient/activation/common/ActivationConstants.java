/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.richclient.activation.common;

import org.openecard.client.common.I18n;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ActivationConstants {

    private static I18n lang = I18n.getTranslation("activation");

    public enum ActivationError {

	BAD_REQUEST, TC_TOKEN_NOT_AVAILABLE, TC_TOKEN_REFUSED, INTERNAL_ERROR;

	@Override
	public String toString() {
	    return lang.translationForKey(this.name());
	}
    }

}
