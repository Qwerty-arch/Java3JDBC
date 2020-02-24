package com.geekbrains.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psGetNickByLoginAndPassword;
    private static PreparedStatement psChangeNick;

    public SQLHandler() {       // модуль отвечает за обработку всех всевозможных запросов
    }

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            psChangeNick = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");
            psGetNickByLoginAndPassword = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            return true;
        } catch (Exception var1) {
            var1.printStackTrace();
            return false;
        }
    }

    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;

        try {
            psGetNickByLoginAndPassword.setString(1, login);
            psGetNickByLoginAndPassword.setString(2, password);
            ResultSet rs = psGetNickByLoginAndPassword.executeQuery();
            if (rs.next()) {
                nick = rs.getString(1);
            }

            rs.close();
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

        return nick;
    }

    public static boolean changeNick(String oldNickname, String newNickname) {
        try {
            psChangeNick.setString(1, newNickname);
            psChangeNick.setString(2, oldNickname);
            psChangeNick.executeUpdate();
            return true;
        } catch (SQLException var3) {
            return false;
        }
    }

    public static void disconnect() {
        try {
            psGetNickByLoginAndPassword.close();
        } catch (SQLException var3) {
            var3.printStackTrace();
        }

        try {
            psChangeNick.close();
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException var1) {
            var1.printStackTrace();
        }

    }
}

