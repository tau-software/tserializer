package li.tau.serializer.client.xml;


import com.google.gwt.xml.client.Element;

public interface XMLSerializator<S, T extends S> {

	public Element serialize(T instance, Element classNode, String className);
	
	public abstract static class ExternalSerializator<S, T extends S> implements XMLSerializator<S, T> {
		
		protected final XMLSerializer serializer;
		
		public ExternalSerializator(XMLSerializer serializer) {
			this.serializer = serializer;
		}
		
	}

}
