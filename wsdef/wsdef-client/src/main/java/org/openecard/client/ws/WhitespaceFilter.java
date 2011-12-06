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
