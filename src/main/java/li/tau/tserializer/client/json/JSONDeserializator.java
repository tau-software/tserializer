package li.tau.tserializer.client.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public abstract class JSONDeserializator<S, T extends S> {
	
	public abstract static class ExternalDeserializator<S, T extends S> extends JSONDeserializator<S, T> {
		protected final JSONSerializer serializer;

		public ExternalDeserializator(JSONSerializer serializer) {
			this.serializer = serializer;
		}
	}
	
	public abstract static class JSONObjectDeserializator<S, T extends S> extends JSONDeserializator<S, T> {
		@Override
		public final T deserialize(JSONValue value, T instance) {
			JSONObject jsonObject = value.isObject();
			if (jsonObject == null) {
				if (value.isNull() == null) {
					JSONSerializer.LOGGER.warning("Expected JSONObject but get: \"" + value.toString() + "\".");
				}
				return instance;
			}
			return deserialize(jsonObject, instance);
		}
		
		protected abstract T deserialize(JSONObject object, T instance);
	}
	
	private static final Set<String> RESERVED_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList(new String[]{"@class", "@enum-type"}));
	
	protected JSONDeserializator<? super S, S> superClassDeserializator = null;

	public abstract class Setter {
		public abstract void set(JSONValue n, T instance);
	}

	protected final HashMap<String, Setter> dispatcher = new HashMap<String, Setter>();
	
	public T makeInstance() {
		throw new UnsupportedOperationException("This class type hasn't got default constructor.");
	};

	protected final boolean deserializeField(JSONValue n, String fieldName, T instance) {
		if (dispatcher.containsKey(fieldName)) {
			dispatcher.get(fieldName).set(n, instance);
			return true;
		} else if (superClassDeserializator != null) {
			return superClassDeserializator.deserializeField(n, fieldName, instance);
		}
		return false;
	}

	/**
	 * @param value expect not null
	 * @param instance
	 * @return
	 */
	public T deserialize(JSONValue value, T instance) {
		JSONObject object = value.isObject();
		if (object != null) {
			for (String key : object.keySet()) {
				if (RESERVED_ATTRIBUTE_NAMES.contains(key)) continue;
				String fieldName = key.startsWith("@") ? key.substring(1) : key;
				if (!deserializeField(object.get(key), fieldName, instance)) {
					JSONSerializer.LOGGER.warning("Can't find method to deserialize key: "
							+ key + " in "
							+ object.toString() + "(" + instance.getClass().getName() + ":" + instance.toString() + ")");
				}
			}
		}
		return instance;
	}

}