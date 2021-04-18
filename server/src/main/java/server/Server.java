package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    public static ServerSocket server; //серверный сокет
    public static final int PORT = 8189; //порт для подключения клиентов
    public static Socket socket; //сокет связи с клиентами
    private List<ClientHandler> clients; // список подключенных клиентов
    private AuthService authService;

    //конструктор сервера, который создаем в методе StartServer
    public Server() {
        // удобный формат хранения списка подключенных клиентов
        clients = new CopyOnWriteArrayList<>();
//        authService = new SimpleAuthService();
        authService = new DataBaseAuthService();
        try {
            //Создаем серверный сокет
            server = new ServerSocket(PORT);
            System.out.println("Я внутри))) Начинаю взлом");

            // в бесконечном цикле ждем подключения к серверу
            while (true) {

                //ждем подключения
                socket = server.accept();
                System.out.println("Client connected" + socket.getRemoteSocketAddress());

                //при подключении добавляем клиента в список их хранения
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //метод отправки сообщения клиента всем подключенным к этому серверу клиентам
    public void broadcastMessage(ClientHandler sender, String msg) {
        String message = String.format("%s: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }
    public void privateMessage(ClientHandler sender, String recipient, String msg){

        for (ClientHandler c: clients) {
            String message = String.format("[%s] to [%s]: %s", sender.getNickname(),recipient, msg);
            if (recipient.equals(c.getNickname())){
                c.sendMessage(message);
                sender.sendMessage(message);
            }
        }
    }
    //метод добавления клиентов в список при подключении
    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }
    //метод удаления клиентов из списка при отключении
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }
    public boolean loginIsAuthenticated(String login){
        for (ClientHandler c:clients) {
            if (login.equals(c.getLogin())){
                return true;
            }
        }
        return false;
    }
    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientList");
        for (ClientHandler c: clients) {
            sb.append(" ").append(c.getNickname());
        }
        String message = sb.toString();
        for (ClientHandler c: clients) {
            c.sendMessage(message);
        }
    }
}
