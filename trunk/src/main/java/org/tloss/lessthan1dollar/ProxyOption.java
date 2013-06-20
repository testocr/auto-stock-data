package org.tloss.lessthan1dollar;

public class ProxyOption {
	boolean userProxy = false;
	String proxy;
	int port;

	public boolean isUserProxy() {
		return userProxy;
	}

	public void setUserProxy(boolean userProxy) {
		this.userProxy = userProxy;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
