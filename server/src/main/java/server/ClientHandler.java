package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String login;

    //конструктор подключаемого клиента
    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            //инициализируем отправку и чтение данных
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //отдельный поток для чтения приходящих данных,
            //чтобы можно было читать сразу нескольким клиентам
            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
                    //цикл аунтентификации
                    while (true) {
                        //считываем приходящие данные логина и пароля
                        String clientMessage = in.readUTF();
                        //при отправке "/end" закрываем соединение
                        if (clientMessage.equals("/end")) {
                            out.writeUTF("/end");
                            throw new RuntimeException("Клиент решил отключиться");
                        }
                        //если сообщение начинается на /auth
                        //считываем его как попытку аутентифицироваться
                        //???не понял как мы понимаем, что приходящее сообщение начинается на /auth,???
                        //???мы же не отправляем такого сообщения????
                        if (clientMessage.startsWith("/auth")) {
                            //разделяем приходящее сообщение с помощью сплита
                            //на 2 токена: логин и пароль
                            String[] token = clientMessage.split("\\s+", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if (!server.loginIsAuthenticated(login)) {
                                    nickname = newNick;
                                    sendMessage("/auth_okay " + nickname);
                                    server.subscribe(this);
                                    System.out.println("Клиент аутентифицировался. Никнейм " + nickname +
                                            " Адрес: " + socket.getRemoteSocketAddress());
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMessage("Такой пользователь уже авторизирован. Попробуйте снова" + "\n");
                                }
                            } else {
                                sendMessage("Неверный логин или пароль" + "\n");
                            }

                        }
                        //регистрация
                        if (clientMessage.startsWith("/reg")) {
                            //разделяем приходящее сообщение с помощью сплита
                            String[] token = clientMessage.split("\\s+", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMessage("/reg_ok");
                            } else {
                                sendMessage("/reg_no");
                            }
                        }

                    }


                    //в бесконечном цикле ждем пока нам что-либо напишут или напишем мы
                    //цикл работы
                    while (true) {

                        //считываем приходящие данные сообщений
                        String clientMessage = in.readUTF();

                        //при отправке "/end" закрываем соединение
                        if (clientMessage.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        if (clientMessage.startsWith("/w")) {
                            String[] token = clientMessage.split("\\s+", 3);
                            server.privateMessage(this, token[1], token[2]);
                        } else {
                            //отправляем введенное сообщение всем подключенным клиентам
                            server.broadcastMessage(this, clientMessage);
                        }
                    }
                }catch (SocketTimeoutException e){
                    try {
                        out.writeUTF("/end");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }catch (RuntimeException e){
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //обязательно удаляем клиента из списка подключенных клиентов,
                    // при его выходе из чата
                    server.unsubscribe(this);
                    System.out.println("Client " + socket.getRemoteSocketAddress() + " disconnect");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //метод для отправки сообщений, написанных клиентом
    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //геттер никнейма подлкюченного клиента
    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
