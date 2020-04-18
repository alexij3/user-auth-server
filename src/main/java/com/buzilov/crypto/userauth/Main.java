package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;
import com.buzilov.crypto.userauth.exception.UserAlreadyRegisteredException;

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
                out.writeUTF("Hello!\n" + getMainMessage());

                message = in.readUTF();

                switch (message) {
                    case "q":
                        out.writeUTF("Closing the connection...");
                        run = false;
                        socket.close();
                        break;

                    case "register": {
                        out.writeUTF("Enter login: ");
                        String login = in.readUTF();
                        out.writeUTF("Enter password: ");
                        String password = in.readUTF();
                        out.writeUTF("Registering...");
                        out.writeUTF(register(login, password));
                        out.writeUTF("\n" + getMainMessage());
                        break;
                    }

                    case "auth":
                        out.writeUTF("Enter login: ");
                        String login = in.readUTF();
                        out.writeUTF("Enter password: ");
                        String password = in.readUTF();
                        out.writeUTF("Authenticating...");
                        out.writeUTF(authenticate(login, password));
                        out.writeUTF("\n" + getMainMessage());
                        break;

                    default:
                        out.writeUTF("Unknown command.");
                        break;
                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static String register(String login, String password) {
        UserRegistrationManager manager = new UserRegistrationManager();

        try {
            manager.register(login, password);
            return "Successfully registered!";
        } catch (UserAlreadyRegisteredException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Something went wrong during registration.";
        }

    }

    public static String authenticate(String login, String password) {
        UserAuthManager authManager = new UserAuthManager();

        try {
            return authManager.authenticate(login, password);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String getMainMessage() {
        return "Type 'register' to register using login and password\ntype 'auth' to authenticate with login and password\nType 'q' to quit";
    }

}