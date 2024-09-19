// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the GameHistoryRequest object

package org.chessio.chessio_client.Models;

public class GameHistoryRequest
{
    // this request contains a username only
    private String userName;

    public GameHistoryRequest() {}

    public GameHistoryRequest(String userName)
    {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
