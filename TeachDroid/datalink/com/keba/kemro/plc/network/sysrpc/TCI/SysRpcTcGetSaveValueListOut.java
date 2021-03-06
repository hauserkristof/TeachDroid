package com.keba.kemro.plc.network.sysrpc.TCI;

import com.keba.jrpc.rpc.*;
import java.io.*;

public class SysRpcTcGetSaveValueListOut implements XDR {
	public SysRpcTcValue[] value;  //variable length with max length of TCI.rpcChunkLen
	public int value_count; //countains the number of elements
	public boolean retVal;

	public SysRpcTcGetSaveValueListOut () {
		value = new SysRpcTcValue[TCI.rpcChunkLen];
		for (int for_i = 0; for_i < TCI.rpcChunkLen; for_i++) {
			value[for_i] = new SysRpcTcValue();
		}
	}

	public void write (RPCOutputStream out) throws RPCException, IOException {
		out.writeInt(value_count);
		for (int for_i = 0; for_i < value_count; for_i++) {
			value[for_i].write(out);
		}
		out.writeBool(retVal);
	}

	public void read (RPCInputStream in) throws RPCException, IOException {
		value_count = in.readInt();
		for (int for_i = 0; for_i < value_count; for_i++) {
			value[for_i].read(in);
		}
		retVal = in.readBool();
	}
}