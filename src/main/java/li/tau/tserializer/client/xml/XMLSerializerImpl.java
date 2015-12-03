package li.tau.tserializer.client.xml;

import static com.google.gwt.xml.client.Node.ATTRIBUTE_NODE;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import li.tau.tserializer.client.TSerializerException;
import li.tau.tserializer.client.TextNodeImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMNodeException;

public class XMLSerializerImpl implements XMLSerializer {
	
	@Override
	public String format() {
		return "xml";
	}
	
	protected static final Logger LOGGER = Logger.getLogger(XMLSerializer.class.getName());

	@SuppressWarnings("rawtypes")
	protected final HashMap<String, XMLDeserializator> deserializators = new HashMap<String, XMLDeserializator>();

	protected static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");
	protected static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
	protected static final Double NaN = new Double(0.0d / 0.0d);
	protected static final TextNodeImpl TEXT_NODE_IMPL = GWT.create(TextNodeImpl.class);
	
	@SuppressWarnings("rawtypes")
	protected abstract class EnumDeserializator<E extends Enum> extends XMLDeserializator<Enum, E> {
		
		private final Class<E> enumClass;
		
		public EnumDeserializator(Class<E> enumClass) {
			this.enumClass = enumClass;
		}
		
		@Override
		public E makeInstance() {
			return null;
		}
		
		@Override
		public E deserialize(Node node, E instance) {
			return valueOf(getTextNodeValue(node));
		}
		
		public Class<E> getEnumClass() {
			return enumClass;
		}

		public abstract E valueOf(String enumName);
		
	}
	
	@SuppressWarnings("rawtypes")
	private abstract class CollectionDeserializator<C extends Collection> extends XMLDeserializator<Collection, C> {
		
