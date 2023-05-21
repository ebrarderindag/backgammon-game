/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

/**
 *
 * @author Ebrar
 */
public class Message implements java.io.Serializable{
      //mesaj tipleri enum 
    public static enum Message_Type {None, Name, Disconnect,RivalConnected, Text, Die1, Die2, Turn, Selected, Selected2, Bitis,Start,}
    public Message_Type type;
    //mesajın içeriği obje tipinde ki istenilen tip içerik yüklenebilsin
    public Object content;
    public Message(Message_Type t)
    {
        this.type=t;
    }
}
