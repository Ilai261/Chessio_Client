// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class contains the online board message handler (not the implementation itself)
// implementation is in OnlineBoardContoller. this class is used to pass the control to it

package org.chessio.chessio_client.MessageHandlers;

import org.chessio.chessio_client.SceneControllers.OnlineBoardController;

public class OnlineBoardChessioMessageHandler implements ChessioMessageHandler {

    private final OnlineBoardController controller;

    public OnlineBoardChessioMessageHandler(OnlineBoardController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessage(String message) {
        controller.handleServerMessage(message); // pass the control to the OnlineBoardController class
    }
}
