package server;

import java.sql.*;

public class DataBaseAuthService implements AuthService {
    private static Connection connection;
    private static PreparedStatement psGetNickname;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNickname;

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname = null;
        try {
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet rs = psGetNickname.executeQuery();

                if (rs.next()) {
                    nickname = rs.getString("nickname");
                }
                rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nickname;
    }

    @Override
    public boolean registration(String login, String nickname, String password) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changeNickname(String newNickname, String oldNickname) {
        try {
            psChangeNickname.setString(1, newNickname);
            psChangeNickname.setString(2, oldNickname);
            psChangeNickname.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            prepareAllStatements();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            psRegistration.close();
            psGetNickname.close();
            psChangeNickname.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void prepareAllStatements() throws SQLException{
        psGetNickname = connection.prepareStatement("SELECT nickname FROM clients WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO clients(login, password, nickname) VALUES (? ,? ,? );");
        psChangeNickname = connection.prepareStatement("UPDATE clients SET nickname = ? WHERE nickname = ?;");
    }
}
