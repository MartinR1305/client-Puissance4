package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ClientController{
	
	//private ClientTCP clientTCP;
	
	@FXML
	private Label client, labelIPAddress,  labelPort,  labelIsConnected, valueIsConnected;
	
	@FXML
	Button connect;
	
	@FXML
	TextField valueIPAddress,valuePort;

	/**
	 * Setter of clientTCP -> use for associating emulator and ClientTCP
	 * @param clientTCP
	 */
    public void setClientTCP(ClientTCP clientTCP) {
        //this.clientTCP = clientTCP;
    }


	/**
	 * Start the Thread of Hour
	 */
	@FXML
	private void initialize() {		

		
	}
}