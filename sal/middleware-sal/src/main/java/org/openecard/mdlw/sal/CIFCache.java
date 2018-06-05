/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import iso.std.iso_iec._24727.tech.schema.CardInfo;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * CIF cache using in memory and if possible a on disk persisted cache.
 * CIFs are indexed by their serial number.<br>
 * The cache can be disabled with the property {@code cache-generated-cifs}.
 *
 * @author Tobias Wich
 */
public class CIFCache {

    private static final Logger LOG = LoggerFactory.getLogger(CIFCache.class);

    private static final String PREFIX = "";//"V2_";
    private static final CIFCache INST = new CIFCache();

    private final WSMarshaller marshaller;
    private final File cacheDir;
    private final Map<String, CardInfoType> memCache;

    public static CIFCache getInstance() {
	return INST;
    }

    private CIFCache() {
	WSMarshaller m;
	File cd;
	try {
	   m = WSMarshallerFactory.createInstance();
	   cd = new File(FileUtils.getHomeConfigDir(), "cif-cache");
	   if (! cd.exists() && ! cd.mkdirs()) {
	       throw new SecurityException("Failed to create cache directory.");
	   }
	} catch (WSMarshallerException ex) {
	   LOG.error("Failed to instantiate marshaller, disabling persistent CIF cache.");
	   m = null;
	   cd = null;
	} catch (IOException | SecurityException ex) {
	   LOG.error("Failed to obtain/ create cache directory, disabling persistent CIF cache.");
	   m = null;
	   cd = null;
	}

	marshaller = m;
	cacheDir = cd;
	memCache = Collections.synchronizedMap(new HashMap<>());
    }


    @Nullable
    public CardInfoType getCif(String mwName, String serial) {
	if (! hasCache()) {
	    LOG.debug("CIF caching is disabled.");
	    return null;
	}

	// check mem cache first
	if (memCache.containsKey(serial)) {
	    LOG.debug("Returning CIF from in memory cache.");
	    return memCache.get(serial);
	} else if (hasPersistentCache()) {
	    LOG.debug("Trying to read CIF from disk cache.");
	    // try reading from disk
	    File cifFile = getCifFile(mwName, serial);
	    if (cifFile.isFile()) {
		try {
		    Document cifDoc = marshaller.str2doc(new FileInputStream(cifFile));
		    Object o = marshaller.unmarshal(cifDoc);
		    if (o instanceof CardInfo) {
			CardInfo cif = (CardInfo) o;
			// save in memory for faster lookup next time
			memCache.put(serial, cif);
			return cif;
		    } else {
			throw new WSMarshallerException("Cache file did not contain a CardInfo file.");
		    }
		} catch (IOException | SAXException | WSMarshallerException ex) {
		    LOG.warn("Failed to read CIF from cache, trying to delete the corrputed file.", ex);
		    try {
			cifFile.delete();
		    } catch (SecurityException ex2) {
			LOG.error(String.format("Failed to delete cache file for serial %s.", serial), ex2);
		    }
		}
	    } else {
		LOG.debug("No cache file found on disk.");
	    }
	}

	return null;
    }

    public synchronized void saveCif(String mwName, String serial, CardInfoType cif) {
	if (! hasCache()) {
	    LOG.debug("CIF caching is disabled.");
	    return;
	}

	// save in memory
	memCache.put(serial, cif);

	// save on disk
	if (hasPersistentCache()) {
	    CardInfo cifTarget = new CardInfo();
	    cifTarget.getSignature().addAll(cif.getSignature());
	    cifTarget.setApplicationCapabilities(cif.getApplicationCapabilities());
	    cifTarget.setCardCapabilities(cif.getCardCapabilities());
	    cifTarget.setCardIdentification(cif.getCardIdentification());
	    cifTarget.setCardType(cif.getCardType());
	    cifTarget.setId(cif.getId());
	    cifTarget.setSchemaVersion(cif.getSchemaVersion());

	    try {
		Document cifDoc = marshaller.marshal(cifTarget);
		String cifXml = marshaller.doc2str(cifDoc);

		File cifFile = getCifFile(mwName, serial);
		FileOutputStream out = new FileOutputStream(cifFile, false);
		OutputStreamWriter ow = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		ow.write(cifXml);
		ow.flush();
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal CIF to DOM.", ex);
	    } catch (TransformerException ex) {
		LOG.error("Failed to serialize DOM.", ex);
	    } catch (IOException ex) {
		LOG.warn("Failed to write CIF to disk.", ex);
	    }
	}
    }

    private boolean hasCache() {
	String v = OpenecardProperties.getProperty("cache-generated-cifs");
	return Boolean.valueOf(v);
    }

    private boolean hasPersistentCache() {
	boolean initOk = marshaller != null && cacheDir != null;
	return initOk;
    }

    @Nonnull
    private File getCifFile(String mwName, String serial) {
	File cifFile = new File(cacheDir, PREFIX + mwName + "_" + serial + ".xml");
	return cifFile;
    }

}
