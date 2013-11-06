package com.asksunny.cli.utils.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is created for easier and cleanner use of Apache common-cli sub-project
 * @author Sunny
 *
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface CLIOptionBinding 
{
	/**
	 * Short option of Posix options
	 * @return
	 */
	char shortOption() default 0;
	
	/**
	 * Long option name of POSIX options
	 * @return
	 */
	String longOption() default "";	
	
	/**
	 * This is only applicable to java.util.Date, java.sql.Date, java.sql.Time, java.sql.Timestamp
	 * @return
	 */
	String format() default "";
	
	
	boolean hasValue() default false;
	
	
	String description() default "";
	
	/**
	 * The argument list in the end of Posix options;
	 * @return
	 */
	boolean values() default false;
}
