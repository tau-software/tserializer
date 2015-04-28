package li.tau.serializer.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TSerializerAlias {

	public String value();

	public Mode mode() default Mode.BOTH;

}
