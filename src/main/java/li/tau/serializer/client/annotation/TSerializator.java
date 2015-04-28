package li.tau.serializer.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import li.tau.serializer.client.xml.XMLSerializator.ExternalSerializator;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TSerializator {

	public Class<? extends ExternalSerializator<?, ?>> value();

}
