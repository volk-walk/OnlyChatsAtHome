package server;

public interface AuthService {
    /**
     * Метод возвращает никнейм по логину и паролю
     * @return некнейм если совпадает логин и пароль и null - если совпадений нет
     * */
    String getNicknameByLoginAndPassword(String login, String password);
}
