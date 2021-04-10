package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    //конструктор подключаемого клиента
    public ClientHandler (Server server, Socket socket){
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
                    //цикл аунтентификации
                    while (true) {
                        //считываем приходящие данные логина и пароля
                        String clientMessage = in.readUTF();
                        //при отправке "/end" закрываем соединение
                        if (clientMessage.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        //если сообщение начинается на /auth
                        //считываем его как попытку аутентифицироваться
                        //???не понял как мы понимаем, что приходящее сообщение начинается на /auth,???
                        //???мы же не отправляем такого сообщения????
                        if (clientMessage.startsWith("/auth")){
                            //разделяем приходящее сообщение с помощью сплита
                            //на 2 токена: логин и пароль
                            String [] token = clientMessage.split("\\s+");
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1],token[2] );
                            if (newNick != null){
                                nickname = newNick;
                                sendMessage("/auth_okay " + nickname);
                                server.subscribe(this);
                                System.out.println("Клиент аутентифицировался. Никнейм "+ nickname +
                                        " Адрес: " + socket.getRemoteSocketAddress());
                                break;
                            }else{
                                sendMessage("Неверный логин или пароль" +"\n");
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
                        if(clientMessage.startsWith("/w")){
                            String[] token = clientMessage.split("\\s+",3);
                            server.privateMessage(this, token[1], token[2]);
                        }else{
                            //отправляем введенное сообщение всем подключенным клиентам
                            server.broadcastMessage(this, clientMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //обязательно удаляем клиента из списка подключенных клиентов,
                    // при его выходе из чата
                    server.unsubscribe(this);
                    System.out.println("Client " +socket.getRemoteSocketAddress() + " disconnect");
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
    public void sendMessage (String msg){
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
}
