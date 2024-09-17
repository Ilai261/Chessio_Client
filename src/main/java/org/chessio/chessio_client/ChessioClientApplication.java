package org.chessio.chessio_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChessioClientApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChessioClientApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 375, 381);  // Set to match the prefWidth and prefHeight from FXML
        stage.setTitle("Chessio");
        stage.setScene(scene);

        // Automatically resize the window to fit the scene's content
        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
