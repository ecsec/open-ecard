/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.ws;

import java.util.LinkedList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class WhitespaceFilter {

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
