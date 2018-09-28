
import java.net.*;
import java.io.*;

public class Client {

	public static void main(String[] args) {
		
		String hostIP = "";
		try{
			hostIP = InetAddress.getLocalHost().getHostAddress();
		}
		catch (Exception e){

		}
		
		String portAsString = "12337";

		if(args.length>0){
			hostIP = args[0];
		}		
		if(args.length>1){
			portAsString = args[1];
		}
		
		
		// initialize socket object and socket reading/writing objects
		Socket soc = null;
		PrintWriter socketPW = null;
		InputStreamReader socketISR = null;
		BufferedReader socketBR = null;		
		
		
		// initialize objects dealing with user input (from System.in)
		InputStreamReader userISR = null;
		BufferedReader userBR = null;	
		
		
		// initialize variables for hostname/IP and port; check if port number is properly formatted
		int port = -1;
		boolean portIsValid = true;
		
		try{
			port = Integer.parseInt(portAsString);
			if(port<0||port>65535)
				portIsValid = false;
		}
		catch(NumberFormatException e){
			portIsValid = false;
		}
		
		
		//end program if port number is not properly formatted
		if(!portIsValid){
			System.out.println("Error: Invalid port number "+portAsString);
		}
		
		
		else{
					try{
			 			
			 			// create socket and socket reading/writing objects
			 			soc = new Socket(hostIP, port);
			 			socketPW = new PrintWriter(soc.getOutputStream(), true);
			 			
			 			socketISR = new InputStreamReader(soc.getInputStream());
			 			socketBR = new BufferedReader(socketISR);
			 			
			 			
			 			// create reader for user input
			 			userISR = new InputStreamReader(System.in);
			 			userBR = new BufferedReader(userISR);

						
						
			 			//all server/client interactions occur here
						boolean exit = false;
						while(!exit){
						
							boolean repeat = true;
							boolean newAccount = false;
							String userInput = "";
							String response = "";
							
							//login or create account
							System.out.println("Welcome to the messageboard!");
							while(repeat){
								System.out.print("Enter your username: ");
								socketPW.println(userBR.readLine());
								response = socketBR.readLine();
								
								if(response.compareTo("found")!=0){
									System.out.println("Username does not exist!");
									System.out.print("Create new account? (y/n): ");
									userInput = userBR.readLine();
									if(userInput.compareToIgnoreCase("y")==0){
										repeat = false;
										newAccount = true;
									}
									socketPW.println(userInput);
								}
								else
									repeat = false;
							}
							
							repeat = true;
							while(repeat){
								System.out.print("Enter your password: ");
								socketPW.println(userBR.readLine());
								response = socketBR.readLine();
								
								if(newAccount==false && response.compareTo("found")!=0){
									System.out.println("Wrong password! Try again.");
								}
								else
									repeat = false;
							}							
							System.out.println();
							System.out.println();
							
							System.out.println("List of groups:");
							System.out.println(socketBR.readLine()+"\n\n");
							
							
							
							repeat = true;
							while(repeat){
								System.out.print("Enter a command (GET, POST, END) with parameters: ");
								userInput = userBR.readLine();
								socketPW.println(userInput);
								if(userInput.compareTo("END")==0){
									repeat = false;
									System.out.println("Logging out...");
								}
								else if(userInput.substring(0,2).compareTo("GET")==0){
									response = socketBR.readLine();
									while(response.compareTo("end")==0){
										System.out.println("    " + response);
										response = socketBR.readLine();
									}
								}
								else if(userInput.substring(0,3).compareTo("POST")==0)
									System.out.println(socketBR.readLine());								
								else
									System.out.println("Invalid command!");
							}
							

							
							/*System.out.print("client input: ");
							socketPW.println(userBR.readLine());
							System.out.println("server response: " + socketBR.readLine());*/
							
							
						
							System.out.print("Exit program? (y/n): ");
							userInput = userBR.readLine();
							if(userInput.compareToIgnoreCase("y")==0)
								exit = true;
							
							System.out.println();
							System.out.println();
						}
						
						
			 			// close all streams/readers/sockets
			 			socketBR.close();
			 			socketISR.close();
			 			socketPW.close();
			 			soc.close();
			 			userBR.close();
			 			userISR.close();

			        } catch (UnknownHostException e) {
			            System.out.println("Error: Could not connect to host "+hostIP);
			        } catch (IOException e) {
			            System.err.println("Error: Could not get I/O for the connection to  host "+hostIP);
			        } 

		}

		
	}
}