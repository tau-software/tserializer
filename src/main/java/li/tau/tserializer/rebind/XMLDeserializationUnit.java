package li.tau.tserializer.rebind;

import static li.tau.tserializer.rebind.SerializerGenerator.getSetterName;
import static li.tau.tserializer.rebind.SerializerGenerator.hasSetter;
import li.tau.tserializer.client.annotation.Mode;
import li.tau.tserializer.client.annotation.TSerializerAlias;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.user.rebind.SourceWriter;

public class XMLDeserializationUnit {
	
	private void writeSetterPrefix(JClassType classType, JField field, SourceWriter sw) {
		String fieldName = (field.isAnnotationPresent(TSerializerAlias.class) && (
				field.getAnnotation(TSerializerAlias.class).mode() == Mode.BOTH ||
				field.getAnnotation(TSerializerAlias.class).mode() == Mode.DESERIALIZATION))?
				field.getAnnotation(TSerializerAlias.class).value():
					field.getName();
		sw.println("dispatcher.put(\"" + fieldName + "\", new Setter() {");
			sw.indent();
			sw.println("public void set(Node node, " + classType.getQualifiedSourceName() + " instance) {");
				sw.indent();
	}
	
	private void writeSetterSuffix(JClassType classType, JField field, SourceWriter sw) {
				sw.outdent();
			sw.println("}");
			sw.outdent();
		sw.println("});");
	}

	void writeStringDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "getTextNodeValue(node)");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeBooleanDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "Boolean.parseBoolean(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeByteDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "Byte.parseByte(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeCharacterDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "getTextNodeValue(node).charAt(0)");
		writeSetterSuffix(classType, field, sw);
	}

	public void writeDoubleDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "parseDouble(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeFloatDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "Float.parseFloat(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeLongDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "Long.parseLong(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeShortDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "Short.parseShort(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}
	
	public void writeIntegerDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "Integer.parseInt(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}

	public void writeEnumDeserializator(JClassType classType, JField field,	SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, field.getType().getQualifiedSourceName() + ".valueOf(getTextNodeValue(node))");
		writeSetterSuffix(classType, field, sw);
	}

	public void writeDeserializableDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "(" + field.getType().getQualifiedSourceName()
				+ ")fromXML(node, \"" + XMLSerializerGenerator.getServerClassName(field.getType()) + "\")");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writeDeserializableArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		String arrayType = java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getParameterizedQualifiedSourceName() + ">";
		sw.println(arrayType + " array = new " + arrayType + "();");
		sw.println("NodeList nodeList = node.getChildNodes();");
		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
			sw.indent();
			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
				sw.indent();
				sw.println("array.add((" 
							+ field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName()
							+ ")fromXML(nodeList.item(i), nodeList.item(i).getNodeName()));");
				sw.outdent();
			sw.println("}");
			sw.outdent();
		sw.println("}");
		writeAssignExpression(field, sw, "array");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writePrimitiveWrapperArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
		if (field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName().equals(java.lang.String.class.getName()) == false) {
			throw new RuntimeException("only string as list<> type possible in deserializator!");
		}
		writeSetterPrefix(classType, field, sw);
		sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName() + ">();");
		sw.println("NodeList nodeList = node.getChildNodes();");
		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
			sw.indent();
			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
				sw.indent();
				sw.println("instance." + field.getName() + ".add(getTextNodeValue(nodeList.item(i)));"); 
				sw.outdent();
			sw.println("}");
			sw.outdent();
		sw.println("}");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writePrimitiveDoubleArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "(double[])fromXML(node, \"double-array\")");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writePrimitiveIntegerArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "(int[])fromXML(node, \"int-array\")");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writeDateArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		writeAssignExpression(field, sw, "(Date[])fromXML(node, \"date-array\")");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writeArrayDeserializator(JClassType classType, JField field, SourceWriter sw) {
		String componentClassName = field.getType().isArray().getComponentType().getQualifiedSourceName();
		if (field.getType().isArray().getComponentType().isTypeParameter() != null) {
			componentClassName = field.getType().isArray().getComponentType().isTypeParameter().getBaseType().getQualifiedSourceName(); 
		}
		writeSetterPrefix(classType, field, sw);
		sw.println("if (node.hasAttributes() && node.getAttributes().getNamedItem(\"class\") != null && deserializators.containsKey(node.getAttributes().getNamedItem(\"class\").getNodeValue())) {");
			sw.indent();
			writeAssignExpression(field, sw, "(" + componentClassName + "[]) fromXML(node, node.getAttributes().getNamedItem(\"class\").getNodeValue())");
			sw.println("return;");
			sw.outdent();
		sw.println("}");
		
		sw.println("NodeList nodeList = node.getChildNodes();");
		sw.println("ArrayList<" +  componentClassName + "> tempList = new ArrayList<" + componentClassName + ">();");
		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
			sw.indent();
			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
				sw.indent();
				sw.println("tempList.add((" + componentClassName + ")fromXML(nodeList.item(i), nodeList.item(i).getNodeName()));");
				sw.outdent();
			sw.println("}");
			writeAssignExpression(field, sw, "tempList.toArray(new " + componentClassName + "[tempList.size()])");
			sw.outdent();
		sw.println("}");
		writeSetterSuffix(classType, field, sw);
	}

	void writeRawArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "();");
		sw.println("NodeList nodeList = node.getChildNodes();");
		sw.println("for (int i = 0; i < nodeList.getLength(); ++i) {");
			sw.indent();
			sw.println("if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {");
				sw.indent();
				sw.println("instance." + field.getName() + ".add(fromXML(nodeList.item(i), nodeList.item(i).getNodeName()));");
				sw.outdent();
			sw.println("}");
			sw.outdent();
		sw.println("}");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writeImplicitDeserializableArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		sw.println("if (instance." + field.getName() + " == null) {");
			sw.indent();
			sw.println("instance." + field.getName() + " = new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName() + ">();");
			sw.outdent();
		sw.println("}");
		sw.println("instance." + field.getName() + ".add((" 
							+ field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName()
							+ ")fromXML(node, node.getNodeName()));");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writeImplicitPrimitiveWrapperArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
		if (field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName().equals(java.lang.String.class.getName()) == false) {
			throw new RuntimeException("only string as list<> type possible in deserializator!");
		}
		writeSetterPrefix(classType, field, sw);
		sw.println("if (instance." + field.getName() + " == null) {");
			sw.indent();
			writeAssignExpression(field, sw, "new " + java.util.ArrayList.class.getName() + "<" + field.getType().isParameterized().getTypeArgs()[0].getQualifiedSourceName() + ">()");
			sw.outdent();
		sw.println("}");
		sw.println("instance." + field.getName() + ".add(getTextNodeValue(node));");
		writeSetterSuffix(classType, field, sw);
	}
	
	void writeImplicitRawArrayListDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		sw.println("if (instance." + field.getName() + " == null) {");
			sw.indent();
			writeAssignExpression(field, sw, "new " + java.util.ArrayList.class.getName() + "()");
			sw.outdent();
		sw.println("}");
		sw.println("instance." + field.getName() + ".add(fromXML(node, node.getNodeName()));");
		writeSetterSuffix(classType, field, sw);
	}
	
