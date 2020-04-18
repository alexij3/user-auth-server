package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static int PORT = 5555;

    public static void main(String[] args) {

        try{
            ServerSocket serverSocket = new ServerSocket(PORT);

            Socket socket = serverSocket.accept();

            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);

            String message = null;

            boolean run = true;

            while (run) {
                out.writeUTF("Hello!\nType 'register' to register using login and password\ntype 'auth' to authenticate with login and password\nType 'q' to quit");

                message = in.readUTF();

                switch (message) {
                    case "q":
                        out.writeUTF("Closing the connection...");
                        run = false;
                        break;

                    case "register":
                        out.writeUTF("Enter login: ");
                        String login = in.readUTF();
                        out.writeUTF("Enter password: ");
                        String password = in.readUTF();
                        out.writeUTF("Registering...");

                        break;

                    case "auth":


                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}