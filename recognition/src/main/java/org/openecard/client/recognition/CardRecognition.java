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

package org.openecard.client.recognition;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.recognition.statictree.LocalFileTree;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerFactory;
import org.openecard.ws.GetRecognitionTree;
import org.openecard.ws.IFD;
import org.openecard.ws.protocols.tls.v1.TLSMarkerType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardRecognition {

    private static final Logger _logger = LogManager.getLogger(CardRecognition.class.getName());
    private static final NamespaceContext nsCtx = new NamespaceContext() {
        Map<String, String> nsMap = Collections.unmodifiableMap(new TreeMap<String, String>() {
            {
                put("iso", "urn:iso:std:iso-iec:24727:tech:schema");
                put("tls", "http://ws.openecard.org/protocols/tls/v1.0");
            }
        });
        @Override
        public String getNamespaceURI(String prefix) {
            return nsMap.get(prefix);
        }
        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String,String> e : nsMap.entrySet()) {
                if (e.getValue().equals(namespaceURI)) {
                    return e.getKey();
                }
            }
            return null;
        }
        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return nsMap.keySet().iterator();
        }
    };

    private final RecognitionTree tree;
    // TODO: replace with custom TLSMarkerType
    private final TreeMap<String,TLSMarkerType> tlsMarkers = new TreeMap<String, TLSMarkerType>();

    private final IFD ifd;
    private final byte[] ctx;


    /**
     * Create recognizer with tree from local (file based) repository.
     * @param ifd
     * @param ctx
     * @throws Exception
     */
    public CardRecognition(IFD ifd, byte[] ctx) throws Exception {
        this(ifd, ctx, null);
    }

    public CardRecognition(IFD ifd, byte[] ctx, GetRecognitionTree client) throws Exception {
	this.ifd = ifd;
	this.ctx = ctx;

        // load alternative tree service if needed
        WSMarshaller marshaller = WSMarshallerFactory.createInstance();
        if (client == null) {
            client = new LocalFileTree(marshaller);
        }

	// request tree from service
	iso.std.iso_iec._24727.tech.schema.GetRecognitionTree req = new iso.std.iso_iec._24727.tech.schema.GetRecognitionTree();
	req.setAction(RecognitionProperties.getAction());
	GetRecognitionTreeResponse resp = client.getRecognitionTree(req);
	checkResult(resp.getResult());
	this.tree = resp.getRecognitionTree();

        // find markers in tree
        Document d = marshaller.marshal(this.tree);
        XPath xpath = XPathFactory.newInstance().newXPath();
        // set namespace context needed in xpath expression
        xpath.setNamespaceContext(nsCtx);

        // compile xpath and get results
        XPathExpression expr = xpath.compile("//iso:Conclusion[iso:TLSMarker]");
        NodeList nodes = (NodeList) expr.evaluate(d, XPathConstants.NODESET);
        for (int i=0; i < nodes.getLength(); i++) {
            Conclusion c = (Conclusion) marshaller.unmarshal(nodes.item(i));
            String type = c.getRecognizedCardType();
            TLSMarkerType marker = c.getTLSMarker();
            tlsMarkers.put(type, marker);
        }
    }


    public TLSMarkerType getTLSMarker(String cardType) {
        return tlsMarkers.get(cardType);
    }


    public RecognitionInfo recognizeCard(String ifdName, BigInteger slot) throws RecognitionException {
	// connect card
	byte[] slotHandle = connect(ifdName, slot);
	// recognise card
	String type = treeCalls(slotHandle, tree.getCardCall());
	// disconnect and return
	disconnect(slotHandle);
	// build result or throw exception if it is null
	if (type == null) {
	    return null;
	}
	RecognitionInfo info = new RecognitionInfo();
	info.setCardType(type);
	return info;
    }


    private void checkResult(Result r) throws RecognitionException {
	if (r.getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    throw new RecognitionException(r);
	}
    }
    /**
     * Special transmit check determining only whether a response is present or not and it contains at least a trailer.<br/>
     * Unexpected result may be the wrong cause, because the command could represent multiple commands.
     *
     * @param r The response to check
     * @return True when result present, false otherwise.
     */
    private boolean checkTransmitResult(TransmitResponse r) {
        if (! r.getOutputAPDU().isEmpty() && r.getOutputAPDU().get(0).length >= 2) {
            return true;
        } else {
            return false;
        }
    }


    private byte[] connect(String ifdName, BigInteger slot) throws RecognitionException {
	Connect c = new Connect();
	c.setContextHandle(ctx);
	c.setIFDName(ifdName);
	c.setSlot(slot);

	ConnectResponse r = ifd.connect(c);
	checkResult(r.getResult());

	return r.getSlotHandle();
    }

    private void disconnect(byte[] slotHandle) throws RecognitionException {
	Disconnect c = new Disconnect();
	c.setSlotHandle(slotHandle);

	DisconnectResponse r = ifd.disconnect(c);
	checkResult(r.getResult());
    }

    private byte[] transmit(byte[] slotHandle, byte[] input, List<ResponseAPDUType> results) {
	Transmit t = new Transmit();
        t.setSlotHandle(slotHandle);
	InputAPDUInfoType apdu = new InputAPDUInfoType();
	apdu.setInputAPDU(input);
        for (ResponseAPDUType result : results) {
            apdu.getAcceptableStatusCode().add(result.getTrailer());
        }
	t.getInputAPDUInfo().add(apdu);

	TransmitResponse r = ifd.transmit(t);
	if (checkTransmitResult(r)) {
	    return r.getOutputAPDU().get(0);
	} else {
	    return null;
	}
    }


    private List<CardCall> branch2list(CardCall first) {
	LinkedList<CardCall> calls = new LinkedList<CardCall>();
	calls.add(first);

	CardCall next = first;
	// while next is a select call
	while (next.getResponseAPDU().get(0).getBody() == null) {
	    // a select only has one call in its conclusion
	    next = next.getResponseAPDU().get(0).getConclusion().getCardCall().get(0);
	    calls.add(next);
	}

	return calls;
    }


    private String treeCalls(byte[] slotHandle, List<CardCall> calls) throws RecognitionException {
	for (CardCall c : calls) {
	    // make list of next feature (aka branch)
	    List<CardCall> branch = branch2list(c);
	    // execute selects and then matcher, matcher decides over success
	    for (CardCall next : branch) {
		boolean matcher = (next.getResponseAPDU().get(0).getBody() != null) ? true : false;
		byte[] resultBytes = transmit(slotHandle, next.getCommandAPDU(), next.getResponseAPDU());
		// break when outcome is wrong
		if (resultBytes == null) {
		    break;
		}
                // get command bytes and trailer
		byte[] result = CardCommands.getDataFromResponse(resultBytes);
		byte[] trailer = CardCommands.getResultFromResponse(resultBytes);
		// if select, only one response exists
		if (!matcher && ! Arrays.equals(next.getResponseAPDU().get(0).getTrailer(), trailer)) {
		    // break when outcome is wrong
		    break;
                } else if (!matcher) {
                    // trailer matches expected response from select, continue
                    continue;
		} else {
		    // matcher command, loop through responses
		    for (ResponseAPDUType r : next.getResponseAPDU()) {
			// next response, when outcome is wrong
			if (! Arrays.equals(r.getTrailer(), trailer)) {
			    continue;
			}
			// check internals for match
			if (checkDataObject(r.getBody(), result)) {
			    if (r.getConclusion().getRecognizedCardType() != null) {
				// type recognised
				return r.getConclusion().getRecognizedCardType();
			    } else {
				// type dependent on subtree
				return treeCalls(slotHandle, r.getConclusion().getCardCall());
			    }
			}
		    }
		}
	    }
	}

	return null;
    }


    private boolean checkDataObject(DataMaskType matcher, byte[] result) {
	// check if we have a tag
	if (matcher.getTag() != null) {
	    byte [] tag = matcher.getTag();
	    try {
		TLV tlv = TLV.fromBER(result);

		// proceed only if tag matches
		long tagNum = ByteUtils.toLong(tag);
		while (tlv != null) {
		    if (tlv.getTagNumWithClass() == tagNum) {
			// check if we have a DataObject
			if (matcher.getDataObject() != null) {
			    return checkDataObject(matcher, tlv);
			} else {
			    return checkMatchingData(matcher.getMatchingData(), tlv.getValue());
			}
		    } else {
			tlv = tlv.getNext();
		    }
		}
	    } catch (TLVException ex) {
	    }
	    // no TLV structure or fallthrough after tag not found
	    return false;
	}

	// DataObject without a Tag is plain stupid
	if (matcher.getDataObject() != null) {
	    return false;
	}

	// ok we have a matcher
	return checkMatchingData(matcher.getMatchingData(), result);
    }

    private boolean checkDataObject(DataMaskType matcher, TLV result) {
	byte[] tag = matcher.getTag();
	long tagNum = 0;
	if (tag != null) {
	    tagNum = ByteUtils.toLong(tag);
	}

	// no tag and dataobject is a fail
	if (tag == null && matcher.getDataObject() != null) {
	    return false;
	}

	if (matcher.getDataObject() != null) {
	    List<TLV> chunks = result.findNextTags(tagNum);
	    for (TLV next : chunks) {
		boolean outcome = checkDataObject(matcher.getDataObject(), next);
		if (outcome == true) {
		    return true;
		}
	    }
	    // no match
	    return false;
	}

	return checkMatchingData(matcher.getMatchingData(), result.getValue());
    }

    private boolean checkMatchingData(MatchingDataType matcher, byte[] result) {
	// get values
	byte[] offsetBytes = matcher.getOffset();
	byte[] lengthBytes = matcher.getLength();
	byte[] valueBytes  = matcher.getMatchingValue();
	byte[] maskBytes   = matcher.getMask();

	// convert values for convenience
	if (offsetBytes == null) {
	    offsetBytes = new byte[] {(byte)0x00, (byte)0x00};
	}
	int offset = ByteUtils.toInteger(offsetBytes);
	int length = ByteUtils.toInteger(lengthBytes);
	if (maskBytes == null) {
	    maskBytes = new byte[valueBytes.length];
	    for (int i=0; i < maskBytes.length; i++) {
		maskBytes[i] = (byte) 0xFF;
	    }
	}

	// some basic integrity checks
	if (maskBytes != null && maskBytes.length != valueBytes.length) {
	    return false;
	}
	if (valueBytes.length != length) {
	    return false;
	}
	if (result.length < length+offset) {
	    return false;
	}

	// check
	for (int i=offset; i < length+offset; i++) {
	    if ((maskBytes[i-offset] & result[i]) != valueBytes[i-offset]) {
		return false;
	    }
	}

	return true;
    }

}
