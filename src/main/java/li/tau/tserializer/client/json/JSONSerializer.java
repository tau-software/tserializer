package li.tau.tserializer.client.json;

import java.util.logging.Logger;

import li.tau.tserializer.client.TSerializer;

import com.google.gwt.json.client.JSONValue;

public interface JSONSerializer extends TSerializer {
	
	Logger LOGGER = Logger.getLogger(JSONSerializer.class.getName());
	
	Object fromJSON(JSONValue json);
	Object fromJSON(JSONValue json, String className);
	
	JSONValue toJSON(Object object);
	
	String getServerClassName(Object instance);

}
