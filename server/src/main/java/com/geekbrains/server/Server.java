package com.geekbrains.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients = new Vector();
    private AuthService authService;

    public AuthService getAuthService() {
        return this.authService;
    }

    public Server() {
        if (!SQLHandler.connect()) {
            throw new RuntimeException("Не удалось подключиться к БД");
        } else {
            this.authService = new DBAuthService();

            try {
                ServerSocket serverSocket = new ServerSocket(8189);
                Throwable var2 = null;

                try {
                    System.out.println("Сервер запущен на порту 8189");

                    while(true) {
                        Socket socket = serverSocket.accept();
                        new ClientHandler(this, socket);
                        System.out.println("Подключился новый клиент");
                    }
                } catch (Throwable var18) {
                    var2 = var18;
                    throw var18;
                } finally {
                    if (serverSocket != null) {
                        if (var2 != null) {
                            try {
                                serverSocket.close();
                            } catch (Throwable var17) {
                                var2.addSuppressed(var17);
                            }
                        } else {
                            serverSocket.close();
                        }
                    }

                }
            } catch (IOException var20) {
                var20.printStackTrace();
            } finally {
                System.out.println("Сервер завершил свою работу");
                SQLHandler.disconnect();
            }

        }
    }

    public void broadcastMsg(String msg) {
        Iterator var2 = this.clients.iterator();

        while(var2.hasNext()) {
            ClientHandler o = (ClientHandler)var2.next();
            o.sendMsg(msg);
        }

    }

    public void privateMsg(ClientHandler sender, String receiverNick, String msg) {
        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMsg("заметка для себя: " + msg);
        } else {
            Iterator var4 = this.clients.iterator();

            ClientHandler o;
            do {
                if (!var4.hasNext()) {
                    sender.sendMsg("Клиент " + receiverNick + " не найден");
                    return;
                }

                o = (ClientHandler)var4.next();
            } while(!o.getNickname().equals(receiverNick));

            o.sendMsg("от " + sender.getNickname() + ": " + msg);
            sender.sendMsg("для " + receiverNick + ": " + msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        this.clients.add(clientHandler);
        this.broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        this.clients.remove(clientHandler);
        this.broadcastClientsList();
    }

    public boolean isNickBusy(String nickname) {
        Iterator var2 = this.clients.iterator();

        ClientHandler o;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            o = (ClientHandler)var2.next();
        } while(!o.getNickname().equals(nickname));

        return true;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * this.clients.size());
        sb.append("/clients ");
        Iterator var2 = this.clients.iterator();

        while(var2.hasNext()) {
            ClientHandler o = (ClientHandler)var2.next();
            sb.append(o.getNickname()).append(" ");
        }

        sb.setLength(sb.length() - 1);
        String out = sb.toString();
        Iterator var6 = this.clients.iterator();

        while(var6.hasNext()) {
            ClientHandler o = (ClientHandler)var6.next();
            o.sendMsg(out);
        }

    }
}