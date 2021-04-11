package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public TextField loginField;
    @FXML
    public TextField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox messagePanel;
    @FXML
    public ListView <String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;
    private boolean authenticated;
    private String nickname;
    private Stage stage;
    private Stage registrationStage;
    private RegistrationController registrationController;


    //метод отображения окон ввода логи и пароля
    //или отправки сообщения в зависимости от аутентификации
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);//если аутентикейтед тру, то панель аутентификации не видно и наоборот
        authPanel.setManaged(!authenticated);//если аутентикейтед тру, то панель аутентификации не резервирует место на окне чата и наоборот
        messagePanel.setVisible(authenticated);//если аутентикейтед тру, то панель ввода сообщений видно и наоборот
        messagePanel.setManaged(authenticated);//если аутентикейтед тру, то панель ввода сообщений резервирует место на окне чата и наоборот
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
        //если аутентикейтед тру, то стираем ник???
        if (!authenticated){
            nickname = "";
        }
        setTitle(nickname);//если аутентифицировались, то в титле видно ник авторизированного
        textArea.clear();//чистим текстарею при аутентификации и выходе
    }

    //метод инициализации окна чата с полем для логина и пароля
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //не понял зачем это???
        Platform.runLater(()->{
                stage = (Stage) textArea.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    System.out.println("sss");
                    if (socket != null && !socket.isClosed()){
                        try {
                            out.writeUTF("/end");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        });
        setAuthenticated(false);
    }


    //метод для подключения окна чата к серверу
    private void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //отдельный поток для работы окна чата
            new Thread(() -> {
            try {
                //цикл работы окна аутентификации
                while (true) {
                    String str = in.readUTF();
                    //если проходящее сообщение начинается на "/", то оно системное
                    if (str.startsWith("/")){
                        //если приходящее сообщение"/end", то выходим из аутентификации - не работает
                        if (str.equals("/end")) {
                            break;
                        }
                        //если приходящее сообщение начинается на /auth_okay,
                        //аутентификации - тру, а никнейм равен первому токену?
                        if (str.startsWith("/auth_okay")){
                            nickname = str.split("\\s+")[1];
                            setAuthenticated(true);
                            break;
                        }
                    }else {
                        textArea.appendText(str);
                    }

                }

                //цикл работы окна отправки сообщений
                while (authenticated) {
                    String str = in.readUTF();
                    if (str.startsWith("/")){
                        if (str.equals("/end")) {

                            break;
                        }
                        if (str.startsWith("/clientList ")){
                            String[] token = str.split("\\s+");
                            Platform.runLater(()->{
                                clientList.getItems().clear();
                                for (int i = 1; i < token.length; i++) {
                                    clientList.getItems().add(token[i]);
                                }
                            });

                        }
                    }else {
                        textArea.appendText(str + "\n");
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //при выходе из аккаунте меняем окно сообщений на окно аутентификации
                System.out.println("disconnect");
                setAuthenticated(false);
                try {
                    socket.close();//закрываем сокет
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод отправки сообщений
    @FXML
    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());//отправляем строку с филда ввода сообщений
            textField.clear();//чистим поле воода сообщения после отправки
            textField.requestFocus();//возвращаем фокус обратно после отправки сообщения
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    //метод отправки данных с полей логина и пароля по кнопке Log in
    @FXML
    public void logIn(ActionEvent actionEvent) {
        //если сокет = null или закрыт, то мы коннектимся
        if (socket == null || socket.isClosed()){
            connect();
        }
        //считываем строку с логин филда иp password филда
        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());//trim игнорирует меножество пробелов, считая их как один
        try {
            out.writeUTF(msg);//отправляем эту строку ClientHandler
            passwordField.clear();//чистим филд пароля
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод подставляющий в титл ник при аутентификации
    private void setTitle(String nickname){
        Platform.runLater(()->{
        if (nickname.equals("")){
            stage.setTitle("OnlyChats");
        }else {
            stage.setTitle(String.format("OnlyChats <[%s]>",nickname));
        }

        });
    }

    @FXML
    public void clickClientList(MouseEvent mouseEvent) {
        String recipient = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w "+recipient+" ");
    }
    public void createRegistrationWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/registration.fxml"));
            Parent root = fxmlLoader.load();
            registrationStage = new Stage();
            registrationStage.setTitle("Registration in <Open chat>");
            registrationStage.setScene(new Scene(root, 400, 300));

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);

            registrationController = fxmlLoader.getController();
            registrationController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void tryToReg(ActionEvent actionEvent){
        if (registrationStage == null){
            createRegistrationWindow();
        }
        registrationStage.show();
    }

}
