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

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * XML-Schema for the Middleware Configuration
 *
 * @author Tobias Wich
 * @author Mike Prechtl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MiddlewareConfig")
@XmlType(name = "MiddlewareConfigType", propOrder = {
    "middlewareSpecs"
})
public class MiddlewareConfigType {

    @XmlElement(name = "MiddlewareSpec")
    private final ArrayList<MiddlewareSpec> middlewareSpecs = new ArrayList<>();

    public ArrayList<MiddlewareSpec> getMiddlewareSpecs() {
        return middlewareSpecs;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "MiddlewareSpec")
    @XmlType(name = "MiddlewareSpecType", propOrder = {
        "middlewareName",
        "libName",
        "searchPath",
        "x32searchPath",
        "x64searchPath",
        "cardConfig"
    })
    public static class MiddlewareSpec {

	@XmlAttribute(name="required")
	private boolean required = false;
        @XmlElement(name="MiddlewareName", required = true)
        private String middlewareName;
        @XmlElement(name = "LibName", required = true)
        private String libName;
        @XmlElement(name = "CardConfig")
        private CardConfigType cardConfig;
        @XmlElement(name = "SearchPath")
        private final ArrayList<String> searchPath = new ArrayList<>();
        @XmlElement(name = "X32SearchPath")
        private final ArrayList<String> x32searchPath = new ArrayList<>();
        @XmlElement(name = "X64SearchPath")
        private final ArrayList<String> x64searchPath = new ArrayList<>();

	public boolean isRequired() {
	    return required;
	}

	public void setRequired(boolean required) {
	    this.required = required;
	}

        public String getMiddlewareName() {
            return middlewareName;
        }

        public void setMiddlewareName(String middlewareSALName) {
            this.middlewareName = middlewareSALName;
        }

        public String getLibName() {
            return libName;
        }

        public void setLibName(String libName) {
            this.libName = libName;
        }

        public CardConfigType getCardConfig() {
            return cardConfig;
        }

        public void setCardConfig(CardConfigType cardConfig) {
            this.cardConfig = cardConfig;
        }

        public void addSearchPath(String searchPath) {
            this.searchPath.add(searchPath);
        }

        public ArrayList<String> getSearchPath() {
            return searchPath;
        }

        public void addX32SearchPath(String x32SearchPath) {
            this.x32searchPath.add(x32SearchPath);
        }

        public ArrayList<String> getX32searchPath() {
            return x32searchPath;
        }

        public void addX64SearchPath(String x64SearchPath) {
            this.x64searchPath.add(x64SearchPath);
        }

        public ArrayList<String> getX64searchPath() {
            return x64searchPath;
        }

    }

}
