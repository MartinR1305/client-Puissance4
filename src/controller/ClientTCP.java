package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.SwingUtilities;

public class ClientTCP implements AutoCloseable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    //private ClientController clientController;

    private boolean connected;
    private boolean disconnected;
    private boolean reconnectInProgress;

    private int portGlobal;
    private String IPGlobale;

    private List<String> serialization;

    /**
     * Method for preparing the connection
     * @param IP : IP for connect to the server
     * @param port : Port for connect to the server
     * @param emulateurController : Used to manage communication between FXML and TCP connections
     * @throws IOException
     */
    public ClientTCP(String IP, int port, ClientController clientController) throws IOException {
        this.connected = false;
        this.disconnected = false;
        this.reconnectInProgress = false; 

        this.portGlobal = port;
        this.IPGlobale = IP;
        
        //this.clientController = clientController;
    }

    /**
     * Method of launching the connection with the server
     * @throws IOException
     */
    public void connectToServer() throws IOException {
        try {
        	//For close reconnect () when programm is closing
        	if (!disconnected) {
        		
        		/* Initialization of communication */
	            this.socket = new Socket(IPGlobale, portGlobal);
	
	            this.out = new PrintWriter(this.socket.getOutputStream(), true);
	            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
	
	            connected = true;
	
	            System.out.println("Connected to the server !");
	            
	            /* Get employee ID and send scores serialized */
	            this.out.println("START"); //First connection to MainApp (Het data)
	
	            if (serialization != null) {
	                for (String old_score : serialization) {
	                	this.out.println(old_score);
	                }
	            }
	            
	            /* Get Message of server */
	            while (connected && !disconnected)
	            {
	            	//receiveMessage();
	            }
        	}
        } catch (IOException IOE) {
        	/* If the server aren't open*/
            if (this.socket == null) {
                if (!disconnected) {
                	System.err.println("Server not open !");
                    reconnect();
                }
            } else {
                IOE.printStackTrace();
            }
        }
    }

    /**
     * Use to retry a connection with the server
     */
    private void reconnect() {
    	
    	//if the port is being modified
    	if (reconnectInProgress) {
            return;
        }
    	
    	//Start the Thread to take a break
    	Thread waitThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000); //5 second of break
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (!connected || !disconnected) {
                            	try {
									connectToServer(); //Retry the connection
								} catch (IOException IOE) {
									IOE.printStackTrace();
								}
                            }
                        }
                    });
                } catch (InterruptedException IE) {
                    IE.printStackTrace();
                }
            }
        });
    	
    	//Starting the Thread
    	waitThread.start();
    }

//    /**
//     * Method the receive Messages from the Server and Update FXML page
//     */
//    public void receiveMessage() {
//    	String finalMessage = "";
//    	try {
//    		//Read Message
//			finalMessage = this.in.readLine();
//	
//			//If the connexion have been stopped
//	        if (finalMessage.equals("STOP")) {
//	            System.out.println("Connection to the server lost !");
//	            
//	            //Update status in FXML page
//	            String [] disconnect = {"disconnected"};
//	            clientController.PrepMiseAJour (disconnect);
//	            
//	            //Closure of socket / in / out
//	            disconnected();
//	            
//	            //If the program are stopping -> don't retry the connection
//	            if (!disconnected) {
//	            	reconnect();
//	            }
//	
//	        } else {
//	        	//All are good and it's just an update of employee ID
//	            String [] message_sep = finalMessage.split(";"); 
//
//	            clientController.PrepMiseAJour (message_sep);
//	        }
//    	}
//    	catch (IOException IOE) {
//			IOE.printStackTrace();
//			disconnected ();
//		}
//    }
    
//    /**
//     * Using to change Port / IP of the connection to Server
//     * @param IP : IP for connect to the server
//     * @param port : Port for connect to the server
//     */
//    public void sendIP_Port (String IP, String Port) {
//    	
//    	System.out.println ("Connection to : " + IP + " - " + Port);
//    	
//    	IPGlobale = IP;
//    	portGlobal = Integer.parseInt(Port);
//    	
//    	reconnectInProgress = true;
//    	
//    	//Stop A potential Thread
//    	if (connected) {
//    		this.out.println ("STOP");
//    	}
//    	
//    	//Update status in FXML page -> potential new server
//        String [] disconnect = {"disconnectedBis"};
//        clientController.PrepMiseAJour (disconnect);
//        
//    	//Reconnect with new informations
//    	reconnect ();
//    	
//    	reconnectInProgress = false;
//    }

    /**
     * Method use to send score to Server (transition between FXML page and Server)
     * @param Saisie_ID
     * @param round_hour
     */
    public void sendMessage(String Saisie_ID, String round_hour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime Date_actuelle = LocalDateTime.now();

        String horloge_Actuelle = Date_actuelle.format(formatter) + " " + round_hour;

        String message = Saisie_ID + ";" + horloge_Actuelle + "; FIN";

        //If Connection are OK
        if (connected) {
        	this.out.println(message);
        } else {
        	// serialization ?
        }
        
        System.out.println ("Score of : " + Saisie_ID + " at " + horloge_Actuelle);
    }

    /**
     * Method use to close socket, Reader and Writer
     */
    public void disconnected() {
        try {
            if (this.socket != null) {
            	//Reader
                if (this.in != null) {
                    this.in.close();
                }
                
                //Writer
                if (this.out != null) {
                	this.out.close();
                }

                System.out.println("Socket Closed");
                this.socket.close();
            }
            
            //If the server is close before the Scoring Machine
            this.socket = null;

            connected = false;

        } catch (IOException IOE) {
            System.err.println("Error disconnecting from the server: " + IOE.getMessage());
        }
    }

    /**
     * Method automatically closes the connection
     */
    @Override
    public void close() throws IOException {
    	disconnected = true;
    	
    	//Using to close Thread
    	if (out != null) {
            out.println("STOP");
        }
    	
    	System.out.println ("Application Stopped !");

    }
}