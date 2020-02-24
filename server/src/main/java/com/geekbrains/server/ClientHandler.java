package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getNickname() {
        return this.nickname;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            (new Thread(() -> {
                try {
                    while(true) {
                        String msg = this.in.readUTF();
                        if (msg.startsWith("/auth ")) {
                            String[] tokens = msg.split("\\s");
                            String nick = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                            if (nick != null && !server.isNickBusy(nick)) {
                                this.sendMsg("/authok " + nick);
                                this.nickname = nick;
                                server.subscribe(this);

                                while(true) {
                                    while(true) {
                                        msg = this.in.readUTF();
                                        if (msg.startsWith("/")) {
                                            if (msg.equals("/end")) {
                                                this.sendMsg("/end");
                                                return;
                                            }

                                            if (msg.startsWith("/w ")) {
                                                tokens = msg.split("\\s", 3);
                                                server.privateMsg(this, tokens[1], tokens[2]);
                                            }

                                            if (msg.startsWith("/changenick ")) {
                                                tokens = msg.split("\\s", 2);
                                                if (tokens[1].contains(" ")) {
                                                    this.sendMsg("Ник не может содержать пробелов");
                                                } else if (server.getAuthService().changeNick(this.nickname, tokens[1])) {
                                                    this.sendMsg("/yournickis " + tokens[1]);
                                                    this.sendMsg("Ваш ник изменен на " + tokens[1]);
                                                    this.nickname = tokens[1];
                                                    server.broadcastClientsList();
                                                } else {
                                                    this.sendMsg("Не удалось изменить ник. Такой ник " + tokens[1] + " уже существует");
                                                }
                                            }
                                        } else {
                                            server.broadcastMsg(this.nickname + ": " + msg);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException var8) {
                    var8.printStackTrace();
                } finally {
                    this.disconnect();
                }

            })).start();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void sendMsg(String msg) {
        try {
            this.out.writeUTF(msg);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public void disconnect() {
        this.server.unsubscribe(this);

        try {
            this.in.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        try {
            this.out.close();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        try {
            this.socket.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }
}
