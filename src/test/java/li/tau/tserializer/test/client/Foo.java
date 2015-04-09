package li.tau.tserializer.test.client;

import java.util.Date;

import li.tau.tserializer.client.annotation.TDeserializable;
import li.tau.tserializer.client.annotation.TSerializable;

public class Foo implements TSerializable, TDeserializable {
	
	public enum Bar implements TDeserializable {
		QWE, SOME_ENUM, ZXC; 
	}

	public String string;
	
	public boolean bool;
	public Boolean boolBoxed;
	
	public byte bt;
	public Byte btBoxed;
	
	public double dbl;
	public Double dblBoxed;
	
	public float flt;
	public Float fltBoxed;

	public int i;
	public Integer iBoxed;
	
	public long l;
	public Long lBoxed;

	public short s;
	public Short sBoxed;
	
	public Date date;
	
	public int[] intArray;
	
	public Bar bar;

	
	
	@SuppressWarnings("deprecation")
	public static Foo get() {
		Foo foo = new Foo();
		foo.string = "asd";
		
		foo.bool = true;
		foo.boolBoxed = true;
		
		foo.bt = 22;
		foo.btBoxed = 22;
		
		foo.dbl = 456774543443.4553;
		foo.dblBoxed = 456774543443.4553;
		
		foo.flt = 4563443.5f;
		foo.fltBoxed = 4563443.5f;

		foo.i = 12345678;
		foo.iBoxed = 12345678;
		
		foo.l = 2344556678888786l;
		foo.lBoxed = 2344556678888786l;

		foo.s = 1445;
		foo.sBoxed = 1445;
		
		foo.date = new Date(85, 2, 9, 1, 2, 3);
		
		foo.intArray = new int[]{1, 2, 3};
		
		foo.bar = Bar.SOME_ENUM;
		
		return foo;
	}

}
