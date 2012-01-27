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

package org.openecard.client.ws;

import java.util.LinkedList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WhitespaceFilter {

    public static void filter(Node root) {
	NodeList childNodes = root.getChildNodes();

	// remove afterwards else the nodelist indices may not be correct
	LinkedList<Node> toRemove = new LinkedList<Node>();

	for (int i = 0; i < childNodes.getLength(); i++) {
	    Node next = childNodes.item(i);

	    // only one textnode
	    if ((next.getNodeType() == Node.TEXT_NODE) && childNodes.getLength() == 1) {
		next.setNodeValue(next.getNodeValue().trim());
	    }
	    // text node but there is more (an element) in this childlist -> remove this node
	    else if (next.getNodeType() == Node.TEXT_NODE) {
		String strippedData = next.getNodeValue().trim();
		if (strippedData.isEmpty()) {
		    toRemove.addFirst(next);
		}
	    }
	    // comments filtered out before this call, node must be an element -> recurse
	    else {
		filter(next);
	    }
	}

	// remove all this bullshit
	for (Node n : toRemove) {
	    root.removeChild(n);
	}
    }

}
