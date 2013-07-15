package org.demo;

import java.util.List;

public interface SerialConnectionCallback {
	public void afterRead(List<byte[]> list , int from);
}
