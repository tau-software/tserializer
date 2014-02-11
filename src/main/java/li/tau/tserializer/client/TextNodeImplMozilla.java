package li.tau.tserializer.client;

import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

public class TextNodeImplMozilla extends TextNodeImpl {
	
	/*
	 * Mozilla splits textNodes if their size is bigger than 4096 characters
	 */
	
	@Override
	public String getNodeValue(Node node) {
		if (node == null) return null;
		if (node.hasChildNodes()) {
			if (node.getChildNodes().getLength() == 1) return node.getFirstChild().getNodeValue();
			StringBuffer sb = new StringBuffer();
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); ++i) {
				sb.append(nodeList.item(i).getNodeValue());
			}
			return sb.toString();
		} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
			return node.getNodeValue();
		}
		
		return "";
	}

}
