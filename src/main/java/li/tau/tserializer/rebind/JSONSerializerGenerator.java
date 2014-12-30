package li.tau.tserializer.rebind;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

import li.tau.tserializer.client.TSerializer;
import li.tau.tserializer.client.annotation.TDeserializator;
import li.tau.tserializer.client.annotation.TSerializable;
import li.tau.tserializer.client.annotation.TSerializator;
import li.tau.tserializer.client.annotation.TSerializerAlias;
import li.tau.tserializer.client.json.JSONSerializerImpl;
import li.tau.tserializer.rebind.JSONClassDeserializationUnit.JSONFieldDeserializationUnit;

import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;

public class JSONSerializerGenerator extends SerializerGenerator {
	
	@Override
	protected void addImports(ClassSourceFileComposerFactory classSourceFileComposerFactory) {
		super.addImports(classSourceFileComposerFactory);
		classSourceFileComposerFactory.addImport("com.google.gwt.json.client.*");
		classSourceFileComposerFactory.addImport("li.tau.tserializer.client.json.*");
	}
	
	@Override
	protected Class<? extends TSerializer> getSuperClassSerializer() {
		return JSONSerializerImpl.class;
	}
	
	@Override
	protected void writeClassesDeserializators() {
		sw.println("JSONDeserializator deserializator;");
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
		
		JSONClassDeserializationUnit classDeserializationUnit = new JSONClassDeserializationUnit(classType, sw);

		sw.println("deserializator = new JSONDeserializator<" + classType.getSuperclass().getQualifiedSourceName() + ", " + classType.getQualifiedSourceName() + ">() {");
			sw.indent();
			sw.println("{");
				sw.indent();

				if (classType.getSuperclass() != null && !classType.getSuperclass().getQualifiedSourceName().equals(java.lang.Object.class.getName())) {
					sw.println("superClassDeserializator = deserializators.get(\"" + classType.getSuperclass().getQualifiedBinaryName()  + "\");");
				}
				
				for (JField field : classType.getFields()) {
					JSONFieldDeserializationUnit fieldDeserializationUnit = getFieldDeserializationUnit(classDeserializationUnit, field);
					if (fieldDeserializationUnit != null) {
						fieldDeserializationUnit.write(field);
					} else {
						fieldNotSupported(classType, field, sw, logger, Type.ERROR);
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

		sw.println("deserializators.put(\"" + classType.getQualifiedBinaryName() + "\", deserializator);");
		if (classType.isAnnotationPresent(TSerializerAlias.class)) {
			sw.println("deserializators.put(\"" + classType.getAnnotation(TSerializerAlias.class).value() + "\", deserializator);");
		}
		sw.println();
	}
	
	protected JSONFieldDeserializationUnit getFieldDeserializationUnit(JSONClassDeserializationUnit classDeserializationUnit, JField field) {
		JType fieldType = field.getType();
		if (isDeserializable(field)) {
			if (fieldType.isEnum() != null) {
				return classDeserializationUnit.new EnumUnit();
			} else if (fieldType.isPrimitive() == JPrimitiveType.BOOLEAN) {
				return classDeserializationUnit.new BooleanUnit();
			} else if (fieldType.isPrimitive() == JPrimitiveType.BYTE) {
				return classDeserializationUnit.new NumberUnit(Byte.TYPE.getSimpleName());
			} else if (fieldType.isPrimitive() == JPrimitiveType.CHAR) {
				return classDeserializationUnit.new CharUnit();
			} else if (fieldType.isPrimitive() == JPrimitiveType.DOUBLE) {
				return classDeserializationUnit.new NumberUnit(Double.TYPE.getSimpleName());
			} else if (fieldType.isPrimitive() == JPrimitiveType.FLOAT) {
				return classDeserializationUnit.new NumberUnit(Float.TYPE.getSimpleName());
			} else if (fieldType.isPrimitive() == JPrimitiveType.INT) {
				return classDeserializationUnit.new NumberUnit(Integer.TYPE.getSimpleName());
			} else if (fieldType.isPrimitive() == JPrimitiveType.LONG) {
				return classDeserializationUnit.new NumberUnit(Long.TYPE.getSimpleName());
			} else if (fieldType.isPrimitive() == JPrimitiveType.SHORT) {
				return classDeserializationUnit.new NumberUnit(Short.TYPE.getSimpleName());
			} if (fieldType.isArray() != null) {
				return classDeserializationUnit.new ArrayUnit();
			} if (fieldType.isClassOrInterface() != null) {

				JClassType fieldClassType = fieldType.isClassOrInterface();

				if (fieldClassType.isAssignableTo(typeOracle.findType(String.class.getName()))) {
					return classDeserializationUnit.new StringUnit();
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Boolean.class.getName()))) {
					return classDeserializationUnit.new BooleanUnit();
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Byte.class.getName()))) {
					return classDeserializationUnit.new NumberUnit(Byte.TYPE.getSimpleName());
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Character.class.getName()))) {
					return classDeserializationUnit.new CharUnit();
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Double.class.getName()))) {
					return classDeserializationUnit.new NumberUnit(Double.TYPE.getSimpleName());
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Float.class.getName()))) {
					return classDeserializationUnit.new NumberUnit(Float.TYPE.getSimpleName());
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Integer.class.getName()))) {
					return classDeserializationUnit.new NumberUnit(Integer.TYPE.getSimpleName());
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Long.class.getName()))) {
					return classDeserializationUnit.new NumberUnit(Long.TYPE.getSimpleName());
				} else if (fieldClassType.isAssignableTo(typeOracle.findType(Short.class.getName()))) {
					return classDeserializationUnit.new NumberUnit(Short.TYPE.getSimpleName());
//				} else if (fieldClassType.isAssignableTo(typeOracle.findType(List.class.getName()))
//							&& !isDeserializable(fieldType.isClassOrInterface())) {
//						if (fieldType.isParameterized() != null) {
//							if (fieldType.isParameterized().getTypeArgs()[0].isAssignableTo(deserializableType) &&
//								fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() == null) {
//								deserializationUnit.writeDeserializableArrayListDeserializator(classType, field, sw);
//							} else if (SerializationUtils.PRIMITIVE_WRAPPERS_SET.contains(fieldType.isParameterized().getTypeArgs()[0].getQualifiedSourceName()) &&
//									fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() == null) {
//								deserializationUnit.writeDeserializableDeserializator(classType, field, sw);
//							} else if (fieldType.isParameterized().getTypeArgs()[0].isTypeParameter() != null) {
//								deserializationUnit.writeRawArrayListDeserializator(classType, field, sw);
//							}
//						}
				} else if (
					fieldType.isAnnotation() == null
					&& fieldType.isArray() == null
					&& fieldType.isGenericType() == null
					&& fieldType.isRawType() == null
//					&& fieldType.isTypeParameter() == null
					&& fieldType.isWildcard() == null) {
					
					
					if (fieldClassType.isAssignableTo(deserializableType)) {
						return classDeserializationUnit.new DeserializableUnit();
					} else if (fieldClassType.isAssignableTo(typeOracle.findType(Collection.class.getName()))) {
						return classDeserializationUnit.new DeserializableUnit();
					} else if (fieldClassType.isAssignableTo(typeOracle.findType(Map.class.getName()))) {
						return classDeserializationUnit.new DeserializableUnit();
					} else if (fieldClassType.isAssignableTo(typeOracle.findType(Date.class.getName()))) {
						return classDeserializationUnit.new DeserializableUnit();
					} else if (fieldType.isParameterized() == null
						&& fieldType.isTypeParameter() != null) {
						return classDeserializationUnit.new TypeParameterUnit();
					}

					/*else if (fieldType.isParameterized() != null) {
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
					}*/
				} 

			}
		}
		return null;
	}
	
	@Override
	protected void writeEnumDeserializator(JEnumType enumType) {
		sw.println("deserializator = new JSONSerializerImpl.EnumDeserializator<" + enumType.getQualifiedSourceName() + ">() {");
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
	
		sw.println("deserializators.put(\"" + enumType.getQualifiedBinaryName() + "\", deserializator);");
		if (enumType.isAnnotationPresent(TSerializerAlias.class)) {
			sw.println("deserializators.put(\"" + enumType.getAnnotation(TSerializerAlias.class).value() + "\", deserializator);");
		}
		sw.println();
	}
	
	protected void writeExternalDeserializator(JClassType type) {
		sw.println("deserializator = new " + type.getAnnotation(TDeserializator.class).value().getCanonicalName() + "(this);");

		sw.println("deserializators.put(\"" + type.getQualifiedBinaryName() + "\", deserializator);");
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
			 
			sw.println("serializators.put(\"" + getGWTClassName(classType) + "\", new JSONSerializator<" + superClassName + ", " + classType.getQualifiedSourceName() + ">() {");
				sw.indent();
				sw.println("@Override");
				sw.println("public JSONObject serialize(" + classType.getQualifiedSourceName() + " instance) {");
					sw.indent();
						sw.println("JSONObject json = null;");
					if (classType.getSuperclass() != null &&
						classType.getSuperclass().getQualifiedSourceName().equals(Object.class.getName()) == false) {
						sw.println("json = toJSON(instance, \"" + getGWTClassName(classType.getSuperclass()) + "\").isObject();");
					} else {
						sw.println("json = new JSONObject();");
					}
					//sw.println(classType.getQualifiedSourceName() + " instance = (" + classType.getQualifiedSourceName() + ")serializable;");
	
					for (JField field : classType.getFields()) {
						if (!isSerializable(field)) {
							logUnserializableField(classType, field);
							continue;
						}
						JType fieldType = field.getType();
						if (fieldType.isPrimitive() != null
							|| (fieldType.isClassOrInterface() != null
								&& (fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(Boolean.class.getName()))
									|| fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(Number.class.getName()))
									|| fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(String.class.getName()))))
							|| fieldType.isEnum() != null) {
							String gwtClassName = fieldType.isPrimitive() == null ? getGWTClassName(fieldType) : fieldType.isPrimitive().getQualifiedBoxedSourceName();
							if (isFieldAsAttributeForSerialization(field)) {
								sw.println(String.format("append(json, \"@%1$s\", toJSON(instance.%2$s, \"%3$s\"));", getFieldNameForSerialization(field), field.getName(), gwtClassName));
							} else {
								sw.println(String.format("append(json, \"%1$s\", toJSON(instance.%2$s, \"%3$s\"));", getFieldNameForSerialization(field), field.getName(), gwtClassName));
							}
						} else {
							if (fieldType.isClassOrInterface() != null
								&& fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(TSerializable.class.getName()))) {
								sw.println(String.format("append(json, \"%1$s\", toJSON(instance.%2$s, \"%3$s\"));", getFieldNameForSerialization(field), field.getName(), getGWTClassName(fieldType)));
	//									sw.println("Element " + field.getName() + "Element = DOCUMENT.createElement(\"" + getFieldName(field) + "\");");
	//									if (fieldType.isTypeParameter() != null) {
	//										sw.println(field.getName() + "Element.setAttribute(\"class\", getClassName(instance." + field.getName() + "));");
	//									}
	//									sw.println("classNode.appendChild(toXML(instance." + field.getName() + ", " + field.getName() + "Element, instance." + field.getName() + ".getClass().getName()));");
							} else if (	fieldType.isClassOrInterface() != null &&
										fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(Collection.class.getName())) &&
										!fieldType.isClassOrInterface().isAssignableTo(typeOracle.findType(EnumSet.class.getName()))) {
								sw.println(String.format("append(json, \"%1$s\", toJSON(instance.%2$s, \"%3$s\"));", getFieldNameForSerialization(field), field.getName(), getGWTClassName(fieldType)));
							} else if (fieldType.isArray() != null || fieldType.isClassOrInterface() != null) {
								sw.println(String.format("append(json, \"%1$s\", toJSON(instance.%2$s, \"%3$s\"));", getFieldNameForSerialization(field), field.getName(), getGWTClassName(fieldType)));
							} else {
								logUnserializableField(classType, field);
							}
						}
					}
					sw.println("return json;");
					sw.outdent();
				sw.println("}");
				sw.outdent();
			sw.println("});");
		}
	}
	
	public static String getArrayClassName(JType type) {
		if (type.isArray() != null) {
			return getArrayClassName(type.isArray().getComponentType()) + "-array";
		} else {
			return type.getSimpleSourceName();
		}
	}

}
