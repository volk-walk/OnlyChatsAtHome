package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegistrationController {

    private Controller controller;
    @FXML
    public TextField loginField;
    @FXML
    public TextField nicknameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    private TextArea textArea;

    @FXML
    public void tryToReg(ActionEvent actionEvent) {
    }
    public void showResult(String result){
        if (result.equals("/reg_ok")){
            textArea.appendText("Вы успешно зарегистрировались\n");
        }else{
            textArea.appendText("Регистрация не удалась. \nТакие логин или никнейм заняты\n");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
