package com.asksunny.cli.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.asksunny.cli.utils.annotation.CLIOptionBinding;

/**
 * This utility class is created for easier and cleanner use of Apache common-cli sub-project,
 * Pojo class need to be annotated with com.asksunny.cli.utils.annotation.CLIOptionBinding
 * @author Sunny
 *
 */
public class CLIOptionAnnotationBasedBinder {
	
	
	public static Options getOptions(Object pojo) throws CLIOptionBindingException
	{
		Options options = new Options();		
		try{
		Class<? extends Object> clazz = pojo.getClass();
		Field[] fields = clazz.getDeclaredFields();			
		for (Field field : fields) {				
			if (field.isAnnotationPresent(CLIOptionBinding.class))
			{
				CLIOptionBinding cliBinding = field
						.getAnnotation(CLIOptionBinding.class);
				Option opt = new Option(String.valueOf(cliBinding.shortOption()), cliBinding.longOption(), cliBinding.hasValue(), cliBinding.description());
				options.addOption(opt);		
				
			}
		}
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(CLIOptionBinding.class)
					&& method.getParameterTypes().length == 1) {
				CLIOptionBinding cliBinding = method
						.getAnnotation(CLIOptionBinding.class);
				Option opt = new Option(String.valueOf(cliBinding.shortOption()), cliBinding.longOption(), cliBinding.hasValue(), cliBinding.description());
				options.addOption(opt);		
			}
		}	
		}catch(Throwable ex){
			throw new CLIOptionBindingException(
					"Failed to generate cli option from POJO:" + pojo.toString());
		}
		return options;
	}
	
	
	
	public static CommandLine bindPosix(Options options, String[] args,
			Object pojo) throws CLIOptionBindingException {
		CommandLineParser parser = new PosixParser();
		CommandLine cli = null;
		try {
			cli = parser.parse(options, args);
			Class<? extends Object> clazz = pojo.getClass();
			Field[] fields = clazz.getDeclaredFields();			
			for (Field field : fields) {				
				if (field.isAnnotationPresent(CLIOptionBinding.class)) {
					
					if(field.isAccessible()){
						
						bindByField( pojo,  cli,  field);
					}else{
						
					Method m=	getSetter( clazz,  field);
					
					  if(m!=null){
						  bindByMethod( pojo,  cli,  m, field
									.getAnnotation(CLIOptionBinding.class));
					  }
					}

				}
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(CLIOptionBinding.class)
						&& method.getParameterTypes().length == 1) {
					 bindByMethod( pojo,  cli,  method, null);
				}
			}
		} catch (Throwable e) {
			throw new CLIOptionBindingException(
					"Failed to bind cli option to POJO:" + pojo.toString(), e);
		}

		return cli;

	}
	
	private static Method getSetter(Class<? extends Object> clazz, Field field)
	{
		
		String name = field.getName();
		name = String.format("%C%s", name.charAt(0), name.length()>1?name.substring(1):"");
		Method method = null;
		try{
			String setter = String.format("set%s", name);
			
			method = clazz.getMethod(setter, field.getType());	
			
			if(method.getModifiers()!=Modifier.PUBLIC){
				method = null;				
			}
		}catch(Exception ex){
			method = null;
		}		
		return method;
	}
	
	private static void bindByField(Object pojo, CommandLine cli, Field field) throws Exception
	{
		CLIOptionBinding cliBinding = field
				.getAnnotation(CLIOptionBinding.class);

		char shortOpt = cliBinding.shortOption();
		String longOpt = cliBinding.longOption();
		if (cliBinding.values()) {
			if (field.getType().isArray()) {
				field.set(pojo, cli.getArgs());
			} else if (cli.getArgs().length > 0) {
				field.set(
						pojo,
						getTypedvalue(field.getType(),
								cli.getArgs()[0],
								cliBinding.format()));

			}
		} else if (shortOpt != 0 && cli.hasOption(shortOpt)) {
			String val = (cli.getOptionValue(shortOpt) == null) ? "true"
					: cli.getOptionValue(shortOpt);
			field.set(
					pojo,
					getTypedvalue(field.getType(), val,
							cliBinding.format()));
		} else if (!longOpt.isEmpty() && cli.hasOption(longOpt)) {
			String val = (cli.getOptionValue(longOpt) == null) ? "true"
					: cli.getOptionValue(longOpt);

			field.set(
					pojo,
					getTypedvalue(field.getType(), val,
							cliBinding.format()));
		}
	}
	
	private static void bindByMethod(Object pojo, CommandLine cli, Method method, CLIOptionBinding fieldBinding) throws Exception
	{
		CLIOptionBinding cliBinding = (fieldBinding==null)?method
				.getAnnotation(CLIOptionBinding.class):fieldBinding;		
		char shortOpt = cliBinding.shortOption();
		String longOpt = cliBinding.longOption();
		if (cliBinding.values()) {
			Class<? extends Object>[] paramTypes = method
					.getParameterTypes();
			String[] sargs = cli.getArgs();
			if (paramTypes.length == 1 && paramTypes[0].isArray()) {
				method.invoke(pojo, (Object) sargs);
			} else if (paramTypes.length <= sargs.length) {
				Object[] params = new Object[paramTypes.length];
				for (int i = 0; i < params.length; i++) {
					params[i] = getTypedvalue(paramTypes[i],
							sargs[i], "");
				}
				method.invoke(pojo, params);
			}

		} else if (shortOpt != 0 && cli.hasOption(shortOpt)) {
			String val = (cli.getOptionValue(shortOpt) == null) ? "true"
					: cli.getOptionValue(shortOpt);
			method.invoke(
					pojo,
					getTypedvalue(method.getParameterTypes()[0],
							val, cliBinding.format()));
		} else if (!longOpt.isEmpty() && cli.hasOption(longOpt)) {
			String val = (cli.getOptionValue(longOpt) == null) ? "true"
					: cli.getOptionValue(longOpt);
			method.invoke(
					pojo,
					getTypedvalue(method.getParameterTypes()[0],
							val, cliBinding.format()));
		}
	}

	private static Object getTypedvalue(Class<? extends Object> type, String strVal, String format)
			throws java.text.ParseException {
		Object obj = null;
		if (strVal == null)
			return null;
		if (type == int.class || type == Integer.class) {
			obj = Integer.valueOf(strVal);
		} else if (type == String.class) {
			obj = strVal;
		} else if (type == long.class || type == Long.class) {
			obj = strVal;
		} else if (type == double.class || type == Double.class) {
			obj = Double.valueOf(strVal);
		} else if (type == short.class || type == Short.class) {
			obj = Short.valueOf(strVal);
		} else if (type == byte.class || type == Byte.class) {
			obj = Byte.valueOf(strVal);
		} else if (type == boolean.class || type == Boolean.class) {
			obj = Boolean.valueOf(strVal);
		} else if (type == float.class || type == Float.class) {
			obj = Float.valueOf(strVal);
		} else if (type == BigDecimal.class) {
			obj = new BigDecimal(strVal);
		}else if (type == Date.class ) {
			SimpleDateFormat sdf = (format == null) ? new SimpleDateFormat(
					"yyyy-MM-dd") : new SimpleDateFormat(format);
			obj = sdf.parse(strVal);			
		}else if (type == java.sql.Date.class ) {
			SimpleDateFormat sdf = (format == null) ? new SimpleDateFormat(
					"yyyy-MM-dd") : new SimpleDateFormat(format);
			obj = new java.sql.Date(sdf.parse(strVal).getTime());			
		}else if (type == Calendar.class ) {
			SimpleDateFormat sdf = (format == null) ? new SimpleDateFormat(
					"yyyy-MM-dd") : new SimpleDateFormat(format);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(strVal));
			obj = cal;			
		}else if (type == Time.class ) {
			SimpleDateFormat sdf = (format == null) ? new SimpleDateFormat(
					"HH:mm:ss") : new SimpleDateFormat(format);
			obj = new java.sql.Time(sdf.parse(strVal).getTime());			
		} else if ( type == Timestamp.class) {
			SimpleDateFormat sdf = (format == null) ? new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss") : new SimpleDateFormat(format);
			obj = new Timestamp(sdf.parse(strVal).getTime());
		} else {
			obj = strVal;
		}
		return obj;
	}
}
