/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

package org.openecard.ws.android;

import de.bund.bsi.ecard.api._1.ConnectionHandle;
import de.bund.bsi.ecard.api._1.InitializeFramework;
import iso.std.iso_iec._24727.tech.schema.APIAccessEntryPointName;
import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.ActionNameType;
import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.ApplicationCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.AuthorizationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.BasicRequirementsType;
import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.BioSensorCapabilityType;
import iso.std.iso_iec._24727.tech.schema.CAMarkerType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardCall;
import iso.std.iso_iec._24727.tech.schema.CardInfo;
import iso.std.iso_iec._24727.tech.schema.CardTypeType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Conclusion;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationStateType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DataMaskType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.EAC1InputType;
import iso.std.iso_iec._24727.tech.schema.EAC1OutputType;
import iso.std.iso_iec._24727.tech.schema.EAC2InputType;
import iso.std.iso_iec._24727.tech.schema.EAC2OutputType;
import iso.std.iso_iec._24727.tech.schema.EACAdditionalInputType;
import iso.std.iso_iec._24727.tech.schema.EACMarkerType;
import iso.std.iso_iec._24727.tech.schema.EmptyResponseDataType;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.MatchingDataType;
import iso.std.iso_iec._24727.tech.schema.MutualAuthMarkerType;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.OutputInfoType;
import iso.std.iso_iec._24727.tech.schema.PACEMarkerType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.PathType;
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
import iso.std.iso_iec._24727.tech.schema.RIMarkerType;
import iso.std.iso_iec._24727.tech.schema.RSAAuthMarkerType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.ResponseAPDUType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import iso.std.iso_iec._24727.tech.schema.SimpleFUStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.TAMarkerType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.Configuration;
import org.openecard.addon.manifest.ConfigurationEntry;
import org.openecard.addon.manifest.EnumEntry;
import org.openecard.addon.manifest.LocalizedString;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.common.util.StringUtils;
import static org.openecard.ws.android.AndroidMarshaller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 *
 * @author Tobias Wich
 */
