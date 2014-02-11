package li.tau.tserializer.rebind;

import li.tau.tserializer.client.annotation.Mode;
import li.tau.tserializer.client.annotation.TSerializerAlias;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;

public class JSONClassDeserializationUnit {
	
	public abstract class JSONFieldDeserializationUnit {
	
		private void writePrefix(JField field) {
			String fieldName = (field.isAnnotationPresent(TSerializerAlias.class) && (
					field.getAnnotation(TSerializerAlias.class).mode() == Mode.BOTH ||
					field.getAnnotation(TSerializerAlias.class).mode() == Mode.DESERIALIZATION))?
					field.getAnnotation(TSerializerAlias.class).value() : field.getName();
			sw.println("dispatcher.put(\"" + fieldName + "\", new Setter() {");
				sw.indent();
				sw.println("public void set(JSONValue json, " + classType.getQualifiedSourceName() + " instance) {");
					sw.indent();
		}
		
		private void writeSuffix() {
					sw.outdent();
				sw.println("}");
				sw.outdent();
			sw.println("});");
		}
		
		public void write(JField field) {
			writePrefix(field);
			writeBody(field);
			writeSuffix();
		}
		
		protected abstract void writeBody(JField field);
	}
	
	public class DeserializableUnit extends JSONFieldDeserializationUnit {
		@Override
		protected void writeBody(JField field) {
			sw.println(String.format("instance.%1$s = (%2$s)fromJSON(json, \"%3$s\");", field.getName(), field.getType().getQualifiedSourceName(), field.getType().getQualifiedBinaryName()));
		}
	}
	
	public class StringUnit extends JSONFieldDeserializationUnit {
		@Override
		protected void writeBody(JField field) {
			sw.println("instance." + field.getName() + " = getString(json);");
		}
	}
	
	public class BooleanUnit extends JSONFieldDeserializationUnit {
		@Override
		protected void writeBody(JField field) {
			sw.println("instance." + field.getName() + " = getBoolean(json);");
		}
	}
	
	public class CharUnit extends JSONFieldDeserializationUnit {
		@Override
		protected void writeBody(JField field) {
			sw.println("instance." + field.getName() + " = getChar(json);");
		}
	}

	public class NumberUnit extends JSONFieldDeserializationUnit {
		
		private final String methodPrefix;
		public NumberUnit(String methodPrefix) {
			this.methodPrefix = methodPrefix;
		}
		
		protected void writeBody(JField field) {
			sw.println("instance." + field.getName() + " = getNumber(json)." + methodPrefix + "Value();");
		};

	}
	
	
//	public class ArrayUnit extends JSONFieldDeserializationUnit {
//
//		@Override
//		protected void writeBody(JField field) {
//			if (fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Integer.TYPE.getName())) {
//				deserializationUnit.writePrimitiveIntegerArrayDeserializator(classType, field, sw);
//			} else if (fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Double.TYPE.getName())) {
//				deserializationUnit.writePrimitiveDoubleArrayDeserializator(classType, field, sw);
//			} else if (fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Date.class.getName())) {
//				deserializationUnit.writeDateArrayDeserializator(classType, field, sw);
//			} else {
//				deserializationUnit.writeArrayDeserializator(classType, field, sw);
//			}
//		}
//		
//	}	

	public class ArrayUnit extends JSONFieldDeserializationUnit {

		protected void writeBody(JField field) {
			sw.println(String.format("instance.%1$s = (%2$s)fromJSON(json, \"%3$s\");", field.getName(), field.getType().getQualifiedSourceName(), JSONSerializerGenerator.getArrayClassName(field.getType())));
//			sw.println("if (json.isObject() != null && json.isObject().get(\"@class\").isString().stringValue() != null && deserializators.containsKey(json.isObject().get(\"@class\").isString().stringValue())) {");
//				sw.indent();
//				sw.println("instance." + field.getName() + " = (" + componentTypeName + "[]) fromJSON(json, json.isObject().get(\"@class\").isString().stringValue());");
//				sw.println("return;");
//				sw.outdent();
//			sw.println(String.format("} else if (deserializators.containsKey(\"%1$s\")) {", field.getType().getQualifiedSourceName()));
//				sw.indent();
//				sw.println(String.format("instance." + field.getName() + " = (" + componentTypeName + "[]) fromJSON(json, \"%1$s\");", field.getType().getQualifiedSourceName()));
//				sw.println("return;");
//				sw.outdent();
//			sw.println("}");
//		
//			sw.println("JSONArray array = json.isArray();");
//			sw.println("if (array == null) array = json.isObject().get(\"#array\").isArray();");
//			sw.println("ArrayList<" +  componentTypeName + "> tempList = new ArrayList<" + componentTypeName + ">();");
//			sw.println("for (int i = 0; i < array.size(); ++i) {");
//				sw.indent();
//				sw.println("tempList.add((" + componentTypeName + ")fromJSON(array.get(i)));");
//				sw.outdent();
//			sw.println("}");
//			sw.println("instance." + field.getName() + " = new " + componentTypeName + "[tempList.size()];");
//			sw.println("tempList.toArray(instance." + field.getName() +");");
		}
		
	}
	
