
package Game;

import java.io.IOException;
import java.util.logging.Logger;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.io.ObjectOutputStream;
import java.net.Socket;
import Game.Game;
import static Game.Player.s_input;
/**
 *
 * @author Ebrar
 */
class Listen extends Thread { // serverdan gelecek mesajları dinleyen thread

    public void Run() {
        //soket bağlı olduğu sürece dön
        while (Player.socket.isConnected()) {
            try {
                //mesaj gelmesini bloking olarak dinyelen komut
                Message received = (Message) (s_input.readObject());

                //mesaj tipine göre yapılacak işlemi ayır.
                switch (received.m_type) {
                    case Name:
                        break;
                    case RivalConnected:
                        String name = received.content.toString();
                        Game.this_game.btn_send_message.setEnabled(true);
                        Game.this_game.timer_slider.start();
                        break;
                    case Disconnect:
                        break;
                    case Text:
                        Game.this_game.txt_receive.setText(received.content.toString());
                        break;
                    case Bitis:
                        break;
                }

            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }   
        }

    }
}

public class Player {

    public static Socket socket; //client için soket tanımlandı
    public static ObjectInputStream s_input; //verileri almak için nesne tanımlandı
    public static ObjectOutputStream s_output; //verileri göndermek için nesne tanımlandı
    public static Listen p_listen; //serverı dinlemek için thread oluşturuldu

    public static void Start(String ip, int port) {
        try {
            
            Player.socket = new Socket(ip, port); // Client Soket nesnesi
            Player.Display("Servera bağlandı");

            Player.s_input = new ObjectInputStream(Player.socket.getInputStream()); // input stream
            Player.s_output = new ObjectOutputStream(Player.socket.getOutputStream()); // output stream
            Player.p_listen = new Listen();
            Player.p_listen.start();
            
            //ilk mesaj olarak isim gönderildi.
            Message message = new Message(Message.Message_Type.Name);
            message.content = Game.this_game.txt_send.getText();
            Player.Send(message);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        public static void Display(String message) {
        System.out.println(message);
    }
    
    //mesaj gönderme fonksiyonu
    public static void Send(Message message) {
        try {
            Player.s_output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //client durdurma fonksiyonu
    public static void Stop() {
        try {
            if (Player.socket != null) {
                Player.p_listen.stop();
                Player.socket.close();
                Player.s_output.flush();
                Player.s_output.close();
                Player.s_input.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }

    }




}
