package org.theta.framework.core.trace;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

@Target(value = { TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceTrace {

}
