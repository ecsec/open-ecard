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
 */
package org.openecard.common

import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.common.util.FileUtils.resolveResourceAsURL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import javax.annotation.Nonnull

/**
 * An internationalization component similar to Java's [java.util.ResourceBundle].
 * It is capable of providing translated versions of key identified values as well as complete files.
 *
 *
 * All translation files must be located below the folder <pre>openecard_i18n</pre> plus a component name.
 * The special file Messages.properties is used to provide translated key value pairs. Completely translated files can
 * have any other name.<br></br>
 * All translation files follow the same scheme to identify their language. The language is written in the form of
 * [BCP 47](https://tools.ietf.org/html/bcp47) language tags. However instead of -, _ is used as a separator,
 * which is the common practice. This implementation only supports a subset of the BCP 47 specification, meaning only
 * language and country codes are allowed. The default language, which is English is described by C. The name of the
 * file and its language is separated by _. The file ending is optional for arbitrary files.<br></br>
 * The following examples illustrate the scheme.
 * <pre> Messages_C.properties
 * Messages_de.properties
 * Messages_de_DE.properties
 * anyotherfile_C
 * anyotherfile_C.html</pre>
 *
 * @author Tobias Wich
 */
class I18n private constructor(private val loaderReference: Class<*>, component: String) {
    private val component: String
    private val translation: Properties
    private val original: Properties
    private val translatedFiles: TreeMap<String, URL?>

    init {
        val userLocale = locale
        val lang = userLocale.language
        val country = userLocale.country
        // load applicable language files
        // the order is: C -> lang -> lang_country
        var defaults = loadFile(component, "C")
        val loadedDefaults = defaults != null
        if (!loadedDefaults) {
            defaults = Properties()
        }
        this.original = defaults!!.clone() as Properties
        val loadedLang: Boolean
        if (!lang.isEmpty()) {
            val target = loadFile(component, lang)
            loadedLang = target != null
            if (loadedLang) {
                defaults = mergeProperties(defaults, target)
            }
        } else {
            loadedLang = false
        }
        val loadedCountry: Boolean
        if (!lang.isEmpty() && !country.isEmpty()) {
            val target = loadFile(component, lang + "_" + country)
            loadedCountry = target != null
            if (loadedCountry) {
                defaults = mergeProperties(defaults, target)
            }
        } else {
            loadedCountry = false
        }

        this.component = component
        this.translation = defaults
        if (!loadedDefaults && !loadedLang && !loadedCountry) {
            logger.warn(
                "The loaded resource '{}' does not contain any translation resources for 'C', '{}' or '{}'.",
                component, lang, country
            )
        } else if (loadedDefaults && !(loadedLang || loadedCountry)) {
            logger.warn(
                "The loaded resource '{}' contains only standard text for 'C' but not translations for '{}' or '{}'.",
                component, lang, country
            )
        } else if (!loadedDefaults && (loadedLang || loadedCountry)) {
            logger.warn(
                "The loaded resource '{}' contains translations for '{}' or '{}', but no standard text for 'C'.",
                component, lang, country
            )
        }
        this.translatedFiles = TreeMap()
    }

    private fun loadFile(component: String, locale: String): Properties? {
        // load properties or die tryin'
        try {
            val fileName = "/openecard_i18n/$component/Messages_$locale.properties"
            val `in` = resolveResourceAsStream(loaderReference, fileName)
            val props = Properties()
            val r: Reader = InputStreamReader(`in`, "utf-8")
            props.load(r)
            return props
        } catch (ex: IOException) {
            return null
        } catch (ex: RuntimeException) {
            return null
        }
    }

    /**
     * public non static api
     */
    /**
     * @return Name of the component this I18n instance is responsible for.
     */
    fun associatedComponent(): String {
        return component
    }

    /**
     * Get the translated value for the given key.
     * The implementation tries to find the key in the requested language, then the default language and if nothing is
     * specified at all, a special string in the form of &lt;No translation for key &lt;requested.key&gt;&gt;
     * is returned.
     *
     * @param key Key as defined in language properties file.
     * @param parameters If any parameters are given here, the string is interpreted as a template and the parameters
     * are applied. The template interpretation uses [String.format] as the rendering method.
     * @return Translation as specified in the translation, or default file.
     */
    fun translationForKey(key: String, vararg parameters: Any?): String {
        val result = translation.getProperty(key.lowercase(Locale.getDefault()))
        if (result == null) {
            return "<<No translation for key <$key>>"
        } else if (parameters.size != 0) {
            val formattedResult = String.format(result, *parameters)
            return formattedResult
        } else {
            return result
        }
    }

    /**
     * Get the translated value for the given key.
     * The implementation tries to find the key in the requested language, then the default language and if nothing is
     * specified at all, a special string in the form of &lt;No translation for key &lt;requested.key&gt;&gt;
     * is returned.
     *
     * @param key Key as defined in language properties file.
     * @param parameters If any parameters are given here, the string is interpreted as a template and the parameters
     * are applied. The template interpretation uses [String.format] as the rendering method.
     * @return Translation as specified in the translation, or default file.
     */
    fun translationForKey(key: I18nKey, vararg parameters: Any?): String {
        return translationForKey(key.key, *parameters)
    }


