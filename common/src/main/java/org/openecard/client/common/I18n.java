package org.openecard.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;


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
    public static I18n getTranslation(String component) {
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
	String lang = OpenecardProperties.getProperty("org.openecard.lang");
	Properties defaults = loadFile(component + "_C.properties");
	Properties target = loadFile(component + "_" + lang + ".properties");
	this.component = component;
	this.translation = mergeProperties(defaults, target);
    }

    private static Properties loadFile(String name) {
	InputStream in = I18n.class.getResourceAsStream("openecard_config/i18n/" + name);
	if (in == null) {
	    in = I18n.class.getResourceAsStream("/openecard_config/i18n/" + name);
	}
	// load properties or die tryin'
	try {
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
     * Get the translated value for the given key. The implementation tries the
     * requested language, then the default and if nothing is specified at all,
     * a special string in the form of &lt;No translation for key &lt;requested.key&gt;&gt;
     * if returned.
     *
     * @param key Key as defined in language properties file.
     * @param parameters If any parameters are given here, the string is interpreted as a template and the parameters are applied.
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
