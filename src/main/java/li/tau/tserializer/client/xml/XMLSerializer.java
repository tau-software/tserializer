package li.tau.tserializer.client.xml;

import li.tau.tserializer.client.TSerializer;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;

public interface XMLSerializer extends TSerializer {
	
	Document getDocument();

	Object fromXML(Node node, Class<?> type);
	Object fromXML(Node node, String className);
	Object fromXML(Node node);

	Element toXML(Object serializable);
	Element toXML(Object object, Element classNode, String className);

	String getServerClassName(Object instance);

}