    /**
     * Calls [.translationForFile] with the second parameter set to null.
     *
     * @param name Name part of the file.
     * @return URL pointing to the translated file.
     * @throws IOException Thrown in case no resource is available.
     */
    @Nonnull
    @Throws(IOException::class)
    fun translationForFile(name: String): URL {
        return translationForFile(name, null)
    }

    /**
     * Get translated version of a file depending on current locale.
     *
     * The file's base path equals the component directory. The language definition is enclosed between the filename
     * and the file ending plus a '.'.
     *
     * An example looks like this:
     * <pre> I18n l = I18n.getTranslation("gui");
     * l.translationForFile("about", "html");
     * // this code in a german environment tries to load the following files until one is found
     * // - openecard_i18n/gui/about_de_DE.html
     * // - openecard_i18n/gui/about_de.html
     * // - openecard_i18n/gui/about_C.html</pre>
     *
     * @param name Name part of the file.
     * @param fileEnding File ending if available, null otherwise.
     * @return URL pointing to the translated, or default file.
     * @throws IOException Thrown in case no resource is available.
     */
    @Nonnull
    @Synchronized
    @Throws(IOException::class)
    fun translationForFile(name: String, fileEnding: String?): URL {
        // check if the url has already been found previously
        var fileEnding = fileEnding
        fileEnding = if (fileEnding != null) (".$fileEnding") else ""
        val mapKey = name + fileEnding
        if (translatedFiles.containsKey(mapKey)) {
            val url = translatedFiles[mapKey]
            if (url == null) {
                throw IOException("No translation available for file '$name$fileEnding'.")
            } else {
                return url
            }
        }

        val locale = Locale.getDefault()
        val lang = locale.language
        val country = locale.country
        val fnameBase = "/openecard_i18n/$component/$name"
        // try to guess correct file to load
        if (!lang.isEmpty() && !country.isEmpty()) {
            val fileName = fnameBase + "_" + lang + "_" + country + fileEnding
            val url = resolveResourceAsURL(loaderReference, fileName)
            if (url != null) {
                translatedFiles[mapKey] = url
                return url
            }
        }
        if (!lang.isEmpty()) {
            val fileName = fnameBase + "_" + lang + fileEnding
            val url = resolveResourceAsURL(loaderReference, fileName)
            if (url != null) {
                translatedFiles[mapKey] = url
                return url
            }
        }
        // else
        val fileName = fnameBase + "_C" + fileEnding
        val url = resolveResourceAsURL(loaderReference, fileName)
        if (url != null) {
            translatedFiles[mapKey] = url
            return url
        }

        // no file found
        translatedFiles[mapKey] = null
        throw IOException("No translation available for file '$name$fileEnding'.")
    }

    /**
     * Get the original English text which is referenced by the key.
     *
     * @param key Reference to the requested text.
     * @param parameters
     * @return A [String] containing the original English text of the message.
     */
    fun getOriginalMessage(key: String, vararg parameters: Any?): String {
        val result = original.getProperty(key.lowercase(Locale.getDefault()))
        if (result == null) {
            return "<<No translation for key <$key>>"
        } else if (parameters.size != 0) {
            val formattedResult = String.format(result, *parameters)
            return formattedResult
        } else {
            return result
        }
    }

    /**
     * Get the original English text which is referenced by the key.
     *
     * @param key Reference to the requested text.
     * @param parameters
     * @return A [String] containing the original English text of the message.
     */
    fun getOriginalMessage(key: I18nKey, vararg parameters: Any?): String {
        return getOriginalMessage(key.key, *parameters)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(I18n::class.java)
        private val translations =
            ConcurrentSkipListMap<String, I18n>()

        init {
            // preload important components
            getTranslation("ifd")
            getTranslation("sal")
        }

        /**
         * Load a translation for the specified component.
         * If no translation for a component exists, a fallback method is used according to [java.util.ResourceBundle]
         * and in case no translation exists at all, an empty I18n instance is returned.
         *
         * This method uses the I18n class as a reference to find resources.
         *
         * @param component String describing the component. This must also be the filename prefix of the translation.
         * @return I18n instance responsible for specified component.
         * @see .getTranslation
         */
        @JvmStatic
        fun getTranslation(component: String): I18n? {
            return getTranslation(I18n::class.java, component)
        }

        /**
         * Load a translation for the specified component.
         * If no translation for a component exists, a fallback method is used according to [java.util.ResourceBundle]
         * and in case no translation exists at all, an empty I18n instance is returned.
         *
         * This method uses the given class as a reference to find resources.
         *
         * @param loaderReference Class used to resolve the classloader used to find the I18n resources.
         * @param component String describing the component. This must also be the filename prefix of the translation.
         * @return I18n instance responsible for specified component.
         */
        @Synchronized
        fun getTranslation(loaderReference: Class<*>, component: String): I18n? {
            if (translations.containsKey(component)) {
                return translations[component]
            } else {
                val t = I18n(loaderReference, component)
                translations[component] = t
                return t
            }
        }


        val locale: Locale
            get() = Locale.getDefault()

        private fun mergeProperties(defaults: Properties, target: Properties): Properties {
            val result = Properties(defaults)
            result.putAll(target)
            return result
        }
    }
}
