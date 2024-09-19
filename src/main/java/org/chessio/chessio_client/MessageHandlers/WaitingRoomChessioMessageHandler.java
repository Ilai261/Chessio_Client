// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class contains the waiting room message handler (not the implementation itself)
// implementation is in WaitingRoomContoller. this class is used to pass the control to it

package org.chessio.chessio_client.MessageHandlers;

import org.chessio.chessio_client.SceneControllers.WaitingRoomController;

public class WaitingRoomChessioMessageHandler implements ChessioMessageHandler {

    private final WaitingRoomController controller;

    public WaitingRoomChessioMessageHandler(WaitingRoomController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessage(String message) {
        controller.handleServerMessage(message); // pass the control to the WaitingRoomController class
    }
}

