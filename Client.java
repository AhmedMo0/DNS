package Projects;

import java.io.*;
import java.net.*;

public class Client
{
    public static void main(String[] args) throws Exception
    {
        String host = "127.0.0.1";
        int port = 22000;

        boolean quit = false;

        while (!quit) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter Website Name / Enter 'quit' to exit");
            String given_URL = input.readLine();
            System.out.println("hfj");

            if (given_URL.equals("quit")) {
                quit = true;
            }
            else {
                //setting up the socket and its timeout that we're going to use to access the server
                DatagramSocket client_socket = new DatagramSocket();
                client_socket.setSoTimeout(3000);

                InetAddress IP_address = InetAddress.getByName(host);
                byte[] sent_data = new byte[1024];
                byte[] received_data = new byte[1024];

                sent_data = given_URL.getBytes();

                //sending the given-by-user website to the server
                DatagramPacket send_packet = new DatagramPacket(sent_data, sent_data.length, IP_address, port);
                client_socket.send(send_packet);

                //setting up the packet we will receive with the IP address of the given-by-user website
                DatagramPacket received_packet = new DatagramPacket(received_data, received_data.length);

                try {
                    //receiving the IP address from the server and turning it to a string
                    client_socket.receive(received_packet);
                    String server_response = new String(received_packet.getData());

                    String[] str = server_response.split("\\|");

                    System.out.println("Reply from Server is:");

                    switch (str[0]) {
                        case "local", "root" -> {
                            System.out.println("URL = " + given_URL);
                            System.out.println("IP Address = " + str[1]);
                            System.out.println("query type = A");
                            System.out.println("Server name : " + str[0] + " DNS");
                        }
                        case "TLD" -> {
                            System.out.println("URL = " + given_URL);
                            System.out.println("IP Address = " + str[1]);
                            System.out.println("query type = A, CNAME");
                            System.out.println("Server name : " + str[0] + " DNS");
                            System.out.println("Canonical name: " + str[2]);
                            System.out.println("Aliases: " + given_URL);
                        }
                        case "authoritative" -> {
                            System.out.println("URL = " + given_URL);
                            System.out.println("IP Address = " + str[1]);
                            System.out.println("query type = A, NS");
                            System.out.println("Server name : " + str[0] + " DNS");
                            System.out.println("Name: authoritative_dns_table.txt");
                            System.out.println("IP = " + str[2]);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached. " + e);
                }

                client_socket.close();
                input.close();
            }
        }
    }
}