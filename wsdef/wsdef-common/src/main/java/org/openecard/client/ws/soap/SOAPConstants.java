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

package org.openecard.client.ws.soap;

import javax.xml.namespace.QName;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface SOAPConstants {

    public static final String SOAP_1_1_PROTOCOL = "SOAP 1.1 Protocol";
    public static final String SOAP_1_2_PROTOCOL = "SOAP 1.2 Protocol";
    public static final String DEFAULT_SOAP_PROTOCOL = SOAP_1_1_PROTOCOL;

    public static final String URI_NS_SOAP_1_1_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String URI_NS_SOAP_1_2_ENVELOPE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String URI_NS_SOAP_ENVELOPE = URI_NS_SOAP_1_1_ENVELOPE;

    public static final String URI_NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String URI_NS_SOAP_1_2_ENCODING = "http://www.w3.org/2003/05/soap-encoding";

    public static final String SOAP_1_1_CONTENT_TYPE = "text/xml";
    public static final String SOAP_1_2_CONTENT_TYPE = "application/soap+xml";

    public static final String URI_SOAP_ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String URI_SOAP_1_2_ROLE_NEXT = URI_NS_SOAP_1_2_ENVELOPE + "/role/next";
    public static final String URI_SOAP_1_2_ROLE_NONE = URI_NS_SOAP_1_2_ENVELOPE + "/role/none";
    public static final String URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER = URI_NS_SOAP_1_2_ENVELOPE + "/role/ultimateReceiver";

    public static final String SOAP_ENV_PREFIX = "env";

    public static final QName SOAP_VERSIONMISMATCH_FAULT = new QName(URI_NS_SOAP_1_2_ENVELOPE, "VersionMismatch", SOAP_ENV_PREFIX);
    public static final QName SOAP_MUSTUNDERSTAND_FAULT = new QName(URI_NS_SOAP_1_2_ENVELOPE, "MustUnderstand", SOAP_ENV_PREFIX);
    public static final QName SOAP_DATAENCODINGUNKNOWN_FAULT = new QName(URI_NS_SOAP_1_2_ENVELOPE, "DataEncodingUnknown", SOAP_ENV_PREFIX);
    public static final QName SOAP_SENDER_FAULT = new QName(URI_NS_SOAP_1_2_ENVELOPE, "Sender", SOAP_ENV_PREFIX);
    public static final QName SOAP_RECEIVER_FAULT = new QName(URI_NS_SOAP_1_2_ENVELOPE, "Receiver", SOAP_ENV_PREFIX);

}
