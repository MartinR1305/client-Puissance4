package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ClientController implements Initializable{

	private ClientTCP clientTCP;
	private static boolean isConnected;

	@FXML
	private Label client, labelIPAddress, labelPort, state, valueIsConnected;

	@FXML
	Button connect, send;

	@FXML
	TextField valueIPAddress, valuePort, message;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
//		setClientTCP(clientTCP);
//		valueIPAddress.setText(clientTCP.getIP());
//		valuePort.setText(String.valueOf(clientTCP.getPort()));
		
		valueIsConnected.setText("Disconnected");
		this.updateState();		
	}

	/**
	 * Setter of clientTCP -> use for associating emulator and ClientTCP
	 * 
	 * @param clientTCP
	 */
	public void setClientTCP(ClientTCP clientTCP) {
		this.clientTCP = clientTCP;
	}
	
	/**
     * When Button are pressed : send new IP / port information
     */
    @FXML
    private void changeIPandPort() {
        
        //Get Information
        if (valueIPAddress.getText() != null && valuePort.getText() != null) {
            clientTCP.sendIP_Port (valueIPAddress.getText(), valuePort.getText());
        }
        
        //Clear
        valueIPAddress.setText(null);
        valuePort.setText(null);
    }

	/**
	 * Method that allows to actualize the state of the connection between the client and the server
	 * 
	 * @param state
	 */
	public void actualizeState(String state) {
		if (state.equals("Connected")) {
			isConnected = true;
		}
		
		else if (state.equals("Disconnected")){
			isConnected = false;
		}
	}
	
	 /**
	 * Method that allows to update the state of the connection between the client and the server
	 */
	public void updateState() {
	    Thread stateUpdateThread = new Thread(() -> {
	        while (true) {
	            try {
	                Thread.sleep(500);
	                Platform.runLater(() -> {
	                    if (isConnected) {
	                        valueIsConnected.setText("Connected");
	                    } else {
	                        valueIsConnected.setText("Disconnected");
	                    }
	                });
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    });
	    stateUpdateThread.setDaemon(true);
	    stateUpdateThread.start();
	}
}