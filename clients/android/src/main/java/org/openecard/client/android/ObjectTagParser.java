/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
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

package org.openecard.client.android;

import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.TCAPIOpen;
import java.io.IOException;
import java.io.StringReader;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * The purpose of this class is to start the eID-procedure in case the eid-object-tag occured.
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class ObjectTagParser {
	
	private ClientEnv env;

	public ObjectTagParser(ClientEnv env) {
		this.env = env;
	}

	/**
	 * Parses the inner HTML-Code of the object tag to retrieve the parameters and starts the eID-procedure
	 * @param html inner HTML-Code of the object tag
	 */
	public void showHTML(String html) {
		
		try {
			String sessionIdentifier = "";
			String serverAddress = "";
			String refreshAddress = "";
			String pathSecurityProtocol = "";
			String binding = "";
			String pathSecurityParameters = "";
			byte[] psk = null;
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(html));
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if(parser.getAttributeValue(null, "name").equals("SessionIdentifier")){
						sessionIdentifier = parser.getAttributeValue(null, "value");
					} else if(parser.getAttributeValue(null, "name").equals("ServerAddress")){
						serverAddress = parser.getAttributeValue(null, "value");
					} else if(parser.getAttributeValue(null, "name").equals("RefreshAddress")){
						refreshAddress = parser.getAttributeValue(null, "value");
					} else if(parser.getAttributeValue(null, "name").equals("PathSecurity-Protocol")){
						pathSecurityProtocol  =parser.getAttributeValue(null, "value");
					} else if(parser.getAttributeValue(null, "name").equals("Binding")){
						binding = parser.getAttributeValue(null, "value");
					} else if(parser.getAttributeValue(null, "name").equals("PathSecurity-Parameters")){
						pathSecurityParameters = parser.getAttributeValue(null, "value");
						 psk = StringUtils.toByteArray(pathSecurityParameters.substring(5, pathSecurityParameters.length() - 6));
					}
				}
				eventType = parser.next();
			}
			  
			System.out.println("SessionIdentifier: " + sessionIdentifier);
			System.out.println("ServerAddress: " + serverAddress);
			System.out.println("RefreshAddress: " + refreshAddress);
			System.out.println("PathSecurity-Protocol: " + pathSecurityProtocol);
			System.out.println("Binding: "  + binding);
			System.out.println("PathSecurity-Parameters: " + pathSecurityParameters);
			System.out.println("PSK: " + ByteUtils.toHexString(psk));
			  
			ChannelHandleType cht = new ChannelHandleType();
			cht.setSessionIdentifier(sessionIdentifier);
			PathSecurityType pst = new PathSecurityType();
			pst.setProtocol(pathSecurityProtocol);
			pst.setParameters(pathSecurityParameters);
			cht.setPathSecurity(pst);
			cht.setBinding(binding);
			cht.setProtocolTerminationPoint(serverAddress);
			TCAPIOpen tcapiOpen = new TCAPIOpen();
			tcapiOpen.setChannelHandle(cht);
			
			//TODO do something with the tc_api_open

		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}