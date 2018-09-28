
import java.net.*;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.TimeZone;
import java.util.ArrayList;
import java.io.*;
import java.security.*;


class Node {
	String username;
	String password;
	Node next;
	
	public Node(String u, String p, Node next){
		username = u;
		password = p;
		next = null;
	}

}


class serverThread extends Thread {

	private Socket soc;
	private String hostIP;
	public byte[] byteOutput = null;
	public boolean emptyByteStream = true;

	public serverThread(Socket s) {
		super("serverThread");
		soc = s;
		hostIP = s.getInetAddress().getHostAddress();
	}

	

	public void run() {
		
		
		Node head = init();	
		

		try{

			// initialize socket reading/writing objects
			PrintWriter socWrite = new PrintWriter(soc.getOutputStream(), true);
			DataOutputStream socWriteBytes = new DataOutputStream(soc.getOutputStream());
			InputStreamReader socISR = new InputStreamReader(soc.getInputStream());
			BufferedReader socRead = new BufferedReader(socISR);
			String clientInput = "";
			String command = "";
			String resource = "";
			String message = "";
			String serverResponse = "";

			

			try{
				soc.setSoTimeout(10000);

				
					while(true){
				
				
						String password = "";
						String username = socRead.readLine();
						String input = "";
						String output = "";
						boolean loggedIn = false;
						boolean newAcc = false;
						
						while(!loggedIn){
							
							password = searchLL(head, username);
							
							while(password!=null){
								socWrite.print("not found\n");
								for(int i=0;i<3;i++)
									socWrite.flush();
								
								if(socRead.readLine().compareToIgnoreCase("y")==0){
									password = "";
									newAcc = true;
								}
								else{
									username = socRead.readLine();
									password = searchLL(head, username);
								}
								
							}
							
							//has and salt
							input = hashPass(socRead.readLine() + "RNLESANCOEI");
							
							if(newAcc){
								addUser(username, input, head);
							}
							
							else if(input.compareTo(password)==0){
								loggedIn = true;
								output = "found";
							}
							
							else{
								output = "not found";
							}
							
							socWrite.print(output);
							for(int i=0;i<3;i++)
								socWrite.flush();
						
						
						}

						
						
						//print groups list;
						String grouping = "";
						for(String name : groupList()){
							grouping+=(name+"\n");
						}
						socWrite.print(grouping);
						for(int i=0;i<3;i++)
							socWrite.flush();
						
					
					
						while(command.compareTo("END")==0){
						
							
							clientInput = socRead.readLine();
							command = "";
							resource = "";
							message = "";
							serverResponse = "";



							// assign <command> and <resource> to string variables
							int counter=0;
							while(counter<clientInput.length() && clientInput.charAt(counter)!=' '){
								command += clientInput.charAt(counter);
								counter++;
							}
							counter++;
							while(counter<clientInput.length() && clientInput.charAt(counter)!=' '){
								resource += clientInput.charAt(counter);
								counter++;
							}

							counter++;
							while(counter<clientInput.length() && clientInput.charAt(counter)!=' '){
								message += clientInput.charAt(counter);
								counter++;
							}


							if(command.compareTo("GET")==0)
								serverResponse += GET(resource);

							else if(command.compareTo("POST")==0)
								serverResponse += POST(resource, message, username);
							
							else if(command.compareTo("END")==0)
								serverResponse += "";

							else
								serverResponse += "Error: Internal Server Error.\r\n";
							
							
							
							
							// print response string, print byte array (if it exists), flush output stream, and close all streams/readers/socket
							socWrite.print(serverResponse);
							for(int i=0;i<3;i++)
								socWrite.flush();

							try {
								Thread.sleep(250);
							} catch (InterruptedException e) {

							}
							
						}
				
				
				}

			} catch (SocketTimeoutException e) {
				serverResponse += "Error: Connection Timeout.\r\n";
			}

			
			


			socRead.close();
			socISR.close();
			socWriteBytes.close();
			socWrite.close();
			soc.close();


		} catch (IOException e) {
			System.err.println("Error: Could not get I/O for the connection to host "+hostIP);
		}

	}


	
	
	private Node init(){
		
		String pass = "";
		String user = "";
		Node head = null;
		Node cur = null;
		

		Path curDir = Paths.get("");
		String pathToFile = curDir.toAbsolutePath().toString() + "passwords.txt";
		File requestedFile = new File(pathToFile);

				try {

					requestedFile.createNewFile();

							// make a buffered reader for the file
							InputStream fileIS = Files.newInputStream(requestedFile.toPath());
							InputStreamReader fileISR = new InputStreamReader(fileIS);
							BufferedReader fileReader = new BufferedReader(fileISR);



							// read file contents
							int nextChar = 'a';
							while ((nextChar = fileReader.read()) != -1) {
								
								user = "";
								pass = "";
								
								while ((nextChar = fileReader.read()) != '|')
									user += (char)nextChar;
								
								while ((nextChar = fileReader.read()) != '\n')
									pass += (char)nextChar;

								
								if(head==null){
									head = new Node(user, pass, null);
									cur = head;
								}
								else{
									cur.next = new Node(user, pass, null);
									cur = cur.next;
								}
									
							}


							// close all streams/readers
							fileReader.close();
							fileISR.close();
							fileIS.close();


				} catch (Exception e) {
					head = null;
				}

		return(head);
	}
	
	
	
