package com.asksunny.protocol.rpc;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;


import java.util.Map;

public class CLIRPCObjectFormatter implements RPCObjectFormatter {

	public CLIRPCObjectFormatter() {
		
	}
	
	public String format(RPCEnvelope obj)
	{
		StringBuilder buf = new StringBuilder();
		switch (obj.getEnvelopeType())
		{		
		case RPC_ENVELOPE_TYPE_SHELL:
			buf.append("SHELL:");
			if(obj.getRpcType()==RPC_RESPONSE_TYPE_ERROR_MSG){
				buf.append("REQUEST:");
			}else{
				buf.append("RESPONSE:");
			}
			buf.append(format(obj.getRpcObjects()));
			break;
		case RPC_ENVELOPE_TYPE_MESSAGE:
			RPCMessageEnvelope msg = (RPCMessageEnvelope)obj;
			if(msg.getMessage()!=null){
				buf.append(new String(msg.getMessage()));
			}
			break;
		}		
		return buf.toString();
	}
	public String format(java.util.List<RPCObject> objs)
	{
		StringBuilder buf = new StringBuilder();
		
		int size = (objs!=null)?objs.size():0;
		if(size>0){
			int idx = 0;
			buf.append("{");
			for(RPCObject obj:objs)
			{
				if(obj.getValue()==null){
					buf.append(idx++).append("->").append("null");	
				}else{
				    buf.append(idx++).append("->").append(format(obj));		
				}
				if(idx<size) buf.append(",");
			}
			buf.append("}");
		}
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	public String format(RPCObject obj) {
		StringBuilder buf = new StringBuilder();		
		switch (obj.getObjectType()) {
		case RPC_OBJECT_TYPE_COLLECTION_INT:
			int[] ints = (int[]) obj.getValue();			
			buf.append("[");
			for (int i = 0; i < ints.length; i++) {
				buf.append(ints[i]);
				if (i < ints.length - 1)
					buf.append(",");
			}	
			buf.append("]");
			break;
		case RPC_OBJECT_TYPE_COLLECTION_BOOLEAN:
			boolean[] bools = (boolean[]) obj.getValue();
			buf.append("[");
			for (int i = 0; i < bools.length; i++) {
				buf.append(bools[i]);
				if (i < bools.length - 1)
					buf.append(",");
			}	
			buf.append("]");
			break;
		case RPC_OBJECT_TYPE_COLLECTION_LONG:
			long[] longs = (long[]) obj.getValue();
			buf.append("[");
			for (int i = 0; i < longs.length; i++) {
				buf.append(longs[i]);
				if (i < longs.length - 1)
					buf.append(",");
			}	
			buf.append("]");
			break;
		case RPC_OBJECT_TYPE_COLLECTION_DOUBLE:
			double[] doubles = (double[]) obj.getValue();
			buf.append("[");
			for (int i = 0; i < doubles.length; i++) {
				buf.append(doubles[i]);
				if (i < doubles.length - 1)
					buf.append(",");
			}
			buf.append("]");
			break;
		case RPC_OBJECT_TYPE_COLLECTION_STRING:
			String[] strs = (String[]) obj.getValue();			
			buf.append("[");
			for (int i = 0; i < strs.length; i++) {
				String val = formatString(strs[i]);		
				buf.append(val);
				if (i < strs.length - 1)
					buf.append(",");
			}
			buf.append("]");
			break;
		case RPC_OBJECT_TYPE_MAP_STRING:
			Map<String, String> map = (Map<String, String>) obj.getValue();			
			buf.append("{");
			int s = map.size();
			int p = 0;
			for (String s1 : map.keySet()) {
				String valstr = map.get(s1);
				String key = formatString(s1);
				valstr = formatString(valstr);
				buf.append(key).append("=>").append(valstr);
				p++;
				if(p<s) buf.append(",");
			}
			buf.append("}");
		case RPC_OBJECT_TYPE_STRING:
			String val = formatString(obj.getValue());
			buf.append(val);
			break;	
		case RPC_OBJECT_TYPE_BINARY:
			RPCBinaryObject bobj = (RPCBinaryObject)obj;
			if(bobj.getName()!=null && ( bobj.getName().equalsIgnoreCase("STDOUT") ||bobj.getName().equalsIgnoreCase("STDERR") ))
			{
				buf.append(new String((byte[])bobj.getValue()));
			}else{
				buf.append("BINARY");
			}
			break;
		default:
			String num = obj.getValue()==null?"0":obj.getValue().toString();
			buf.append(num);
		}
		return buf.toString();
	}
	
	protected String formatString(Object val)
	{
		StringBuilder buf = new StringBuilder();
		if(val==null){
			buf.append("null");
		}else{
			buf.append("\"");
			String sval = val.toString();
			int len = sval.length();
			for (int i = 0; i < len; i++) {
				char c = sval.charAt(i);
				switch(c)
				{
				case '\n':
					buf.append('\\').append('n');
					break;
				case '"':
					buf.append('\\').append('"');
					break;
				case '\\':
					buf.append('\\').append('\\');
					break;
				default:
					buf.append(c);
					break;
				}
				
			}
			buf.append("\"");
		}
		return buf.toString();
	}

}
