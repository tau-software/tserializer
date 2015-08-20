package li.tau.tserializer.client;

@SuppressWarnings("serial")
public class TSerializerException extends RuntimeException {
	
	public TSerializerException(String message) {
		super(message);
	}
	
	public TSerializerException(String message, Throwable cause) {
		super(message, cause);
	}

}
