import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
				DatagramSocket clientSocket = new DatagramSocket();
				
				Scanner scan = new Scanner(System.in);
				String msg;
				boolean exit = false;
				
				// server info
				
				InetAddress serverIP = InetAddress.getByName("localhost");
				int serverPort = 22100;
				
				
				byte[] request;
				byte[] response = new byte[4096];
				
				while(!exit)
				{
					System.out.println("start client");
					
					String input = scan.nextLine();
					
					request = input.getBytes();
					
					DatagramPacket clientPacket = new DatagramPacket(request, request.length, serverIP, serverPort);
					
					clientSocket.send(clientPacket);
					
					System.out.println("Done sent");
					
					if(input.equals("quit"))
					{
						exit = true;
						break;
					}
					// get response
					DatagramPacket serverPacket = new DatagramPacket(response, response.length);
					
					clientSocket.receive(serverPacket);
					
					
					msg = new String(serverPacket.getData());
					
					System.out.println("=============================");
					System.out.println(msg.trim());
					System.out.println("=============================");
					
					request = new byte[4096];
			}
			
			clientSocket.close();
			scan.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		

	}

}
