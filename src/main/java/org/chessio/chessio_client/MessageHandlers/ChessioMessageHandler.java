// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class creates the message handler interface, used to pass between the waiting room handler
// and the online game handler

package org.chessio.chessio_client.MessageHandlers;

public interface ChessioMessageHandler {
    void handleMessage(String message);
}
