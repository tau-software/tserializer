package li.tau.serializer.client;

public interface Serializer {
	
	Object fromString(String str);
	String toString(Object object);
	
	String format();

}
