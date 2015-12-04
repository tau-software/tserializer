package li.tau.serializer.rebind;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;

import li.tau.serializer.client.Serializer;
import li.tau.serializer.client.TextNodeImpl;
import li.tau.serializer.client.annotation.Mode;
import li.tau.serializer.client.annotation.TDeserializator;
import li.tau.serializer.client.annotation.TSerializable;
import li.tau.serializer.client.annotation.TSerializator;
import li.tau.serializer.client.annotation.TSerializerAlias;
import li.tau.serializer.client.annotation.TSerializerImplicit;
import li.tau.serializer.client.annotation.TSerializerImplicitCollection;
import li.tau.serializer.client.annotation.TSerializerOmitField;
import li.tau.serializer.client.xml.XMLSerializerImpl;

import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;

public class XMLSerializerGenerator extends SerializerGenerator {
	
	private final XMLDeserializationUnit deserializationUnit = new XMLDeserializationUnit();
	
	@Override
	protected void addImports(ClassSourceFileComposerFactory classSourceFileComposerFactory) {
		super.addImports(classSourceFileComposerFactory);
		classSourceFileComposerFactory.addImport("com.google.gwt.xml.client.*");
		classSourceFileComposerFactory.addImport("com.google.gwt.json.client.*");
		classSourceFileComposerFactory.addImport("li.tau.serializer.client.xml.*");

		classSourceFileComposerFactory.addImport(TextNodeImpl.class.getName());
	}

	@Override
	protected Class<? extends Serializer> getSuperClassSerializer() {
		return XMLSerializerImpl.class;
	}
	
	@Override
	protected void writeClassesDeserializators() {
		sw.println("XMLDeserializator deserializator;");
		sw.println();
		super.writeClassesDeserializators();
	}

