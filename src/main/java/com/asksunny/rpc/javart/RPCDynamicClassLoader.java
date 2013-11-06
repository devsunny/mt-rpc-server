package com.asksunny.rpc.javart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * This class is used to dynamically deploy remote jar to runtime in isolated classloader and able to remove jar file afterward.
 * @author SunnyLiu
 *
 */
public class RPCDynamicClassLoader extends URLClassLoader {

	File tmpJarFile = null;
	
	public RPCDynamicClassLoader(URL[] urls, ClassLoader parent,
			URLStreamHandlerFactory factory) {
		super(urls, parent, factory);		
	}

	public RPCDynamicClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);		
	}

	public RPCDynamicClassLoader(URL[] urls) {
		super(urls);		
	}

	
	public static RPCDynamicClassLoader  getClassLoader(byte[] jarContent) throws IOException  {		
		File tmpFile = null;
		if(jarContent!=null && jarContent.length>0){
			tmpFile = File.createTempFile("rpc_", ".jar");
			FileOutputStream fout = new FileOutputStream(tmpFile);
			try{
				fout.write(jarContent);
				fout.flush();
			}finally{
				if(fout!=null) fout.close();
			}
		}
		@SuppressWarnings("resource")
		RPCDynamicClassLoader loader = (tmpFile!=null)?
				new RPCDynamicClassLoader(new URL[]{tmpFile.toURI().toURL()}, RPCDynamicClassLoader.class.getClassLoader())
				: new RPCDynamicClassLoader(new URL[]{}, RPCDynamicClassLoader.class.getClassLoader())
		;		
		loader.setTmpJarFile(tmpFile);
		return loader;
	}

	public File getTmpJarFile() {
		return tmpJarFile;
	}

	public void setTmpJarFile(File tmpJarFile) {
		this.tmpJarFile = tmpJarFile;
	}
	
	
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {		
		return super.findClass(name);
	}

	public void close()
	{
		try {
			super.close();
		} catch (IOException e) {
			;
		}finally{
			if(tmpJarFile!=null) tmpJarFile.delete();
		}
		
	}

	
}
