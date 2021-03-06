package com.keba.kemro.teach.network.rpc.protocol;

import com.keba.jrpc.rpc.*;
import java.io.*;

public class RpcTcGetFirstNodeIn implements XDR {
	public int scopeHnd;
	public RpcTcNodeKind kind;

	public RpcTcGetFirstNodeIn () {
		kind = new RpcTcNodeKind();
	}

	public void write (RPCOutputStream out) throws RPCException, IOException {
		out.writeInt(scopeHnd);
		kind.write(out);
	}

	public void read (RPCInputStream in) throws RPCException, IOException {
		scopeHnd = in.readInt();
		kind.read(in);
	}
}