package org.tloss.common.db;

import java.io.PrintWriter;
import java.net.InetAddress;

import org.apache.derby.drda.NetworkServerControl;

public class NetworkServerUtil  {

    private int portNum;
    private NetworkServerControl serverControl;
	private PrintWriter pw;

    public NetworkServerUtil(int port, PrintWriter pw) {

        this.portNum = port;
		this.pw = pw;
        try {
          serverControl = new
			  NetworkServerControl(InetAddress.getByName("localhost"), port);
          pw.println("Derby Network Server created");
        } catch (Exception e) {
            e.printStackTrace();
          }
    }

    /**
     * trace utility of server
     */
    public void trace(boolean onoff) {
      try {
        serverControl.trace(onoff);
      } catch (Exception e) {
          e.printStackTrace();
        }
    }


	/**
	 * Try to test for a connection
	 * Throws exception if unable to get a connection
	 */
	public void testForConnection()
	throws Exception {
		serverControl.ping();
	}


    /**
     * Shutdown the NetworkServer
     */
    public void shutdown() {
        try {
            serverControl.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	/**
	 * Start Derby Network server
	 * 
	 */
    public void start() {
        try {
			serverControl.start(pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
		PrintWriter printWriter  = new PrintWriter(System.out);
		NetworkServerUtil networkServerUtil =  new NetworkServerUtil(12345, printWriter);
		networkServerUtil.start();
	}
}

