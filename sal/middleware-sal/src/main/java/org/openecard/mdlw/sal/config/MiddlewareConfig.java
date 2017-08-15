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

package org.openecard.mdlw.sal.config;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.CardTypeType;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.FuturePromise;
import org.openecard.common.util.Promise;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Configuration of the Middleware. The Middleware-Config instance is required for loading a Middleware Config ZIP-File.
 * Each ZIP-File contains one xml-file which configures the Middleware and the used cards. The images of the cards are
 * stored in the "card-images"-folder.
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 */
public class MiddlewareConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MiddlewareConfig.class);

    private static final String MIDDLEWARE_CONFIG_PATH = "mwconfig.xml";
    private static final String CARD_IMAGE_PATH = "card-images/";
    private static final String CIF_TEMPLATE_PATH = "/middleware/mw_cif_template.xml";

    private static final Promise<Document> CIF_DOC;
    private static final Promise<WSMarshaller> MARSHALLER;

    static {
	MARSHALLER = new FuturePromise<>(new Callable<WSMarshaller>() {
	    @Override
	    public WSMarshaller call() throws Exception {
		return WSMarshallerFactory.createInstance();
	    }
	});
	CIF_DOC = new FuturePromise<>(new Callable<Document>() {
	    @Override
	    public Document call() throws Exception {
		InputStream in = FileUtils.resolveResourceAsStream(MiddlewareConfig.class, CIF_TEMPLATE_PATH);
		return MARSHALLER.deref().str2doc(in);
	    }
	});
    }


    private static List<MiddlewareSALConfig> mwSALConfigs;
    private final Map<String, byte[]> CARD_IMAGES = new HashMap<>();

    private MiddlewareConfigType middlewareConfigXml;

    public MiddlewareConfig(@Nonnull InputStream bundleStream) throws IOException, FileNotFoundException, JAXBException {
	LOG.debug("Loading middleware config.");
	loadMwZIPConfig(bundleStream);
    }

    private void loadMwZIPConfig(InputStream is)
	    throws JAXBException {
	JAXBContext ctx = JAXBContext.newInstance(MiddlewareConfigType.class);
	middlewareConfigXml = new MiddlewareConfigType();

	try (ZipInputStream zipIn = new ZipInputStream(is)) {
	    ZipEntry zipEntry;
	    while ((zipEntry = zipIn.getNextEntry()) != null) {
		if (! zipEntry.isDirectory()) {
		    String name = zipEntry.getName();
		    if (name.equals(MIDDLEWARE_CONFIG_PATH)) {
			LOG.debug("Reading middleware config from XML file.");
			middlewareConfigXml = (MiddlewareConfigType) ctx.createUnmarshaller().unmarshal(zipIn);
		    }
		    if (name.startsWith(CARD_IMAGE_PATH)) {
			String cardImageName = name.replace(CARD_IMAGE_PATH, "");
			LOG.debug("CardImageName: " + cardImageName);
			CARD_IMAGES.put(cardImageName, FileUtils.toByteArray(zipIn));
		    }
		}
		zipIn.closeEntry();
	    }
	} catch (IOException ex) {
	    LOG.debug("Stream closed.");
	}
    }

    /**
     * Returns the MiddlewareSAL configs which are specified in the Middleware Config.
     *
     * @return MiddlewareSAL config.
     */
    @Nonnull
    public List<MiddlewareSALConfig> getMiddlewareSALConfigs() {
	if (mwSALConfigs == null) {
	    if (middlewareConfigXml != null) {
		mwSALConfigs = new ArrayList<>();
		ArrayList<MiddlewareConfigType.MiddlewareSpec> mwSpecs = middlewareConfigXml
			.getMiddlewareSpecs();
		for (MiddlewareConfigType.MiddlewareSpec mwSpec : mwSpecs) {
		    MiddlewareSALConfig mwSALConfig = new MiddlewareSALConfig(this, mwSpec);
		    MiddlewareConfig.mwSALConfigs.add(mwSALConfig);
		}
		return MiddlewareConfig.mwSALConfigs;
	    }
	} else {
	    return mwSALConfigs;
	}
        return Collections.EMPTY_LIST;
    }

    /**
     * Maps the Middleware name to the object identifier of the card.
     *
     * @param middlewareCardType
     * @return object identifier
     */
    @Nullable
    public String mapMiddlewareType(@Nonnull String middlewareCardType) {
        for (MiddlewareSALConfig mwSALConfig : getMiddlewareSALConfigs()) {
            String mwType = mwSALConfig.mapMiddlewareType(middlewareCardType);
            if (mwType != null) {
                return mwType;
            }
        }

	// nothing found
	return null;
    }

    /**
     * Returns the Card-Image as byte array.
     *
     * @param imageName name of the image
     * @return Card Image as InputStream or {@code null} if card is not known by Middleware Config.
     */
    @Nullable
    public InputStream getCardImage(String imageName) {
	return new ByteArrayInputStream(CARD_IMAGES.get(imageName));
    }

    /**
     * Returns the CardInfo-Template as CardInfoType.
     *
     * @return CardInfo-Template or {@code null} if template can not be parsed.
     */
    @Nonnull
    private synchronized CardInfoType getCardInfoTemplate() {
	CardInfoType cardInfo;
	try {
	    WSMarshaller m = MARSHALLER.deref();
	    Document doc = CIF_DOC.deref();
	    cardInfo = m.unmarshal(doc, CardInfoType.class).getValue();
	    return cardInfo;
	} catch (WSMarshallerException ex) {
	    String msg = "Can not parse CardInfo-Document.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(CARD_IMAGE_PATH, ex);
	} catch (InterruptedException ex) {
	    String msg = "Shutdown requested while retrieving CIF template.";
	    LOG.debug(msg);
	    throw new RuntimeException(msg);
	} catch (NullPointerException ex) {
	    String msg = "Marshaller and/ or CIF Template could not be loaded correctly.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg);
	}
    }

    @Nonnull
    private CardTypeType mapCardSpecToCardType(CardConfigType.CardSpec cardSpec) {
        CardTypeType cardType = new CardTypeType();
        cardType.setObjectIdentifier(cardSpec.getObjectIdentifier());
        cardType.setDate(cardSpec.getDate());
        cardType.getCardTypeName().addAll(cardSpec.getCardTypeName());
        cardType.setStatus(cardSpec.getStatus());
        cardType.setVersion(cardSpec.getVersion());
        return cardType;
    }

    /**
     * Stores the CardType-Spec in the CardInfo Template. The filled CardInfo Template will be returned.
     *
     * @param cardSpec specification of the card.
     * @return {@code CardInfoType} or {@code null} if there is no available CardInfo Template.
     */
    public CardInfoType getCardInfoByCardSpec(CardConfigType.CardSpec cardSpec) {
	CardInfoType cardInfo = getCardInfoTemplate();
	cardInfo.setCardType(mapCardSpecToCardType(cardSpec));
	return cardInfo;
    }

}
