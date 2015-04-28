package li.tau.serializer.rebind;

import java.util.HashSet;
import java.util.Set;

public class SerializationUtils {
	
	public static final Set<String> PRIMITIVE_WRAPPERS_SET = new HashSet<String>();
	
	static {
		PRIMITIVE_WRAPPERS_SET.add(Boolean.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(Byte.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(Double.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(Float.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(Integer.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(Long.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(Short.class.getName());
		PRIMITIVE_WRAPPERS_SET.add(String.class.getName());
	}

}
