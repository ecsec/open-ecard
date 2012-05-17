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

package org.openecard.client.gui.definition;

import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Hyperlink implements InputInfoUnit {

    private String text;
    private URL href;

    /**
     * @return the text
     */
    public String getText() {
	return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
	this.text = text;
    }

    /**
     * @return the href
     */
    public URL getHref() {
	return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(URL href) {
	this.href = href;
    }
    /**
     * @param href the href to set
     */
    public void setHref(String href) throws MalformedURLException {
	this.href = new URL(href);
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.HYPERLINK;
    }

}
