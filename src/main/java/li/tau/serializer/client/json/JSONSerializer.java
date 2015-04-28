package li.tau.serializer.client.json;

import java.util.logging.Logger;

import li.tau.serializer.client.Serializer;

import com.google.gwt.json.client.JSONValue;

public interface JSONSerializer extends Serializer {
	
	Logger LOGGER = Logger.getLogger(JSONSerializer.class.getName());
	
	Object fromJSON(JSONValue json);
	Object fromJSON(JSONValue json, String className);
	
	JSONValue toJSON(Object object);
	
	String getServerClassName(Object instance);

}
