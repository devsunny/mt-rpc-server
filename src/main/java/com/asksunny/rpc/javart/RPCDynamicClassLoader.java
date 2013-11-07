package com.asksunny.rpc.javart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to dynamically deploy remote jar to runtime in isolated
 * classloader and able to remove jar file afterward.
 * 
 * @author SunnyLiu
 * 
 */
public class RPCDynamicClassLoader extends URLClassLoader {

	final static Logger log = LoggerFactory.getLogger(JavaRPCRuntime.class);

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

	public static RPCDynamicClassLoader getClassLoader(byte[] jarContent)
			throws IOException {
		File tmpFile = null;
		if (jarContent != null && jarContent.length > 0) {
			tmpFile = File.createTempFile("rpc_", ".jar");
			FileOutputStream fout = new FileOutputStream(tmpFile);
			try {
				fout.write(jarContent);
				fout.flush();
			} finally {
				if (fout != null)
					fout.close();
			}
		}

		if (log.isDebugEnabled() && tmpFile != null)
			log.debug("Temp Jar file:{}", tmpFile);
		@SuppressWarnings("resource")
		RPCDynamicClassLoader loader = (tmpFile != null) ? new RPCDynamicClassLoader(
				new URL[] { tmpFile.toURI().toURL() })
				: new RPCDynamicClassLoader(new URL[] {});
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
		if (log.isDebugEnabled())
			log.debug("FindClass:{}", name);
		try {
			return super.findClass(name);
		} catch (Exception ex) {
			return this.getClass().getClassLoader().loadClass(name);
		}
	}

	public void close() {
		try {
			super.close();
		} catch (IOException e) {
			;
		} finally {
			if (tmpJarFile != null)
				tmpJarFile.delete();
		}

	}

}
