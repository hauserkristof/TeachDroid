package com.keba.kemro.teach.network.rpc.protocol;

import com.keba.jrpc.rpc.*;
import java.io.*;

public class RpcTcGetFirstDirEntryChunkIn implements XDR {
	public String dirPath;
	public RpcTcDirEntryKind kind;

	public RpcTcGetFirstDirEntryChunkIn () {
		kind = new RpcTcDirEntryKind();
	}

	public void write (RPCOutputStream out) throws RPCException, IOException {
		out.writeString(dirPath);
		kind.write(out);
	}

	public void read (RPCInputStream in) throws RPCException, IOException {
		dirPath = in.readString();
		kind.read(in);
	}
}