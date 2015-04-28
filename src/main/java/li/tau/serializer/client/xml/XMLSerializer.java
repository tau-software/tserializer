package li.tau.serializer.client.xml;

import li.tau.serializer.client.Serializer;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;

public interface XMLSerializer extends Serializer {
	
	Document getDocument();

	Object fromXML(Node node, Class<?> type);
	Object fromXML(Node node, String className);
	Object fromXML(Node node);

	Element toXML(Object serializable);
	Element toXML(Object object, Element classNode, String className);

	String getServerClassName(Object instance);

}
