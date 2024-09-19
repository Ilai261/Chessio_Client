// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the login request to the server, it contains a username and a password.

package org.chessio.chessio_client.Models;

public class LoginRequest
{
    private String userName;
    private String password;

    public LoginRequest(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
