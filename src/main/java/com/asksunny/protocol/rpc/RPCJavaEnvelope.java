package com.asksunny.protocol.rpc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.asksunny.cli.utils.CLICommand;
import com.asksunny.io.utils.StreamCopier;

public class RPCJavaEnvelope extends AbstractRPCEnvelope {

	byte[] jarSource;
	String className;

	public RPCJavaEnvelope() {
		super.envelopeType = (RPC_ENVELOPE_TYPE_JAVA);
		super.rpcType = RPC_TYPE_REQUEST;
	}

	public static RPCJavaEnvelope createJavaEnvelope(CLICommand rcmd) {

		RPCJavaEnvelope envelope = new RPCJavaEnvelope();
		envelope.setRpcType(RPC_TYPE_REQUEST);
		String jar = rcmd.peek();
		if (jar.endsWith(".jar")) {
			envelope.setJarSource(rcmd.shift());
		}
		envelope.setClassName(rcmd.shift());
		for (String cmd : rcmd.getCmdArray()) {
			
			envelope.addRpcObjects(cmd);
		}
		return envelope;

	}

	public byte[] getJarSource() {
		return jarSource;
	}

	public RPCJavaEnvelope setJarSource(Object jarSource) {
		if (jarSource != null) {
			if (jarSource instanceof String || jarSource instanceof File) {
				File jarFile = null;
				if (jarSource instanceof String) {
					jarFile = new File((String) jarSource);
				} else {
					jarFile = (File) jarSource;
				}
				FileInputStream fin = null;
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				try {
					fin = new FileInputStream(jarFile);
					StreamCopier.copy(fin, bout);
					this.jarSource = bout.toByteArray();
				} catch (Exception ex) {
					throw new RuntimeException("File to load Jar file:"
							+ jarSource, ex);
				} finally {
					if (fin != null) {
						try {
							fin.close();
						} catch (IOException e) {
							;
						}
					}
				}
			} else if (jarSource instanceof byte[]) {
				this.jarSource = (byte[]) jarSource;
			} else if (jarSource instanceof InputStream) {
				InputStream fin = (InputStream) jarSource;
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				try {
					StreamCopier.copy(fin, bout);
					this.jarSource = bout.toByteArray();
				} catch (IOException ex) {
					throw new RuntimeException(
							"File to load Jar from inputstream:", ex);
				} finally {
					if (fin != null) {
						try {
							fin.close();
						} catch (IOException e) {
							;
						}
					}
				}

			}

		}
		return this;
	}

	public String getClassName() {
		return className;
	}

	public RPCJavaEnvelope setClassName(String className) {
		this.className = className;
		return this;
	}

}
