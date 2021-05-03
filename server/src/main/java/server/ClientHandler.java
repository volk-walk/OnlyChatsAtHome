package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String login;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

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
                                    logger.info("Клиент аутентифицировался. Логин " + login +
                                            " Адрес: " + socket.getRemoteSocketAddress());
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    logger.info("Неудачная попытка аутентификации.");
                                    sendMessage("Такой пользователь уже авторизирован. Попробуйте снова" + "\n");
                                }
                            } else {
                                logger.info("Неудачная попытка аутентификации.");
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
                                logger.info("Зарегистрирован новый пользователь.\nЛогин: " + token[1] +
                                        "\nНикнейм: " + token[2]);
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
                        if (clientMessage.startsWith("/changenickname")){
                            logger.info("Попытка смены ника у клиента:" + this.login);
                            String[] token = clientMessage.split("\\s+",2);
                            if (token.length < 2){
                                continue;
                            }
                            if (token[1].contains(" ")){
                                sendMessage("Никнейк не может содержать пробелы");
                                continue;
                            }
                            if (server.getAuthService().changeNickname(token[1],this.nickname)){
                                logger.info("Клиент: " + this.login + " cменил никнейм с " + this.nickname +
                                        " на " + token[1]);
                                sendMessage("/yournickis " + token[1]);
                                sendMessage("Ваш никнейм изменен на " + token[1]);
                                this.nickname = token[1];
                                server.broadcastClientList();
                            }else {
                                sendMessage("Не удалось изменить никнейм. Никнейм " + token[1] + " уже занят.");
                            }

                        }
                        if (clientMessage.startsWith("/w")) {
                            String[] token = clientMessage.split("\\s+", 3);
                            server.privateMessage(this, token[1], token[2]);
                        } else {
                            //отправляем введенное сообщение всем подключенным клиентам
                            logger.info("Клиент " + login + " отправил сообщение.");
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
                    logger.info("Клиент " + login + " " + socket.getRemoteSocketAddress() + " отключился");
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
