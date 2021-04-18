package server;

import java.sql.*;

public class DataBaseAuthService implements AuthService {
    private static Connection connection;
    private static Statement stmt;

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT login, password, nickname FROM clients");

            while (rs.next()){
                if (rs.getString("login").equals(login)
                        && rs.getString("password").equals(password)){
                    return rs.getString("nickname");
                }
            }
                rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String nickname, String password) {
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT login, password, nickname FROM clients");

            while (rs.next()){
                if(rs.getString("login").equals(login)
                        || rs.getString("nickname").equals(nickname)){
                    return false;
                }

            }
            String str = String.format("INSERT INTO clients (login, password, nickname) VALUES ('%s', '%s', '%s')"
                    , login, password,nickname);
            stmt.executeUpdate(str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return true;
    }

    @Override
    public boolean changeNickname(String newNickname, String login) {
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT nickname FROM clients");
            while(rs.next()){
                if (rs.getString("nickname").equals(newNickname)){
                    return false;
                }
            }
            String str = String.format("UPDATE clients SET nickname = '%s' WHERE login = '%s'", newNickname, login);
            stmt.executeUpdate(str);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            disconnect();
        }
        return true;
    }

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    private static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
