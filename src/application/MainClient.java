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
		
		//Server launch
		//clientController = new ClientController();
        client = new ClientTCP("172.24.21.200", 8080, clientController);
        
        //clientController.setClientTCP(client);

        // Start the server in a separate thread
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

		System.out.println("Launch of the Programme");

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource(".." + File.separator + "view" + File.separator + "Client.fxml"));
		Parent root = fxmlLoader.load();

		//clientController = fxmlLoader.getController();
		//clientController.setClientTCP(client);

		// Scene creation
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);

		primaryStage.setTitle("Scoring Emulator");
		primaryStage.show();

	}

	/**
	 * When the page are closed -> close connection with the Server
	 */
	@Override
	public void stop() throws IOException {

		// Clean up and release resources before the application exits
		System.out.println("Application is about to exit");

		// Close the server
		if (client != null) {
			client.close();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}