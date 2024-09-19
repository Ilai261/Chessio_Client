// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class is the main JavaFX application class

package org.chessio.chessio_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChessioClientApplication extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        // start the app
        FXMLLoader fxmlLoader = new FXMLLoader(ChessioClientApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 375, 381);  // set to match the prefWidth and prefHeight from FXML
        stage.setTitle("Chessio");
        stage.setScene(scene);

        // automatically resize the window to fit the scene's content
        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
