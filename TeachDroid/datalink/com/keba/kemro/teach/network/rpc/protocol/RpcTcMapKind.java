package com.keba.kemro.teach.network.rpc.protocol;

import com.keba.jrpc.rpc.*;
import java.io.*;

public class RpcTcMapKind implements XDR {
	public static final int rpcMapVoid = 0;
	public static final int rpcMapInternal = 1;
	public static final int rpcMapExternal = 2;
	public int value;



	public RpcTcMapKind () {
	}
	public void write (RPCOutputStream out) throws RPCException, IOException {
		out.writeInt(value); 
	}
	public void read (RPCInputStream in) throws RPCException, IOException {
		value = in.readInt(); 
	}
}