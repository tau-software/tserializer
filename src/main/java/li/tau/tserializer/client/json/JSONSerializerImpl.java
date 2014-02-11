package li.tau.tserializer.client.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import li.tau.tserializer.client.json.JSONDeserializator.JSONObjectDeserializator;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JSONSerializerImpl implements JSONSerializer {
	
	protected static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	@SuppressWarnings("rawtypes")
	protected abstract class EnumDeserializator<E extends Enum> extends JSONDeserializator<Enum, E> {
		
		@Override
		public E makeInstance() {
			return null;
		}
		
		@Override
		public E deserialize(JSONValue object, E instance) {
			return valueOf(getString(object.isObject().get("#value")));
		}
		
		public abstract E valueOf(String enumName);
		
	}

	
	public class NumberJSONSerializator implements JSONSerializator<Object, Number> {
		@Override
		public JSONValue serialize(Number instance) {
			return new JSONNumber(instance.doubleValue());
		}
	}
	
	@Override
	public String format() {
		return "json";
	}

	@SuppressWarnings("rawtypes")
	protected final HashMap<String, JSONDeserializator> deserializators = new HashMap<String, JSONDeserializator>();
	
	@Override
	public Object fromString(String str) {
		if (str == null) return null;
		return fromJSON(JSONParser.parseStrict(str));
	}
	
	@Override
	public Object fromJSON(JSONValue json) {
		if (json == null || json.isNull() != null) return null;
		if (json.isBoolean() != null) return json.isBoolean().booleanValue();
		if (json.isNumber() != null) return json.isNumber().doubleValue();
		if (json.isString() != null) return json.isString().stringValue();
		if (json.isArray() != null) return fromJSON(json.isArray());
		if (json.isObject() != null && json.isObject().containsKey("$date")) return fromJSON(json.isObject(), "date");
		return fromJSON(json.isObject());
	}
	
	protected Object[] fromJSON(JSONArray array) {
		int size = array.size();
		Object[] result = new Object[size];
		for (int i = 0; i < size; ++i) {
			result[i] = fromJSON(array.get(i));
		}
		return result;
	}

	protected Object fromJSON(JSONObject object) {
		if (object == null) return null;
		JSONValue classValue = object.get("@class");
		if (classValue == null) throw new IllegalArgumentException("Couldn't find class name in JSONObject: " + object.toString()); 
		return fromJSON(object, classValue.isString().stringValue());
	}
	
	@Override
	public Object fromJSON(JSONValue value, String className) {
		if (value == null) return null;
		if (value.isObject() != null) {
			JSONValue classValue = value.isObject().get("@class");
			if (classValue != null && classValue.isString() != null) {
				className = classValue.isString().stringValue();
			}
		}
		JSONDeserializator<?, ?> deserializator = deserializators.get(className);
		if (deserializator == null) {
			if (className.endsWith("-array")) {
				deserializator = new ArrayDeserializator(className.substring(0, className.lastIndexOf("-array")));
			} else {
				throw new RuntimeException("Couldn't dispatch className = " + className + " in node:\n" + value.toString());
			}
		}
		return deserialize(value, deserializator);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object deserialize(JSONValue value, JSONDeserializator deserializator) {
		return deserializator.deserialize(value, deserializator.makeInstance());
	}

//	protected static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
//	protected static final Double NaN = new Double(0.0d / 0.0d);
//	protected static final TextNodeImpl TEXT_NODE_IMPL = GWT.create(TextNodeImpl.class);
//	
	@SuppressWarnings("rawtypes")
	private abstract class CollectionDeserializator<C extends Collection> extends JSONDeserializator<Collection, C> {
		
		@SuppressWarnings("unchecked")
		@Override
		public C deserialize(JSONValue value, C instance) {
			JSONArray array = value.isArray();
			if (array == null && value.isObject() != null && value.isObject().containsKey("#array")) {
				array = value.isObject().get("#array").isArray();
			}
			if (array != null) {
				for (int i = 0; i < array.size(); ++i) {
					instance.add(fromJSON(array.get(i)));
				}
			}
			return instance;
		};
		
	}
	
	private abstract class PrimitiveArrayDeserializator<T> extends JSONDeserializator<Object, T> {
		
		@Override
		public T deserialize(JSONValue value, T instance) {
			JSONArray array = value.isArray();
			if (array == null && value.isObject() != null && value.isObject().containsKey("#array")) {
				array = value.isObject().get("#array").isArray();
			}
			if (array == null) return null;
			return deserialize(array);
		}
		
		@Override
		public T makeInstance() {
			return null;
		}
		
		protected abstract T deserialize(JSONArray array);
		
	}
	
	protected class ArrayDeserializator extends JSONDeserializator<Object, Object[]> {
		
		private final String componentClassName;
		
		ArrayDeserializator(String componentClassName) {
			this.componentClassName = componentClassName;
		}
		
		@Override
		public Object[] deserialize(JSONValue value, Object[] instance) {
			JSONArray array = value.isArray();
			if (array == null && value.isObject() != null && value.isObject().containsKey("#array")) {
				array = value.isObject().get("#array").isArray();
			}
			if (array == null) return null;
			Object[] result = new Object[array.size()];
			for (int i = 0; i < result.length; ++i) {
				result[i] = fromJSON(array.get(i), componentClassName);
			}
			return result;
		}
		
		
		@Override
		public Object[] makeInstance() {
			return null;
		}
		
	}

	@SuppressWarnings("rawtypes")
	public abstract class MapDeserializator<M extends Map> extends JSONObjectDeserializator<Map, M> {
		
		@SuppressWarnings("unchecked")
		@Override
		protected M deserialize(JSONObject object, M instance) {
			if (!object.containsKey("#map") || object.get("#map") == null) return instance;
			JSONArray array = object.get("#map").isArray();
			if (array == null) {
				LOGGER.warning("#map should be JSONArray type: \"" + object.toString() + "\"");
				return instance;
			}
			int arraySize = array.size();
			for (int i = 0; i < arraySize; ++i) {
				JSONArray entry = array.get(i).isArray();
				instance.put(fromJSON(entry.get(0)), fromJSON(entry.get(1)));
			}
			return instance;
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public JSONSerializerImpl() {
		JSONDeserializator deserializator = new JSONDeserializator<Object, Byte>() {
			@Override
			public Byte deserialize(JSONValue value, Byte instance) {
				return ((Double)value.isObject().get("#value").isNumber().doubleValue()).byteValue();
			}
			@Override
			public Byte makeInstance() {
				return null;
			}
			
		};
		deserializators.put("byte", deserializator);
		
		deserializator = new JSONDeserializator<Object, Float>() {
			@Override
			public Float deserialize(JSONValue value, Float instance) {
				return ((Double)value.isObject().get("#value").isNumber().doubleValue()).floatValue();
			}
			@Override
			public Float makeInstance() {
				return null;
			}
			
		};
		deserializators.put("float", deserializator);
		
		deserializator = new JSONDeserializator<Object, Integer>() {
			@Override
			public Integer deserialize(JSONValue value, Integer instance) {
				return ((Double)value.isObject().get("#value").isNumber().doubleValue()).intValue();
			}
			@Override
			public Integer makeInstance() {
				return null;
			}
			
		};
		deserializators.put("int", deserializator);
		
		deserializator = new JSONDeserializator<Object, Long>() {
			@Override
			public Long deserialize(JSONValue value, Long instance) {
				return ((Double)value.isObject().get("#value").isNumber().doubleValue()).longValue();
			}
			@Override
			public Long makeInstance() {
				return null;
			}
			
		};
		deserializators.put("long", deserializator);
		
		deserializator = new JSONDeserializator<Object, Short>() {
			@Override
			public Short deserialize(JSONValue value, Short instance) {
				return ((Double)value.isObject().get("#value").isNumber().doubleValue()).shortValue();
			}
			@Override
			public Short makeInstance() {
				return null;
			}
			
		};
		deserializators.put("short", deserializator);
		
		deserializator = new JSONDeserializator<Object, String>() {
			@Override
			public String deserialize(JSONValue value, String instance) {
				return value.isObject().get("#value").isString().stringValue();
			}
			@Override
			public String makeInstance() {
				return null;
			}
		};
		deserializators.put("string", deserializator);
		
		deserializator = new JSONDeserializator<Object, Date>() {
			@Override
			public Date deserialize(JSONValue value, Date instance) {
				return DATE_TIME_FORMAT.parse(value.isObject().get("$date").isString().stringValue());
			};
			@Override
			public Date makeInstance() {
				return null;
			}
		};
		deserializators.put(Date.class.getName(), deserializator);
		deserializators.put("date", deserializator);
		
		//TODO byte array
		
		deserializator = new PrimitiveArrayDeserializator<boolean[]>() {
			@Override
			protected boolean[] deserialize(JSONArray array) {
				boolean[] result = new boolean[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = array.get(i).isBoolean().booleanValue();
				}
				return result;
			}
		};
		deserializators.put("boolean-array", deserializator);
		
		deserializator = new JSONDeserializator<Object, char[]>() {
			@Override
			public char[] deserialize(JSONValue value, char[] instance) {
				return value.isString().stringValue().toCharArray();
			}
			@Override
			public char[] makeInstance() {
				return null;
			}
		};
		deserializators.put("char-array", deserializator);
		
		deserializator = new PrimitiveArrayDeserializator<double[]>() {
			@Override
			protected double[] deserialize(JSONArray array) {
				double[] result = new double[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = array.get(i).isNumber().doubleValue();
				}
				return result;
			}
		};
		deserializators.put("double-array", deserializator);
	
		deserializator = new PrimitiveArrayDeserializator<float[]>() {
			@Override
			protected float[] deserialize(JSONArray array) {
				float[] result = new float[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = (float) array.get(i).isNumber().doubleValue();
				}
				return result;
			}
		};
		deserializators.put("float-array", deserializator);
		
		deserializator = new PrimitiveArrayDeserializator<int[]>() {
			@Override
			protected int[] deserialize(JSONArray array) {
				int[] result = new int[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = (int) array.get(i).isNumber().doubleValue();
				}
				return result;
			}
		};
		deserializators.put("int-array", deserializator);
		
		deserializator = new PrimitiveArrayDeserializator<long[]>() {
			@Override
			protected long[] deserialize(JSONArray array) {
				long[] result = new long[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = (long) array.get(i).isNumber().doubleValue();
				}
				return result;
			}
		};
		deserializators.put("long-array", deserializator);
		
		deserializator = new PrimitiveArrayDeserializator<short[]>() {
			@Override
			protected short[] deserialize(JSONArray array) {
				short[] result = new short[array.size()];
				for (int i = 0; i < array.size(); ++i) {
					result[i] = (short) array.get(i).isNumber().doubleValue();
				}
				return result;
			}
		};
		deserializators.put("short-array", deserializator);
		
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
		
		serializators.put(Boolean.class.getName(), new JSONSerializator<Object, Boolean>() {
			@Override
			public JSONValue serialize(Boolean instance) {
				return JSONBoolean.getInstance(instance);
			}
	    });
		classNamesMap.put(Boolean.class.getName(), "boolean");
		
		NumberJSONSerializator numberSerializator = new NumberJSONSerializator();
		
		serializators.put(Byte.class.getName(), numberSerializator);
		classNamesMap.put(Byte.class.getName(), "byte");

		serializators.put(Double.class.getName(), numberSerializator);
		classNamesMap.put(Double.class.getName(), "double");

		serializators.put(Float.class.getName(), numberSerializator);
		classNamesMap.put(Float.class.getName(), "float");

		serializators.put(Integer.class.getName(), numberSerializator);
		classNamesMap.put(Integer.class.getName(), "int");
		
		serializators.put(Long.class.getName(), numberSerializator);
		classNamesMap.put(Long.class.getName(), "long");

		serializators.put(Short.class.getName(), numberSerializator);
		classNamesMap.put(Short.class.getName(), "short");
		
		serializators.put(String.class.getName(), new JSONSerializator<Object, String>() {
			@Override
			public JSONValue serialize(String instance) {
				return new JSONString(instance);
			}
	    });
		classNamesMap.put(String.class.getName(), "string");
		
		serializators.put(Date.class.getName(), new JSONSerializator<Object, Date>() {
			@Override
			public JSONValue serialize(Date instance) {
				JSONObject object = new JSONObject();
				object.put("$date", new JSONString(DATE_TIME_FORMAT.format(instance)));
				return object;
			}
	    });
		classNamesMap.put(Date.class.getName(), "date");
		
		//TODO
//		serializators.put(byte[].class.getName(), new JSONSerializator<Object, byte[]>() {
//			@Override
//			public JSONValue serialize(byte[] instance) {
//				ByteUnits
//				JSONArray array = new JSONArray();
//				for (byte i = 0; i < instance.length; ++i) {
//					array.set(i, new JSONNumber(instance[i]));
//				}
//				return array;
//			}
//	    });
//		classNamesMap.put(byte[].class.getName(), "byte-array");
		
		serializators.put(boolean[].class.getName(), new JSONSerializator<Object, boolean[]>() {
			@Override
			public JSONValue serialize(boolean[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, JSONBoolean.getInstance(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(boolean[].class.getName(), "boolean-array");

		serializators.put(double[].class.getName(), new JSONSerializator<Object, double[]>() {
			@Override
			public JSONValue serialize(double[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, new JSONNumber(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(double[].class.getName(), "double-array");
		
		serializators.put(float[].class.getName(), new JSONSerializator<Object, float[]>() {
			@Override
			public JSONValue serialize(float[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, new JSONNumber(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(float[].class.getName(), "float-array");

		serializators.put(int[].class.getName(), new JSONSerializator<Object, int[]>() {
			@Override
			public JSONValue serialize(int[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, new JSONNumber(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(int[].class.getName(), "int-array");
		
		serializators.put(long[].class.getName(), new JSONSerializator<Object, long[]>() {
			@Override
			public JSONValue serialize(long[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, new JSONNumber(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(long[].class.getName(), "long-array");
		
		serializators.put(short[].class.getName(), new JSONSerializator<Object, short[]>() {
			@Override
			public JSONValue serialize(short[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, new JSONNumber(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(short[].class.getName(), "short-array");
		
		serializators.put(String[].class.getName(), new JSONSerializator<Object, String[]>() {
			@Override
			public JSONValue serialize(String[] instance) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < instance.length; ++i) {
					array.set(i, new JSONString(instance[i]));
				}
				return array;
			}
	    });
		classNamesMap.put(String[].class.getName(), "string-array");
		
		final JSONSerializator<Object, Collection> collectionSerializator = new JSONSerializator<Object, Collection>() {

			@Override
			public JSONValue serialize(Collection instance) {
				JSONObject object = new JSONObject();
				JSONArray array = new JSONArray();
				int index = 0;
				for (Object obj : instance) {
					array.set(index++, toJSON(obj));
				}
				object.put("#array", array);
				return object;
			}
			
		};
		serializators.put(ArrayList.class.getName(), collectionSerializator);
		serializators.put(HashSet.class.getName(), collectionSerializator);
		serializators.put(LinkedHashSet.class.getName(), collectionSerializator);
		
//
//	serializators.put(BigDecimal.class.getName(), new Serializator<Object, BigDecimal>() {
//		@Override
//		public Element serialize(BigDecimal instance, Element classNode, String className) {
//			appendTextNodeToElement(classNode, instance.toString());
//			return classNode;
//		}
//    });
//
	
//	
//	serializators.put(Object[].class.getName(), new Serializator<Object, Object[]>() {
//		@Override
//		public Element serialize(Object[] instance, Element classNode, String className) {
//			if (instance != null) {
//				if (instance.length > 0) {
//					if (!instance.getClass().getName().equals(className)) {
//						classNode.setAttribute("class", getServerClassName(instance[0].getClass().getName()) + "-array");
//					}
//					for (Object child : instance) {
//						classNode.appendChild(toXML(child));
//					}
//				}
//			}
//			return classNode;
//		}
//    });
//	
//	
//	serializators.put(EnumMap.class.getName(), new Serializator<AbstractMap, EnumMap>() {
//		@Override
//		public Element serialize(EnumMap instance, Element classNode, String className) {
//			if (instance == null || instance.isEmpty()) return null;
////			if (instance != null) {
////				Iterator it = instance.keySet().iterator();
////				while (it.hasNext()) {
////					if (instance.get(it.next()) == null) it.remove();
////				}
////			}
//			classNode = mapSerializator.serialize(instance, classNode, className);
//			if (instance != null && !instance.isEmpty()) {
//				classNode.setAttribute("enum-type", getServerClassName(instance.keySet().iterator().next()));
//			}
//			return classNode;
//		}
//	});
//	
//	Serializator<AbstractSet, EnumSet> enumSetSerializator = new Serializator<AbstractSet, EnumSet>() {
//		@Override
//		public Element serialize(EnumSet instance, Element classNode, String className) {
//			classNode.setAttribute("enum-type", getServerClassName(instance.toArray()[0]));
//			StringBuilder sb = new StringBuilder();
//			for (Object child : instance) {
//				sb.append(((Enum)child).name()).append(",");
//			}
//			appendTextNodeToElement(classNode, sb.substring(0, sb.length() - 1));
//			return classNode;
//		}
//    };
//    serializators.put(EnumSet.class.getName(), enumSetSerializator);
//	serializators.put("java.util.RegularEnumSet", enumSetSerializator);
//	serializators.put("java.util.JumboEnumSet", enumSetSerializator);

		final JSONSerializator<Object, Map> mapSerializator = new JSONSerializator<Object, Map>() {
			@Override
			public JSONObject serialize(Map instance) {
				if (instance == null) return null;
				JSONObject object = new JSONObject();
				JSONArray array = new JSONArray();
				int index = 0;
				for (Object obj : instance.entrySet()) {
					Entry entry = (Entry) obj;
					JSONObject entryJsonObject = new JSONObject();
					entryJsonObject.put(escapeBSONKey(entry.getKey().getClass().getName()), toJSON(entry.getKey(), entry.getKey().getClass().getName()));
					Object value = entry.getValue();
					if (value != null) {
						entryJsonObject.put(escapeBSONKey(value.getClass().getName()), toJSON(value, value.getClass().getName()));
					}
					entryJsonObject.put("@class", new JSONString("entry"));
					array.set(index++, entryJsonObject);
				}
				object.put("#array", array);
				return object;
			}
		};
		serializators.put(Map.class.getName(), mapSerializator);
		serializators.put(HashMap.class.getName(), mapSerializator);
		serializators.put(LinkedHashMap.class.getName(), mapSerializator);
	}
	
	protected String escapeBSONKey(String instanceClassName) {
	    String className = classNamesMap.get(instanceClassName);
		if (className == null && instanceClassName.startsWith("[L")) {
			String innerClassName = instanceClassName.substring(2, instanceClassName.length() - 1);
			className = classNamesMap.get(innerClassName);
			if (className == null) className = innerClassName;
			className += "-array";
		}

	    if (className == null) return instanceClassName.replaceAll("\\.", "-").replaceAll("\\$", "_-");
	    return className;
	}
	
//	
//	@SuppressWarnings({"rawtypes"})
//	protected XMLSerializerImpl() {
//		Deserializator deserializator = new CollectionDeserializator<Set>() {
//			@Override
//			public Set makeInstance() {
//				return new HashSet();
//			}
//		};
//		deserializators.put("set", deserializator);
//		deserializators.put(Set.class.getName(), deserializator);
//		deserializators.put(HashSet.class.getName(), deserializator);
//		
//		deserializator = new CollectionDeserializator<LinkedHashSet>() {
//			@Override
//			public LinkedHashSet makeInstance() {
//				return new LinkedHashSet();
//			}
//		};
//		deserializators.put(LinkedHashSet.class.getName(), deserializator);
//		
//		deserializator = new CollectionDeserializator<SortedSet>() {
//			@Override
//			public SortedSet makeInstance() {
//				return new TreeSet();
//			}
//		};
//		deserializators.put("sorted-set", deserializator);
//		deserializators.put(TreeSet.class.getName(), deserializator);
//		
//
//		deserializator = new Deserializator<Object, Boolean>() {
//			@Override
//			public Boolean deserialize(Node node, Boolean instance) {
//				return new Boolean(getTextNodeValue(node));
//			}
//			@Override
//			public Boolean makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("boolean", deserializator);
//		
//		deserializator = new Deserializator<Object, Integer>() {
//			@Override
//			public Integer deserialize(Node node, Integer instance) {
//				return new Integer(getTextNodeValue(node));
//			}
//			@Override
//			public Integer makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("int", deserializator);
//		
//		deserializator = new Deserializator<Object, double[]>() {
//			@Override
//			public double[] deserialize(Node node, double[] instance) {
//				JSONArray array = JSONParser.parseStrict(getTextNodeValue(node)).isArray();
//				double[] result = new double[array.size()];
//				for (int i = 0; i < array.size(); ++i) {
//					result[i] = array.get(i).isNumber().doubleValue();
//				}
//				return result;
//			}
//			@Override
//			public double[] makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("double-array", deserializator);
//		
//		deserializator = new Deserializator<Object, int[]>() {
//			@Override
//			public int[] deserialize(Node node, int[] instance) {
//				JSONArray array = JSONParser.parseStrict(getTextNodeValue(node)).isArray();
//				int[] result = new int[array.size()];
//				for (int i = 0; i < array.size(); ++i) {
//					result[i] = (int) array.get(i).isNumber().doubleValue();
//				}
//				return result;
//			}
//			@Override
//			public int[] makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("int-array", deserializator);
//		
//		deserializator = new Deserializator<Object, Date[]>() {
//			@Override
//			public Date[] deserialize(Node node, Date[] instance) {
//				String arrayStr = getTextNodeValue(node).replaceAll("[\\[\\]]", "");
//				String[] values = arrayStr.split(",");
//				if (values.length == 0) return new Date[0];
//				instance = new Date[values.length];
//				for (int i = 0; i < values.length; ++i) {
//					instance[i] = DATE_TIME_FORMAT.parse(values[i].trim());
//				}
//				return instance;
//			}
//				
//			@Override
//			public Date[] makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("date-array", deserializator);
//		
//		deserializator = new Deserializator<Object, Double>() {
//			@Override
//			public Double deserialize(Node node, Double instance) {
//				return parseDouble(getTextNodeValue(node));
//			}
//			@Override
//			public Double makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("double", deserializator);
//
//		deserializator = new Deserializator<Object, String>() {
//			@Override
//			public String deserialize(Node node, String instance) {
//				return getTextNodeValue(node);
//			}
//			@Override
//			public String makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put("string", deserializator);
//		
//		deserializator = new Deserializator<Object, BigDecimal>() {
//			@Override
//			public BigDecimal deserialize(Node node, BigDecimal instance) {
//				return new BigDecimal(getTextNodeValue(node));
//			}
//			@Override
//			public BigDecimal makeInstance() {
//				return null;
//			}
//		};
//		deserializators.put(BigDecimal.class.getName(), deserializator);
//		deserializators.put("big-decimal", deserializator);
//		

//		
//		deserializator = new Deserializator<AbstractMap, EnumMap>() {
//			@Override
//			public EnumMap makeInstance() {
//				return null;
//			}
//			@SuppressWarnings("unchecked")
//			@Override
//			public EnumMap deserialize(Node node, EnumMap instance) {
//				if (node.hasChildNodes() == false) {
//					LOGGER.log(Level.WARNING, "EnumMap instance without elements: " + node.getNodeValue());
//					return null;
//				}
//				
//				Map enumMap = new HashMap();
//				
//				String enumType = node.getAttributes().getNamedItem("enum-type").getNodeValue();
//				Deserializator deserializator = deserializators.get(enumType);
//				
//				if (deserializator == null) {
//					LOGGER.log(Level.SEVERE, "Couldn't find deserializator for type: " + enumType);
//					return null;
//				}
//				
//				if (!(deserializator instanceof EnumDeserializator)) {
//					LOGGER.log(Level.SEVERE, "Wrong type of deserializtor for type: " + enumType);
//					return null;
//				}
//				
//				EnumDeserializator enumDeserializator = (EnumDeserializator)deserializator;
//				
//				NodeList nodeList = node.getChildNodes();
//				for (int i = 0; i < nodeList.getLength(); ++i) {
//					Node entryNode = nodeList.item(i);
//					if (entryNode.getNodeType() == Node.ELEMENT_NODE && "entry".equals(entryNode.getNodeName())) {
//						Node keyNode = null, valueNode = null;
//						NodeList entryNodeList = entryNode.getChildNodes();
//						int j = 0;
//						while (j < entryNodeList.getLength()) {
//							if (entryNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
//								if (keyNode == null) keyNode = entryNodeList.item(j);
//								else {
//									valueNode = entryNodeList.item(j);
//									break;
//								}
//							}
//							++j;
//						}
//						enumMap.put(enumDeserializator.deserialize(keyNode, null), fromXML(valueNode, valueNode.getNodeName()));
//					}
//				}
//				return new EnumMap(enumMap);
//			}
//		};
//		deserializators.put(EnumMap.class.getName(), deserializator);
//		
//		deserializator = new Deserializator<AbstractSet, EnumSet>() {
//			@Override
//			public EnumSet makeInstance() {
//				return null;
//			}
//			@SuppressWarnings("unchecked")
//			@Override
//			public EnumSet deserialize(Node node, EnumSet instance) {
//				if (node.hasChildNodes() == false) {
//					LOGGER.log(Level.WARNING, "EnumSet instance without elements: " + node.getNodeValue());
//					return null;
//				}
//				
//				List enumList = new ArrayList();
//				
//				String enumType = node.getAttributes().getNamedItem("enum-type").getNodeValue();
//				Deserializator deserializator = deserializators.get(enumType);
//				
//				if (deserializator == null) {
//					LOGGER.log(Level.SEVERE, "Couldn't find deserializator for type: " + enumType);
//					return null;
//				}
//				
//				if (!(deserializator instanceof EnumDeserializator)) {
//					LOGGER.log(Level.SEVERE, "Wrong type of deserializtor for type: " + enumType);
//					return null;
//				}
//				
//				EnumDeserializator enumDeserializator = (EnumDeserializator)deserializator;
//				
//				String nodeValue = getTextNodeValue(node);
//				for (String enumName : nodeValue.split(",")) {
//					enumList.add(enumDeserializator.valueOf(enumName.trim()));
//				}
//
//				return EnumSet.copyOf(enumList);
//			}
//		};
//		deserializators.put(EnumSet.class.getName(), deserializator);
//		
//	}
//	
//	public static String getTextNodeValue(Node n) {
//		return TEXT_NODE_IMPL.getNodeValue(n);
//	}
//
//	public static Double parseDouble(String text) {
//		if ("NaN" == text) return NaN;
//		return Double.parseDouble(text);
//	}
//

	protected boolean getBoolean(JSONValue value) {
		return value.isBoolean().booleanValue();
	}

	protected Character getChar(JSONValue value) {
		return value.isString().stringValue().charAt(0);
	}

	protected String getString(JSONValue value) {
		return value.isString().stringValue();
	}
	
	protected Number getNumber(JSONValue value) {
		return value.isNumber().doubleValue();
	}

	@SuppressWarnings("rawtypes")
	protected final HashMap<String, JSONSerializator> serializators = new HashMap<String, JSONSerializator>();
	
	@Override
	public String toString(Object object) {
		JSONValue json = toJSON(object);
		if (json == null) return null;
//		if (json.isObject() != null && !json.isObject().containsKey("@class")) {
//			json.isObject().put("@class", new JSONString(getServerClassName(object)));
//		}
		return json.toString();
	}
	
	@Override
	public JSONValue toJSON(Object object) {
		if (object == null) return null;
		String className = null;
		if (object instanceof Boolean || object instanceof Double || object instanceof String || object instanceof Date) {
			className = object.getClass().getName();
		}
		return toJSON(object, className);
	}
	
	protected JSONString toJSON(String value) {
		if (value == null) return null;
		return new JSONString(value);
	}

	protected JSONBoolean toJSON(Boolean value) {
		if (value == null) return null;
		return JSONBoolean.getInstance(value);
	}
	
	protected JSONNumber toJSON(Number value) {
		if (value == null) return null;
		return new JSONNumber(value.doubleValue());
	}

	@SuppressWarnings("rawtypes")
	protected JSONString toJSON(Enum value) {
		if (value == null) return null;
		return new JSONString(value.toString());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected JSONValue toJSON(Object object, String className) {
		if (object == null) return null;
		String objectClassName = className == null ? object.getClass().getName() : className;
		if (className != null && className.startsWith("[L")) objectClassName = Object[].class.getName();
		JSONSerializator serializator = serializators.get(objectClassName);
		JSONValue value = null;
		if (serializator != null) {
			value = serializator.serialize(object);
		} else if (object instanceof Enum<?>) {
			value = new JSONString(((Enum<?>)object).name());
		} else {
			throw new RuntimeException("Can't serialize this class:" + object.getClass().getName() + " (provided className = " + className + ")");
		}
		if (!object.getClass().getName().equals(className)) {
			value = setClassName(value, getServerClassName(object));
		}
		return value;
	}
	
	protected JSONValue setClassName(JSONValue value, String className) {
		JSONObject jsonObject = value.isObject();
		if (jsonObject == null) {
			jsonObject = new JSONObject();
			if (value.isArray() != null) {
				jsonObject.put("#array", value);
			} else {
				jsonObject.put("#value", value);
			}
		}
		jsonObject.put("@class", new JSONString(className));
		return jsonObject;
	}
	
	@SuppressWarnings("rawtypes")
	protected JSONObject toJSON(Collection collection, String className) {
		if (collection == null) return null;
		JSONObject jsonObject = new JSONObject();
		if (!collection.getClass().getName().equals(className)) {
			if (collection instanceof List) {
				jsonObject.put("@class", new JSONString("list"));
			} else {
				jsonObject.put("@class", new JSONString(collection.getClass().getName()));
			}
		}
		JSONArray array = new JSONArray();
		int index = 0;
		for (Object collectionChild : collection) {
			array.set(index++, toJSON(collectionChild));
		}
		jsonObject.put("#array", array);
		return jsonObject;
	}
	
//	
//	protected Element createClassNode(Object instance) {
//		try {
//			return DOCUMENT.createElement(getServerClassName(instance));
//		} catch(DOMNodeException e) {
//			LOGGER.log(Level.SEVERE, getServerClassName(instance), e);
//			throw e;
//		}
//	}
//	
	protected final HashMap<String, String> classNamesMap = new HashMap<String, String>();
//	
	@Override
	public String getServerClassName(Object instance) {
		String instanceClassName = instance.getClass().getName();
	    String className = classNamesMap.get(instanceClassName);
		if (className == null && instanceClassName.startsWith("[L")) {
			String innerClassName = instanceClassName.substring(2, instanceClassName.length() - 1);
			className = classNamesMap.get(innerClassName);
			if (className == null) className = innerClassName;
			className += "-array";
		}

	    if (className == null) return instanceClassName;
	    return className;
	}
//
//	protected static final Document DOCUMENT = XMLParser.createDocument();
//
//	public static Element createDataElement(String className) {
//		Element dataElement = DOCUMENT.createElement("data");
//		dataElement.setAttribute("class", className);
//		return dataElement;
//	}
//
//	protected static void appendTextNodeToElement(Node node, String textNodeName, String textNodeData) {
//		Node textNode = DOCUMENT.createElement(textNodeName);
//		textNode.appendChild(DOCUMENT.createTextNode(textNodeData));
//		node.appendChild(textNode);
//	}
//
//	protected static void appendTextAttributeNodeToElement(Element element, String textNodeName, String textNodeData) {
//		element.setAttribute(textNodeName, textNodeData);
//	}
//
//	protected static void appendTextNodeToElement(Node node, String textNodeData) {
//		node.appendChild(DOCUMENT.createTextNode(textNodeData));
//	}
//
//	public static void appendPrimitiveWrapperAttributeNodeToElement(Element element, String nodeName, Object primitiveWrapperNodeData) {
//		if (primitiveWrapperNodeData != null) {
//			appendTextAttributeNodeToElement(element, nodeName, primitiveWrapperNodeData.toString());
//		}
//	}
//
//	public static void appendPrimitiveWrapperNodeToElement(Node node, String nodeName, Object primitiveWrapperNodeData) {
//		if (primitiveWrapperNodeData != null) {
//			appendTextNodeToElement(node, nodeName, primitiveWrapperNodeData.toString());
//		}
//	}
//
//	protected static void appendPrimitiveIntegerArrayNodeToElement(Node node, String nodeName, int[] array) {
//		if (array != null) {
//			appendTextNodeToElement(node, nodeName, Arrays.toString(array));
//		}
//	}
//
//	protected static void appendPrimitiveWrapperNodeToElement(Node node, Object primitiveWrapperNodeData) {
//		if (primitiveWrapperNodeData != null) {
//			appendTextNodeToElement(node, primitiveWrapperNodeData.toString());
//		}
//	}
//	
//	protected static void appendDateNodeToElement(Node node, Date dateNodeData) {
//		if (dateNodeData != null) {
//			appendTextNodeToElement(node, DATE_TIME_FORMAT.format(dateNodeData));
//		}
//	}
//
//	protected void appendSerializableToObject(JSONObject json, String fieldName, String fieldTypeName, Object object) {
//		if (object != null) {
//			JSONValue value = toJSON(object, object.getClass().getName());
//			
//			if (tSerializableElement != null) element.appendChild(tSerializableElement);
//		}
//	}
//	
//	protected void appendToObject(String fieldName, String fieldTypeName, Object object) {
//		if (object != null) {
//			if (object instanceof List) {
//				Element collectionElement = DOCUMENT.createElement(fieldName);
//				if (!object.getClass().getName().equals(fieldTypeName)) {
//					collectionElement.setAttribute("class", "list");
//				}
//				JSONArray array = new JSONArray();
//				Collection<?> collection = (Collection<?>) object;
//				int index = 0;
//				for (Object collectionChild : collection) {
//					array.set(index++, toJSON(collectionChild));
//				}
//				element.appendChild(collectionElement);
//			} else {
//				appendSerializableToObject(json, fieldName, fieldTypeName, object);
//			}
//		}
//	}
//	
//	protected void appendArrayNodeToElement(Element element, String nodeName, Object[] objects) {
//		if (objects != null) {
//			Element arrayElement = DOCUMENT.createElement(nodeName);
//			for (Object object : objects) {
//				arrayElement.appendChild(toXML(object));
//			}
//			element.appendChild(arrayElement);
//		}
//	}
//	
//	protected String getEnumValue(Enum<?> enm) {
//		return enm == null ? null : enm.name();
//	}
	
	protected void append(JSONObject json, String fieldName, JSONValue value) {
		if (value != null) {
			json.put(fieldName, value);
		}
	}

}
