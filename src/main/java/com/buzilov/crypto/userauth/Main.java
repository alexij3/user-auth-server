package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.audit.service.AuditLogService;
import com.buzilov.crypto.userauth.db.DocumentRepository;
import com.buzilov.crypto.userauth.dto.Document;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.buzilov.crypto.userauth.exception.UserAlreadyRegisteredException;
import com.buzilov.crypto.userauth.mac.OperationPermissionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Main {

    private static int PORT = 5555;

    private static final DocumentRepository repository = new DocumentRepository();

    private static final String QUIT_COMMAND = "q";
    private static final String REGISTER_COMMAND = "register";
    private static final String AUTH_COMMAND = "auth";
    private static final String LIST_ALL_DOCUMENTS_COMMAND = "listAll";
    private static final String APPEND_TEXT_COMMAND = "appendText";
    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static AuditLogService auditLogService = new AuditLogService();

    public static void main(String[] args) {

        LOGGER.info("Server started.");

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }

        } catch (Exception e){
            LOGGER.error("Error has occured in main method", e);
        }

    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private String name;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            clientHandlers.add(this);
            this.name = "Client " + clientHandlers.size();
        }

        @Override
        public void run() {
            try {
                LOGGER.info("New client {} has connected to the server.", this.name);

                InputStream sin = clientSocket.getInputStream();
                OutputStream sout = clientSocket.getOutputStream();

                DataInputStream in = new DataInputStream(sin);
                DataOutputStream out = new DataOutputStream(sout);

                String message = null;

                boolean run = true;

                out.writeUTF("Hello!\n" + getMainMessage());

                while (run) {
                    if (!Authentication.isUserAuthenticated()) {
                        message = in.readUTF();
                        switch (message) {
                            case QUIT_COMMAND:
                                out.writeUTF("Closing the connection...");
                                run = false;
                                clientSocket.close();
                                clientHandlers.remove(this);
                                LOGGER.info("{} has been disconnected from the server.", this.name);
                                break;
                            case REGISTER_COMMAND: {
                                out.writeUTF("Enter login: ");
                                String login = in.readUTF();
                                out.writeUTF("Enter password: ");
                                String password = in.readUTF();

                                try {
                                    register(login, password);
                                    out.writeUTF("Successfully registered! What will you do next?\n" + getMainMessage());
                                    LOGGER.info("Registered user with login {}.", login);
                                } catch (Exception e) {
                                    out.writeUTF(e.getMessage());
                                    LOGGER.error("Something went wrong during registration", e);
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
                                        LOGGER.info("Authenticated user with login {}.", login);
                                    } else {
                                        out.writeUTF("Bad credentials. Try again.\n" + getMainMessage());
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Something went wrong during authentication", e);
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
                        message = in.readUTF();
                        switch (message) {
                            case QUIT_COMMAND:
                                LOGGER.info("User {} has logged out.", Authentication.getCurrentUserInfo().getLogin());
                                auditLogService.writeLog(Authentication.getCurrentUserInfo().getLogin(), "Logging out.");
                                Authentication.setUserInfo(null);
                                out.writeUTF(getMainMessage());
                                break;

                            case LIST_ALL_DOCUMENTS_COMMAND:
                                List<Document> documents = getAllDocuments();
                                out.writeUTF("[Documents]:\n" + getAllDocumentsMessage(documents) + "\n" + getDocumentsOperationsMessage());

                                boolean isInAllDocumentsList = true;

                                while (isInAllDocumentsList) {

                                    message = in.readUTF();

                                    if (message.equals(QUIT_COMMAND)) {
                                        isInAllDocumentsList = false;
                                        out.writeUTF(getAuthenticatedMessage());
                                    } else {
                                        try {
                                            final int documentId = Integer.parseInt(message);
                                            Optional<Document> detailedDocument = documents.stream()
                                                    .filter(document -> document.getId() == documentId)
                                                    .findFirst();

                                            if (detailedDocument.isPresent()) {
                                                boolean isInDetailedDocument = true;

                                                Document detailedDocumentObject = detailedDocument.get();
                                                out.writeUTF(getDocumentDetails(detailedDocumentObject) + "\n" + getDetailedDocumentOperations(detailedDocumentObject));

                                                while (isInDetailedDocument) {
                                                    message = in.readUTF();

                                                    switch (message) {
                                                        case QUIT_COMMAND:
                                                            isInDetailedDocument = false;
                                                            out.writeUTF(getDocumentsOperationsMessage());
                                                            break;

                                                        case APPEND_TEXT_COMMAND:
                                                            out.writeUTF("Write text to append to document's content: ");
                                                            message = in.readUTF();
                                                            detailedDocumentObject.setContent(detailedDocumentObject.getContent() + message);
                                                            Document updatedDocument = repository.update(detailedDocumentObject);
                                                            out.writeUTF("\n" + getDocumentDetails(updatedDocument) + "\n" + getDetailedDocumentOperations(updatedDocument) + "\n");
                                                            break;

                                                        default:
                                                            out.writeUTF("Wrong command! Try again.\n");
                                                            break;
                                                    }
                                                }


                                            } else {
                                                out.writeUTF(String.format("Wrong document id '%d'. Try again.\n", documentId));
                                                LOGGER.error("Could find requested document with id {}", documentId);
                                            }

                                        } catch (NumberFormatException e) {
                                            out.writeUTF(String.format("You should either type '%s' or valid document ID! Try again.\n", QUIT_COMMAND));
                                        }
                                    }
                                }

                                break;

                            default:
                                out.writeUTF("Unknown command." + "\n\n" + getAuthenticatedMessage());
                                out.flush();
                                break;
                        }
                    }

                }
            } catch (Exception e) {
                try {
                    clientSocket.close();
                    LOGGER.error("Something went wrong on the {} side, trying to close the socket.", this.name);
                    clientHandlers.remove(this);
                } catch (IOException ex) {
                    LOGGER.error("Something went wrong when closing the client socket.", e);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientHandler that = (ClientHandler) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
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
            registeredUser.ifPresent(Authentication::setUserInfo);
            return registeredUser;
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    public static List<Document> getAllDocuments() {
        return repository.getAll();
    }

    public static String getMainMessage() {
        return String.format("Type '%s' to register using login and password\nType '%s' to authenticate with login and password\nType '%s' to quit\n",
                REGISTER_COMMAND, AUTH_COMMAND, QUIT_COMMAND);
    }

    public static String getAuthenticatedMessage() {
        return String.format("1) Type '%s' to list all the documents.\n2) Type '%s' to logout.", LIST_ALL_DOCUMENTS_COMMAND, QUIT_COMMAND);
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

            stringBuilder.append("\n\n");
        }

        return stringBuilder.toString();
    }

    public static String getDocumentsOperationsMessage() {
        return "1) Enter document ID to get detailed content.\n2) Enter 'q' to go back.\n";
    }

    public static String getDocumentDetails(Document document) {
        auditLogService.writeLog(Authentication.getCurrentUserInfo().getLogin(), String.format("Requested details for document with id %d", document.getId()));
        LOGGER.info("User {} has requested details for document with ID {}.", Authentication.getCurrentUserInfo().getLogin(), document.getId());

        StringBuilder sb = new StringBuilder();

        sb.append("[Document details]:")
                .append("\n")
                .append("ID: ")
                .append(document.getId())
                .append("\n")
                .append("Name: ")
                .append(document.getName())
                .append("\n")
                .append("Content: ")
                .append(document.getContent())
                .append("\n");

        return sb.toString();
    }

    public static String getDetailedDocumentOperations(Document document) {
        StringBuilder sb = new StringBuilder();

        OperationPermissionEvaluator evaluator = new OperationPermissionEvaluator();
        List<OperationPermissionEvaluator.Operation> evaluatedOperations = evaluator.evaluateOperations(document, Authentication.getCurrentUserInfo());

        if (evaluatedOperations.contains(OperationPermissionEvaluator.Operation.WRITE)) {
            sb.append(String.format("1) Type '%s' to append text to document content.\n2) Type '%s' to go back.\n", APPEND_TEXT_COMMAND, QUIT_COMMAND));
        } else {
            sb.append(String.format("Type '%s' to go back.\n", QUIT_COMMAND));
        }

        return sb.toString();
    }

}