		@Override
		public C deserialize(Node node, C instance) {
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node n = nodeList.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					deserializeField(n, n.getNodeName(), instance);
				}
			}
			return instance;
		};
		
		@Override
		@SuppressWarnings("unchecked")
		protected boolean deserializeField(Node n, String fieldName, C instance) {
			return instance.add(fromXML(n, n.getNodeName()));
		};
		
	}

	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public abstract class MapDeserializator<M extends Map> extends XMLDeserializator<Map, M> {
		
		@Override
		public M deserialize(Node node, M instance) {
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node entryNode = nodeList.item(i);
				if (entryNode.getNodeType() == Node.ELEMENT_NODE && "entry".equals(entryNode.getNodeName())) {
					Node keyNode = null, valueNode = null;
					NodeList entryNodeList = entryNode.getChildNodes();
					int j = 0;
					while (j < entryNodeList.getLength()) {
						if (entryNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
							if (keyNode == null) keyNode = entryNodeList.item(j);
							else {
								valueNode = entryNodeList.item(j);
								break;
							}
						}
						++j;
					}
					entryNode.normalize();
					instance.put(fromXML(keyNode, keyNode.getNodeName()),
							fromXML(valueNode, valueNode.getNodeName()));
				}
			}
			return instance;
		};
		
	}
	
	@SuppressWarnings({"rawtypes"})
	protected XMLSerializerImpl() {
		XMLDeserializator deserializator = new CollectionDeserializator<Set>() {
			@Override
			public Set makeInstance() {
				return new HashSet();
			}
		};
		deserializators.put("set", deserializator);
		deserializators.put(Set.class.getName(), deserializator);
		deserializators.put(HashSet.class.getName(), deserializator);
		
		deserializator = new CollectionDeserializator<LinkedHashSet>() {
			@Override
			public LinkedHashSet makeInstance() {
				return new LinkedHashSet();
			}
		};
		deserializators.put(LinkedHashSet.class.getName(), deserializator);
		deserializators.put("linked-hash-set", deserializator);
		
		deserializator = new CollectionDeserializator<SortedSet>() {
			@Override
			public SortedSet makeInstance() {
				return new TreeSet();
			}
		};
		deserializators.put("sorted-set", deserializator);
		deserializators.put(SortedSet.class.getName(), deserializator);
		deserializators.put(TreeSet.class.getName(), deserializator);
		
		deserializator = new CollectionDeserializator<List>() {
			@Override
			public List makeInstance() {
				return new ArrayList();
			}
		};
		deserializators.put("list", deserializator);
		deserializators.put(List.class.getName(), deserializator);
		deserializators.put(ArrayList.class.getName(), deserializator);
		deserializators.put(Collection.class.getName(), deserializator);

		deserializator = new XMLDeserializator<Object, Boolean>() {
			@Override
			public Boolean deserialize(Node node, Boolean instance) {
				return new Boolean(getTextNodeValue(node));
			}
			@Override
			public Boolean makeInstance() {
				return null;
			}
		};
		deserializators.put("boolean", deserializator);
		
		deserializator = new XMLDeserializator<Object, Integer>() {
			@Override
			public Integer deserialize(Node node, Integer instance) {
				return new Integer(getTextNodeValue(node));
			}
			@Override
			public Integer makeInstance() {
				return null;
			}
		};
		deserializators.put("int", deserializator);
		
		deserializator = new XMLDeserializator<Object, Long>() {
			@Override
			public Long deserialize(Node node, Long instance) {
				return new Long(getTextNodeValue(node));
			}
			@Override
			public Long makeInstance() {
				return null;
			}
		};
		deserializators.put("long", deserializator);

		deserializator = new XMLDeserializator<Object, double[]>() {
			@Override
			public double[] deserialize(Node node, double[] instance) {
				JSONArray array = JSONParser.parseLenient(getTextNodeValue(node)).isArray();
				double[] result = new double[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = array.get(i).isNumber().doubleValue();
				}
				return result;
			}
			@Override
			public double[] makeInstance() {
				return null;
			}
		};
		deserializators.put("double-array", deserializator);
		
		deserializator = new XMLDeserializator<Object, int[]>() {
			@Override
			public int[] deserialize(Node node, int[] instance) {
				JSONArray array = JSONParser.parseStrict(getTextNodeValue(node)).isArray();
				int[] result = new int[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = (int) array.get(i).isNumber().doubleValue();
				}
				return result;
			}
			@Override
			public int[] makeInstance() {
				return null;
			}
		};
		deserializators.put("int-array", deserializator);
		
		deserializator = new XMLDeserializator<Object, Date[]>() {
			@Override
			public Date[] deserialize(Node node, Date[] instance) {
				String arrayStr = getTextNodeValue(node).replaceAll("[\\[\\]]", "");
				String[] values = arrayStr.split(",");
				if (values.length == 0) return new Date[0];
				instance = new Date[values.length];
				for (int i = 0; i < values.length; ++i) {
					instance[i] = DATE_TIME_FORMAT.parse(values[i].trim());
				}
				return instance;
			}
				
			@Override
			public Date[] makeInstance() {
				return null;
			}
		};
		deserializators.put("date-array", deserializator);
		
		deserializator = new XMLDeserializator<Object, Object[]>() {
			@Override
			public Object[] deserialize(Node node, Object[] instance) {
				NodeList childNodes = node.getChildNodes();
				Object[] result = new Object[childNodes.getLength()];
				for (int i = 0; i < result.length; ++i) {
					if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						result[i] = fromXML(childNodes.item(i));
					}
				}
				return result;
			}
			@Override
			public Object[] makeInstance() {
				return null;
			}
		};
		deserializators.put("object-array", deserializator);

		
		deserializator = new XMLDeserializator<Object, Double>() {
			@Override
			public Double deserialize(Node node, Double instance) {
				return parseDouble(getTextNodeValue(node));
			}
			@Override
			public Double makeInstance() {
				return null;
			}
		};
		deserializators.put("double", deserializator);

		deserializator = new XMLDeserializator<Object, String>() {
			@Override
			public String deserialize(Node node, String instance) {
				return getTextNodeValue(node);
			}
			@Override
			public String makeInstance() {
				return null;
			}
		};
		deserializators.put("string", deserializator);
		
		deserializator = new XMLDeserializator<Object, BigDecimal>() {
			@Override
			public BigDecimal deserialize(Node node, BigDecimal instance) {
				return new BigDecimal(getTextNodeValue(node));
			}
			@Override
			public BigDecimal makeInstance() {
				return null;
			}
		};
		deserializators.put(BigDecimal.class.getName(), deserializator);
		deserializators.put("big-decimal", deserializator);
		
		deserializator = new MapDeserializator<LinkedHashMap>() {
			@Override
			public LinkedHashMap makeInstance() {
				return new LinkedHashMap();
			};
		};
		deserializators.put(LinkedHashMap.class.getName(), deserializator);

		deserializator = new MapDeserializator<HashMap>() {
			@Override
			public HashMap makeInstance() {
				return new HashMap();
			};
		};
		deserializators.put("map", deserializator);
		deserializators.put(Map.class.getName(), deserializator);
		deserializators.put(HashMap.class.getName(), deserializator);
		

		deserializator = new MapDeserializator<LinkedHashMap>() {
			@Override
			public LinkedHashMap makeInstance() {
				return new LinkedHashMap();
			};
		};
		deserializators.put("linked-hash-map", deserializator);
		
		deserializator = new XMLDeserializator<AbstractMap, EnumMap>() {
			@Override
			public EnumMap makeInstance() {
				return null;
			}
			@SuppressWarnings("unchecked")
			@Override
			public EnumMap deserialize(Node node, EnumMap instance) {
				if (node.hasChildNodes() == false) {
					LOGGER.log(Level.WARNING, "EnumMap instance without elements: " + node.getNodeValue());
					return null;
				}
				
				Map enumMap = new HashMap();
				
				String enumType = node.getAttributes().getNamedItem("enum-type").getNodeValue();
				XMLDeserializator deserializator = deserializators.get(enumType);
				
				if (deserializator == null) {
					LOGGER.log(Level.SEVERE, "Couldn't find deserializator for type: " + enumType);
					return null;
				}
				
				if (!(deserializator instanceof EnumDeserializator)) {
					LOGGER.log(Level.SEVERE, "Wrong type of deserializtor for type: " + enumType);
					return null;
				}
				
				EnumDeserializator enumDeserializator = (EnumDeserializator)deserializator;
				
				NodeList nodeList = node.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); ++i) {
					Node entryNode = nodeList.item(i);
					if (entryNode.getNodeType() == Node.ELEMENT_NODE && "entry".equals(entryNode.getNodeName())) {
						Node keyNode = null, valueNode = null;
						NodeList entryNodeList = entryNode.getChildNodes();
						int j = 0;
						while (j < entryNodeList.getLength()) {
							if (entryNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
								if (keyNode == null) keyNode = entryNodeList.item(j);
								else {
									valueNode = entryNodeList.item(j);
									break;
								}
							}
							++j;
						}
						enumMap.put(enumDeserializator.deserialize(keyNode, null), fromXML(valueNode, valueNode.getNodeName()));
					}
				}
				return new EnumMap(enumMap);
			}
		};
		deserializators.put(EnumMap.class.getName(), deserializator);
		
		deserializator = new XMLDeserializator<AbstractSet, EnumSet>() {
			@Override
			public EnumSet makeInstance() {
				return null;
			}
			@SuppressWarnings("unchecked")
			@Override
			public EnumSet deserialize(Node node, EnumSet instance) {
				String enumType = node.getAttributes().getNamedItem("enum-type").getNodeValue();
				XMLDeserializator deserializator = deserializators.get(enumType);
				
				if (deserializator == null) {
					LOGGER.log(Level.SEVERE, "Couldn't find deserializator for type: " + enumType);
					return null;
				}
				
				if (!(deserializator instanceof EnumDeserializator)) {
					LOGGER.log(Level.SEVERE, "Wrong type of deserializtor for type: " + enumType);
					return null;
				}
				
				if (node.hasChildNodes() == false) {
					LOGGER.log(Level.FINE, "EnumSet instance without elements: " + node.getNodeValue());
					return EnumSet.noneOf(((EnumDeserializator) deserializator).getEnumClass());
				}
				
				List enumList = new ArrayList();
				
				EnumDeserializator enumDeserializator = (EnumDeserializator)deserializator;
				
				String nodeValue = getTextNodeValue(node);
				for (String enumName : nodeValue.split(",")) {
					enumList.add(enumDeserializator.valueOf(enumName.trim()));
				}

				return EnumSet.copyOf(enumList);
			}
		};
		deserializators.put(EnumSet.class.getName(), deserializator);
		
		deserializator = new XMLDeserializator<Object, Date>() {
			@Override
			public Date deserialize(Node node, Date instance) {
				return DATE_TIME_FORMAT.parse(getTextNodeValue(node));
			}
			@Override
			public Date makeInstance() {
				return null;
			}
		};
		deserializators.put(Date.class.getName(), deserializator);
		deserializators.put("date", deserializator);
		
		deserializator = new XMLDeserializator<Object, java.sql.Date>() {
			@Override
			public java.sql.Date deserialize(Node node, java.sql.Date instance) {
				return java.sql.Date.valueOf(getTextNodeValue(node));
			}
			@Override
			public java.sql.Date makeInstance() {
				return null;
			}
		};
		deserializators.put("sql-date", deserializator);
		
		deserializator = new XMLDeserializator<Object, Void>() {
			@Override
			public Void deserialize(Node node, Void instance) {
				return instance;
			}
			@Override
			public Void makeInstance() {
				return null;
			}
		};
		deserializators.put("null", deserializator);
		
		serializators.put(String.class.getName(), new XMLSerializator<Object, String>() {
			@Override
			public Element serialize(String instance, Element classNode, String className) {
				appendTextNodeToElement(classNode, instance);
				return classNode;
			}
	    });
		classNamesMap.put(String.class.getName(), "string");
		
		serializators.put(Boolean.class.getName(), new XMLSerializator<Object, Boolean>() {
			@Override
			public Element serialize(Boolean instance, Element classNode, String className) {
				appendPrimitiveWrapperNodeToElement(classNode, instance);
				return classNode;
			}
	    });
		classNamesMap.put(Boolean.class.getName(), "boolean");

		serializators.put(Integer.class.getName(), new XMLSerializator<Object, Integer>() {
			@Override
			public Element serialize(Integer instance, Element classNode, String className) {
				appendPrimitiveWrapperNodeToElement(classNode, instance);
				return classNode;
			}
	    });
		classNamesMap.put(Integer.class.getName(), "int");
		
		serializators.put(int[].class.getName(), new XMLSerializator<Object, int[]>() {
			@Override
			public Element serialize(int[] instance, Element classNode, String className) {
				appendTextNodeToElement(classNode, Arrays.toString(instance));
				return classNode;
			}
	    });
		classNamesMap.put(int[].class.getName(), "int-array");

		serializators.put(Double.class.getName(), new XMLSerializator<Object, Double>() {
			@Override
			public Element serialize(Double instance, Element classNode, String className) {
				appendPrimitiveWrapperNodeToElement(classNode, instance);
				return classNode;
			}
	    });
		classNamesMap.put(Double.class.getName(), "double");

		serializators.put(BigDecimal.class.getName(), new XMLSerializator<Object, BigDecimal>() {
			@Override
			public Element serialize(BigDecimal instance, Element classNode, String className) {
				appendTextNodeToElement(classNode, instance.toString());
				return classNode;
			}
	    });

		serializators.put(Date.class.getName(), new XMLSerializator<Object, Date>() {
			@Override
			public Element serialize(Date instance, Element classNode, String className) {
				appendDateNodeToElement(classNode, instance);
				return classNode;
			}
	    });
		classNamesMap.put(Date.class.getName(), "date");

		serializators.put(java.sql.Date.class.getName(), new XMLSerializator<Object, java.sql.Date>() {
			@Override
			public Element serialize(java.sql.Date instance, Element classNode, String className) {
				appendTextNodeToElement(classNode, DATE_FORMAT.format(instance));
				return classNode;
			}
	    });
		classNamesMap.put(java.sql.Date.class.getName(), "sql-date");

		serializators.put(ArrayList.class.getName(), new XMLSerializator<Object, ArrayList>() {
			@Override
			public Element serialize(ArrayList instance, Element classNode, String className) {
				if (instance != null) {
//					if (!instance.getClass().getName().equals(className)) {
//						classNode.setAttribute("class", getServerClassName(instance));
//					}
					for (Object child : instance) {
						classNode.appendChild(toXML(child));
					}
				}
				return classNode;
			}
	    });
		classNamesMap.put(ArrayList.class.getName(), "list");
		
		serializators.put(Object[].class.getName(), new XMLSerializator<Object, Object[]>() {
			@Override
			public Element serialize(Object[] instance, Element classNode, String className) {
				if (instance != null && instance.length > 0) {
					if (!instance.getClass().getName().equals(className)) {
						classNode.setAttribute("class", getServerClassName(instance[0].getClass().getName()) + "-array");
					}
					for (Object child : instance) {
						classNode.appendChild(toXML(child));
					}
				}
				return classNode;
			}
	    });
		
		final XMLSerializator<Object, Map> mapSerializator = new XMLSerializator<Object, Map>() {
			@Override
			public Element serialize(Map instance, Element classNode, String className) {
				if (instance != null) {
					if (!instance.getClass().getName().equals(className)) {
						classNode.setAttribute("class", instance.getClass().getName());
					}
					for (Object obj : instance.entrySet()) {
						Entry entry = (Entry) obj;
						Element entryElement = DOCUMENT.createElement("entry");
						entryElement.appendChild(toXML(entry.getKey()));
						Object value = entry.getValue();
						if (value != null) entryElement.appendChild(toXML(entry.getValue()));
						classNode.appendChild(entryElement);
					}
				}
				return classNode;
			}
		};
		serializators.put(Map.class.getName(), mapSerializator);
		serializators.put(HashMap.class.getName(), mapSerializator);
		serializators.put(LinkedHashMap.class.getName(), mapSerializator);
		
		serializators.put(EnumMap.class.getName(), new XMLSerializator<AbstractMap, EnumMap>() {
			@Override
			public Element serialize(EnumMap instance, Element classNode, String className) {
				if (instance == null || instance.isEmpty()) return null;
//				if (instance != null) {
//					Iterator it = instance.keySet().iterator();
//					while (it.hasNext()) {
//						if (instance.get(it.next()) == null) it.remove();
//					}
//				}
				classNode = mapSerializator.serialize(instance, classNode, className);
				if (instance != null && !instance.isEmpty()) {
					classNode.setAttribute("enum-type", getServerClassName(instance.keySet().iterator().next()));
				}
				return classNode;
			}
		});
		
		XMLSerializator<AbstractSet, EnumSet> enumSetSerializator = new XMLSerializator<AbstractSet, EnumSet>() {
			@Override
			public Element serialize(EnumSet instance, Element classNode, String className) {
				classNode.setAttribute("enum-type", getServerClassName(instance.toArray()[0]));
				StringBuilder sb = new StringBuilder();
				for (Object child : instance) {
					sb.append(((Enum)child).name()).append(",");
				}
				appendTextNodeToElement(classNode, sb.substring(0, sb.length() - 1));
				return classNode;
			}
	    };
	    serializators.put(EnumSet.class.getName(), enumSetSerializator);
		serializators.put("java.util.RegularEnumSet", enumSetSerializator);
		serializators.put("java.util.JumboEnumSet", enumSetSerializator);
	}
	
	@Override
	public Document getDocument() {
		return DOCUMENT;
	}

	public static String getTextNodeValue(Node node) {
		if (node.getNodeType() == ATTRIBUTE_NODE) {
			return node.getNodeValue();
		}
		return TEXT_NODE_IMPL.getNodeValue(node);
	}

	public static Double parseDouble(String text) {
		if ("NaN" == text) return NaN;
		return Double.parseDouble(text);
	}

	@Override
	public Object fromXML(Node node, Class<?> type) {
		assert (node != null) && (type != null);
		return fromXML(node, type.getName().replaceAll("\\$", "_-"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object fromXML(Node node, String className) {
		Node classAttribute = node.getAttributes().getNamedItem("class");
		if (classAttribute != null) {
			className = classAttribute.getNodeValue().replaceAll("\\$", "_-");
		}
		XMLDeserializator deserializator = deserializators.get(className);
		if (deserializator != null) {
			return deserialize(node, deserializator);
		}

		throw new RuntimeException("Couldn't dispatch className = " + className + " in node:\n" + node.toString());
	}
	
	@Override
	public Object fromString(String str) {
		if (str == null || str.trim().isEmpty()) return null;
		try {
			return fromXML(XMLParser.parse(str).getDocumentElement());
		} catch (TSerializerException e) {
			throw e;
		} catch (Exception e) {
			throw new TSerializerException("Problem with string deserialization: " + str, e);
		}
	}
	
	@Override
	public Object fromXML(Node node) {
		assert node != null;
		return fromXML(node, node.getNodeName());
	}

	protected <T> T deserialize(Node node, XMLDeserializator<? super T, T> deserializator) {
		T instance = deserializator.makeInstance();
		return deserializator.deserialize(node, instance);
	}

	@SuppressWarnings("rawtypes")
	protected final HashMap<String, XMLSerializator> serializators = new HashMap<String, XMLSerializator>();
	
	@Override
	public String toString(Object object) {
		Element element = toXML(object);
		if (element == null) return null;
		return element.toString();
	}

	@Override
	public Element toXML(Object object) {
		if (object == null) return null;
		return toXML(object, createClassNode(object), object.getClass().getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Element toXML(java.lang.Object object, Element classNode, String className) {
		if (className.startsWith("[L")) className = Object[].class.getName();
		XMLSerializator serializator = serializators.get(className);
		if (serializator != null) {
			return serializator.serialize(object, classNode, className);
		} else if(object instanceof Enum<?>) {
			appendTextNodeToElement(classNode, ((Enum<?>)object).name());
			return classNode;
		}
		throw new RuntimeException("Can't serialize this class:" + object.getClass().getName() + " (provided className = " + className + ")");
	}
	
	protected Element createClassNode(Object instance) {
		try {
			return DOCUMENT.createElement(getServerClassName(instance));
		} catch(DOMNodeException e) {
			LOGGER.log(Level.SEVERE, getServerClassName(instance), e);
			throw e;
		}
	}
	
	protected final HashMap<String, String> classNamesMap = new HashMap<String, String>();
	
	@Override
	public String getServerClassName(Object instance) {
	    String className = classNamesMap.get(instance.getClass().getName());
		if (className == null && instance.getClass().getName().startsWith("[L")) {
			String innerClassName = instance.getClass().getName().substring(2, instance.getClass().getName().length() - 1);
			className = classNamesMap.get(innerClassName);
			if (className == null) className = innerClassName;
			className += "-array";
		}

	    if (className == null) return instance.getClass().getName().replaceAll("\\$", "_-");
	    return className;
	}

	protected static final Document DOCUMENT = XMLParser.createDocument();

	public static Element createDataElement(String className) {
		Element dataElement = DOCUMENT.createElement("data");
		dataElement.setAttribute("class", className);
		return dataElement;
	}

	protected static void appendTextNodeToElement(Node node, String textNodeName, String textNodeData) {
		Node textNode = DOCUMENT.createElement(textNodeName);
		textNode.appendChild(DOCUMENT.createTextNode(textNodeData));
		node.appendChild(textNode);
	}

	protected static void appendTextAttributeNodeToElement(Element element, String textNodeName, String textNodeData) {
		element.setAttribute(textNodeName, textNodeData);
	}

	protected static void appendTextNodeToElement(Node node, String textNodeData) {
		node.appendChild(DOCUMENT.createTextNode(textNodeData));
	}

	public static void appendPrimitiveWrapperAttributeNodeToElement(Element element, String nodeName, Object primitiveWrapperNodeData) {
		if (primitiveWrapperNodeData != null) {
			appendTextAttributeNodeToElement(element, nodeName, primitiveWrapperNodeData.toString());
		}
	}

	public static void appendPrimitiveWrapperNodeToElement(Node node, String nodeName, Object primitiveWrapperNodeData) {
		if (primitiveWrapperNodeData != null) {
			appendTextNodeToElement(node, nodeName, primitiveWrapperNodeData.toString());
		}
	}

	protected static void appendPrimitiveIntegerArrayNodeToElement(Node node, String nodeName, int[] array) {
		if (array != null) {
			appendTextNodeToElement(node, nodeName, Arrays.toString(array));
		}
	}

	protected static void appendPrimitiveWrapperNodeToElement(Node node, Object primitiveWrapperNodeData) {
		if (primitiveWrapperNodeData != null) {
			appendTextNodeToElement(node, primitiveWrapperNodeData.toString());
		}
	}
	
	protected static void appendDateNodeToElement(Node node, Date dateNodeData) {
		if (dateNodeData != null) {
			appendTextNodeToElement(node, DATE_TIME_FORMAT.format(dateNodeData));
		}
	}

	protected void appendSerializableNodeToElement(Element element, String nodeName, String fieldTypeName, Object object) {
		if (object != null) {
			Element tSerializableElement = DOCUMENT.createElement(nodeName);
			tSerializableElement = toXML(object, tSerializableElement, object.getClass().getName());
			if (!object.getClass().getName().equals(fieldTypeName) && !(object instanceof EnumSet)) {
				tSerializableElement.setAttribute("class", getServerClassName(object));
			}
			if (tSerializableElement != null) element.appendChild(tSerializableElement);
		}
	}
	
	protected void appendNodeToElement(Element element, String nodeName, String fieldTypeName, Object object) {
		if (object != null) {
			if (object instanceof List) {
				Element collectionElement = DOCUMENT.createElement(nodeName);
				if (!object.getClass().getName().equals(fieldTypeName)) {
					collectionElement.setAttribute("class", "list");
				}
				Collection<?> collection = (Collection<?>) object;
				for (Object collectionChild : collection) {
					collectionElement.appendChild(toXML(collectionChild));
				}
				element.appendChild(collectionElement);
			} else {
				appendSerializableNodeToElement(element, nodeName, fieldTypeName, object);
			}
		}
	}
	
	protected void appendArrayNodeToElement(Element element, String nodeName, Object[] objects) {
		if (objects != null) {
			Element arrayElement = DOCUMENT.createElement(nodeName);
			for (Object object : objects) {
				arrayElement.appendChild(toXML(object));
			}
			element.appendChild(arrayElement);
		}
	}
	
	protected String getEnumValue(Enum<?> enm) {
		return enm == null ? null : enm.name();
	}

}
