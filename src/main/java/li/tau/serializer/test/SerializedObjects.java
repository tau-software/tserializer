package li.tau.serializer.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import li.tau.serializer.client.annotation.TDeserializable;
import li.tau.serializer.client.annotation.TSerializable;

public class SerializedObjects {
	
	public static abstract class SerializedObject implements TSerializable, TDeserializable {
		public abstract String getDesiredSerializationString();
	}
	
	public static class WithEmptyList extends SerializedObject {
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((list == null) ? 0 : list.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WithEmptyList other = (WithEmptyList) obj;
			if (list == null) {
				if (other.list != null)
					return false;
			} else if (!list.equals(other.list))
				return false;
			return true;
		}

		public List<Object> list;
		
		public static WithEmptyList create() {
			WithEmptyList withEmptyList = new WithEmptyList();
			withEmptyList.list = new ArrayList<Object>();
			return withEmptyList;
		}
		
		@Override
		public String getDesiredSerializationString() {
			return "{ \"list\" : { } , \"@class\" : \"" + this.getClass().getName() + "\"}";
		}
	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class WithMaps extends SerializedObject {
		
		public Integer index;
		
		public Map map;
		
		public Map emptyMap;
		
		public WithMaps() {}
		
		public WithMaps(int index) {
			this.index = index;
		}
		
		public static WithMaps create() {
			WithMaps withMaps = new WithMaps();
			withMaps.map = new LinkedHashMap();
			withMaps.map.put(true, false);
			withMaps.map.put(12, 16);
			withMaps.map.put(2344556678889123L, 2344556678889000L);
			withMaps.map.put(34.0, 38.0);
			withMaps.map.put("asd", "cfr");
			withMaps.map.put("emptyValue", null);
			withMaps.map.put(null, "emptyKey");
			withMaps.map.put(new WithMaps(1), new WithMaps(2));
			withMaps.map.put(new int[] {1, 2}, new double[] {3.0, 4.0});
			withMaps.map.put(new Object[] {1, 2}, new Object[] {new WithMaps(3)});
			withMaps.emptyMap = new HashMap();
			return withMaps;
		}
		
		@Override
		public String getDesiredSerializationString() {
			return "{ \"map\" :" +
					" { \"#map\" : [" +
						"[true, false]," +
						"[12, 16]," +
						"[2344556678889123, 2344556678889000]," +
						"[34.0, 38.0]," +
						"[\"asd\", \"cfr\"]," +
						"['emptyValue', null]," +
						"[null, 'emptyKey']," +
						"[{ \"index\" : 1, \"@class\" : \"li.tau.serializer.test.SerializedObjects$WithMaps\"}, { \"index\" : 2, \"@class\" : \"li.tau.serializer.test.SerializedObjects$WithMaps\"}]," +
						"[{'#array':[1,2],'@class':'int-array'}, {'#array':[3.0, 4.0],'@class':'double-array'}]," +
						"[{\"#array\" : [ { \"@class\" : \"int\", \"#value\" : 1}, { \"@class\" : \"int\", \"#value\" : 2}], \"@class\" : \"object-array\"}, { \"#array\" : [ { \"index\" : 3, \"@class\" : \"li.tau.serializer.test.SerializedObjects$WithMaps\"}], \"@class\" : \"object-array\"}]]," +
						" \"@class\" : \"linked-hash-map\"}," +
					" \"emptyMap\" : { }," +
					" \"@class\" : \"li.tau.serializer.test.SerializedObjects$WithMaps\"}";
		}
		
	}

}
