
package Game;

/**
 *
 * @author Ebrar
 */
public class Message implements java.io.Serializable {
    public static enum Message_Type {None, Name, Disconnect,RivalConnected, Text, Die1, Die2, Turn, Selected, Selected2, Bitis,Start,}
    public Message_Type m_type;
    public Object content;
    public Message(Message_Type t)
    {
        this.m_type=t;
    } 
}
