package li.tau.tserializer.client.xml;


import com.google.gwt.xml.client.Element;

public interface XMLSerializator<S, T extends S> {

	Element serialize(T instance, Element classNode, String className);
	
	abstract class ExternalSerializator<S, T extends S> implements XMLSerializator<S, T> {
		
		protected final XMLSerializer serializer;
		
		public ExternalSerializator(XMLSerializer serializer) {
			this.serializer = serializer;
		}
		
	}

}
