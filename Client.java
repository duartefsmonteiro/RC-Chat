import java.nio.channels.SelectionKey;
import java.util.LinkedList;

public class Client {
    public String nick;
    public String room;
    public STATE state;
    public SelectionKey key;
    
    public Client(SelectionKey key) {
        nick = null;
        room = null;
        state = STATE.INIT;
        this.key = key;
    }

    public String toString() {
        return room == null ? nick + " null " + state + " " + key : nick + " " + room + " " + state + " " + key;
    }

    public void joinRoom(String room) {
        if (this.room != null)
            leaveRoom();
        if(ChatServer.rooms.containsKey(room))
            ChatServer.rooms.get(room).add(this);
        else {
            LinkedList<Client> c = new LinkedList<Client>();
            c.add(this);
            ChatServer.rooms.put(room, c);
        }
        this.room = room;
        state = STATE.INSIDE;
    }

    public void leaveRoom() {
        ChatServer.rooms.get(room).remove(this);
        room = null;
        state = STATE.OUTSIDE;
    }
}