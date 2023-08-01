import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private final String SERVER_ADRESS = "192.168.0.108";
    private Socket clientSocket;

    public void start () throws IOException{
        clientSocket = new Socket(SERVER_ADRESS, ChatServer.PORT);
        System.out.println("Client Conectado ao servidor em " + SERVER_ADRESS + " : " +ChatServer.PORT);
        
    } 

    private void massageLoop () {
        
    }

    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient();
            client.start();
        } catch (Exception e) {
            System.out.println("Erro ao conectar ao servidor " + e.getMessage());
        }
        System.out.println("Cliente desconectado");
    }
}
