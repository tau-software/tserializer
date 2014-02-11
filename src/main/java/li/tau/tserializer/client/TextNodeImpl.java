package li.tau.tserializer.client;

import com.google.gwt.xml.client.Node;

public class TextNodeImpl {

	public String getNodeValue(Node node) {
		if (node == null) return null;
		return (node.hasChildNodes())?node.getFirstChild().getNodeValue():"";
	}

}
