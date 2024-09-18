package org.chessio.chessio_client.MessageHandlers;

import org.chessio.chessio_client.SceneControllers.WaitingRoomController;

public class WaitingRoomChessioMessageHandler implements ChessioMessageHandler {

    private final WaitingRoomController controller;

    public WaitingRoomChessioMessageHandler(WaitingRoomController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessage(String message) {
        controller.handleServerMessage(message); // Delegate to the WaitingRoomController
    }
}

