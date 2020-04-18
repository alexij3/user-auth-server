package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.db.DocumentRepository;
import com.buzilov.crypto.userauth.dto.Document;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.buzilov.crypto.userauth.exception.UserAlreadyRegisteredException;
import com.buzilov.crypto.userauth.mac.OperationPermissionEvaluator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class Main {

    private static int PORT = 5555;

    private static final String QUIT_COMMAND = "q";
    private static final String REGISTER_COMMAND = "register";
    private static final String AUTH_COMMAND = "auth";
    private static final String LIST_ALL_DOCUMENTS_COMMAND = "listAll";

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            Socket socket = serverSocket.accept();

            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);

            String message = null;

            boolean run = true;

            out.writeUTF("Hello!\n" + getMainMessage());

            while (run) {
                message = in.readUTF();

                if (!Authentication.isUserAuthenticated()) {
                    switch (message) {
                        case QUIT_COMMAND:
                            out.writeUTF("Closing the connection...");
                            run = false;
                            socket.close();
                            break;
                        case REGISTER_COMMAND: {
                            out.writeUTF("Enter login: ");
                            String login = in.readUTF();
                            out.writeUTF("Enter password: ");
                            String password = in.readUTF();

                            try {
                                register(login, password);
                                out.writeUTF("Successfully registered! What will you do next?\n" + getMainMessage());
                            } catch (Exception e) {
                                out.writeUTF(e.getMessage());
                            }

                            break;
                        }
                        case AUTH_COMMAND: {
                            out.writeUTF("Enter login: ");
                            String login = in.readUTF();
                            out.writeUTF("Enter password: ");
                            String password = in.readUTF();

                            try {
                                Optional<UserInfo> user = authenticate(login, password);
                                if (user.isPresent()) {
                                    out.writeUTF(String.format("Hello, %s! What will you do next?\n", login) + "\n" + getAuthenticatedMessage());
                                } else {
                                    out.writeUTF("Bad credentials. Try again.\n" + getMainMessage());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.writeUTF(e.getMessage() + "\nTry again.\n" + getMainMessage());
                            }
                            break;
                        }
                        default:
                            out.writeUTF("Unknown command." + "\n\n" + getMainMessage());
                            out.flush();
                            break;
                    }
                } else {
                    switch (message) {
                        case QUIT_COMMAND:
                            out.writeUTF("Closing the connection...");
                            run = false;
                            socket.close();
                            break;

                        case LIST_ALL_DOCUMENTS_COMMAND:
                            List<Document> documents = getAllDocuments();
                            out.writeUTF("Documents:\n" + getAllDocumentsMessage(documents));
                            break;

                        default:
                            out.writeUTF("Unknown command." + "\n\n" + getAuthenticatedMessage());
                            out.flush();
                            break;
                    }
                }

            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void register(String login, String password) throws Exception {
        UserRegistrationManager manager = new UserRegistrationManager();

        try {
            manager.register(login, password);
        } catch (UserAlreadyRegisteredException e) {
            throw new UserAlreadyRegisteredException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }

    }

    public static Optional<UserInfo> authenticate(String login, String password) throws Exception {
        UserAuthManager authManager = new UserAuthManager();

        try {
            Optional<UserInfo> registeredUser = authManager.authenticate(login, password);
            registeredUser.ifPresent(userInfo -> Authentication.currentUserInfo = userInfo);
            return registeredUser;
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    public static List<Document> getAllDocuments() {
        final DocumentRepository repository = new DocumentRepository();
        return repository.getAll();
    }

    public static String getMainMessage() {
        return String.format("Type '%s' to register using login and password\nType '%s' to authenticate with login and password\nType '%s' to quit\n",
                REGISTER_COMMAND, AUTH_COMMAND, QUIT_COMMAND);
    }

    public static String getAuthenticatedMessage() {
        return String.format("Type '%s' to list all the documents.", LIST_ALL_DOCUMENTS_COMMAND);
    }

    public static String getAllDocumentsMessage(List<Document> documents) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Document document : documents) {
            OperationPermissionEvaluator operationPermissionEvaluator = new OperationPermissionEvaluator();
            List<OperationPermissionEvaluator.Operation> evaluatedOperations = operationPermissionEvaluator.evaluateOperations(document, Authentication.getCurrentUserInfo());

            stringBuilder.append("Document ID: ")
                    .append(document.getId())
                    .append("\n")
                    .append("Document name: ")
                    .append(document.getName())
                    .append("\n")
                    .append("Allowed operations: ");

            for (OperationPermissionEvaluator.Operation operation : evaluatedOperations) {
                stringBuilder.append(operation);
                stringBuilder.append(" ");
            }
        }

        return stringBuilder.toString();
    }

}