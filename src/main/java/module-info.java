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

    opens org.chessio.chessio_client to javafx.fxml;
    exports org.chessio.chessio_client;
    exports org.chessio.chessio_client.SceneControllers;
    opens org.chessio.chessio_client.SceneControllers to javafx.fxml;
}