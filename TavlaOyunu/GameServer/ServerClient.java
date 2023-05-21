
package GameServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Ebrar
 */
public class ServerClient extends Thread{
    
    Socket soket;
    ObjectOutputStream s_output;
    ObjectInputStream s_input;
    int id;
    public String name = "None";
    
    Listen listenThread; //clientten gelenleri dinleme threadi   
    PairingThread pairThread; //client eşleştirme thredi
    ServerClient rival; //rakip client
    public boolean paired = false; //eşleşme durumu

    public ServerClient(Socket i_socet, int id) {
        this.soket = i_socet;
        this.id = id;
        try {
            this.s_output = new ObjectOutputStream(this.soket.getOutputStream());
            this.s_input = new ObjectInputStream(this.soket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        //thread nesneleri
        this.listenThread = new Listen(this);
        this.pairThread = new PairingThread(this);

    }

    //client mesaj gönderme fonksiyonu
    public void Send(Message message) {
        try {
            this.s_output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //client dinleme threadi
    //her clientin ayrı bir dinleme threadı var
    class Listen extends Thread {

        ServerClient TheClient;

        //thread nesne alması için yapıcı metod
        Listen(ServerClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //client bağlı olduğu sürece dönsün
            while (TheClient.soket.isConnected()) {
                try {
                    
                    Message received = (Message) (TheClient.s_input.readObject()); //mesaj bekleme
          
                    //mesaj tipine göre işlemlere ayır
                    switch (received.type) {
                        case Name:
                            TheClient.name = received.content.toString();
                            // isim verisini gönderdikten sonra eşleştirme işlemine başla
                            TheClient.pairThread.start();
                            break;
                        case Disconnect:
                            break;
                        case Text:
                            //gelen metni rakibe gönder
                            Server.Send(TheClient.rival, received);
                            break;
                        case Bitis:
                            break;

                    }

                } catch (IOException ex) {
                    Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
                    //client bağlantısı koparsa listeden sil
                    Server.Clients.remove(TheClient);

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
                    //client bağlantısı koparsa listeden sil
                    Server.Clients.remove(TheClient);
                }
            }

        }
    }

    
    //her clientin bir eşleştirme threadi var
    class PairingThread extends Thread { //eşleştirme threadi

        ServerClient TheClient;

        PairingThread(ServerClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //client bağlı ve eşleşmemiş olduğu durumda dön
            while (TheClient.soket.isConnected() && TheClient.paired == false) {
                try {
                    //lock mekanizması
                    //sadece bir client içeri girebilir
                    //diğerleri release olana kadar bekler
                    Server.pairTwo.acquire(1);
                    
                    //client eğer eşleşmemişse gir
                    if (!TheClient.paired) {
                        ServerClient crival = null;
                        //eşleşme sağlanana kadar dön
                        while (crival == null && TheClient.soket.isConnected()) {
                            //liste içerisinde eş arıyor
                            for (ServerClient clnt : Server.Clients) {
                                if (TheClient != clnt && clnt.rival == null) {
                                    //eşleşme sağlandı ve gerekli işaretlemeler yapıldı
                                    crival = clnt;
                                    crival.paired = true;
                                    crival.rival = TheClient;
                                    TheClient.rival = crival;
                                    TheClient.paired = true;
                                    break;
                                }
                            }
                            //saniyede bir dönsün
                            sleep(1000); //thredi uyutuyoruz
                        }
                        //eşleşme başarılı
                        //her iki tarafa da eşleşme mesajı gönder ve oyunu başlat
                        Message msg1 = new Message(Message.Message_Type.RivalConnected);
                        msg1.content = TheClient.name;
                        Server.Send(TheClient.rival, msg1);

                        Message msg2 = new Message(Message.Message_Type.RivalConnected);
                        msg2.content = TheClient.rival.name;
                        Server.Send(TheClient, msg2);
                        
                        Message msg3 = new Message(Message.Message_Type.Selected);
                        msg3.content = TheClient.name;
                        Server.Send(TheClient.rival, msg3);
                        
                        Message msg4 = new Message(Message.Message_Type.Selected2);
                        msg4.content = TheClient.rival.name;
                        Server.Send(TheClient, msg4);
                    }
                    //lock mekanizması serbest bırakıldı, bırakılmazsa deadlock olur
                    Server.pairTwo.release(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PairingThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
