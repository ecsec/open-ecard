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

package org.openecard.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.client.common.util.FileUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class I18n {

    private static final ConcurrentSkipListMap<String,I18n> translations;

    static {
	translations = new ConcurrentSkipListMap<String,I18n>();
	// preload important components
	getTranslation("ifd");
	getTranslation("sal");
    }

    /**
     * Load a translation for the specified component. If no translation for a
     * component exists, and empty I18n instance is returned.
     *
     * @param component String describing the component. This must also be the filename prefix of the translation.
     * @return I18n instance responsible for specified component.
     */
    public synchronized static I18n getTranslation(String component) {
	if (translations.containsKey(component)) {
	    return translations.get(component);
	} else {
	    I18n t = new I18n(component);
	    translations.put(component, t);
	    return t;
	}
    }


    private final String component;
    private final Properties translation;

    private I18n(String component) {
	Locale userLocale = Locale.getDefault();
	String lang = userLocale.getLanguage();
	String country = userLocale.getCountry();
	// load applicable language files
	// the order is: C -> lang -> lang_country
	Properties defaults = loadFile(component, "C");
	if (!lang.isEmpty()) {
	    Properties target = loadFile(component, lang);
	    defaults = mergeProperties(defaults, target);
	}
	if (!lang.isEmpty() && !country.isEmpty()) {
	    Properties target = loadFile(component, lang + "_" + country);
	    defaults = mergeProperties(defaults, target);
	}

	this.component = component;
	this.translation = defaults;
    }

    private static Properties loadFile(String component, String locale) {
	// load properties or die tryin'
	try {
	    String fileName = "/openecard_i18n/" + component + "/Messages_" + locale + ".properties";
	    InputStream in = FileUtils.resolveResourceAsStream(I18n.class, fileName);
	    Properties props = new Properties();
	    Reader r = new InputStreamReader(in, "utf-8");
	    props.load(r);
	    return props;
	} catch (IOException ex) {
	    return new Properties();
	} catch (RuntimeException ex) { // no such file and stuff
	    return new Properties();
	}
    }

    private static Properties mergeProperties(Properties defaults, Properties target) {
	Properties result = new Properties(defaults);
	result.putAll(target);
	return result;
    }


    ///
    /// public non static api
    ///

    /**
     * @return Name of the component this I18n instance is responsible for.
     */
    public String associatedComponent() {
	return component;
    }

    /**
     * Get the translated value for the given key. <br/>
     * The implementation tries to find the key in the requested language, then the default language and if nothing is
     * specified at all, a special string in the form of &lt;No translation for key &lt;requested.key&gt;&gt;
     * is returned.
     *
     * @param key Key as defined in language properties file.
     * @param parameters If any parameters are given here, the string is interpreted as a template and the parameters
     * are applied. The template interpretation uses {@link String#format()} as the rendering method.
     * @return Translation as specified in the translation, or default file.
     */
    public String translationForKey(String key, Object ... parameters) {
	String result = translation.getProperty(key.toLowerCase());
	if (result == null) {
	    return "<<No translation for key <" + key + ">>";
	} else if (parameters.length != 0) {
	    String formattedResult = String.format(result, parameters);
	    return formattedResult;
	} else {
	    return result;
	}
    }

}