	private ArrayList<String> groupList(){
		
		ArrayList<String> groups = new ArrayList<String>();
		
		Path curDir = Paths.get("");
		String pathToFile = curDir.toAbsolutePath().toString();
		File requestedFile = new File(pathToFile);
		
		
		/*File[] list = requestedFile.listFiles();

			for (int i = 0; i < list.length; i++) {
			  if (list[i].isFile() && list[i].compareTo("passwords") == false && list[i].compareTo("Client") == false && list[i].compareTo("Server") == false )
				groups.add(list[i]);
			}
			
		return(groups);*/
		return(null);
	}
	
	
	
	
	private void addUser(String username, String password, Node head){
		
		while(head.next != null)
			head = head.next;
		head.next = new Node(username, password, null);
		
		
		Path curDir = Paths.get("");
		String pathToFile = curDir.toAbsolutePath().toString() + "passwords.txt";
		File requestedFile = new File(pathToFile);


		
				try {
					requestedFile.createNewFile();

							// make a buffered reader for the file
							OutputStream fileOS = Files.newOutputStream(requestedFile.toPath());
							PrintWriter osWriter = new PrintWriter(fileOS);

							
							osWriter.println(username+"|"+password+"\n");
							for(int i=0;i<3;i++)
								osWriter.flush();


							// close all streams/readers
							osWriter.close();
							fileOS.close();
			


				} catch (Exception e) {
				}	
		
	}
	
	
	
	
	private String searchLL(Node head, String username){
		while(head!=null){
			if((head.username).compareTo(username)==0){
				return(head.password);
			}
		}
		return(null);
	}
	
	
	
	private String hashPass(String raw){
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] input = raw.getBytes("UTF-8");
			byte[] bytes = digest.digest(input);
			return(new String(bytes, "UTF-8"));
		}
		catch(Exception e){

		}
		return(raw);
	}

	

	private String GET(String resource){

		String returnVal = "";

		Path curDir = Paths.get("");
		String pathToFile = curDir.toAbsolutePath().toString() + resource;
		File requestedFile = new File(pathToFile);

		// check if requested group exists
		if(requestedFile.exists()){

				try {

							// make a buffered reader for the file
							InputStream fileIS = Files.newInputStream(requestedFile.toPath());
							InputStreamReader fileISR = new InputStreamReader(fileIS);
							BufferedReader fileReader = new BufferedReader(fileISR);



							// read file contents
							int nextChar = 'a';
							while ((nextChar = fileReader.read()) != -1) {
								returnVal += (char)nextChar;
							}


							// close all streams/readers
							fileReader.close();
							fileISR.close();
							fileIS.close();


				} catch (Exception e) {
					returnVal = "Error: Internal Server Error.\r\n";
				}
					
			
		}


		// check if file exists
		else if(!requestedFile.exists())
			returnVal += "Error: Group does not exist!\r\n";

		else
			returnVal += "Error: Internal Server Error.\r\n";


		return(returnVal);

	}


	
	
		private String POST(String resource, String message, String username){
			
		if(resource.compareTo("passwords")==0 || resource.compareTo("Server")==0 || resource.compareTo("Client")==0)
			return("Error: Invalid group name.\r\n");

		String returnVal = "";

		Path curDir = Paths.get("");
		String pathToFile = curDir.toAbsolutePath().toString() + resource + ".txt";
		File requestedFile = new File(pathToFile);
		
		boolean newFile = false;
		if(!requestedFile.exists())
			newFile = true;
		

				try {
					requestedFile.createNewFile();
							//generate an Expires time
							SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
							sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
							String timestamp = sdf.format(new Date(System.currentTimeMillis()));
				
		

							// make a buffered reader for the file
							OutputStream fileOS = Files.newOutputStream(requestedFile.toPath());
							PrintWriter osWriter = new PrintWriter(fileOS);

							
							osWriter.println("Message posted by "+username+" on "+timestamp);
							osWriter.println(resource);
							osWriter.println();
							for(int i=0;i<3;i++)
								osWriter.flush();


							// close all streams/readers
							osWriter.close();
							fileOS.close();
			


				} catch (Exception e) {
					returnVal = "Error: Internal Server Error.\r\n";
				}			



		return(returnVal);

	}
	
	
}



public class Server {

	public static void main(String[] args) {


		String portAsString = "12337";
		if(args.length>0){
			portAsString = args[0];
		}		


		// initialize server socket
		ServerSocket soc = null;

		// initialize variables for host/IP and port; check if port number is properly formatted
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
			System.err.println("Error: Invalid port number "+portAsString);
		}


		else{
			try{

				// create thread pool and server socket, and start listening for connections

				ThreadPoolExecutor tpc = new ThreadPoolExecutor(5, 50, 30, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
				soc = new ServerSocket(port);

				while(true){

					Socket s = soc.accept();
					serverThread st = new serverThread(s);

					try{
						tpc.execute(st);

					} catch (RejectedExecutionException e) {


						// if 50 threads are in use, reject new clients

						try{

							// initialize socket writing objects
							PrintWriter socWrite = new PrintWriter(s.getOutputStream(), true);
							String serverResponse = "HTTP/1.0 503 Service Unavailable\r\n";

							// print response string, flush output stream, and close all streams/readers/socket
							socWrite.println(serverResponse);
							for(int i=0;i<3;i++)
								socWrite.flush();

							try {
								Thread.sleep(250);
							} catch (InterruptedException x) {

							}

							socWrite.close();
							s.close();


						} catch (IOException q) {
							System.err.println("Error: Could not get I/O for the connection to host " + soc.getInetAddress().getHostAddress());
						}


					}
				}


			} catch (IOException e) {
				System.err.println("Error: Could not listen on port "+port);
			} 
		}
	}
}