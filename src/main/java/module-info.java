module org.chessio.chessio_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires spring.beans;
    requires spring.web;
    requires spring.context;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires chesslib;
    requires tomcat.embed.websocket;
    requires org.apache.logging.log4j;

    opens org.chessio.chessio_client to javafx.fxml;
    exports org.chessio.chessio_client;
    exports org.chessio.chessio_client.SceneControllers;
    opens org.chessio.chessio_client.SceneControllers to javafx.fxml;
    exports org.chessio.chessio_client.Models;
    opens org.chessio.chessio_client.Models to javafx.fxml;
    exports org.chessio.chessio_client.Manager;
    opens org.chessio.chessio_client.Manager to javafx.fxml;
}