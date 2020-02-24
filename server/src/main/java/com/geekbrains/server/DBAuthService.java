package com.geekbrains.server;

public class DBAuthService implements AuthService {
    public DBAuthService() {
    }

    public String getNicknameByLoginAndPassword(String login, String password) {
        return SQLHandler.getNicknameByLoginAndPassword(login, password);
    }

    public boolean changeNick(String oldNickname, String newNickname) {
        return SQLHandler.changeNick(oldNickname, newNickname);
    }
}
