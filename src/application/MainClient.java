package application;

import java.io.File;
import java.io.IOException;

import controller.ClientController;
import controller.ClientTCP;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {
	private ClientTCP client;
	private ClientController clientController;

	/**
	 * Method for do preparation and start the Connection in a thread
	 * @throws IOException
	 */
	public MainClient() throws IOException {
		clientController = new ClientController();
		client = new ClientTCP("10.188.174.139", 8090, clientController);
		
		clientController.setClientTCP(client);
		
		new Thread(() -> {
			try {
				client.connectToServer();
			} catch (IOException IOError) {
				IOError.printStackTrace();
			}
		}).start();
	}

	/**
	 * Method starting the program
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource(".." + File.separator + "view" + File.separator + "Client.fxml"));
		Parent root = fxmlLoader.load();

		clientController = fxmlLoader.getController();
		clientController.setClientTCP(client);

		// Scene creation
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();

	}
	
	/**
	 * Method that allows to close the connection with the server when the client is closed
	 * @throws Exception 
	 */
	@Override
	public void stop() throws Exception {
		System.out.println("Client is about to close");
		
		if(client != null) {
			client.close();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}