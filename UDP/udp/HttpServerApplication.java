package udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

public class HttpServerApplication {
	
	public static void main(String[] args) throws IOException, InvalidParameterSpecException, NoSuchAlgorithmException {

		int port = 8080; // Default port number
		String directory = "/Users/janegarciu/Documents/NetworkProgramming/UDP"; // Default directory

		udp.UDPServer server = new udp.UDPServer(); // server object
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // User input
		String httpsRequest;
		
		System.out.print("Please enter your https command (Enter 'quit' to quit application): ");
		httpsRequest = keyboard.readLine();
		while(!httpsRequest.equalsIgnoreCase("quit")) { 
			
				String[] splitRequest = httpsRequest.split(" ");

				for(int i = 0; i < splitRequest.length; i++) {

					if(splitRequest[i].contains("-p"))
					{
						port = Integer.parseInt(splitRequest[i+1]);
					}

		            if(splitRequest[i].contains("-d"))
		            {
						for (int j = i+1; j < splitRequest.length; j++) {
							directory += splitRequest[j];
						}
					} 
				}

				System.out.println("");
				System.out.println("Starting server...");
				System.out.println("Running at port: " + port);
				System.out.println("Directory: " + directory);
				System.out.println(""); 
				
				// Connect to Server
				server.listenAndServe( port, directory);
				
				System.out.print("Please enter 'quit' if you want to quit else keep going by inputing any other command: ");
				httpsRequest = keyboard.readLine();
			}
		
			System.out.println("Application terminated!");
		}
	}