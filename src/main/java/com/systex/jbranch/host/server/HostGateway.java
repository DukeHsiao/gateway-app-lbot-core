package com.systex.jbranch.host.server;

/**
 * @author Alex Lin
 * @version 2011/05/16 11:20 PM
 */
public interface HostGateway {

	void send(Object content) throws Exception;
	
	Object receive() throws Exception;
}
