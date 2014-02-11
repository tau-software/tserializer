package li.tau.tserializer.client.json;

import com.google.gwt.json.client.JSONValue;

public interface JSONSerializator<S, T extends S> {

	JSONValue serialize(T instance);
	
	abstract class ExternalSerializator<S, T extends S> implements JSONSerializator<S, T> {
		
		protected final JSONSerializer serializer;
		
		public ExternalSerializator(JSONSerializer serializer) {
			this.serializer = serializer;
		}
		
	}
	
}
