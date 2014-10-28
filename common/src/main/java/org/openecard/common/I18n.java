/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An internationalization component similar to Java's {@link java.util.ResourceBundle}.
 * It is capable of providing translated versions of key identified values as well as complete files.
 * <p>
 * All translation files must be located below the folder <pre>openecard_i18n</pre> plus a component name.
 * The special file Messages.properties is used to provide translated key value pairs. Completely translated files can
 * have any other name.<br/>
 * All translation files follow the same scheme to identify their language. The language is written in the form of
 * <a href="https://tools.ietf.org/html/bcp47">BCP 47</a> language tags. However instead of -, _ is used as a separator,
 * which is the common practice. This implementation only supports a subset of the BCP 47 specification, meaning only
 * language and country codes are allowed. The default language, which is English is described by C. The name of the
 * file and its language is separated by _. The file ending is optional for arbitrary files.<br/>
 * The following examples illustrate the scheme.
 * <pre> Messages_C.properties
 * Messages_de.properties
 * Messages_de_DE.properties
 * anyotherfile_C
 * anyotherfile_C.html</pre>
 *
 * @author Tobias Wich
 */
public class I18n {

    private static final Logger logger = LoggerFactory.getLogger(I18n.class);
    private static final ConcurrentSkipListMap<String,I18n> translations;

    static {
	translations = new ConcurrentSkipListMap<>();
	// preload important components
	getTranslation("ifd");
	getTranslation("sal");
    }

    /**
     * Load a translation for the specified component.
     * If no translation for a component exists, a fallback method is used according to {@link java.util.ResourceBundle}
     * and in case no translation exists at all, an empty I18n instance is returned.
     * <p>This method uses the I18n class as a reference to find resources.</p>
     *
     * @param component String describing the component. This must also be the filename prefix of the translation.
     * @return I18n instance responsible for specified component.
     * @see #getTranslation(java.lang.Class, java.lang.String)
     */
    public static I18n getTranslation(String component) {
	return getTranslation(I18n.class, component);
    }

    /**
     * Load a translation for the specified component.
     * If no translation for a component exists, a fallback method is used according to {@link java.util.ResourceBundle}
     * and in case no translation exists at all, an empty I18n instance is returned.
     * <p>This method uses the given class as a reference to find resources.</p>
     *
     * @param loaderReference Class used to resolve the classloader used to find the I18n resources.
     * @param component String describing the component. This must also be the filename prefix of the translation.
     * @return I18n instance responsible for specified component.
     */
    public synchronized static I18n getTranslation(Class<?> loaderReference, String component) {
	if (translations.containsKey(component)) {
	    return translations.get(component);
	} else {
	    I18n t = new I18n(loaderReference, component);
	    translations.put(component, t);
	    return t;
	}
    }


    private final Class<?> loaderReference;
    private final String component;
    private final Properties translation;
    private final Properties original;
    private final TreeMap<String, URL> translatedFiles;

    public static Locale getLocale() {
	return Locale.getDefault();
    }


    private I18n(Class<?> loaderReference, String component) {
	this.loaderReference = loaderReference;
	Locale userLocale = getLocale();
	String lang = userLocale.getLanguage();
	String country = userLocale.getCountry();
	// load applicable language files
	// the order is: C -> lang -> lang_country
	Properties defaults = loadFile(component, "C");
	this.original = (Properties) defaults.clone();
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
	this.translatedFiles = new TreeMap<>();
    }

    private Properties loadFile(String component, String locale) {
	// load properties or die tryin'
	try {
	    String fileName = "/openecard_i18n/" + component + "/Messages_" + locale + ".properties";
	    InputStream in = FileUtils.resolveResourceAsStream(loaderReference, fileName);
	    Properties props = new Properties();
	    Reader r = new InputStreamReader(in, "utf-8");
	    props.load(r);
	    return props;
	} catch (IOException | RuntimeException ex) {
	    logger.warn("Failed to load resource {} for lang {}.", component, locale);
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
     * Get the translated value for the given key.
     * The implementation tries to find the key in the requested language, then the default language and if nothing is
     * specified at all, a special string in the form of &lt;No translation for key &lt;requested.key&gt;&gt;
     * is returned.
     *
     * @param key Key as defined in language properties file.
     * @param parameters If any parameters are given here, the string is interpreted as a template and the parameters
     *   are applied. The template interpretation uses {@link String#format()} as the rendering method.
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


    /**
     * Calls {@link #translationForFile(java.lang.String, java.lang.String)} with the second parameter set to null.
     * @param name
     * @return
     * @throws IOException
     */
    public URL translationForFile(String name) throws IOException {
	return translationForFile(name, null);
    }

    /**
     * Get translated version of a file depending on current locale.
     * <p>The file's base path equals the component directory. The language definition is enclosed between the filename
     * and the file ending plus a '.'.</p>
     * <p>An example looks like this:<br/>
     * <pre> I18n l = I18n.getTranslation("gui");
     * l.translationForFile("about", "html");
     * // this code in a german environment tries to load the following files until one is found
     * // - openecard_i18n/gui/about_de_DE.html
     * // - openecard_i18n/gui/about_de.html
     * // - openecard_i18n/gui/about_C.html</pre>
     * </p>
     *
     * @param name Name part of the file
     * @param fileEnding File ending if available, null otherwise.
     * @return URL pointing to the translated, or default file.
     * @throws IOException Thrown in case no resource is available.
     */
    public synchronized URL translationForFile(String name, String fileEnding) throws IOException {
	// check if the url has already been found previously
	fileEnding = fileEnding != null ? ("." + fileEnding) : "";
	String mapKey = name + fileEnding;
	if (translatedFiles.containsKey(mapKey)) {
	    URL url = translatedFiles.get(mapKey);
	    if (url == null) {
		throw new IOException("No translation available for file '" + name + fileEnding + "'.");
	    } else {
		return url;
	    }
	}

	Locale locale = Locale.getDefault();
	String lang = locale.getLanguage();
	String country = locale.getCountry();
	String fnameBase = "/openecard_i18n/" + component + "/" + name;
	// try to guess correct file to load
	if (!lang.isEmpty() && !country.isEmpty()) {
	    String fileName = fnameBase + "_" + lang + "_" + country + fileEnding;
	    URL url = FileUtils.resolveResourceAsURL(loaderReference, fileName);
	    if (url != null) {
		translatedFiles.put(mapKey, url);
		return url;
	    }
	}
	if (!lang.isEmpty()) {
	    String fileName = fnameBase + "_" + lang + fileEnding;
	    URL url = FileUtils.resolveResourceAsURL(loaderReference, fileName);
	    if (url != null) {
		translatedFiles.put(mapKey, url);
		return url;
	    }
	}
	// else
	String fileName = fnameBase + "_C" + fileEnding;
	URL url = FileUtils.resolveResourceAsURL(loaderReference, fileName);
	if (url != null) {
	    translatedFiles.put(mapKey, url);
	    return url;
	}

	// no file found
	translatedFiles.put(mapKey, null);
	throw new IOException("No translation available for file '" + name + fileEnding + "'.");
    }

    /**
     * Get the original English text which is referenced by the key.
     *
     * @param key Reference to the requested text.
     * @param parameters
     * @return A {@link String} containing the original English text of the message.
     */
    public String getOriginalMessage(String key, Object ... parameters) {
	String result = original.getProperty(key.toLowerCase());
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
