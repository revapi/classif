package org.revapi.classif.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Because I know of no standardized way of expressing a nullable reference, let's roll our own, because why not?
 */
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, TYPE_PARAMETER, TYPE_USE})
@Documented
public @interface Nullable {
}
