package Projects;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.stream.Stream;

public class Server
{
    public static void main(String[] args) throws Exception {

        String host = "127.0.0.1";
        int port_number = 22000;

        Hashtable<String, String> local = new Hashtable<>();
        File file = new File("local_dns_table.txt");
        FileReader file_reader = new FileReader(file);
        BufferedReader reader = new BufferedReader(file_reader);
        String line_of_file;
        while ((line_of_file = reader.readLine()) != null) {
            String[] line = line_of_file.split(" ");
            local.put(line[0], line[1]);
        }

        Hashtable<String, String> root = new Hashtable<>();
        file = new File("root_dns_table.txt");
        file_reader = new FileReader(file);
        reader = new BufferedReader(file_reader);
        while ((line_of_file = reader.readLine()) != null) {
            String[] line = line_of_file.split(" ");
            root.put(line[0], line[1]);
        }

        Hashtable<String, ArrayList<String>> TLD = new Hashtable<>();
        file = new File("TLD_dns_table.txt");
        file_reader = new FileReader(file);
        reader = new BufferedReader(file_reader);
        while ((line_of_file = reader.readLine()) != null) {
            String[] line = line_of_file.split(" ");
            ArrayList<String> list = new ArrayList<>();
            list.add(line[1]);
            if (line.length == 3)
                list.add(line[2]);
            TLD.put(line[0], list);
        }

        Hashtable<String, String> authoritative = new Hashtable<>();
        file = new File("authoritative_dns_table.txt");
        file_reader = new FileReader(file);
        reader = new BufferedReader(file_reader);
        while ((line_of_file = reader.readLine()) != null)  {
            String[] line = line_of_file.split(" ");
            authoritative.put(line[0], line[1]);
        }

        reader.close();

        DatagramSocket server_socket = new DatagramSocket(port_number);
        byte[] received_data = new byte[1024];
        byte[] sent_data = new byte[1024];

        while (true) {
            DatagramPacket received_packet = new DatagramPacket(received_data, received_data.length);
            server_socket.receive(received_packet);
            String given_URL = new String(received_packet.getData(), 0, received_packet.getLength());

            String response = null;

            if (local.get(given_URL) != null) {
                System.out.println("Client Requested: " + given_URL);
                System.out.println("URL     :: " + given_URL);
                System.out.println("Query type = A");
                System.out.println("IP Address :: " + local.get(given_URL));
                System.out.println("Found record on local DNS servers.");
                System.out.println("Name: local_dns_table.txt");
                response = "local|" + local.get(given_URL);
            }

            else if (root.get(given_URL) != null) {
                System.out.println("Client Requested: " + given_URL);
                System.out.println("URL     :: " + given_URL);
                System.out.println("Query type = A");
                System.out.println("IP Address :: " + root.get(given_URL));
                System.out.println("Found record on root DNS servers.");
                System.out.println("Name: root_dns_table.txt");
                response = "root|" + root.get(given_URL);
            }

            else if (TLD.get(given_URL) != null) {
                System.out.println("Client Requested: " + given_URL);
                System.out.println("URL     :: " + given_URL);
                System.out.println("Query type = A");
                System.out.println("IP Address :: " + TLD.get(given_URL).get(0));
                System.out.println("Query type = CNAME");
                System.out.println("Aliases: " + given_URL);
                System.out.println("Canonical name: " + TLD.get(given_URL).get(1));
                System.out.println("Found record on TLD DNS servers.");
                System.out.println("Name: TLD_dns_table.txt");
                response = "TLD|" + TLD.get(given_URL).get(0) + "|" + TLD.get(given_URL).get(1);
            }

            else if (authoritative.get(given_URL) != null) {
                System.out.println("Client Requested: " + given_URL);
                System.out.println("URL     :: " + given_URL);
                System.out.println("Query type = A");
                System.out.println("IP Address :: " + authoritative.get(given_URL));
                System.out.println("Query type = NS");
                System.out.println("Found record on authoritative DNS servers.");
                System.out.println("Name: authoritative_dns_table.txt");
                System.out.println("IP = " +  InetAddress.getByName(host));
                response = "authoritative|" + authoritative.get(given_URL) + "|" + InetAddress.getByName(host);
            }

            InetAddress client_address = received_packet.getAddress();
            int client_port = received_packet.getPort();

            assert response != null;
            sent_data = response.getBytes();
            DatagramPacket send_packet = new DatagramPacket(sent_data, sent_data.length, client_address, client_port);
            server_socket.send(send_packet);
        }
    }
}