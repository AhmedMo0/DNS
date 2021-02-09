import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.*;





/**
 * @author Servers port
 * 
 * 					@ Local: 22100
 * 					@ root : 22200
 * 					@ TLD : 22300
 * 					@ Au: 22400
 * 
 * 
 * */


class LocalD extends Thread{
	
	DatagramSocket serverSocket;
	String serverName;
	int serverPort, nextPort;
	
	// file info containers
	ArrayList<String> list;		
	HashMap<String, ArrayList<String> > hash;
	
	
	byte[] requestBytes ;
	byte[] responseBytes;
	
	String request, QType, Cname, Aliase, reqIP;
	
	InetAddress clientIP = null;
	int clientPort;
	
	
	
	boolean exit = false;
	
	LocalD(String sName, int port, int nxtPort){
		
		try {
			
			// initialize Socket
			serverSocket = new DatagramSocket(port);
			serverName = sName;
			nextPort = nxtPort;
			request = "";
			QType = "";
			Cname = "";
			Aliase = "";
			reqIP = "";
			
			
		
			hash = new HashMap<String, ArrayList<String> >();
						
			
			try {
				
				/*
				   * for (Map.Entry<KeyType, ValueType> entry : map.entrySet())
			    * */
				
				  // get file
				
				  File file = new File(serverName + "_dns_table.txt");  
				  
				  // buffer to read data
				  BufferedReader bfr = new BufferedReader(new FileReader(file)); 
				  String st; 
		
					  // read each line from the file
					  
					  while ((st = bfr.readLine()) != null) 
					    {
						  String[] data = st.trim().split(" ");
						  //System.out.println(data[0]);
						  list= new ArrayList<>();
						  
						  Collections.addAll(list, data);  
						  list.remove(0);
						  hash.put(data[0], list); // make hashMap ready  
					        
					    }
					  
					  bfr.close();
					  
					  
			     
			    } catch (Exception e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
			
		}catch(IOException e) {}
	
	}
	
	
	
	@Override
	public void run() {
		try {
			
			
			
			while(!exit) {
				// Create Client packet
				requestBytes = new byte[4096];
				responseBytes = new byte[4096];
		
				DatagramPacket clientPacket = new DatagramPacket(requestBytes,requestBytes.length);
				String request;
				
				
				// Receive Packet
				synchronized (clientPacket) {
					serverSocket.receive(clientPacket);
					request = new String(clientPacket.getData()).trim();
				}
				
				
				
				//Extract client information
				
				clientIP = clientPacket.getAddress();
			    clientPort = clientPacket.getPort();
			    
			  
			    //////////////////////////////
			    
			
			    boolean req = true;
			    boolean a = false;
		    	if(hash.get(request) != null)
		    	{
		    		// check if second string in number so it's the port else it's CName
		    		if(hash.get(request).size() > 1)
			    		if ( Pattern.matches("\\b\\d+\\b", hash.get(request).get(1) ) )
			    			a = true;
		    			
		    		
		    		// URL found in map
		    		// Create a server packet
		    		if(hash.get(request).size() == 1 ||( a && hash.get(request).size() == 2) )
		    		{
		    			
		    			// this condition if response has ip with port
		    			if(a) {
		    				responseBytes = (String.format("Reply from Server: URL = %s \nIP Address = %s#%s\nQuery Type: A\nServer Name: %s DNS", request, hash.get(request).get(0), hash.get(request).get(1), serverName) ).getBytes();
		    				QType = "A";
		    				reqIP = hash.get(request).get(0);
		    			}
		    			// this condition in case if response from AU server
		    			else if(nextPort == 0) {
		    				responseBytes = (String.format("Reply from Server: URL = %s \nIP Address = %s\nQuery Type: A, NS\nServer Name: %s DNS", request, hash.get(request).get(0), serverName) ).getBytes();
		    				QType = "A, NS";
		    				reqIP = hash.get(request).get(0);
		    			}
		    			// normal response
		    			else {
		    				responseBytes = (String.format("Reply from Server: URL = %s \nIP Address = %s\nQuery Type: A\nServer Name: %s DNS", request, hash.get(request).get(0), serverName) ).getBytes();
		    				QType = "A";
		    				reqIP = hash.get(request).get(0);
		    			}
		    			
		    		} 
		    		
		    		else if(hash.get(request).size() == 2  ) 
		    		{
		    			// CName ip
		    			
		    			responseBytes = (String.format("Reply from Server: URL = %s \nIP Address = %s\nQuery Type: A, CNAME\nServer Name: %s DNS\nCanonical Name: %s\nAliases: %s", request, hash.get(request).get(0), serverName, hash.get(request).get(1),request) ).getBytes();
		    			QType = "A, CNAME";
		    			Cname = hash.get(request).get(1);
		    			Aliase = request;
		    			reqIP = hash.get(request).get(0);
		    			
		    		}
		    		
					DatagramPacket serverPacket = new DatagramPacket(responseBytes, responseBytes.length, clientIP, clientPort);
					
					// send response
					
					serverSocket.send(serverPacket);
		    	}
		    	// End Connection
		    	else if(request.compareToIgnoreCase("quit") == 0)
			    {
			    	exit = true;
			    	req = false;
			    	
			    	
			    	
			    	// tell root server to quit
			    	InetAddress rootIP = InetAddress.getByName("localhost");
			    	
		    		serverSocket.send( new DatagramPacket("quit".getBytes(), "quit".getBytes().length, rootIP, nextPort) );
			    	
			    	// close server socket
			    	serverSocket.close();
			    }
		    	
		    	else
		    	{
		    		// URL doesn't exist here so, search in other servers
		    		// Create socket to connect with root DNS
		    		
		    		
		    		if(nextPort == 0)
		    		{
		    			String s = "this URL " + request + " Not Found!!";
		    			serverSocket.send( new DatagramPacket(s.getBytes(), s.getBytes().length, clientIP, clientPort) );
		    		}
		    		else {
			    		// Send request to next server
		    			req = false; 
			    		InetAddress rootIP = InetAddress.getByName("localhost");
			    		
			    		requestBytes = request.getBytes();
			    		DatagramPacket to_nxtServer_packet = new DatagramPacket(requestBytes, requestBytes.length, rootIP, nextPort);
			    		serverSocket.send(to_nxtServer_packet);
			    		
			    		// receive from root
			    		
			    		responseBytes = new byte[4096];
			    		DatagramPacket from_nxtServer_packet = new DatagramPacket(responseBytes, responseBytes.length);
			    		
			    		synchronized (from_nxtServer_packet) {
			    			serverSocket.receive(from_nxtServer_packet);
						}
			    		
			    		
			    		// send this new packet to client
			    		
			    		DatagramPacket toClientPacket = new DatagramPacket(responseBytes, responseBytes.length, clientIP, clientPort);
						
						// send response
						
						serverSocket.send(toClientPacket);
		    		}
					
					
		    		
		    	}
		    	
		    	
		    	if(request != "" && req)
		    		System.out.println("request: "+ request);
		    	if(QType != "")
		    		System.out.println("Query Type: " + QType);
		    	if(reqIP != "")
		    		System.out.println("IP Address: " + reqIP);
		    	if(Cname != "")
		    		System.out.println("Canonical Name: " + Cname);
		    	if(Aliase != "")
		    		System.out.println("Alies: " + Aliase);
		    	
		    	if(reqIP != "")
		    		System.out.println("=====================");
		    	
		    	
		    	if(nextPort == 22200 && exit)
		    		System.out.println("Client is Disconnected");
		    
		    	
		    	
		    	request = "";
		    	QType = "";
		    	reqIP= "";
		    	Cname = "";
		    	Aliase = "";
			    
			}
			
		}catch(Exception e) {}
		
		
		
	}
	
	
	
	
	
}




public class Server {
	
	public static void main(String[] args) {
		
	        
		System.out.println("Server connected");
		
		
			// Start dns
			
			LocalD local = new LocalD("local", 22100, 22200);
			local.start();
			
			LocalD root = new LocalD("root", 22200, 22300);
			root.start();
			
			LocalD tld = new LocalD("tld", 22300, 22400);
			tld.start();
			
			LocalD au = new LocalD("authoritative", 22400, 0);
			au.start();
			
			


	}









}
