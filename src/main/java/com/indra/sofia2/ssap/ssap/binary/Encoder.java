package com.indra.sofia2.ssap.ssap.binary;

public interface Encoder {

	
	public String encode(byte[] data);
	
	public byte[] decode(String data);
	
}
