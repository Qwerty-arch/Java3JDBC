package com.geekbrains.server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String var1, String var2);

    boolean changeNick(String var1, String var2);
}
