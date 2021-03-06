package com.keba.kemro.teach.network.rpc.protocol;

import com.keba.jrpc.rpc.*;
import java.io.*;

public class RpcTcConvertExeUnitHandleOut implements XDR {
	public int scopeHnd;
	public boolean retVal;

	public RpcTcConvertExeUnitHandleOut () {
	}

	public void write (RPCOutputStream out) throws RPCException, IOException {
		out.writeInt(scopeHnd);
		out.writeBool(retVal);
	}

	public void read (RPCInputStream in) throws RPCException, IOException {
		scopeHnd = in.readInt();
		retVal = in.readBool();
	}
}