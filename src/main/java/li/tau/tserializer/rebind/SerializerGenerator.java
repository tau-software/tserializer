package li.tau.tserializer.rebind;

import static com.google.gwt.core.ext.TreeLogger.ERROR;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import li.tau.tserializer.client.TSerializer;
import li.tau.tserializer.client.annotation.Mode;
import li.tau.tserializer.client.annotation.TDeserializable;
import li.tau.tserializer.client.annotation.TSerializable;
import li.tau.tserializer.client.annotation.TSerializerAlias;
import li.tau.tserializer.client.annotation.TSerializerAsAttribute;
import li.tau.tserializer.client.annotation.TSerializerImplicit;
import li.tau.tserializer.client.annotation.TSerializerImplicitCollection;
import li.tau.tserializer.client.annotation.TSerializerOmitField;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public abstract class SerializerGenerator extends Generator {
	
	public static final Class<?> SERIALIZABLE_TYPE = TSerializable.class;
	public static final Class<?> DESERIALIZABLE_TYPE = TDeserializable.class;

	protected TreeLogger logger;
	protected TypeOracle typeOracle;
	protected SourceWriter sw;
	
	public static JClassType serializableType;
	public static JClassType deserializableType;

	@Override
	public final String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		this.logger = logger;
		typeOracle = context.getTypeOracle();
		
		try {
			serializableType = typeOracle.getType(SERIALIZABLE_TYPE.getName());
			deserializableType = typeOracle.getType(DESERIALIZABLE_TYPE.getName());
		} catch (NotFoundException e) {
			logger.log(Type.ERROR, e.getLocalizedMessage(), e);
			throw new UnableToCompleteException();
		}

		JClassType classType = typeOracle.findType(typeName);
		if (classType == null || !classType.getQualifiedSourceName().equals(TSerializer.class.getName())) {
			throw new UnableToCompleteException();
		}

		String packageName = classType.getPackage().getName();
		String simpleName = classType.getSimpleSourceName() + "Rebind";

		ClassSourceFileComposerFactory classSourceFileComposerFactory = new ClassSourceFileComposerFactory(packageName, simpleName);
		addImports(classSourceFileComposerFactory);
		classSourceFileComposerFactory.setSuperclass(getSuperClassSerializer().getName());
		
		PrintWriter pw;
		if ((pw = context.tryCreate(logger, packageName, simpleName)) == null) {
			return packageName + "." + simpleName;
		}

		sw = classSourceFileComposerFactory.createSourceWriter(context, pw);

		sw.println("public " + simpleName + "() {");
			sw.indent();
			sw.println("super();");
			sw.println();
			writeClassesDeserializators();
			sw.println();
			writeClassesSerializators();
			writeClassNamesMap();
			sw.outdent();
		sw.println("}");
		sw.println();

		sw.commit(logger);

		return packageName + "." + simpleName;
	}
	
	protected void addImports(ClassSourceFileComposerFactory classSourceFileComposerFactory) {
		classSourceFileComposerFactory.addImport(java.util.ArrayList.class.getName());
		classSourceFileComposerFactory.addImport(java.util.Date.class.getName());
		classSourceFileComposerFactory.addImport(java.util.HashMap.class.getName());

		classSourceFileComposerFactory.addImport(com.google.gwt.core.client.GWT.class.getName());
		classSourceFileComposerFactory.addImport(com.google.gwt.i18n.client.DateTimeFormat.class.getName());

		classSourceFileComposerFactory.addImport(Mode.class.getName());
		
		classSourceFileComposerFactory.addImport(DESERIALIZABLE_TYPE.getName());
		classSourceFileComposerFactory.addImport(SERIALIZABLE_TYPE.getName());
	}
	
	protected abstract Class<? extends TSerializer> getSuperClassSerializer();
	
	protected Set<JEnumType> enumTypeSet = new HashSet<JEnumType>();
	
	protected void writeClassesDeserializators() {
		HashSet<String> deserializableClasses = new HashSet<String>();
		for (JClassType classType : typeOracle.getTypes()) {
			if (classType.isAssignableTo(deserializableType)) {
				while (classType.getSuperclass() != null &&
					!classType.getQualifiedSourceName().startsWith("java.")) {
					deserializableClasses.add(classType.getQualifiedSourceName());
					classType = classType.getSuperclass();
				}
			}
		}
		while (deserializableClasses.isEmpty() == false) {
			String[] classNames = deserializableClasses.toArray(new String[deserializableClasses.size()]);
			for (String className : classNames) {
				if (!deserializableClasses.contains(typeOracle.findType(className).getSuperclass().getQualifiedSourceName())) {
					writeClassDeserializator(typeOracle.findType(className));
					deserializableClasses.remove(className);
				}
			}
		}
		for (JEnumType enumType : enumTypeSet) {
			writeEnumDeserializator(enumType);
		}
		enumTypeSet.clear();
	}
	
	protected abstract void writeClassDeserializator(JClassType classType);
	protected abstract void writeEnumDeserializator(JEnumType enumType);
	
	private void writeClassesSerializators() {
		
		HashSet<String> serializableClasses = new HashSet<String>();
		
		for (JClassType classType : typeOracle.getTypes()) {
			if (classType.isAssignableTo(XMLSerializerGenerator.serializableType)) {
				while (classType.getSuperclass() != null &&
						!classType.getPackage().getName().startsWith("java.")) {
					serializableClasses.add(classType.getQualifiedSourceName());
					classType = classType.getSuperclass();
				}
			}
		}

		while (serializableClasses.isEmpty() == false) {
			String[] classNames = serializableClasses.toArray(new String[serializableClasses.size()]);
			for (String className : classNames) {
				if (!serializableClasses.contains(typeOracle.findType(className).getSuperclass().getQualifiedSourceName())) {
					writeClassSerializator(typeOracle.findType(className));
					serializableClasses.remove(className);
				}
			}
		}

	}
	
	protected abstract void writeClassSerializator(JClassType classType);
	
	protected String getImplicitFieldName(JClassType classType) {
		for (JField field : classType.getFields()) {
			if (field.isAnnotationPresent(TSerializerImplicit.class)) {
				return field.getName();
			}
		}
		return null;
	}
	
	protected boolean isAccessibleImplicitField(JClassType classType, Mode mode) {
		for (JField field : classType.getFields()) {
			if (field.isAnnotationPresent(TSerializerImplicit.class)) {
				return field.getAnnotation(TSerializerImplicit.class).mode() == Mode.BOTH ||
				field.getAnnotation(TSerializerImplicit.class).mode() == mode;
			}
		}
		return false;
	}
	
	protected String getImplicitCollectionWithoutItemNameFieldName(JClassType classType, Mode mode) {
		for (JField field : classType.getFields()) {
			TSerializerImplicitCollection annotation = field.getAnnotation(TSerializerImplicitCollection.class);
			if (annotation != null && (annotation.mode() == Mode.BOTH || annotation.mode() == mode) && annotation.itemFieldName().isEmpty()) {
				return field.getName();
			}
		}
		return null;
	}
	
	protected Map<String, String> getImplicitCollectionWithItemFieldNameMap(JClassType classType, Mode mode) {
		Map<String, String> map = new HashMap<String, String>();
		for (JField field : classType.getFields()) {
			TSerializerImplicitCollection annotation = field.getAnnotation(TSerializerImplicitCollection.class);
			if (annotation != null && (annotation.mode() == Mode.BOTH || annotation.mode() == mode) && !annotation.itemFieldName().isEmpty()) {
				map.put(annotation.itemFieldName(), field.getName());
			}
		}
		return map;
	}
	
	protected boolean isImplicitCollectionFieldForDeserialization(JField field) {
		return field.isAnnotationPresent(TSerializerImplicitCollection.class) &&
		(field.getAnnotation(TSerializerImplicitCollection.class).mode() == Mode.BOTH || 
		field.getAnnotation(TSerializerImplicitCollection.class).mode() == Mode.DESERIALIZATION);
	}
	
	protected boolean isDeserializable(JClassType classType) {
		return classType.isAssignableTo(deserializableType) ||
				(classType.isEnum() != null) ||
				classType.isAssignableTo(typeOracle.findType(java.util.Set.class.getName()));
	}
	
	protected void fieldNotSupported(JClassType classType, JField field, SourceWriter sw, TreeLogger logger, Type type) {
		String reason = getReason(field);
		if (reason != null) {
			logger.log(type, field.getName() + "@" + classType.getParameterizedQualifiedSourceName() + " won't be deserialized. Reason: " + reason + ".");
		}
	}
	
	protected String getReason(JField field) {
		
		//normal behaivour
		if (field.isTransient()) return null;
		if (field.isStatic()) return null;
		
		
		if (field.isPrivate()) {
			return "is private, has setter = " + hasSetter(field);
		}
		if (field.isProtected()) return "is protected";
		if (field.isFinal()) return "is final";
		if (field.isAnnotationPresent(TSerializerOmitField.class) &&
		(field.getAnnotation(TSerializerOmitField.class).value() == Mode.SERIALIZATION ||
		field.getAnnotation(TSerializerOmitField.class).value() == Mode.BOTH)) return "annotation \'OmitField\' is present";
		if (field.getType().isAnnotation() != null) return "is annotation";
		if (field.getType().isGenericType() != null) return "is genericType";
		if (field.getType().isInterface() != null) return "is interface";
		if (field.getType().isPrimitive() != null) return "is primitive";
		if (field.getType().isRawType() != null) return "is rawType";
		if (field.getType().isTypeParameter() != null) return "is typeParameter";
		if (field.getType().isWildcard() != null) return "is wildcard";
		
		return "unknown";
	}
	
	private void writeClassNamesMap() {
		for (JClassType classType : typeOracle.getTypes()) {
			if (classType.isClass() != null &&
				classType.isAbstract() == false &&
				classType.getAnnotation(TSerializerAlias.class) != null) {
				sw.println("classNamesMap.put(\"" + getGWTClassName(classType) + "\", \"" + classType.getAnnotation(TSerializerAlias.class).value() + "\");");
			}
		}
	}
	
	public static String getGWTClassName(JType classType) {
//		if (classType.isClassOrInterface() != null && classType.isClassOrInterface().isMemberType()) {
//			return classType.isClassOrInterface().getEnclosingType().getQualifiedBinaryName() + "$" + classType.getSimpleSourceName();
//		} else {
		if (classType.isArray() != null) {
			String bracket = "";
			for (int i = 0; i < classType.isArray().getRank(); ++i) bracket += "[";
			if (classType.isArray().getComponentType().isPrimitive() != null) {
				return bracket + classType.isArray().getComponentType().isPrimitive().getSimpleSourceName().substring(0, 1).toUpperCase();
			} else {
				return bracket + "L" + getGWTClassName(classType.isArray().getComponentType());
			}
		} else {
			return classType.getQualifiedBinaryName();
		}
	}
	
	protected void logUnserializableField(JClassType classType, JField field) {
		logger.log(ERROR, field.getName() + "@" + classType.getParameterizedQualifiedSourceName() + " won't be serialized");
	}
	
	protected boolean isSerializable(JField field) {
		return 
			isReadAccessible(field)
			&& field.isStatic() == false &&
			field.isTransient() == false &&
			(field.isAnnotationPresent(TSerializerOmitField.class) == false ||
					(field.getAnnotation(TSerializerOmitField.class).value() != Mode.SERIALIZATION &&
					field.getAnnotation(TSerializerOmitField.class).value() != Mode.BOTH));
	}

	protected boolean isDeserializable(JField field) {
		return isWriteAccessible(field)
				&& field.isFinal() == false
				&& field.isStatic() == false
				&& field.isTransient() == false
				&& (field.isAnnotationPresent(TSerializerOmitField.class) == false ||
						(field.getAnnotation(TSerializerOmitField.class).value() != Mode.DESERIALIZATION &&
						field.getAnnotation(TSerializerOmitField.class).value() != Mode.BOTH));
	}
	
	static boolean isReadAccessible(JField field) {
		return field.isPublic() || hasGetter(field);
	}

	static boolean isWriteAccessible(JField field) {
		return field.isPublic() || hasSetter(field);
	}
	
	static boolean hasGetter(JField field) {
		return field.getEnclosingType().findMethod(getGetterName(field), new JType[0]) != null;
	}

	static boolean hasSetter(JField field) {
		return field.getEnclosingType().findMethod(getSetterName(field), new JType[]{field.getType()}) != null;
	}
	
	static String getGetterName(JField field) {
		String getName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
		if (field.getEnclosingType().findMethod(getName, new JType[0]) != null) {
			return getName;
		}
		return "is" + getName.substring(3);
	}

	static String getSetterName(JField field) {
		return "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
	}

	protected String getFieldNameForSerialization(JField field) {
		if (field.isAnnotationPresent(TSerializerAlias.class) && (
				field.getAnnotation(TSerializerAlias.class).mode() == Mode.BOTH ||
				field.getAnnotation(TSerializerAlias.class).mode() == Mode.SERIALIZATION)) {
			return field.getAnnotation(TSerializerAlias.class).value();
		} else {
			return field.getName();
		}
	}
	
	protected boolean isFieldAsAttributeForSerialization(JField field) {
		return field.getAnnotation(TSerializerAsAttribute.class) != null &&
		(field.getAnnotation(TSerializerAsAttribute.class).mode().equals(Mode.BOTH) ||
		field.getAnnotation(TSerializerAsAttribute.class).mode().equals(Mode.SERIALIZATION));
	}
	

	
}
