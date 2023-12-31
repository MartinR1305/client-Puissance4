package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import javafx.application.Platform;

public class ClientTCP implements AutoCloseable {

	private Socket clientSocket;
	private PrintWriter writer;
	private BufferedReader reader;

	private ClientController clientController;

	private boolean isConnectedToServer;
	private boolean is2ndClientConnected;
	private boolean isClientOpened;
	private boolean isReconnectedToServerInProgress;

	private int port;
	private String IP;
	private int numClient;

	/**
	 * Default Constructor for a ClientTCP
	 * 
	 * @param IP
	 * @param port
	 * @param clientController
	 * @throws IOException
	 */
	public ClientTCP(String IP, int port, ClientController clientController) throws IOException {
		this.isConnectedToServer = false;
		this.is2ndClientConnected = false;
		this.isClientOpened = true;
		this.isReconnectedToServerInProgress = false;

		this.numClient = -1;

		this.port = port;
		this.IP = IP;
		this.clientController = clientController;
	}

	/**
	 * Getter for the client's number
	 * 
	 * @return
	 */
	public int getNumClient() {
		return this.numClient;
	}

	/**
	 * Getter for the client's IP
	 * 
	 * @return
	 */
	public String getIP() {
		return this.IP;
	}

	/**
	 * Getter for the client's IP
	 * 
	 * @return
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Method of launching the connection with the server
	 * 
	 * @throws IOException
	 */
	public void connectToServer() throws IOException {
		try {
			// For close reconnect () when program is closing
			if (isClientOpened) {

				// Initialization of communication
				this.clientSocket = new Socket(IP, port);

				this.writer = new PrintWriter(this.clientSocket.getOutputStream(), true);
				this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

				isConnectedToServer = true;

				System.out.println("Connected to the server !");
				clientController.actualizeState("Connected");
				
				this.display2PlayersConnected();

				/* Get Message of server */
				while (isConnectedToServer && isClientOpened) {
					receiveMessage();
				}
			}
		} catch (IOException IOE) {
			/* If the server aren't open */
			if (this.clientSocket == null) {
				if (isClientOpened) {
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

		// if the port is being modified
		if (isReconnectedToServerInProgress) {
			return;
		}

		// Start the Thread to take a break
		Thread waitThread = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(5000); // 5 second of break
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (!isConnectedToServer || isClientOpened) {
								try {
									connectToServer(); // Retry the connection
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

		// Starting the Thread
		waitThread.start();
	}

	/**
	 * Method use to close socket, Reader and Writer
	 */
	public void disconnect() {
		try {
			if (this.clientSocket != null) {
				// Reader
				if (this.reader != null) {
					this.reader.close();
				}

				// Writer
				if (this.writer != null) {
					this.writer.close();
				}

				System.out.println("Socket Closed");
				this.clientSocket.close();
				clientController.actualizeState("Disconnected");
			}

			// If the server is close before the Scoring Machine
			this.clientSocket = null;

			isConnectedToServer = false;

		} catch (IOException IOE) {
			System.err.println("Error disconnecting from the server: " + IOE.getMessage());
		}
	}

	/**
	 * Method the receive Messages from the Server and Update FXML page
	 */
	public void receiveMessage() {
		String finalMessage = "";
		try {
			// Read Message
			finalMessage = this.reader.readLine();

			// If the connection have been stopped
			if (finalMessage.equals("STOP")) {
				System.out.println("Connection to the server lost !");

				// Update status in FXML page
				clientController.actualizeState("Disconnected");

				// Closure of socket / in / out
				disconnect();

				// If the program are stopping -> don't retry the connection
				if (isClientOpened) {
					reconnect();
				}

			}

			Pattern pattern = Pattern.compile("N(\\d+)");
			Matcher matcher = pattern.matcher(finalMessage);

			// Message for client's number assignment
			if (matcher.find()) {
				String integerPart = matcher.group(1);
				numClient = Integer.parseInt(integerPart) - 1;
				System.out.println("Le client a le numéro : " + numClient);
			}
			
			if (finalMessage.equals("2 Players Connected")) {
				System.out.println("Les deux joueurs sont connectés au serveur, la partie peut être lancé ! ");
				is2ndClientConnected = true;
			}

		} catch (IOException IOE) {
			IOE.printStackTrace();
			disconnect();
		}
	}

	/**
	 * Using to change Port / IP of the connection to Server
	 * 
	 * @param IP   : IP for connect to the server
	 * @param port : Port for connect to the server
	 */
	public void sendIP_Port(String IP, String Port) {

		System.out.println("Connection to : " + IP + " - " + Port);

		this.IP = IP;
		port = Integer.parseInt(Port);

		isReconnectedToServerInProgress = true;

		// Stop A potential Thread
		if (isConnectedToServer) {
			this.writer.println("STOP");
		}

		// Update status in FXML page -> potential new server
		clientController.actualizeState("Disconnected");

		// Reconnect with new informations
		reconnect();

		isReconnectedToServerInProgress = false;
	}

	 /**
	 * Method that allows to display if the two players are connected or not
	 */
	public void display2PlayersConnected() {
	    Thread thread = new Thread(() -> {
	        while (true) {
	            try {
	                Thread.sleep(500);
	                Platform.runLater(() -> {
	                	System.out.println(is2ndClientConnected);
	                });
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    });

	    thread.setDaemon(true);
	    thread.start();
	}

	/**
	 * Method that allows to close the client
	 */
	@Override
	public void close() throws Exception {
		isClientOpened = false;

		if (writer != null) {
			writer.println("STOP");
		}

		System.out.println("Client stopped");
	}

}