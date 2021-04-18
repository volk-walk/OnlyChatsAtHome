package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    

    private class User{
        private String login;
        private String password;
        private String nickname;

        public User(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }
    private List<User> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new User("qaz","qaz", "qaz"));
        users.add(new User("wsx","wsx", "wsx"));
        users.add(new User("edc","edc", "edc"));
        for (int i = 0; i < 10; i++) {
            users.add(new User("log"+i, "pass"+i, "nick"+i));
        }
    }
    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User u: users) {
            if (u.login.equals(login) && u.password.equals(password)){
                return u.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String nickname, String password) {
        for (User u: users) {
            if (u.login.equals(login) || u.nickname.equals(nickname)){
                return false;
            }
        }
        users.add(new User(login, password, nickname));
        return true;
    }

    @Override
    public boolean changeNickname(String newNickname, String login) {
        return false;
    }
}
