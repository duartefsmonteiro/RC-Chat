import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ChatClient {
    /* GUI Variables */
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();

    /* Connection Variables */
    int state;
    Socket clientSocket;
    DataOutputStream outServer;
    BufferedReader inServer;

    /* Constructor */
    public ChatClient(String server, int port) throws IOException {
        /* GUI Initialization */
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(event -> {
            try {
                newMessage(chatBox.getText());
            }
            catch (IOException exception) {}
            finally {
                    chatBox.setText("");
            }
        });
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent event) {
                chatBox.requestFocusInWindow();
            }
        });

        state = 0;
        clientSocket = new Socket(server, port);
        outServer = new DataOutputStream(clientSocket.getOutputStream());
        inServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    /* Add string to text box */
    public void printMessage(final String message) {
        chatArea.append(message);
    }

    /* Invoked method when user inserts a message */
    public void newMessage(String message) throws IOException {
        /* Check if message if empty */
        if (message.isEmpty())
            return;
        
        message = processMessage(message);
        byte[] ptext = message.getBytes("UTF-8");
        outServer.write(ptext, 0, ptext.length);
        outServer.flush();
    }

    /* Main method */
    public void run() throws IOException {
        STATE state = STATE.RUNNING;
        String server_msg;
        String[] token;

        while (state == STATE.RUNNING) {
            token = inServer.readLine().split(" ");
            server_msg = "";

            switch (token[0]) {
                case "MESSAGE":
                    server_msg = "[SERVER]: " + token[1] + ": ";
                    for (int i = 2; i < token.length; ++i)
                        server_msg += token[i] + " ";
                    break;
                case "NEWNICK":
                    server_msg = "[SERVER]: " + token[1] + " changed nick to " + token[2] + "";
                    break;
                case "JOINED":
                    server_msg = "[SERVER]: " + token[1] + " joined";
                    break;
                case "LEFT":
                    server_msg = "[SERVER]: " + token[1] + " left";
                    break;
                case "BYE":
                    state = STATE.QUIT;
                    server_msg = "[SERVER]: Godspeed!";
                    break;
                default:
                    for (int i = 0; i < token.length; ++i)
                        server_msg += token[i] + " ";
            }

            printMessage(server_msg + "\n");
        }

        clientSocket.close();
        System.exit(0);
    }

    public boolean isCommand(String text) {
        Set<String> commands = new HashSet<>(Arrays.asList("/nick", "/join", "/leave", "/bye", "/priv"));
        return commands.contains(text) ? true : false;
    }

    public String processMessage(String message) {
        return !message.startsWith("/") || isCommand(message.split(" ")[0]) ? message + "\n" : "/" + message + "\n";
    }

    /* Instantiate and Initialize ChatClient */
    public static void main(String[] args) throws NumberFormatException, IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }
}