//	public void writeMapDeserializator(JClassType classType, JField field, SourceWriter sw) {
//		// TODO Auto-generated method stub
//		
//	}
	
	private void writeAssignExpression(JField field, SourceWriter sw, String expression) {
		if (hasSetter(field)) {
			sw.println("instance." + getSetterName(field) + "(" + expression + ");");
		} else {
			sw.println("instance." + field.getName() + " = " + expression + ";");
		}
	}

	public void writeParameterizedDeserializableDeserializator(JClassType classType, JField field, SourceWriter sw) {
		writeSetterPrefix(classType, field, sw);
		String castClassName = "";
		if (field.getType().isTypeParameter() != null) {
			castClassName = field.getType().isTypeParameter().getBaseType().getQualifiedSourceName();
			sw.println("if (!node.hasAttributes() || node.getAttributes().getNamedItem(\"class\") == null) {");
				sw.indent();
				sw.println("throw new RuntimeException(\"Need more information about type of this field: " + field.getName() + " in class " + classType.getQualifiedSourceName() + "\");");
				sw.outdent();
			sw.println("}");
			writeAssignExpression(field, sw, "(" + castClassName + ") fromXML(node, node.getAttributes().getNamedItem(\"class\").getNodeValue())");
		} else if (field.getType().isParameterized() != null) {
			castClassName = field.getType().isParameterized().getBaseType().getQualifiedSourceName();
			sw.println("if (node.hasAttributes() && node.getAttributes().getNamedItem(\"class\") != null) {");
				sw.indent();
				writeAssignExpression(field, sw, "(" + castClassName + ") fromXML(node, node.getAttributes().getNamedItem(\"class\").getNodeValue())");
				sw.outdent();
			sw.println("} else {");
				sw.indent();
				writeAssignExpression(field, sw, "(" + castClassName + ") fromXML(node, \"" + castClassName + "\")");
				sw.outdent();
			sw.println("}");
		}
		writeSetterSuffix(classType, field, sw);
	}

}
