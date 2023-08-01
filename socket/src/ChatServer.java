import java.net.ServerSocket;
import java.util.ArrayList;
import java.io.IOException;

public class ChatServer {
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private final ArrayList<ClientSocket> clients = new ArrayList<>();

    public void start () throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado na porta: " + PORT);
        clientConectionLoop();
    }
    
    private void clientConectionLoop() throws IOException{
        while (true) {
            ClientSocket clientSocket = new ClientSocket(serverSocket.accept()); // esse méteodo no parentes retorna um socket ent eu pego ele
            clients.add(clientSocket);
            new Thread(() -> clientMessageLoop(clientSocket)).start();
        }
        
    }

    private void clientMessageLoop (ClientSocket clientSocket) {
        String msg;
        try {
            while((msg = clientSocket.getMessage()) != null){
                System.out.println("O cliente: " + clientSocket.getRemoteSocketAddress() + " Digitou: " + msg);
                sendMessageToAll(clientSocket, msg);
            }
        } finally {
            System.out.println("Cliente: " + clientSocket.getRemoteSocketAddress() + " Descnectou");
            clients.remove(clientSocket);
            clientSocket.close(); // ao finalizar o serviço precisamos fechar
        }
    }

    private void sendMessageToAll (ClientSocket sender, String message){
        for (ClientSocket clientSocket : clients) {
            if(!sender.equals(clientSocket)) {
                clientSocket.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer();
        server.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
        System.out.println("Servidor finalizado");
    }



}