public class Unmarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(Unmarshaller.class);

    private final DocumentBuilder documentBuilder;

    Unmarshaller(DocumentBuilder documentBuilder) {
	this.documentBuilder = documentBuilder;
    }


    public Object unmarshal(Reader in) throws XmlPullParserException, IOException,
	    ParserConfigurationException, DatatypeConfigurationException {
	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	factory.setNamespaceAware(true);
	XmlPullParser parser = factory.newPullParser();
	parser.setInput(in);
	int eventType = parser.getEventType();
	while (eventType != XmlPullParser.END_DOCUMENT) {
	    if (eventType == XmlPullParser.START_TAG) {
		Object obj = parse(parser);
		return obj;
	    }
	    eventType = parser.next();
	}
	return null;
    }


    private ResponseAPDUType parseResponseAPDUType(XmlPullParser parser) throws XmlPullParserException,
	    IOException, ParserConfigurationException {
	ResponseAPDUType responseAPDUType = new ResponseAPDUType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Trailer")) {
		    responseAPDUType.setTrailer(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("Body")) {
		    responseAPDUType.setBody(this.parseDataMaskType(parser, "Body"));
		} else if (parser.getName().equals("Conclusion")) {
		    responseAPDUType.setConclusion(this.parseConclusion(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ResponseAPDU")));
	return responseAPDUType;
    }

    private Conclusion parseConclusion(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException {
	Conclusion conc = new Conclusion();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("RecognizedCardType")) {
		    conc.setRecognizedCardType(parser.nextText());
		} else if (parser.getName().equals("CardCall")) {
		    conc.getCardCall().add(this.parseCardCall(parser));
		}
	    }

	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Conclusion")));
	return conc;
    }

    private DataMaskType parseDataMaskType(XmlPullParser parser, String endTag) throws XmlPullParserException, IOException {
	DataMaskType dataMaskType = new DataMaskType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Tag")) {
		    dataMaskType.setTag(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("MatchingData")) {
		    dataMaskType.setMatchingData(this.parseMatchingDataType(parser));
		} else if (parser.getName().equals("DataObject")) {
		    dataMaskType.setDataObject(this.parseDataMaskType(parser, "DataObject"));
		}

	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(endTag)));
	return dataMaskType;
    }

    private MatchingDataType parseMatchingDataType(XmlPullParser parser) throws XmlPullParserException, IOException {
	MatchingDataType matchingDataType = new MatchingDataType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Offset")) {
		    matchingDataType.setOffset(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("Length")) {
		    matchingDataType.setLength(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("MatchingValue")) {
		    matchingDataType.setMatchingValue(StringUtils.toByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("MatchingData")));

	return matchingDataType;
    }

    private CardCall parseCardCall(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException {
	CardCall c = new CardCall();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("CommandAPDU")) {
		    c.setCommandAPDU(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("ResponseAPDU")) {
		    c.getResponseAPDU().add(this.parseResponseAPDUType(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardCall")));
	return c;
    }

    private Object parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParserConfigurationException, DatatypeConfigurationException {
	if (parser.getName().equals("DestroyChannelResponse")) {
	    DestroyChannelResponse destroyChannelResponse = new DestroyChannelResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			destroyChannelResponse.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			destroyChannelResponse.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			destroyChannelResponse.setResult(this.parseResult(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DestroyChannelResponse")));
	    return destroyChannelResponse;
	}
	else if (parser.getName().equals("DestroyChannel")) {
	    DestroyChannel destroyChannel = new DestroyChannel();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			destroyChannel.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DestroyChannel")));
	    return destroyChannel;
	}

	else if (parser.getName().equals("EstablishChannelResponse")) {
	    EstablishChannelResponse establishChannelResponse = new EstablishChannelResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			establishChannelResponse.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			establishChannelResponse.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			establishChannelResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("AuthenticationProtocolData")) {
			establishChannelResponse.setAuthenticationProtocolData(this.parseDIDAuthenticationDataType(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("EstablishChannelResponse")));
	    return establishChannelResponse;
	}

	else if (parser.getName().equals("DIDAuthenticate")) {
	    DIDAuthenticate didAuthenticate = new DIDAuthenticate();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("DIDName")) {
			didAuthenticate.setDIDName(parser.nextText());
		    } else if (parser.getName().equals("SlotHandle")) {
			ConnectionHandleType cht = new ConnectionHandleType();
			cht.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
			didAuthenticate.setConnectionHandle(cht);
		    } else if (parser.getName().equals("AuthenticationProtocolData")) {
			didAuthenticate.setAuthenticationProtocolData(this.parseDIDAuthenticationDataType(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthenticate")));
	    return didAuthenticate;
	} else if (parser.getName().equals("DIDAuthenticateResponse")) {
	    DIDAuthenticateResponse response = new DIDAuthenticateResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    } if (parser.getName().equals("AuthenticationProtocolData")) {
			response.setAuthenticationProtocolData(this.parseDIDAuthenticationDataType(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthenticateResponse")));
	    return response;
	}

	else if (parser.getName().equals("StartPAOSResponse")) {
	    StartPAOSResponse startPAOSResponse = new StartPAOSResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			startPAOSResponse.setResult(this.parseResult(parser));
		    }

		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("StartPAOSResponse")));
	    return startPAOSResponse;
	} else if (parser.getName().equals("InitializeFramework")) {
	    InitializeFramework initializeFramework = new InitializeFramework();
	    return initializeFramework;
	} else if (parser.getName().equals("Conclusion")) {
	    return parseConclusion(parser);
	} else if (parser.getName().equals("WaitResponse")) {
	    WaitResponse waitResponse = new WaitResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			waitResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("IFDEvent")) {
			waitResponse.getIFDEvent().add(parseIFDStatusType(parser, "IFDEvent"));
		    } else if (parser.getName().equals("SessionIdentifier")) {
			waitResponse.setSessionIdentifier(parser.nextText());
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("WaitResponse")));
	    return waitResponse;

	} else if (parser.getName().equals("GetStatusResponse")) {
	    GetStatusResponse getStatusResponse = new GetStatusResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			getStatusResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("IFDStatus")) {
			getStatusResponse.getIFDStatus().add(parseIFDStatusType(parser, "IFDStatus"));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("GetStatusResponse")));
	    return getStatusResponse;

	} else if (parser.getName().equals("ListIFDs")) {
	    ListIFDs listIFDs = new ListIFDs();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ContextHandle")) {
			listIFDs.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ListIFDs")));
	    return listIFDs;
	} else if (parser.getName().equals("GetIFDCapabilities")) {
	    GetIFDCapabilities getIFDCapabilities = new GetIFDCapabilities();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ContextHandle")) {
			getIFDCapabilities.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			getIFDCapabilities.setIFDName(parser.nextText());
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("GetIFDCapabilities")));
	    return getIFDCapabilities;
	} else if (parser.getName().equals("GetIFDCapabilitiesResponse")) {
	    GetIFDCapabilitiesResponse resp = new GetIFDCapabilitiesResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			resp.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			resp.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			resp.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("GetIFDCapabilitiesResponse")) {
			resp.setIFDCapabilities(parseIFDCapabilitiesType(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("GetIFDCapabilitiesResponse")));
	    return resp;

	} else if (parser.getName().equals("BeginTransaction")) {
	    BeginTransaction trans = new BeginTransaction();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			trans.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("BeginTransaction")));
	    return trans;
	} else if (parser.getName().equals("BeginTransactionResponse")) {
	    BeginTransactionResponse response = new BeginTransactionResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			response.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			response.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("BeginTransactionResponse")));
	    return response;

	} else if (parser.getName().equals("EndTransaction")) {
	    EndTransaction end = new EndTransaction();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			end.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("EndTransaction")));
	    return end;
	} else if (parser.getName().equals("EndTransactionResponse")) {
	    EndTransactionResponse response = new EndTransactionResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			response.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			response.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("EndTransactionResponse")));
	    return response;

	} else if (parser.getName().equals("CardApplicationPath")) {
	    CardApplicationPath path = new CardApplicationPath();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		String name = parser.getName();
		if (eventType == XmlPullParser.START_TAG) {
		    if (name.equals("CardAppPathRequest")) {
			path.setCardAppPathRequest(parseCardApplicationPath(parser));
		    } else if (name.equals("Profile")) {
			path.setProfile(parser.nextText());
		    } else if (name.equals("RequestID")) {
			path.setRequestID(parser.nextText());
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationPath")));
	    return path;
	} else if (parser.getName().equals("CardAppPathRequest") || parser.getName().equals("CardApplicationPathResult")) {
	    CardApplicationPathType type = new CardApplicationPathType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ChannelHandle")) {
			type.setChannelHandle(parseChannelHandle(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			type.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			type.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("SlotIndex")) {
			type.setSlotIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			type.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardAppPathRequest")));
	    return type;
	} else if (parser.getName().equals("CardApplicationPathResponse")) {
	    CardApplicationPathResponse resp = new CardApplicationPathResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardAppPathResultSet")) {
			resp.setCardAppPathResultSet((CardApplicationPathResponse.CardAppPathResultSet) parse(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationPathResponse")));
	    return resp;
	} else if (parser.getName().equals("CardAppPathResultSet")) {
	    CardApplicationPathResponse.CardAppPathResultSet result = new CardApplicationPathResponse.CardAppPathResultSet();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardApplicationPathResult")) {
			result.getCardApplicationPathResult().add(parseCardApplicationPath(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardAppPathResultSet")));
	    return result;
	} else if (parser.getName().equals("CardApplicationConnect")) {
	    CardApplicationConnect result = new CardApplicationConnect();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("CardApplicationPath")) {
			result.setCardApplicationPath(parseCardApplicationPath(parser));
		    } else if (parser.getName().equals("Output")) {
			result.setOutput(parseOutputInfoType(parser));
		    } else if (parser.getName().equals("ExclusiveUse")) {
			result.setExclusiveUse(Boolean.getBoolean(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationConnect")));
	    return result;
	} else if (parser.getName().equals("CardApplicationConnectResponse")) {
	    CardApplicationConnectResponse result = new CardApplicationConnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			result.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("ConnectionHandle")) {
			result.setConnectionHandle(parseConnectionHandle(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationConnectResponse")));
	    return result;
	} else if (parser.getName().equals("CardApplicationDisconnect")) {
	    CardApplicationDisconnect result = new CardApplicationDisconnect();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ConnectionHandle")) {
			result.setConnectionHandle(parseConnectionHandle(parser));
		    } else if (parser.getName().equals("Action")) {
			result.setAction(ActionType.fromValue(parser.nextText()));
		    } else if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationDisconnect")));
	    return result;
	} else if (parser.getName().equals("CardApplicationDisconnectResponse")) {
	    CardApplicationDisconnectResponse result = new CardApplicationDisconnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			result.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplicationDisconnectResponse")));
	    return result;
	} else if (parser.getName().equals("GetRecognitionTreeResponse")) {
	    GetRecognitionTreeResponse resp = new GetRecognitionTreeResponse();
	    RecognitionTree recTree = new RecognitionTree();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			resp.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("CardCall")) {
			recTree.getCardCall().add(this.parseCardCall(parser));
		    }
		} else if (eventType == XmlPullParser.END_TAG) {
		    if (parser.getName().equals("CardCall")) {

		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("GetRecognitionTreeResponse")));
	    resp.setRecognitionTree(recTree);
	    return resp;

	} else if (parser.getName().equals("EstablishContext")) {
	    EstablishContext establishContext = new EstablishContext();
	    return establishContext;

	} else if (parser.getName().equals("EstablishContextResponse")) {
	    EstablishContextResponse establishContextResponse = new EstablishContextResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			establishContextResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			establishContextResponse.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("EstablishContextResponse")));
	    return establishContextResponse;

	} else if (parser.getName().equals("ListIFDsResponse")) {
	    ListIFDsResponse listIFDsResponse = new ListIFDsResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			listIFDsResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("IFDName")) {
			listIFDsResponse.getIFDName().add(parser.nextText());
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ListIFDsResponse")));
	    return listIFDsResponse;

	} else if (parser.getName().equals("ConnectResponse")) {
	    ConnectResponse connectResponse = new ConnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			connectResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("SlotHandle")) {
			connectResponse.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ConnectResponse")));
	    return connectResponse;

	} else if (parser.getName().equals("Connect")) {
	    Connect c = new Connect();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("IFDName")) {
			c.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("ContextHandle")) {
			c.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("Slot")) {
			c.setSlot(new BigInteger(parser.nextText()));
		    } // TODO exclusive
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Connect")));
	    return c;
	} else if (parser.getName().equals("Disconnect")) {
	    Disconnect d = new Disconnect();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			d.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("Action")) {
			d.setAction(ActionType.fromValue(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Disconnect")));
	    return d;
	} else if (parser.getName().equals("DisconnectResponse")) {
	    DisconnectResponse response = new DisconnectResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Profile")) {
			response.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			response.setRequestID(parser.nextText());
		    } else if (parser.getName().equals("Result")) {
			response.setResult(this.parseResult(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("DisconnectResponse")));
	    return response;

	} else if (parser.getName().equals("Transmit")) {
	    Transmit t = new Transmit();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("InputAPDUInfo")) {
			t.getInputAPDUInfo().add(this.parseInputAPDUInfo(parser));
		    } else if (parser.getName().equals("SlotHandle")) {
			t.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Transmit")));
	    return t;

	} else if (parser.getName().equals("TransmitResponse")) {
	    TransmitResponse transmitResponse = new TransmitResponse();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("Result")) {
			transmitResponse.setResult(this.parseResult(parser));
		    } else if (parser.getName().equals("OutputAPDU")) {
			transmitResponse.getOutputAPDU().add(StringUtils.toByteArray(parser.nextText()));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("TransmitResponse")));
	    return transmitResponse;
	} else if (parser.getName().equals("CardInfo")) {
	    // TODO CardIdentification and CardCapabilities are ignored
	    CardInfo cardInfo = new CardInfo();
	    ApplicationCapabilitiesType applicationCapabilities = new ApplicationCapabilitiesType();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ObjectIdentifier")) {
			CardTypeType cardType = new CardTypeType();
			cardType.setObjectIdentifier(parser.nextText());
			cardInfo.setCardType(cardType);
		    } else if (parser.getName().equals("ImplicitlySelectedApplication")) {
			try {
			    // TODO iso:Path, see CardInfo_ecard-AT_0-9-0
			    String selectedApplication = parser.nextText();
			    applicationCapabilities.setImplicitlySelectedApplication(StringUtils.toByteArray(selectedApplication));
			} catch (XmlPullParserException ex) {
			}
		    } else if (parser.getName().equals("CardApplication")) {
			applicationCapabilities.getCardApplication().add(this.parseCardApplication(parser));
		    } else if (parser.getName().equals("CardTypeName")) {
			InternationalStringType internationalString = new InternationalStringType();
			String lang = parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang");
			internationalString.setLang(lang);
			internationalString.setValue(parser.nextText());
			cardInfo.getCardType().getCardTypeName().add(internationalString);
		    } else if (parser.getName().equals("SpecificationBodyOrIssuer")) {
			cardInfo.getCardType().setSpecificationBodyOrIssuer(parser.nextText());
		    } else if (parser.getName().equals("Status")) {
			cardInfo.getCardType().setStatus(parser.nextText());
		    } else if (parser.getName().equals("Date")) {
			// currently not working; see http://code.google.com/p/android/issues/detail?id=14379
			/*String text = parser.nextText();
			XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(text);
			cardInfo.getCardType().setDate(date);*/
		    } else if (parser.getName().equals("Version")) {
			cardInfo.getCardType().setVersion(this.parseVersion(parser));
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardInfo")));
	    cardInfo.setApplicationCapabilities(applicationCapabilities);
	    return cardInfo;
	} else if (parser.getName().equals("AddonSpecification")) {
	    AddonSpecification addonBundleDescription = new AddonSpecification();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ID")) {
			addonBundleDescription.setId(parser.nextText());
		    } else if (parser.getName().equals("Version")) {
			addonBundleDescription.setVersion(parser.nextText());
		    } else if (parser.getName().equals("License")) {
			addonBundleDescription.setLicense(parser.nextText());
		    } else if (parser.getName().equals("LocalizedName")) {
			LocalizedString string = new LocalizedString();
			string.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
			string.setValue(parser.nextText());
			addonBundleDescription.getLocalizedName().add(string);
		    } else if (parser.getName().equals("LocalizedDescription")) {
			LocalizedString string = new LocalizedString();
			string.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
			string.setValue(parser.nextText());
			addonBundleDescription.getLocalizedDescription().add(string);
		    } else if (parser.getName().equals("About")) {
			LocalizedString string = new LocalizedString();
			string.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
			string.setValue(parser.nextText());
			addonBundleDescription.getAbout().add(string);
		    } else if (parser.getName().equals("Logo")) {
			addonBundleDescription.setLogo(parser.nextText());
		    } else if (parser.getName().equals("ConfigDescription")) {
			addonBundleDescription.setConfigDescription(parseConfigDescription(parser));
		    } else if (parser.getName().equals("BindingActions")) {
			addonBundleDescription.getBindingActions().addAll(parseBindingActions(parser));
		    } else if (parser.getName().equals("ApplicationActions")) {
			addonBundleDescription.getApplicationActions().addAll(parseApplicationActions(parser));
		    } else if (parser.getName().equals("IFDActions")) {
			addonBundleDescription.getIfdActions().addAll(
				parseProtocolPluginSpecification(parser, "IFDActions"));
		    } else if (parser.getName().equals("SALActions")) {
			addonBundleDescription.getSalActions().addAll(
				parseProtocolPluginSpecification(parser, "SALActions"));
		    } else {
			throw new IllegalArgumentException(parser.getName()
				+ " in AddonSpecification is not supported.");
		    }
		}
	    } while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AddonSpecification")));
	    return addonBundleDescription;
	} else if (parser.getName().equals("EstablishChannel")) {
	    EstablishChannel result = new EstablishChannel();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("SlotHandle")) {
			result.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("AuthenticationProtocolData")) {
			result.setAuthenticationProtocolData(parseDIDAuthenticationDataType(parser));
		    } else if (parser.getName().equals("Profile")) {
			result.setProfile(parser.nextText());
		    } else if (parser.getName().equals("RequestID")) {
			result.setRequestID(parser.nextText());
		    } else {
			throw new IOException("Unmarshalling of " + parser.getName() + " in EstablishChannel not supported.");
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("EstablishChannel")));
	    return result;
	} else {
	    throw new IOException("Unmarshalling of " + parser.getName() + " is not yet supported.");
	}
    }

    private ConnectionHandleType parseConnectionHandle(XmlPullParser parser) throws XmlPullParserException, IOException,
		ParserConfigurationException, DatatypeConfigurationException {
	    ConnectionHandle result = new ConnectionHandle();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ChannelHandle")) {
			result.setChannelHandle(parseChannelHandle(parser));
		    } else if (parser.getName().equals("ContextHandle")) {
			result.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("IFDName")) {
			result.setIFDName(parser.nextText());
		    } else if (parser.getName().equals("SlotIndex")) {
			result.setSlotIndex(new BigInteger(parser.nextText()));
		    } else if (parser.getName().equals("CardApplication")) {
			result.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("SlotHandle")) {
			result.setSlotHandle(StringUtils.toByteArray(parser.nextText()));
		    } else if (parser.getName().equals("RecognitionInfo")) {
			result.setRecognitionInfo(parseRecognitionInfo(parser));
		    } else if (parser.getName().equals("SlotInfo")) {
			result.setSlotInfo(parseSlotInfo(parser));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("ConnectionHandle")));
	    return result;
    }

    private ConnectionHandleType.RecognitionInfo parseRecognitionInfo(XmlPullParser parser) throws XmlPullParserException, IOException,
		ParserConfigurationException, DatatypeConfigurationException {
	ConnectionHandleType.RecognitionInfo result = new ConnectionHandleType.RecognitionInfo();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("CardType")) {
		    result.setCardType(parser.nextText());
		} else if (parser.getName().equals("CardIdentifier")) {
		    result.setCardIdentifier(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("CaptureTime")) {
		    // TODO
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("RecognitionInfo")));
	return result;
    }

    private ConnectionHandleType.SlotInfo parseSlotInfo(XmlPullParser parser) throws XmlPullParserException, IOException,
		ParserConfigurationException, DatatypeConfigurationException {
	    ConnectionHandleType.SlotInfo result = new ConnectionHandleType.SlotInfo();
	    int eventType;
	    do {
		parser.next();
		eventType = parser.getEventType();
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("ProtectedAuthPath")) {
			result.setProtectedAuthPath(Boolean.getBoolean(parser.nextText()));
		    }
		}
	    } while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("SlotInfo")));
	    return result;
    }

    private ChannelHandleType parseChannelHandle(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException, DatatypeConfigurationException {
	ChannelHandleType ch = new ChannelHandleType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ProtocolTerminationPoint")) {
		    ch.setProtocolTerminationPoint(parser.nextText());
		} else if (parser.getName().equals("SessionIdentifier")) {
		    ch.setSessionIdentifier(parser.nextText());
		} else if (parser.getName().equals("Binding")) {
		    ch.setBinding(parser.nextText());
		} else if (parser.getName().equals("PathSecurity")) {
		    ch.setPathSecurity(parsePathSecurityType(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ChannelHandle")));
	return ch;
    }

    private PathSecurityType parsePathSecurityType(XmlPullParser parser) throws XmlPullParserException, IOException,
	    ParserConfigurationException, DatatypeConfigurationException {
	PathSecurityType p = new PathSecurityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Protocol")) {
		    p.setProtocol(parser.nextText());
		} else if (parser.getName().equals("Parameters")) {
		    p.setParameters(parse(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("PathSecurity")));
	return p;
    }

    private Collection<? extends ProtocolPluginSpecification> parseProtocolPluginSpecification(XmlPullParser parser,
	    String tagName) throws XmlPullParserException, IOException {
	ArrayList<ProtocolPluginSpecification> list = new ArrayList<>();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ProtocolPluginSpecification")) {
		    list.add(parseProtocolPluginSpecification(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(tagName)));
	return list;
    }

    private CardApplicationPathType parseCardApplicationPath(XmlPullParser parser) throws XmlPullParserException,
		IOException, ParserConfigurationException, DatatypeConfigurationException {
	CardApplicationPathType type = new CardApplicationPathType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    String name = parser.getName();
	    if (eventType == XmlPullParser.START_TAG) {
		if (name.equals("ChannelHandle")) {
		    type.setChannelHandle(parseChannelHandle(parser));
		} else if (name.equals("ContextHandle")) {
		    type.setContextHandle(StringUtils.toByteArray(parser.nextText()));
		} else if (name.equals("IFDName")) {
		    type.setIFDName(parser.nextText());
		} else if (name.equals("SlotIndex")) {
		    type.setSlotIndex(new BigInteger(parser.nextText()));
		} else if (name.equals("CardApplication")) {
		    type.setCardApplication(StringUtils.toByteArray(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && isCardAppPathEndTag(parser.getName())));
	return type;
    }

    private boolean isCardAppPathEndTag(String name) {
	return "CardApplicationPathResult".equals(name) ||
		"CardApplicationPath".equals(name) ||
		"CardAppPathRequest".equals(name);
    }

    private SlotCapabilityType parseSlotCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	SlotCapabilityType slotCapType = new SlotCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    slotCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Protocol")) {
		    String protocol = parser.nextText();
		    slotCapType.getProtocol().add(protocol);
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("SlotCapabilityType")));
	return slotCapType;
    }

    private DisplayCapabilityType parseDisplayCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	DisplayCapabilityType displayCapType = new DisplayCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    displayCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Lines")) {
		    displayCapType.setLines(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Columns")) {
		    displayCapType.setColumns(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("VirtualLines")) {
		    displayCapType.setVirtualLines(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("VirtualColumns")) {
		    displayCapType.setVirtualColumns(new BigInteger(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("DisplayCapability")));
	return displayCapType;
    }

    private KeyPadCapabilityType parseKeyPadCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	KeyPadCapabilityType keyPadCapType = new KeyPadCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    keyPadCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Keys")) {
		    keyPadCapType.setKeys(new BigInteger(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("KeyPadCapability")));
	return keyPadCapType;
    }

    private BioSensorCapabilityType parseBioSensorCapability(XmlPullParser parser) throws XmlPullParserException, IOException {
	BioSensorCapabilityType bioCapType = new BioSensorCapabilityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    bioCapType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("BiometricType")) {
		    bioCapType.setBiometricType(new BigInteger(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("BioSensorCapability")));
	return bioCapType;
    }

    private ProtocolPluginSpecification parseProtocolPluginSpecification(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	ProtocolPluginSpecification protocolPluginDescription = new ProtocolPluginSpecification();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ClassName")) {
		    protocolPluginDescription.setClassName(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    protocolPluginDescription.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    protocolPluginDescription.getLocalizedDescription().add(localizedString);
		} else if (parser.getName().equals("URI")) {
		    protocolPluginDescription.setUri(parser.nextText());
		} else if (parser.getName().equals("ConfigDescription")) {
		    protocolPluginDescription.setConfigDescription(parseConfigDescription(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ProtocolPluginSpecification")));
	return protocolPluginDescription;
    }

    private Collection<? extends AppExtensionSpecification> parseApplicationActions(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	ArrayList<AppExtensionSpecification> list = new ArrayList<>();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("AppExtensionSpecification")) {
		    list.add(parseAppExtensionSpecification(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ApplicationActions")));
	return list;
    }

    private AppExtensionSpecification parseAppExtensionSpecification(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	AppExtensionSpecification appExtensionActionDescription = new AppExtensionSpecification();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ClassName")) {
		    appExtensionActionDescription.setClassName(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appExtensionActionDescription.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appExtensionActionDescription.getLocalizedDescription().add(localizedString);
		} else if (parser.getName().equals("ID")) {
		    appExtensionActionDescription.setId(parser.nextText());
		} else if (parser.getName().equals("ConfigDescription")) {
		    appExtensionActionDescription.setConfigDescription(parseConfigDescription(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AppExtensionSpecification")));
	return appExtensionActionDescription;
    }

    private Collection<? extends AppPluginSpecification> parseBindingActions(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	ArrayList<AppPluginSpecification> list = new ArrayList<>();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("AppPluginSpecification")) {
		    list.add(parseAppPluginSpecification(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("BindingActions")));
	return list;
    }

    private AppPluginSpecification parseAppPluginSpecification(XmlPullParser parser)
	    throws XmlPullParserException, IOException {
	AppPluginSpecification appPluginActionDescription = new AppPluginSpecification();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ClassName")) {
		    appPluginActionDescription.setClassName(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appPluginActionDescription.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    appPluginActionDescription.getLocalizedDescription().add(localizedString);
		} else if (parser.getName().equals("ResourceName")) {
		    appPluginActionDescription.setResourceName(parser.nextText());
		} else if (parser.getName().equals("ConfigDescription")) {
		    appPluginActionDescription.setConfigDescription(parseConfigDescription(parser));
		} else {
		    throw new IllegalArgumentException("Unexpected Tag found: " + parser.getName());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AppPluginSpecification")));
	return appPluginActionDescription;
    }

    private Configuration parseConfigDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
	Configuration c = new Configuration();
	List<ConfigurationEntry> entries = c.getEntries();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("EnumEntry")) {
		    entries.add(parseEnumEntry(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("ConfigDescription")));
	return c;
    }

    private EnumEntry parseEnumEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	EnumEntry entry = new EnumEntry();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Key")) {
		    entry.setKey(parser.nextText());
		} else if (parser.getName().equals("LocalizedName")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    entry.getLocalizedName().add(localizedString);
		} else if (parser.getName().equals("LocalizedDescription")) {
		    LocalizedString localizedString = new LocalizedString();
		    localizedString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    localizedString.setValue(parser.nextText());
		    entry.getLocalizedDescription().add(localizedString);
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("EnumEntry")));
	return entry;
    }

    private CardTypeType.Version parseVersion(XmlPullParser parser) throws XmlPullParserException, IOException {
	CardTypeType.Version version = new CardTypeType.Version();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Major")) {
		    version.setMajor(parser.nextText());
		} else if (parser.getName().equals("Minor")) {
		    version.setMinor(parser.nextText());
		} else if (parser.getName().equals("SubMinor")) {
		    version.setSubMinor(parser.nextText());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Version")));
	return version;
    }

    private CardApplicationType parseCardApplication(XmlPullParser parser) throws XmlPullParserException, IOException {
	CardApplicationType cardApplication = new CardApplicationType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ApplicationIdentifier")) {
		    cardApplication.setApplicationIdentifier(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("ApplicationName")) {
		    cardApplication.setApplicationName(parser.nextText());
		} else if (parser.getName().equals("RequirementLevel")) {
		    cardApplication.setRequirementLevel(BasicRequirementsType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("CardApplicationACL")) {
		    cardApplication.setCardApplicationACL(this.parseACL(parser, "CardApplicationACL"));
		} else if (parser.getName().equals("DIDInfo")) {
		    cardApplication.getDIDInfo().add(this.parseDIDInfo(parser));
		} else if (parser.getName().equals("DataSetInfo")) {
		    cardApplication.getDataSetInfo().add(this.parseDataSetInfo(parser));
		} else if (parser.getName().equals("InterfaceProtocol")) {
		    cardApplication.getInterfaceProtocol().add(parser.nextText());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("CardApplication")));
	return cardApplication;
    }

    private DataSetInfoType parseDataSetInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	DataSetInfoType dataSetInfo = new DataSetInfoType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("RequirementLevel")) {
		    dataSetInfo.setRequirementLevel(BasicRequirementsType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DataSetACL")) {
		    dataSetInfo.setDataSetACL(this.parseACL(parser, "DataSetACL"));
		} else if (parser.getName().equals("DataSetName")) {
		    dataSetInfo.setDataSetName(parser.nextText());
		} else if (parser.getName().equals("DataSetPath")) {
		    dataSetInfo.setDataSetPath(this.parseDataSetPath(parser));
		} else if (parser.getName().equals("LocalDataSetName")) {
		    InternationalStringType internationalString = new InternationalStringType();
		    internationalString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    internationalString.setValue(parser.nextText());
		    dataSetInfo.getLocalDataSetName().add(internationalString);
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DataSetInfo")));
	return dataSetInfo;
    }

    private PathType parseDataSetPath(XmlPullParser parser) throws XmlPullParserException, IOException {
	PathType path = new PathType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("efIdOrPath")) {
		    path.setEfIdOrPath(StringUtils.toByteArray(parser.nextText()));
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DataSetPath")));
	return path;
    }

    private DIDInfoType parseDIDInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDInfoType didInfo = new DIDInfoType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("RequirementLevel")) {
		    didInfo.setRequirementLevel(BasicRequirementsType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DIDACL")) {
		    didInfo.setDIDACL(this.parseACL(parser, "DIDACL"));
		} else if (parser.getName().equals("DifferentialIdentity")) {
		    didInfo.setDifferentialIdentity(this.parseDifferentialIdentity(parser));
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDInfo")));
	return didInfo;
    }

    private DifferentialIdentityType parseDifferentialIdentity(XmlPullParser parser) throws XmlPullParserException, IOException {
	DifferentialIdentityType differentialIdentity = new DifferentialIdentityType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("DIDName")) {
		    differentialIdentity.setDIDName(parser.nextText());
		} else if (parser.getName().equals("LocalDIDName")) {
		    InternationalStringType internationalString = new InternationalStringType();
		    internationalString.setLang(parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang"));
		    internationalString.setValue(parser.nextText());
		    differentialIdentity.getLocalDIDName().add(internationalString);
		} else if (parser.getName().equals("DIDProtocol")) {
		    differentialIdentity.setDIDProtocol(parser.nextText());
		} else if (parser.getName().equals("DIDMarker")) {
		    differentialIdentity.setDIDMarker(this.parseDIDMarkerType(parser));
		} else if (parser.getName().equals("DIDScope")) {
		    differentialIdentity.setDIDScope(DIDScopeType.fromValue(parser.nextText()));
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DifferentialIdentity")));
	return differentialIdentity;
    }

    private DIDMarkerType parseDIDMarkerType(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDMarkerType didMarker = new DIDMarkerType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("PACEMarker")) {
		    didMarker.setPACEMarker((PACEMarkerType) this.parseMarker(parser, PACEMarkerType.class));
		} else if (parser.getName().equals("TAMarker")) {
		    didMarker.setTAMarker((TAMarkerType) this.parseMarker(parser, TAMarkerType.class));
		} else if (parser.getName().equals("CAMarker")) {
		    didMarker.setCAMarker((CAMarkerType) this.parseMarker(parser, CAMarkerType.class));
		} else if (parser.getName().equals("RIMarker")) {
		    didMarker.setRIMarker((RIMarkerType) this.parseMarker(parser, RIMarkerType.class));
		} else if (parser.getName().equals("CryptoMarker")) {
		    didMarker.setCryptoMarker((CryptoMarkerType) this.parseMarker(parser, CryptoMarkerType.class));
		} else if (parser.getName().equals("PinCompareMarker")) {
		    didMarker.setPinCompareMarker((PinCompareMarkerType) this.parseMarker(parser, PinCompareMarkerType.class));
		} else if (parser.getName().equals("RSAAuthMarker")) {
		    didMarker.setRSAAuthMarker((RSAAuthMarkerType) this.parseMarker(parser, RSAAuthMarkerType.class));
		} else if (parser.getName().equals("MutualAuthMarker")) {
		    didMarker.setMutualAuthMarker((MutualAuthMarkerType) this.parseMarker(parser, MutualAuthMarkerType.class));
		} else if (parser.getName().equals("EACMarker")) {
		    didMarker.setEACMarker((EACMarkerType) this.parseMarker(parser, EACMarkerType.class));
		} else {
		    LOG.error(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDMarker")));
	return didMarker;
    }

    private Collection<? extends Element> parseAnyTypes(XmlPullParser parser, String name, String ns, Document d, Boolean firstCall, String[] attribNames, String[] attribValues)
	    throws XmlPullParserException, IOException {
	int eventType;
	List<Element> elements = new ArrayList<>();
	boolean terminalNode = false;
	do {
	    String[] attributeNames = new String[0];
	    String[] attributeValues = new String[0];
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		int attributeCount = parser.getAttributeCount();
		if (attributeCount > 0) {
		    attributeNames = new String[attributeCount];
		    attributeValues = new String[attributeCount];
		    for (int i = 0; i < attributeCount; i++) {
			attributeNames[i] = parser.getAttributeName(i);
			attributeValues[i] = parser.getAttributeValue(i);
		    }
		}
		elements.addAll(parseAnyTypes(parser, parser.getName(), parser.getNamespace(), d, true, attributeNames, attributeValues));
	    } else if (eventType == XmlPullParser.TEXT) {
		if (parser.getText().trim().length() > 0) {
		    Element em = d.createElementNS(ns, name);
		    em.setTextContent(parser.getText());
		    elements.add(em);
		    terminalNode = true;
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));
	if (!terminalNode && firstCall) {
	    Element test = d.createElementNS(ns, name);
	    for (Element e : elements) {
		test.appendChild(e);
	    }
	    List<Element> elements2 = new ArrayList<>();

	    for (int i = 0; i < attribNames.length; i++) {
		test.setAttribute(attribNames[i], attribValues[i]);
	    }
	    elements2.add(test);
	    return elements2;
	}
	return elements;
    }

    private DIDAbstractMarkerType parseMarker(XmlPullParser parser, Class<? extends DIDAbstractMarkerType> cls) throws XmlPullParserException, IOException {
	try {
	    DIDAbstractMarkerType paceMarker = cls.newInstance();
	    paceMarker.setProtocol(parser.getAttributeValue(null, "Protocol"));
	    Document d = documentBuilder.newDocument();
	    String name = cls.getSimpleName().replace("Type", "");
	    paceMarker.getAny().addAll(parseAnyTypes(parser, name, parser.getNamespace(), d, false, new String[0], new String[0]));
	    return paceMarker;
	} catch (InstantiationException | IllegalAccessException e) {
	    throw new IOException("Error while instantiating the abstract marker type.");
	}
    }

    private AccessControlListType parseACL(XmlPullParser parser, String endTag) throws XmlPullParserException, IOException {
	AccessControlListType accessControlList = new AccessControlListType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("AccessRule")) {
		    accessControlList.getAccessRule().add(this.parseAccessRule(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(endTag)));
	return accessControlList;
    }

    private AccessRuleType parseAccessRule(XmlPullParser parser) throws XmlPullParserException, IOException {
	AccessRuleType accessRule = new AccessRuleType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("CardApplicationServiceName")) {
		    accessRule.setCardApplicationServiceName(parser.nextText());
		} else if (parser.getName().equals("Action")) {
		    accessRule.setAction(this.parseAction(parser));
		} else if (parser.getName().equals("SecurityCondition")) {
		    accessRule.setSecurityCondition(this.parseSecurityCondition(parser));
		} else {
		    throw new IOException("not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AccessRule")));
	return accessRule;
    }

    private SecurityConditionType parseSecurityCondition(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType securityCondition = new SecurityConditionType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("always")) {
		    securityCondition.setAlways(true);
		} else if (parser.getName().equals("never")) {
		    securityCondition.setNever(false);
		} else if (parser.getName().equals("DIDAuthentication")) {
		    securityCondition.setDIDAuthentication(this.parseDIDAuthenticationState(parser));
		} else if (parser.getName().equals("not")) {
		    securityCondition.setNot(this.parseSecurityCondition(parser));
		} else if (parser.getName().equals("and")) {
		    securityCondition.setAnd(this.parseSecurityConditionTypeAnd(parser));
		} else if (parser.getName().equals("or")) {
		    securityCondition.setOr(this.parseSecurityConditionTypeOr(parser));
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("SecurityCondition")));
	return securityCondition;
    }

    private DIDAuthenticationStateType parseDIDAuthenticationState(XmlPullParser parser) throws XmlPullParserException, IOException {
	DIDAuthenticationStateType didAuthenticationState = new DIDAuthenticationStateType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("DIDName")) {
		    didAuthenticationState.setDIDName(parser.nextText());
		} else if (parser.getName().equals("DIDScope")) {
		    didAuthenticationState.setDIDScope(DIDScopeType.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DIDState")) {
		    didAuthenticationState.setDIDState(Boolean.parseBoolean(parser.nextText()));
		} else if (parser.getName().equals("DIDStateQualifier")) {
		    didAuthenticationState.setDIDStateQualifier(StringUtils.toByteArray(parser.nextText()));
		} else {
		    throw new IOException(parser.getName() + " not yet implemented");
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("DIDAuthentication")));
	return didAuthenticationState;
    }

    private SecurityConditionType.Or parseSecurityConditionTypeOr(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType.Or securityConditionOr = new SecurityConditionType.Or();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("SecurityCondition")) {
		    securityConditionOr.getSecurityCondition().add(this.parseSecurityCondition(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("or")));
	return securityConditionOr;
    }

    private SecurityConditionType.And parseSecurityConditionTypeAnd(XmlPullParser parser) throws XmlPullParserException, IOException {
	SecurityConditionType.And securityConditionAnd = new SecurityConditionType.And();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("SecurityCondition")) {
		    securityConditionAnd.getSecurityCondition().add(this.parseSecurityCondition(parser));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("and")));
	return securityConditionAnd;
    }

    private ActionNameType parseAction(XmlPullParser parser) throws XmlPullParserException, IOException {
	ActionNameType action = new ActionNameType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("APIAccessEntryPoint")) {
		    action.setAPIAccessEntryPoint(APIAccessEntryPointName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("ConnectionServiceAction")) {
		    action.setConnectionServiceAction(ConnectionServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("CardApplicationServiceAction")) {
		    action.setCardApplicationServiceAction(CardApplicationServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("NamedDataServiceAction")) {
		    action.setNamedDataServiceAction(NamedDataServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("CryptographicServiceAction")) {
		    action.setCryptographicServiceAction(CryptographicServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("DifferentialIdentityServiceAction")) {
		    action.setDifferentialIdentityServiceAction(DifferentialIdentityServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("AuthorizationServiceAction")) {
		    action.setAuthorizationServiceAction(AuthorizationServiceActionName.fromValue(parser.nextText()));
		} else if (parser.getName().equals("LoadedAction")) {
		    action.setLoadedAction(parser.nextText());
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Action")));
	return action;
    }

    private InputAPDUInfoType parseInputAPDUInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
	InputAPDUInfoType inputAPDUInfo = new InputAPDUInfoType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("InputAPDU")) {
		    inputAPDUInfo.setInputAPDU(StringUtils.toByteArray(parser.nextText()));
		} else if (parser.getName().equals("AcceptableStatusCode")) {
		    inputAPDUInfo.getAcceptableStatusCode().add(StringUtils.toByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("InputAPDUInfo")));
	return inputAPDUInfo;
    }

    private DIDAuthenticationDataType parseDIDAuthenticationDataType(XmlPullParser parser) throws XmlPullParserException, IOException {
	Document document = documentBuilder.newDocument();
	DIDAuthenticationDataType didAuthenticationDataType;

	String attrValue = parser.getAttributeValue(XSI_NS, "type");
	if (attrValue != null && attrValue.contains("EAC1InputType")) {
	    didAuthenticationDataType = new EAC1InputType();
	} else if (attrValue != null && attrValue.contains("EAC1OutputType")) {
	    didAuthenticationDataType = new EAC1OutputType();
	} else if (attrValue != null && attrValue.contains("EAC2InputType")) {
	    didAuthenticationDataType = new EAC2InputType();
	} else if (attrValue != null && attrValue.contains("EAC2OutputType")) {
	    didAuthenticationDataType = new EAC2OutputType();
	} else if (attrValue != null && attrValue.contains("EACAdditionalInputType")) {
	    didAuthenticationDataType = new EACAdditionalInputType();
	} else if (attrValue != null && attrValue.contains("EmptyResponseDataType")) {
	    didAuthenticationDataType = new EmptyResponseDataType();
	} else {
	    didAuthenticationDataType = new DIDAuthenticationDataType();
	}

	if (parser.getAttributeValue(null, "Protocol") != null && ! parser.getAttributeValue(null, "Protocol").isEmpty()) {
	    didAuthenticationDataType.setProtocol(parser.getAttributeValue(null, "Protocol"));
	}

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		Element em = AndroidMarshaller.createElementIso(document, parser.getName());
		em.setTextContent(parser.nextText());
		didAuthenticationDataType.getAny().add(em);
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("AuthenticationProtocolData")));

	return didAuthenticationDataType;
    }

    private IFDCapabilitiesType parseIFDCapabilitiesType(XmlPullParser parser) throws XmlPullParserException, IOException {
	IFDCapabilitiesType cap = new IFDCapabilitiesType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("OpticalSignalUnit")) {
		    cap.setOpticalSignalUnit(Boolean.getBoolean(parser.nextText()));
		} else if (parser.getName().equals("AcousticSignalUnit")) {
		    cap.setAcousticSignalUnit(Boolean.getBoolean(parser.nextText()));
		} else if (parser.getName().equals("SlotCapability")) {
		    cap.getSlotCapability().add(parseSlotCapability(parser));
		} else if (parser.getName().equals("DisplayCapability")) {
		    cap.getDisplayCapability().add(parseDisplayCapability(parser));
		} else if (parser.getName().equals("KeyPadCapability")) {
		    cap.getKeyPadCapability().add(parseKeyPadCapability(parser));
		} else if (parser.getName().equals("BioSensorCapability")) {
		    cap.getBioSensorCapability().add(parseBioSensorCapability(parser));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("IFDCapabilitiesType")));
	return cap;
    }

    private Result parseResult(XmlPullParser parser) throws XmlPullParserException, IOException {
	Result r = new Result();
	int eventType;
	if (parser == null) {
	    return r;
	}
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("ResultMajor")) {
		    r.setResultMajor(parser.nextText());
		} else if (parser.getName().equals("ResultMinor")) {
		    r.setResultMinor(parser.nextText());
		} else if (parser.getName().equals("ResultMessage")) {
		    InternationalStringType internationalStringType = new InternationalStringType();
		    String lang = parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang");
		    internationalStringType.setLang(lang);
		    // TODO problem with parsing result message (international string)
		    try {
			String value = parser.nextText();
			internationalStringType.setValue(value);
			r.setResultMessage(internationalStringType);
		    } catch (Exception e) {}
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("Result")));
	return r;
    }

    private IFDStatusType parseIFDStatusType(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
	IFDStatusType ifdStatusType = new IFDStatusType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("IFDName")) {
		    ifdStatusType.setIFDName(parser.nextText());
		} else if (parser.getName().equals("Connected")) {
		    ifdStatusType.setConnected(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("ActiveAntenna")) {
		    ifdStatusType.setActiveAntenna(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("SlotStatus")) {
		    ifdStatusType.getSlotStatus().add(parseSlotStatusType(parser));
		} else if (parser.getName().equals("DisplayStatus")) {
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "DisplayStatus"));
		} else if (parser.getName().equals("KeyPadStatus")) {
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "KeyPadStatus"));
		} else if (parser.getName().equals("BioSensorStatus")) {
		    ifdStatusType.getBioSensorStatus().add(parseSimpleFUStatusType(parser, "BioSensorStatus"));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));

	return ifdStatusType;
    }

    private SlotStatusType parseSlotStatusType(XmlPullParser parser) throws XmlPullParserException, IOException {
	SlotStatusType slotStatusType = new SlotStatusType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    slotStatusType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("CardAvailable")) {
		    slotStatusType.setCardAvailable(Boolean.valueOf(parser.nextText()));
		} else if (parser.getName().equals("ATRorATS")) {
		    slotStatusType.setATRorATS(StringUtils.toByteArray(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("SlotStatus")));

	return slotStatusType;
    }

    private OutputInfoType parseOutputInfoType(XmlPullParser parser) throws XmlPullParserException, IOException {
	OutputInfoType result = new OutputInfoType();
	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Timeout")) {
		    result.setTimeout(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("DisplayIndex")) {
		    result.setDisplayIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Message")) {
		    result.setMessage(parser.nextText());
		} else if (parser.getName().equals("AcousticalSignal")) {
		    result.setAcousticalSignal(Boolean.getBoolean(parser.nextText()));
		} else if (parser.getName().equals("OpticalSignal")) {
		    result.setOpticalSignal(Boolean.getBoolean(parser.nextText()));
		}
	    }
	} while (! (eventType == XmlPullParser.END_TAG && parser.getName().equals("Output")));
	return result;
    }

    private SimpleFUStatusType parseSimpleFUStatusType(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
	SimpleFUStatusType simpleFUStatusType = new SimpleFUStatusType();

	int eventType;
	do {
	    parser.next();
	    eventType = parser.getEventType();
	    if (eventType == XmlPullParser.START_TAG) {
		if (parser.getName().equals("Index")) {
		    simpleFUStatusType.setIndex(new BigInteger(parser.nextText()));
		} else if (parser.getName().equals("Available")) {
		    simpleFUStatusType.setAvailable(Boolean.valueOf(parser.nextText()));
		}
	    }
	} while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(name)));

	return simpleFUStatusType;
    }

}
