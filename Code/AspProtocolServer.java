import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.TreeMap;

import org.nyu.cs9163.consts.ConstantTerms;

/**
 * This class is implemented to build a protocol Arbitrary Storage Protocol v1.
 * For more information visit http://cs9163.org/resources/mdp0001.txt
 * @author Sourabh Taletiya (N15776267)
 * @see http://cs9163.org/resources/mdp0001.txt 
 * */
public class AspProtocolServer implements Serializable{


	private final static int PORT = 9090;
	private ServerSocket serverSocket;
	private Socket socket;
	private TreeMap<String, String> storeIt = new TreeMap<String, String>();
	/**
	 * Constructor
	 * @throws InterruptedException */
	public AspProtocolServer() throws IOException, InterruptedException {
		storeIt.clear();
		serverSocket = new ServerSocket(PORT);
		serverSocket.setSoTimeout(600000);
		socket = serverSocket.accept();


		System.out.println("Socket Informtion \n "+ 
				" Host Address - " + socket.getRemoteSocketAddress()
				+" Host Port - " + socket.getLocalPort() );
		PrintWriter out =
				new PrintWriter(socket.getOutputStream(), true);
		out.println(ConstantTerms.RC_READY);
		out.flush();
		Thread.sleep(1000);
		//System.out.println("Connection Successful");

	}





	private void closeConnection() {
		try {
			socket.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void aspProtocolRules() throws IOException, InterruptedException {
		try{
			DataInputStream is = new DataInputStream(socket.getInputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(is,Charset.defaultCharset()));
			PrintWriter out =  new PrintWriter(socket.getOutputStream(), true);
			DataInputStream is1 = new DataInputStream(socket.getInputStream());
			BufferedReader in1 = new BufferedReader(new InputStreamReader(is1,Charset.defaultCharset()));

			while(true){


				char[] buf =  new char[1024];
				Arrays.fill(buf,0,buf.length,'\0');
				in.read(buf);
				String query = new String(buf).trim(); // = "PUT 38 hello";
				System.out.println(query);System.out.println("legth : "+query.length());
				if(query != null && !query.trim().equals("") && query.length()>0){
					int firstSpace = 0;
					int length = 0;
					String key = "";
					StringBuilder data ;
					boolean flag = false; 

					//Get Method - PUT or GET or CLEAR
					firstSpace = query.indexOf(ConstantTerms.SPACE); 
					System.out.println("Firstspace : "+ firstSpace);
					if(firstSpace > 0){
						String method = query.substring(0, firstSpace);

						//Defining Methods
						if(method.equalsIgnoreCase(ConstantTerms.GET)){

							//Get Length
							length = Integer.parseInt(query.substring(firstSpace+1, query.indexOf(ConstantTerms.SPACE, firstSpace+1)));
							//Get Key
							key = query.substring(query.indexOf(ConstantTerms.SPACE, firstSpace+1) + 1, query.length()).trim();

							//Checking If Key Error
							if(!storeIt.containsKey(key)){
								out.println(ConstantTerms.RC_KEY_ERROR);
								out.flush();
								Thread.sleep(1000);
							}  else if( storeIt.get(key).length() < length){
								out.println(ConstantTerms.RC_LENGTH_ERROR);
								out.flush();
								Thread.sleep(1000);
							}
							else {
								out.println(storeIt.get(key).substring(0, length));
								out.flush();
								Thread.sleep(1000);

								out.println(ConstantTerms.RC_READY);
								out.flush();
								Thread.sleep(1000);
							}


						} else if(method.equalsIgnoreCase(ConstantTerms.PUT)){
							System.out.println("PUT Method Called");
							flag = true;
							while(flag){
								//Get Length
								length = Integer.parseInt(query.substring(firstSpace+1, query.indexOf(ConstantTerms.SPACE, firstSpace+1)));
								//Get Key
								key = query.substring(query.indexOf(ConstantTerms.SPACE, firstSpace+1) + 1, query.length()).trim();

								System.out.println("Got the key : " + key);
								//out1.writeUTF(ConstantTerms.RC_PROCEED);
								out.println(ConstantTerms.RC_PROCEED);
								out.flush();
								Thread.sleep(1000);

								System.out.println("Waiting for client data");
								Arrays.fill(buf,0,buf.length,'\0');
								in1.read(buf);
								String tempData = new String(buf);
								data = new StringBuilder();
								System.out.println(tempData);
								while((!tempData.equals(ConstantTerms.PERIOD)))
								{      
									data.append(tempData);
									System.out.println("Waiting for client data");
									Arrays.fill(buf,0,buf.length,'\0');
									in1.read(buf);
									tempData = new String(buf);
								}
								//Checking If Key Error
								if(storeIt.containsKey(key)){
									out.println(ConstantTerms.RC_KEY_ERROR);
									out.flush();
									Thread.sleep(1000);
								}  else if(data.length() < length){
									out.println(ConstantTerms.RC_LENGTH_ERROR);
									out.flush();
									Thread.sleep(1000);
								}
								else {
									if(!storeIt.containsKey(key)){
										storeIt.put(key,data.toString().substring(0, length));
										out.println(ConstantTerms.RC_READY);
										out.flush();
										Thread.sleep(1000);

									}
									System.out.println(storeIt);
									flag = false;

								}

							}
						} else if(method.equalsIgnoreCase(ConstantTerms.CLEAR)){

							key = query.substring(firstSpace+1, query.length()).trim();

							if(storeIt.containsKey(key)){
								storeIt.remove(key);
								out.println(ConstantTerms.RC_READY);
								out.flush();
								Thread.sleep(1000);
							}else{
								out.println(ConstantTerms.RC_KEY_ERROR);
								out.flush();
								Thread.sleep(1000);
							}

						} else if(method.equalsIgnoreCase(ConstantTerms.QUIT)){
							closeConnection(); break; 
						} else{
							out.println(ConstantTerms.RC_UNKNOWN_ERROR);
							out.flush();
							Thread.sleep(1000);
						} } }else{
							System.out.println("RC_Unknown error");
							out.println(ConstantTerms.RC_UNKNOWN_ERROR);
							out.flush();
							Thread.sleep(1000);
						}

			}
		}catch (Exception e) {
			if(socket.isConnected()){
				PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
				out.println(ConstantTerms.RC_UNKNOWN_ERROR);
				out.flush();
				Thread.sleep(1000);}
			e.printStackTrace();
		}
	}

	/**
	 * main method
	 * */
	public static void main(String[] args) {

		AspProtocolServer asp = null;
		try{
			asp = new AspProtocolServer();
			//System.out.println("Connection Success");
			//System.out.println("Calling ASP Protocol");
			//Calling ASP ProtocolRules
			asp.aspProtocolRules();
		}catch(Exception e){
			System.out.println("Exception Occured !!" + e.getMessage());
			e.printStackTrace();

		}/*finally{
			asp.closeConnection();
		}*/
	}

}