	@Override
	protected void writeClassDeserializator(JClassType classType) {
		if (classType.isAnnotationPresent(TDeserializator.class)) {
			writeExternalDeserializator(classType);
			return;
		}
		if (classType.isEnum() != null) {
			writeEnumDeserializator(classType.isEnum());
			return;
		}

		sw.println("deserializator = new XMLDeserializator<" + classType.getSuperclass().getQualifiedSourceName() + ", " + classType.getQualifiedSourceName() + ">() {");
			sw.indent();
			sw.println("{");
				sw.indent();

				String implicitFieldName = getImplicitFieldName(classType);
				if (implicitFieldName != null && isAccessibleImplicitField(classType, Mode.DESERIALIZATION)) {
					sw.println("implicitFieldName = \"" + implicitFieldName + "\";");
				}
				
				String implicitCollectionWithoutItemNameFieldName = getImplicitCollectionWithoutItemNameFieldName(classType, Mode.DESERIALIZATION);
				if (implicitCollectionWithoutItemNameFieldName != null) {
					sw.println("implicitCollectionFieldName = \"" + implicitCollectionWithoutItemNameFieldName + "\";");
				}
				
				Map<String, String> implicitCollectionWithItemFieldNameMap = getImplicitCollectionWithItemFieldNameMap(classType, Mode.DESERIALIZATION);
				if (implicitCollectionWithItemFieldNameMap.isEmpty() == false) {
					sw.println("implicitCollectionItemFieldNameMap = new HashMap<String, String>();");
					for (Entry<String, String> entry : implicitCollectionWithItemFieldNameMap.entrySet()) {
						sw.println("implicitCollectionItemFieldNameMap.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
					}
				}
				
				if (classType.getSuperclass() != null && !classType.getSuperclass().getQualifiedSourceName().equals(java.lang.Object.class.getName())) {
					sw.println("superClassDeserializator = deserializators.get(\"" + getServerClassName(classType.getSuperclass()) + "\");");
				}
				
				for (JField field : classType.getFields()) {
					JType fieldType = field.getType();
					if (field.isPublic()
						&& field.isFinal() == false
						&& field.isStatic() == false
						&& field.isTransient() == false
						&& field.isEnumConstant() == null
						&& (field.isAnnotationPresent(TSerializerOmitField.class) == false
							|| field.getAnnotation(TSerializerOmitField.class).value() == Mode.SERIALIZATION)) {
						
						if (fieldType.isArray() != null) {
							if (fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Integer.TYPE.getName())) {
								deserializationUnit.writePrimitiveIntegerArrayDeserializator(classType, field, sw);
							} else if (fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Double.TYPE.getName())) {
								deserializationUnit.writePrimitiveDoubleArrayDeserializator(classType, field, sw);
							} else if (fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Date.class.getName())) {
								deserializationUnit.writeDateArrayDeserializator(classType, field, sw);
							} else {
								deserializationUnit.writeArrayDeserializator(classType, field, sw);
							}
						} else if (fieldType.isClassOrInterface() != null
								&& fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(java.util.List.class.getName()))
								&& !isDeserializable(fieldType.isClassOrInterface())) {
							if (fieldType.isParameterized() != null) {
								if (fieldType.isParameterized().getTypeArgs()[0].isAssignableTo(deserializableType) &&
									fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() == null) {
									if (isImplicitCollectionFieldForDeserialization(field)) {
										deserializationUnit.writeImplicitDeserializableArrayListDeserializator(classType, field, sw);
									} else {
										deserializationUnit.writeDeserializableArrayListDeserializator(classType, field, sw);
									}
								} else if (SerializationUtils.PRIMITIVE_WRAPPERS_SET.contains(fieldType.isParameterized().getTypeArgs()[0].getQualifiedSourceName()) &&
										fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() == null) {
									if (isImplicitCollectionFieldForDeserialization(field)) {
										deserializationUnit.writeImplicitPrimitiveWrapperArrayListDeserializator(classType, field, sw);
									} else {
										deserializationUnit.writeDeserializableDeserializator(classType, field, sw);
									}
								} else if (fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() != null) {
									if (isImplicitCollectionFieldForDeserialization(field)) {
										deserializationUnit.writeImplicitRawArrayListDeserializator(classType, field, sw);
									} else {
										deserializationUnit.writeRawArrayListDeserializator(classType, field, sw);
									}
								} else {
									fieldNotSupported(classType, field, sw, logger, Type.ERROR);
								}
							} else {
								fieldNotSupported(classType, field, sw, logger, Type.ERROR);
							}
						} else if (fieldType.isClassOrInterface() != null
							&& fieldType.isAnnotation() == null
							&& fieldType.isArray() == null
							&& fieldType.isGenericType() == null
							&& fieldType.isRawType() == null
							&& fieldType.isTypeParameter() == null
							&& fieldType.isWildcard() == null) {
							
							JClassType fieldClass = fieldType.isClassOrInterface();
							
							if (fieldType.isEnum() != null) {
								deserializationUnit.writeEnumDeserializator(classType, field, sw);
							} else if (fieldType.isParameterized() != null) {
								if (fieldType.isParameterized().getTypeArgs().length == 1) {
									if (fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() == null) {
										if (isDeserializable(fieldType.isClassOrInterface())) {
											if (fieldType.isParameterized().getTypeArgs()[0].isEnum() != null) {
												enumTypeSet.add(fieldType.isParameterized().getTypeArgs()[0].isEnum());
											}
											deserializationUnit.writeDeserializableDeserializator(classType, field, sw);
										} else {
											logger.log(Type.INFO, field.getName() + " is parameterized but will not be deserialized.");
										}
									} else {
										deserializationUnit.writeParameterizedDeserializableDeserializator(classType, field, sw);
									}
								} else {
									if (fieldClass.isAssignableTo(typeOracle.findType(java.util.Map.class.getName()))) {
										deserializationUnit.writeDeserializableDeserializator(classType, field, sw);
									} else {
										logger.log(Type.INFO, field.getName() + " is parameterized but have too much parameters.");
									}
								}
							} else {	
								if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.String.class.getName()))) {
									deserializationUnit.writeStringDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.Boolean.class.getName()))) {
									deserializationUnit.writeBooleanDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.Character.class.getName()))) {
									deserializationUnit.writeCharacterDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.util.Date.class.getName()))) {
									deserializationUnit.writeDeserializableDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.Double.class.getName()))) {
									deserializationUnit.writeDoubleDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.Float.class.getName()))) {
									deserializationUnit.writeFloatDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.Long.class.getName()))) {
									deserializationUnit.writeLongDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(java.lang.Integer.class.getName()))) {
									deserializationUnit.writeIntegerDeserializator(classType, field, sw);
								} else if (fieldClass.isAssignableTo(typeOracle.findType(BigDecimal.class.getName()))
										|| fieldClass.isAssignableTo(deserializableType)
										|| fieldClass.getSuperclass() == null) {
									deserializationUnit.writeDeserializableDeserializator(classType, field, sw);
								} else {
									fieldNotSupported(classType, field, sw, logger, Type.ERROR);
								}
							}
						} else if (fieldType.isPrimitive() != null) {
							if (JPrimitiveType.BOOLEAN.equals(fieldType.isPrimitive())) {
								deserializationUnit.writeBooleanDeserializator(classType, field, sw);
							} else if (JPrimitiveType.DOUBLE.equals(fieldType.isPrimitive())) {
								deserializationUnit.writeDoubleDeserializator(classType, field, sw);
							} else if (JPrimitiveType.INT.equals(fieldType.isPrimitive())) {
								deserializationUnit.writeIntegerDeserializator(classType, field, sw);
							} else if (JPrimitiveType.LONG.equals(fieldType.isPrimitive())) {
								deserializationUnit.writeLongDeserializator(classType, field, sw);
							} else if (JPrimitiveType.FLOAT.equals(fieldType.isPrimitive())) {
								deserializationUnit.writeFloatDeserializator(classType, field, sw);
							} else {
								fieldNotSupported(classType, field, sw, logger, Type.ERROR);
							}
						} else if (fieldType.isClassOrInterface() != null
							&& fieldType.isAnnotation() == null
							&& fieldType.isArray() == null
							&& fieldType.isGenericType() == null
							&& fieldType.isPrimitive() == null
							&& fieldType.isRawType() == null
							&& fieldType.isWildcard() == null
							&& fieldType.isParameterized() == null
							&& fieldType.isTypeParameter() != null) {
								deserializationUnit.writeParameterizedDeserializableDeserializator(classType, field, sw);
						} else {
							fieldNotSupported(classType, field, sw, logger, Type.ERROR);
						}
					} else {
						fieldNotSupported(classType, field, sw, logger, Type.INFO);
					}
				}
				sw.outdent();
			sw.println("}");
			if (classType.isDefaultInstantiable()) {
				sw.println();
				sw.println("@Override");
				sw.println("public " + classType.getQualifiedSourceName() + " makeInstance() {");
					sw.indent();
					sw.println("return new " + classType.getQualifiedSourceName() + "();");
					sw.outdent();
					sw.println("}\n");
			}
			sw.outdent();
		sw.println("};");

		sw.println("deserializators.put(\"" + getServerClassName(classType) + "\", deserializator);");
		if (classType.isAnnotationPresent(TSerializerAlias.class)) {
			sw.println("deserializators.put(\"" + classType.getAnnotation(TSerializerAlias.class).value() + "\", deserializator);");
		}
		sw.println();
	}
	
	@Override
	protected void writeEnumDeserializator(JEnumType enumType) {
		sw.println("deserializator = new EnumDeserializator<" + enumType.getQualifiedSourceName() + ">() {");
			sw.indent();
			sw.println();
			sw.println("@Override");
			sw.println("public " + enumType.getQualifiedSourceName() + " valueOf(String enumName) {");
				sw.indent();
				sw.println("return " + enumType.getQualifiedSourceName() + ".valueOf(enumName);");
				sw.outdent();
			sw.println("}\n");
			sw.outdent();
		sw.println("};");
	
		sw.println("deserializators.put(\"" + getServerClassName(enumType) + "\", deserializator);");
		if (enumType.isAnnotationPresent(TSerializerAlias.class)) {
			sw.println("deserializators.put(\"" + enumType.getAnnotation(TSerializerAlias.class).value() + "\", deserializator);");
		}
		sw.println();
	}
	
	protected void writeExternalDeserializator(JClassType type) {
		sw.println("deserializator = new " + type.getAnnotation(TDeserializator.class).value().getCanonicalName() + "(this);");

		sw.println("deserializators.put(\"" + getServerClassName(type) + "\", deserializator);");
		if (type.isAnnotationPresent(TSerializerAlias.class)) {
			sw.println("deserializators.put(\"" + type.getAnnotation(TSerializerAlias.class).value() + "\", deserializator);");
		}
		sw.println();
	}
	
	@Override
	protected void writeClassSerializator(JClassType classType) {
		if (classType.isAnnotationPresent(TSerializator.class)) {
			sw.println("serializators.put(\"" + getGWTClassName(classType) + "\", new " + classType.getAnnotation(TSerializator.class).value().getCanonicalName() + "(this));");
		} else {
			String superClassName = "?";
			if (classType.getSuperclass() != null && !classType.getSuperclass().getQualifiedSourceName().equals(Object.class.getName())) superClassName = classType.getSuperclass().getQualifiedSourceName();
			else superClassName = Object.class.getCanonicalName();
			 
			sw.println("serializators.put(\"" + getGWTClassName(classType) + "\", new XMLSerializator<" + superClassName + ", " + classType.getQualifiedSourceName() + ">() {");
				sw.indent();
				sw.println("@Override");
				sw.println("public Element serialize(" + classType.getQualifiedSourceName() + " instance, Element classNode, String className) {");
					sw.indent();
					if (classType.getSuperclass() != null &&
						classType.getSuperclass().getQualifiedSourceName().equals(Object.class.getName()) == false) {
						sw.println("classNode = toXML(instance, classNode, \"" + getGWTClassName(classType.getSuperclass()) + "\");");
					}
					//sw.println(classType.getQualifiedSourceName() + " instance = (" + classType.getQualifiedSourceName() + ")serializable;");
	
					for (JField field : classType.getFields()) {
						if (!isSerializable(field)) {
							logUnserializableField(classType, field);
							continue;
						}
						JType fieldType = field.getType();
						if (fieldType.isPrimitive() != null
							|| SerializationUtils.PRIMITIVE_WRAPPERS_SET.contains(fieldType.getQualifiedSourceName())) {
							if (isFieldAsAttributeForSerialization(field)) {
								sw.println("appendPrimitiveWrapperAttributeNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", instance." + field.getName() + ");");
							} else if (isFieldImplicit(field)){
								sw.println("appendPrimitiveWrapperNodeToElement(classNode, instance." + field.getName() + ");");
							} else {
								sw.println("appendPrimitiveWrapperNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", instance." + field.getName() + ");");
							}
						} else if (fieldType.isEnum() != null) { 
							if (isFieldAsAttributeForSerialization(field)) {
								sw.println("appendPrimitiveWrapperAttributeNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", getEnumValue(instance." + field.getName() + "));");
							} else if (isFieldImplicit(field)){
								sw.println("appendPrimitiveWrapperNodeToElement(classNode, getEnumValue(instance." + field.getName() + "));");
							} else {
								sw.println("appendPrimitiveWrapperNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", getEnumValue(instance." + field.getName() + "));");
							}
						} else {
							if (fieldType.isArray() != null && fieldType.isArray().getComponentType().getQualifiedSourceName().equals(Integer.TYPE.getName())) {
								sw.println("appendPrimitiveIntegerArrayNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", instance." + field.getName() + ");");
							} else if (	fieldType.isClassOrInterface() != null &&
										fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(TSerializable.class.getName()))) {
								sw.println("appendSerializableNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", \"" + getGWTClassName(fieldType) + "\", instance." + field.getName() + ");");
	//									sw.println("Element " + field.getName() + "Element = DOCUMENT.createElement(\"" + getFieldName(field) + "\");");
	//									if (fieldType.isTypeParameter() != null) {
	//										sw.println(field.getName() + "Element.setAttribute(\"class\", getClassName(instance." + field.getName() + "));");
	//									}
	//									sw.println("classNode.appendChild(toXML(instance." + field.getName() + ", " + field.getName() + "Element, instance." + field.getName() + ".getClass().getName()));");
							} else if (	fieldType.isClassOrInterface() != null &&
										fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(Collection.class.getName())) &&
										!fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(EnumSet.class.getName()))) {
								sw.println("if (instance." + field.getName() + " != null) {");
									sw.indent();
	
								if (isFieldImplicitCollection(field)) {
									sw.println("for (int i = 0; i < instance." + field.getName() + ".toArray().length; ++i) {");
										sw.indent();
										if (field.getAnnotation(TSerializerImplicitCollection.class).itemFieldName() != null && !field.getAnnotation(TSerializerImplicitCollection.class).itemFieldName().isEmpty()) {
											sw.println("classNode.appendChild(toXML((" + XMLSerializerGenerator.SERIALIZABLE_TYPE.getName() + ")instance." + field.getName() + ".toArray()[i], DOCUMENT.createElement(\"" + field.getAnnotation(TSerializerImplicitCollection.class).itemFieldName() + "\"), instance." + field.getName() + ".toArray()[i].getClass().getName()));");
										} else {
											sw.println("classNode.appendChild(toXML((" + XMLSerializerGenerator.SERIALIZABLE_TYPE.getName() + ")instance." + field.getName() + ".toArray()[i], createClassNode(instance." + field.getName() + ".toArray()[i]), instance." + field.getName() + ".toArray()[i].getClass().getName()));");
										}
										sw.outdent();
									sw.println("}");
								} else {
									sw.println("appendArrayNodeToElement(classNode, \"" + field.getName() + "\",  instance." + field.getName() + ".toArray());");
								}
									sw.outdent();
								sw.println("}");
							} else if (fieldType.isArray() != null) {
								sw.println("appendArrayNodeToElement(classNode, \"" + field.getName() + "\",  instance." + field.getName() + ");");
							} else if (fieldType.isClassOrInterface() != null) {
								sw.println("appendNodeToElement(classNode, \"" + getFieldNameForSerialization(field) + "\", \"" + getGWTClassName(fieldType) + "\", instance." + field.getName() + ");");
							} else {
								logUnserializableField(classType, field);
							}
						}
					}
					sw.println("return classNode;");
					sw.outdent();
				sw.println("}");
				sw.outdent();
			sw.println("});");
		}
	}
	
	private static boolean isFieldImplicit(JField field) {
		return field.getAnnotation(TSerializerImplicit.class) != null &&
		(field.getAnnotation(TSerializerImplicit.class).mode().equals(Mode.BOTH) ||
		field.getAnnotation(TSerializerImplicit.class).mode().equals(Mode.SERIALIZATION));
	}
	
	private static boolean isFieldImplicitCollection(JField field) {
		return field.getAnnotation(TSerializerImplicitCollection.class) != null &&
		(field.getAnnotation(TSerializerImplicitCollection.class).mode().equals(Mode.BOTH) ||
		field.getAnnotation(TSerializerImplicitCollection.class).mode().equals(Mode.SERIALIZATION));
	}

	public String getAliasOrServerClassName(JType type) {
		JClassType classType = type.isClassOrInterface();
		if (classType != null) {
			TSerializerAlias alias = classType.getAnnotation(TSerializerAlias.class);
			if (alias != null && (alias.mode() == Mode.BOTH || alias.mode() == Mode.SERIALIZATION)) {
				return alias.value();
			}
		}
		return getServerClassName(type);
	}
	
	public static String getServerClassName(JType classType) {
		if (classType.isClassOrInterface() != null && classType.isClassOrInterface().isMemberType()) {
			return classType.isClassOrInterface().getEnclosingType().getQualifiedSourceName() + "_-" + classType.getSimpleSourceName();
		} else {
			return classType.getQualifiedSourceName();
		}
	}
	

}
