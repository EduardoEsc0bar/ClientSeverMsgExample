package org.example.clientsevermsgexample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ComboBox<String> dropdownPort;

    @FXML
    private Button clearBtn;

    @FXML
    private TextArea resultArea;

    @FXML
    private TextField urlName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7", "13", "21", "23", "71", "80", "119", "161");
    }

    @FXML
    void checkConnection(ActionEvent event) {
        String host = urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue());

        try (Socket sock = new Socket(host, port)) {
            resultArea.appendText(host + " listening on port " + port + "\n");
        } catch (UnknownHostException e) {
            resultArea.setText(e + "\n");
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port " + port + "\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.setText("");
        urlName.setText("");
    }

    @FXML
    void startServer(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();

        Label titleLabel = new Label("Server");
        titleLabel.setLayoutX(100);
        titleLabel.setLayoutY(50);

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefSize(400, 200);
        chatArea.setLayoutX(100);
        chatArea.setLayoutY(100);

        TextField inputField = new TextField();
        inputField.setLayoutX(100);
        inputField.setLayoutY(320);
        inputField.setPrefWidth(300);

        Button sendButton = new Button("Send");
        sendButton.setLayoutX(420);
        sendButton.setLayoutY(320);

        root.getChildren().addAll(titleLabel, chatArea, inputField, sendButton);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Server");
        stage.show();

        new Thread(() -> runServer(chatArea, inputField, sendButton)).start();
    }

    private void runServer(TextArea chatArea, TextField inputField, Button sendButton) {
        try (ServerSocket serverSocket = new ServerSocket(6666)) {
            appendText(chatArea, "Server running on port 6666\nWaiting for client...\n");
            Socket clientSocket = serverSocket.accept();
            appendText(chatArea, "Client connected!\n");

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

            Thread reader = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = dis.readUTF()) != null) {
                        appendText(chatArea, "Client: " + msg + "\n");
                    }
                } catch (IOException e) {
                    appendText(chatArea, "Client disconnected.\n");
                }
            });
            reader.setDaemon(true);
            reader.start();

            sendButton.setOnAction(e -> {
                String message = inputField.getText();
                try {
                    dos.writeUTF(message);
                    dos.flush();
                    appendText(chatArea, "Server: " + message + "\n");
                    inputField.clear();
                } catch (IOException ex) {
                    appendText(chatArea, "Error sending message.\n");
                }
            });

        } catch (IOException e) {
            appendText(chatArea, "Error: " + e.getMessage() + "\n");
        }
    }

    @FXML
    void startClient(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();

        Label titleLabel = new Label("Client");
        titleLabel.setLayoutX(100);
        titleLabel.setLayoutY(50);

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefSize(400, 200);
        chatArea.setLayoutX(100);
        chatArea.setLayoutY(100);

        TextField inputField = new TextField();
        inputField.setLayoutX(100);
        inputField.setLayoutY(320);
        inputField.setPrefWidth(300);

        Button sendButton = new Button("Send");
        sendButton.setLayoutX(420);
        sendButton.setLayoutY(320);

        root.getChildren().addAll(titleLabel, chatArea, inputField, sendButton);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Client");
        stage.show();

        new Thread(() -> runClient(chatArea, inputField, sendButton)).start();
    }

    private void runClient(TextArea chatArea, TextField inputField, Button sendButton) {
        try {
            Socket socket = new Socket("localhost", 6666);
            appendText(chatArea, "Connected to server!\n");

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            Thread reader = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = dis.readUTF()) != null) {
                        appendText(chatArea, "Server: " + msg + "\n");
                    }
                } catch (IOException e) {
                    appendText(chatArea, "Server disconnected.\n");
                }
            });
            reader.setDaemon(true);
            reader.start();

            sendButton.setOnAction(e -> {
                String message = inputField.getText();
                try {
                    dos.writeUTF(message);
                    dos.flush();
                    appendText(chatArea, "Client: " + message + "\n");
                    inputField.clear();
                } catch (IOException ex) {
                    appendText(chatArea, "Error sending message.\n");
                }
            });

        } catch (IOException e) {
            appendText(chatArea, "Error: " + e.getMessage() + "\n");
        }
    }

    private void appendText(TextArea area, String text) {
        Platform.runLater(() -> area.appendText(text));
    }
}
