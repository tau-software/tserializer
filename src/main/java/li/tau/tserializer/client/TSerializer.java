package li.tau.tserializer.client;

public interface TSerializer {
	
	Object fromString(String str);
	String toString(Object object);
	
	String format();

}
