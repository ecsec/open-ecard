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

package org.openecard.mdlw.sal;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.CardTypeType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openecard.common.util.FileUtils;
import org.openecard.mdlw.event.CardConfigType;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Configuration of the Middleware. The Middleware-Config instance is required for loading a Middleware Config ZIP-File.
 * Each ZIP-File contains one xml-file which configures the Middleware and the used cards. The images of the cards are
 * stored in the "card-images"-folder.
 *
 * @author Mike Prechtl
 */
public class MiddlewareConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MiddlewareConfig.class);

    private static final String NAME_OF_MIDDLEWARE_CONFIG_XML = "mwconfig.xml";
    private static final String NAME_OF_ROOT_FOLDER = "middleware/";
    private static final String PATH_TO_CARD_IMAGES = "card-images/";
    private static final String NAME_OF_CARD_INFO_TEMPLATE = "mw_cif_template.xml";

    private static List<MiddlewareSALConfig> mwSALConfigs;
    private static final Map<String, byte[]> CARD_IMAGES = new HashMap<>();

    private MiddlewareConfigType middlewareConfigXml;
    private Document cifDocument;
    private WSMarshaller cifMarshaller;
    private String resFolder;

    public MiddlewareConfig(@Nonnull String mwConfigPath) throws IOException, FileNotFoundException, JAXBException {
	LOG.info("Path of Middleware Config: '{}'", mwConfigPath);
	this.resFolder = mwConfigPath.substring(0, mwConfigPath.lastIndexOf('/') + 1);
	String nameOfMiddlewareConfig = mwConfigPath.substring(mwConfigPath.lastIndexOf('/') + 1);
        String pathToMwConfigXml = NAME_OF_ROOT_FOLDER + NAME_OF_MIDDLEWARE_CONFIG_XML;
        String pathToCardImages = NAME_OF_ROOT_FOLDER + PATH_TO_CARD_IMAGES;

        try (InputStream is = getStream(resFolder, nameOfMiddlewareConfig)) {
	    loadMwZIPConfig(is, NAME_OF_ROOT_FOLDER, pathToMwConfigXml, pathToCardImages);
        } catch (FileNotFoundException ex) {
	    InputStream is = getStream(mwConfigPath);
	    loadMwZIPConfig(is, NAME_OF_ROOT_FOLDER, pathToMwConfigXml, pathToCardImages);
	}
    }

    private void loadMwZIPConfig(InputStream is, String rootFolder, String pathToMwConfigXml, String pathToCardImages)
	    throws JAXBException {
	JAXBContext ctx = JAXBContext.newInstance(MiddlewareConfigType.class);
	middlewareConfigXml = new MiddlewareConfigType();

	try (ZipInputStream zipIn = new ZipInputStream(is)) {
	    ZipEntry zipEntry;
	    while ((zipEntry = zipIn.getNextEntry()) != null) {
		if (! zipEntry.isDirectory()) {
		    String name = zipEntry.getName();
		    if (name.startsWith(pathToMwConfigXml)) {
			String middlewareConfigXmlName = name.replace(rootFolder, "");
			LOG.debug("Name of Middleware Config XML: " + middlewareConfigXmlName);
			middlewareConfigXml = (MiddlewareConfigType) ctx.createUnmarshaller().unmarshal(zipIn);
		    }
		    if (name.startsWith(pathToCardImages)) {
			String cardImageName = name.replace(pathToCardImages, "");
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
     * Loads a resource which is within the middleware-resource folder.
     *
     * @param resFolder Name of the resource folder.
     * @param resname Name of the resource.
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getStream(String resFolder, String resname) throws FileNotFoundException {
        InputStream in = getClass().getResourceAsStream(resFolder + resname);
        if (in == null) {
            in = getClass().getResourceAsStream("/" + resFolder + resname);
        }
        if (in == null) {
	    throw new FileNotFoundException("Unable to load file '" + resname + "'.");
	}
        return in;
    }

    /**
     * Loads a resource above an absolute path.
     *
     * @param absPath path to the Middleware Config ZIP.
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getStream(String absPath) throws FileNotFoundException {
	File file = new File(absPath);
        InputStream in = new FileInputStream(file);
        return in;
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
     * Returns the name of the CardInfo-Template.
     *
     * @return
     */
    public String getCardInfoTemplateName() {
        return NAME_OF_CARD_INFO_TEMPLATE;
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
    public byte[] getCardImage(String imageName) {
	return CARD_IMAGES.get(imageName);
    }

    /**
     * Returns the CardInfo-Template as CardInfoType.
     *
     * @return CardInfo-Template or {@code null} if template can not be parsed.
     */
    @Nullable
    private CardInfoType getCardInfoTemplate() {
        if (cifDocument != null && cifMarshaller != null) {
            CardInfoType cardInfo;
            try {
                cardInfo = (CardInfoType) cifMarshaller.unmarshal(cifDocument);
                return cardInfo;
            } catch (WSMarshallerException ex) {
                LOG.warn("Can not parse CardInfo-Document.");
            }
        }

        // return nothing
        return null;
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
	if (cardInfo != null) {
	    cardInfo.setCardType(mapCardSpecToCardType(cardSpec));
	    return cardInfo;
	}

	// return nothing
	return null;
    }

}
