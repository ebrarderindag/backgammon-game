
package GameServer;
import java.util.logging.Logger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;


/**
 *
 * @author Ebrar
 */
//client gelişini dinleme threadi
class ServerThread extends Thread {

    public void Run() {
        
        while (!Server.server_socket.isClosed()) { //server kapanana kadar dinle
            try {
                Server.Display("Client Bekleniyor...");
                // clienti bekleyen satır
                //bir client gelene kadar bekler
                Socket clientSocket = Server.server_socket.accept();
                //client gelirse bu satıra geçer
                Server.Display("Client Geldi.");
                //gelen client soketinden bir sclient nesnesi oluştur
                //bir adet id de kendimiz verdik
                ServerClient n_Client = new ServerClient(clientSocket, Server.client_id);
                
                Server.client_id++;              
                Server.Clients.add(n_Client); //client listeye eklendi                
                n_Client.listenThread.start(); //client mesaj dinlemesini başlatıldı.

            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

public class Server {
   
    public static ServerSocket server_socket; //server soketi eklendi
    public static int client_id = 0;  
    public static int port = 0; // Serverın dileyeceği port
    public static ServerThread run_thread; //Serverı sürekli dinlemede tutacak thread nesnesi
    public static ArrayList<ServerClient> Clients = new ArrayList<>();   
    public static Semaphore pairTwo = new Semaphore(1, true); //semafor nesnesi
    
    
     
    
    // serverdan clientlere mesaj gönderme fonksiyonu
    public static void Send(ServerClient cl, Message msg) { 

        try {
            cl.s_output.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
        public static void Display(String msg) {

        System.out.println(msg);

    }

    //başlaşmak için port numarası veriyoruz
    public static void Start(int openport) {
        try {
            Server.port = openport;
            Server.server_socket = new ServerSocket(Server.port);

            Server.run_thread = new ServerThread();
            Server.run_thread.start();

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }




}
