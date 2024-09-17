package org.chessio.chessio_client.MessageHandlers;

import org.chessio.chessio_client.SceneControllers.OnlineBoardController;

public class OnlineBoardChessioMessageHandler implements ChessioMessageHandler {

    private final OnlineBoardController controller;

    public OnlineBoardChessioMessageHandler(OnlineBoardController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessage(String message) {
        controller.handleServerMessage(message); // Delegate to the OnlineBoardController
    }
}
