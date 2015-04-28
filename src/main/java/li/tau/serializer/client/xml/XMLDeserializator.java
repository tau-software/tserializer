package li.tau.serializer.client.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

public abstract class XMLDeserializator<S, T extends S> {
	
	public abstract static class ExternalDeserializator<S, T extends S> extends XMLDeserializator<S, T> {
		protected final XMLSerializer serializer;

		public ExternalDeserializator(XMLSerializer serializer) {
			this.serializer = serializer;
		}
	}
	
	private static final Set<String> RESERVED_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList(new String[]{"class", "enum-type"}));
	
	protected String implicitFieldName = null;
	protected String implicitCollectionFieldName = null;
	protected HashMap<String, String> implicitCollectionItemFieldNameMap = null;
	protected XMLDeserializator<? super S, S> superClassDeserializator = null;

	public abstract class Setter {
		public abstract void set(Node node, T instance);
	}

	protected final HashMap<String, Setter> dispatcher = new HashMap<String, Setter>();
	
	public T makeInstance() {
		throw new RuntimeException("This class type hasn't got default constructor.");
	};

	protected boolean deserializeField(Node n, String fieldName, T instance) {
		if (dispatcher.containsKey(fieldName)) {
			dispatcher.get(fieldName).set(n, instance);
			return true;
		} else if (superClassDeserializator != null) {
			return superClassDeserializator.deserializeField(n, fieldName,
					instance);
		}
		return false;
	}

	protected boolean deserializeImplicitCollectionField(Node n, T instance) {
		if (implicitCollectionItemFieldNameMap != null
				&& implicitCollectionItemFieldNameMap.containsKey(n.getNodeName())) {
			return deserializeField(n, implicitCollectionItemFieldNameMap.get(n.getNodeName()), instance);
		} else if (implicitCollectionFieldName != null) {
			return deserializeField(n, implicitCollectionFieldName, instance);
		} else if (superClassDeserializator != null) {
			return superClassDeserializator.deserializeImplicitCollectionField(n, instance);
		}
		return false;
	}

	public T deserialize(Node node, T instance) {
		if (node != null) {
			if (node.hasAttributes()) {
				NamedNodeMap attributes = node.getAttributes();
				int attributesLength = attributes.getLength();
				for (int i = 0; i < attributesLength; ++i) {
					Node n = attributes.item(i);
					if ((!RESERVED_ATTRIBUTE_NAMES.contains(n.getNodeName()))
						&& deserializeField(n, n.getNodeName(), instance) == false) {
						GWT.log("Couldn't deserialize field described in node attribute: "
								+ n.getNodeName() + " in "
								+ instance.toString(), null);
					}
				}
			}

			if (implicitFieldName != null) {
				if (deserializeField(node, implicitFieldName, instance) == false) {
					GWT.log("Couldn't deserialize implicit field: " + implicitFieldName
							+ " in " + instance.toString(), null);
				}
			} else {
				NodeList nodeList = node.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); ++i) {
					Node n = nodeList.item(i);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						if (deserializeField(n, n.getNodeName(), instance) == false) {
							if (deserializeImplicitCollectionField(n, instance) == false) {
								GWT.log("Can't find method to deserialize field: "
										+ n.getNodeName() + " in "
										+ instance.getClass().getName() + ":" + instance.toString(), null);
							}
						}
					}
				}
			}
		}
		return instance;
	}
}