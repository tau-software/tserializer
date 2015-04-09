package li.tau.tserializer.test.client;

import li.tau.tserializer.client.TSerializer;
import li.tau.tserializer.client.json.JSONSerializerImpl;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

@Ignore
public class GwtTestJSON extends GWTTestCase {
	
	TSerializer serializer;
	Foo foo;
	
	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		serializer = GWT.create(TSerializer.class);
		this.foo = Foo.get();
	}

	@Test
	public void test() {
		assertTrue(serializer instanceof JSONSerializerImpl);
		
		//TODO null value
		
		//TODO arrays
		
		check("true", foo.bool);
		check("true", foo.boolBoxed);
		
		check("{\"#value\":22, \"@class\":\"byte\"}", foo.bt);
		check("{\"#value\":22, \"@class\":\"byte\"}", foo.btBoxed);

		//TODO byte array
		//TODO char
		//TODO chararray
		
		check("456774543443.4553", foo.dbl);
		check("456774543443.4553", foo.dblBoxed);

		check("{\"#value\":4563443.5, \"@class\":\"float\"}", foo.flt);
		check("{\"#value\":4563443.5, \"@class\":\"float\"}", foo.fltBoxed);
		
		check("{\"#value\":12345678, \"@class\":\"int\"}", foo.i);
		check("{\"#value\":12345678, \"@class\":\"int\"}", foo.iBoxed);

		check("{\"#value\":2344556678888786, \"@class\":\"long\"}", foo.l);
		check("{\"#value\":2344556678888786, \"@class\":\"long\"}", foo.lBoxed);

		check("{\"#value\":1445, \"@class\":\"short\"}", foo.s);
		check("{\"#value\":1445, \"@class\":\"short\"}", foo.sBoxed);
		
		check("\"asd\"", foo.string);
		
		//TODO throwable
		
		//TODO primitive, object, multidimensional arrays, arrays of internal classes
	}
	
	@Test
	public void testEnum() {
		check("{\"#value\":\"SOME_ENUM\", \"@class\":\"li.tau.tserializer.client.Foo$Bar\"}", foo.bar);
	}
	
	@Test
	public void testDate() {
		check("{\"$date\":\"1985-03-09T01:02:03.000Z\"}", foo.date);
	}
	
	@Test
	public void testIntArray() {
		String jsonArray = "{\"#array\":[1,2,3], \"@class\":\"int-array\"}";
		checkSerialization(jsonArray, foo.intArray);
		int[] array = (int[]) serializer.fromString(jsonArray);
		assertEquals(1, array[0]);
		assertEquals(2, array[1]);
		assertEquals(3, array[2]);
	}
	
	@Test
	public void testEmptyObject() {
		String jsonString = "{\"bool\":false, \"bt\":0, \"dbl\":0, \"flt\":0, \"i\":0, \"l\":0, \"s\":0, \"@class\":\"li.tau.tserializer.client.Foo\"}";
		checkSerialization(jsonString, new Foo());
		assertEquals(jsonString, serializer.toString(serializer.fromString(jsonString)));
	}
	
	@Test
	public void testObject() {
		String fooJSON = "{\"string\":\"asd\", \"bool\":true, \"boolBoxed\":true, \"bt\":22, \"btBoxed\":22, \"dbl\":456774543443.4553, \"dblBoxed\":456774543443.4553, \"flt\":4563443.5, \"fltBoxed\":4563443.5, \"i\":12345678, \"iBoxed\":12345678, \"l\":2344556678888786, \"lBoxed\":2344556678888786, \"s\":1445, \"sBoxed\":1445, \"date\":{\"$date\":\"1985-03-09T01:02:03.000Z\"}, \"intArray\":[1,2,3], \"bar\":\"SOME_ENUM\", \"@class\":\"li.tau.tserializer.client.Foo\"}";
		
		checkSerialization(fooJSON, foo);
		assertEquals("Deserialization error", fooJSON, serializer.toString(serializer.fromString(fooJSON)));
	}

	protected void check(String json, Object object) {
		checkSerialization(json, object);
		checkDeserialization(json, object);
	}
	
	
	protected void checkSerialization(String json, Object object) {
		assertEquals("Serialization error", json, serializer.toString(object));
	}

	protected void checkDeserialization(String json, Object object) {
		assertEquals("Deserialization error", object, serializer.fromString(json));
	}

	@Override
	public String getModuleName() {
		return "li.tau.tserializer.JSONSerializer";
	}

}