	public class ListUnit extends JSONFieldDeserializationUnit {

		private final String componentTypeName;
		
		public ListUnit(JType componentType) {
			if (componentType.isTypeParameter() != null) {
				this.componentTypeName = componentType.isTypeParameter().getBaseType().getQualifiedSourceName(); 
			} else {
				this.componentTypeName = componentType.getQualifiedSourceName();
			}
		}

		protected void writeBody(JField field) {
			sw.println("if (json.isObject() != null && json.isObject().get(\"@class\").isString().stringValue() != null && deserializators.containsKey(json.isObject().get(\"@class\").isString().stringValue())) {");
				sw.indent();
				sw.println("instance." + field.getName() + " = (" + componentTypeName + "[]) fromJSON(json, json.isObject().get(\"@class\").isString().stringValue());");
				sw.println("return;");
				sw.outdent();
			sw.println(String.format("} else if (deserializators.containsKey(\"%1$s\")) {", field.getType().getQualifiedSourceName()));
				sw.indent();
				sw.println(String.format("instance." + field.getName() + " = (" + componentTypeName + "[]) fromJSON(json, \"%1$s\");", field.getType().getQualifiedSourceName()));
				sw.println("return;");
				sw.outdent();
			sw.println("}");
		
			sw.println("JSONArray array = json.isArray();");
			sw.println("if (array == null) array = json.isObject().get(\"#array\").isArray();");
			sw.println("ArrayList<" +  componentTypeName + "> tempList = new ArrayList<" + componentTypeName + ">();");
			sw.println("for (int i = 0; i < array.size(); ++i) {");
				sw.indent();
				sw.println("tempList.add((" + componentTypeName + ")fromJSON(array.get(i)));");
				sw.outdent();
			sw.println("}");
			sw.println("instance." + field.getName() + " = new " + componentTypeName + "[tempList.size()];");
			sw.println("tempList.toArray(instance." + field.getName() +");");
		}
	}
	

	
	public class EnumUnit extends JSONFieldDeserializationUnit {
		@Override
		protected void writeBody(JField field) {
			sw.println("instance." + field.getName() + " = " + field.getType().getQualifiedSourceName() + ".valueOf(getString(json));");
		}
	}
	
//	
//	void writeDeserializableArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		writeSetterPrefix(classType, field, sw);
//		sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getParameterizedQualifiedSourceName() + ">();");
//		sw.println("NodeList nodeList = node.getChildNodes();");
//		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
//			sw.indent();
//			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
//				sw.indent();
//				sw.println("instance." + field.getName() + ".add((" 
//							+ field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName()
//							+ ")fromXML(nodeList.item(i), nodeList.item(i).getNodeName()));");
//				sw.outdent();
//			sw.println("}");
//			sw.outdent();
//		sw.println("}");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writePrimitiveWrapperArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		if (field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName().equals(java.lang.String.class.getName()) == false) {
//			throw new RuntimeException("only string as list<> type possible in deserializator!");
//		}
//		writeSetterPrefix(classType, field, sw);
//		sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName() + ">();");
//		sw.println("NodeList nodeList = node.getChildNodes();");
//		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
//			sw.indent();
//			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
//				sw.indent();
//				sw.println("instance." + field.getName() + ".add(getTextNodeValue(nodeList.item(i)));"); 
//				sw.outdent();
//			sw.println("}");
//			sw.outdent();
//		sw.println("}");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writePrimitiveDoubleArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		writeSetterPrefix(classType, field, sw);
//		sw.println("instance." + field.getName() + " = (double[])fromXML(node, \"double-array\");");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writePrimitiveIntegerArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		writeSetterPrefix(classType, field, sw);
//		sw.println("instance." + field.getName() + " = (int[])fromXML(node, \"int-array\");");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writeDateArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		writeSetterPrefix(classType, field, sw);
//		sw.println("instance." + field.getName() + " = (Date[])fromXML(node, \"date-array\");");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writeArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		String componentClassName = field.getType().isArray().getComponentType().getQualifiedSourceName();
//		if (field.getType().isArray().getComponentType().isTypeParameter() != null) {
//			componentClassName = field.getType().isArray().getComponentType().isTypeParameter().getBaseType().getQualifiedSourceName(); 
//		}
//		writeSetterPrefix(classType, field, sw);
//		sw.println("if (node.hasAttributes() && node.getAttributes().getNamedItem(\"class\") != null && deserializators.containsKey(node.getAttributes().getNamedItem(\"class\").getNodeValue())) {");
//			sw.indent();
//			sw.println("instance." + field.getName() + " = (" + componentClassName + "[]) fromXML(node, node.getAttributes().getNamedItem(\"class\").getNodeValue());");
//			sw.println("return;");
//			sw.outdent();
//		sw.println("}");
//		
//		sw.println("NodeList nodeList = node.getChildNodes();");
//		sw.println("ArrayList<" +  componentClassName + "> tempList = new ArrayList<" + componentClassName + ">();");
//		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
//			sw.indent();
//			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
//				sw.indent();
//				sw.println("tempList.add((" + componentClassName + ")fromXML(nodeList.item(i), nodeList.item(i).getNodeName()));");
//				sw.outdent();
//			sw.println("}");
//			sw.println("instance." + field.getName() + " = new " + componentClassName + "[tempList.size()];");
//			sw.println("tempList.toArray(instance." + field.getName() +");");
//			sw.outdent();
//		sw.println("}");
//		writeSetterSuffix(classType, field, sw);
//	}
//
//	void writeRawArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		writeSetterPrefix(classType, field, sw);
//		sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "();");
//		sw.println("NodeList nodeList = node.getChildNodes();");
//		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
//			sw.indent();
//			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
//				sw.indent();
//				sw.println("instance." + field.getName() + ".add(fromXML(nodeList.item(i), nodeList.item(i).getNodeName()));");
//				sw.outdent();
//			sw.println("}");
//			sw.outdent();
//		sw.println("}");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writeImplicitDeserializableArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		writeSetterPrefix(classType, field, sw);
//		sw.println("if (instance." + field.getName() + " == null) {");
//			sw.indent();
//			sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName() + ">();");
//			sw.outdent();
//		sw.println("}");
//		sw.println("instance." + field.getName() + ".add((" 
//							+ field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName()
//							+ ")fromXML(node, node.getNodeName()));");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
//	void writeImplicitPrimitiveWrapperArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		if (field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName().equals(java.lang.String.class.getName()) == false) {
//			throw new RuntimeException("only string as list<> type possible in deserializator!");
//		}
//		writeSetterPrefix(classType, field, sw);
//		sw.println("if (instance." + field.getName() + " == null) {");
//			sw.indent();
//			sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName() + ">();");
//			sw.outdent();
//		sw.println("}");
//		sw.println("instance." + field.getName() + ".add(getTextNodeValue(node));");
//		writeSetterSuffix(classType, field, sw);
//	}
//	
	
	public class TypeParameterUnit extends JSONFieldDeserializationUnit {
		@Override
		protected void writeBody(JField field) {
			String castClassName = field.getType().isTypeParameter().getBaseType().getQualifiedSourceName();
			sw.println("instance." + field.getName() + " = (" + castClassName + ") fromJSON(json);");
//			} else if (field.getType().isParameterized() != null) {
//				castClassName = field.getType().isParameterized().getBaseType().getQualifiedSourceName();
//				sw.println("if (node.hasAttributes() && node.getAttributes().getNamedItem(\"class\") != null) {");
//					sw.indent();
//					sw.println("instance." + field.getName() + " = (" + castClassName + ") fromXML(node, node.getAttributes().getNamedItem(\"class\").getNodeValue());");
//					sw.outdent();
//				sw.println("} else {");
//					sw.indent();
//					sw.println("instance." + field.getName() + " = (" + castClassName + ") fromXML(node, \"" + castClassName + "\");");
//					sw.outdent();
//				sw.println("}");
//			}
		}
	}

	private final JClassType classType;
	private final SourceWriter sw;

	public JSONClassDeserializationUnit(JClassType classType, SourceWriter sw) {
		this.classType = classType;
		this.sw = sw;
	}
	
}
