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
        authService = new SimpleAuthService();
        try {
            //Создаем серверный сокет
            server = new ServerSocket(PORT);
            System.out.println("Сервер запустился");

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
// /w qaz sdfsdfsd
    public void privateMessage(ClientHandler sender, String recipient, String msg){

        for (ClientHandler c: clients) {
            String message = String.format("%s: %s", sender.getNickname(), msg);
            if (recipient.equals(c.getNickname())){
                c.sendMessage(message);
                sender.sendMessage(message);
            }
        }
    }
    //метод добавления клиентов в список при подключении
    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }
    //метод удаления клиентов из списка при отключении
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
