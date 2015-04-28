package li.tau.serializer.client.json;

import com.google.gwt.json.client.JSONValue;

public interface JSONSerializator<S, T extends S> {

	public JSONValue serialize(T instance);
	
	public abstract static class ExternalSerializator<S, T extends S> implements JSONSerializator<S, T> {
		
		protected final JSONSerializer serializer;
		
		public ExternalSerializator(JSONSerializer serializer) {
			this.serializer = serializer;
		}
		
	}
	
}
