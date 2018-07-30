
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatClientAgent class that supports sending and receiving messages from a
 * ChatServerAgent. Note that messages have the following format: "message + : + agentUID".
 */
public class ChatClientAgent extends Agent {

    private InetAddress addressOfServer; // Server's IP address

    private static final String BROADCAST_MSG = "Looking for chat server"; // Client's broadcast message.

    /**
     * Constructor for ChatClientAgent.
     */
    public ChatClientAgent() {
        super("ChatClientAgent");
    }


    /**
     * Retrieves the chat client's broadcast message.
     * @return
     */
    public static String getBroadcastMsg() {
        return BROADCAST_MSG;
    }

    /**
     * Starts up this agent by looking for a server and then supporting chatting
     * until the chat has ended.
     *
     * @return boolean representing completion of the method.
     */
    @Override
    public boolean start() {
        boolean endChat = false;
        String serverID = lookForChatServer();
        // While the chat has not ended, send and receive messages.
        while (!endChat) {
            SocketPacketPackage socketPack = sendMessage(serverID);
            String rec = receiveMessage(socketPack, serverID);
            // Keyword for ending chat.
            if (rec.toLowerCase().equals("end chat")) {
                endChat();
                endChat = true;
            }
        }
        return true;
    }

    /**
     * Queries the AgentSystem for available ChatServer agents.
     *
     * @return String representing the UID of the found agent.
     */
    private String lookForChatServer() {
        System.out.println("ChatClientAgent[id=" + getUID() + "]: Looking for ChatServerAgents...");
        String foundID = "";
        String outMessage = BROADCAST_MSG;
        DatagramPacket pack;

        // While a ChatServer has not been found, query the AgentSystem.
        while (foundID.equals("")) {
            pack = AgentSystem.findAgent(outMessage);
            String str = new String(pack.getData(), 0, pack.getLength());
            String[] split = str.split(":");
            if (!split[0].equals("pong")) {
                foundID = split[1];
                addressOfServer = pack.getAddress();
            }
        }

        // Connection established.
        System.out.println("ChatClientAgent[id=" + getUID() + "]: Found ChatServerAgent[id=" + foundID + "]");
        System.out.println("Connection granted: type 'end chat' to end the chat");
        return foundID;
    }

    /**
     * Sends a message to the server agent.
     *
     * @param serverID String representing the UID of the server agent.
     * @return SocketPacketPackage representing the datagram socket and datagram
     * packet used to send the message.
     */
    private SocketPacketPackage sendMessage(String serverID) {
        System.out.print("Enter message: ");
        try {
            DatagramSocket s = new DatagramSocket();

            // User inputs their messsage.
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String outMessage = stdin.readLine();

            outMessage += ":" + getUID();
            byte[] buf = outMessage.getBytes();
            int port = AgentSystem.getPort();
            DatagramPacket out = new DatagramPacket(buf, buf.length, addressOfServer, port);

            // Have the agent system send the message.
            AgentSystem.sendMsg(s, outMessage, addressOfServer, port);
            return new SocketPacketPackage(s, out);
        } catch (SocketException ex) {
            Logger.getLogger(PingAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatClientAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; // Error occurred.
    }

    /**
     * Receives a message from the server.
     *
     * @param pack SocketPacketPackage representing the socket/packet pair that
     * is used to receive the message.
     * @param serverID String representing the UID of the server agent.
     * @return String representing the message received.
     */
    private String receiveMessage(SocketPacketPackage pack, String serverID) {
        try {
            pack.socket.receive(pack.packet); // Receive the datagram packet.

            // Process the received message.
            String str = new String(pack.packet.getData(), 0, pack.packet.getLength());
            String[] split = str.split(":");
            // If message includes a UID and a message (i.e. not a broadcast message).
            if (split.length > 1) {
                System.out.println("Received: " + split[0]);
            }
            return split[0];
        } catch (IOException ex) {
            Logger.getLogger(PingAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; // Error occurred.
    }

    /**
     * Ends a chat between the client and server agents.
     */
    private void endChat() {
        System.out.println("...ending chat");
        try {
            DatagramSocket s = new DatagramSocket();
            String outMessage = "end chat";
            byte[] buf = outMessage.getBytes();
            int port = AgentSystem.getPort();
            DatagramPacket out = new DatagramPacket(buf, buf.length, addressOfServer, port);

            // Have the AgentSystem send the message.
            AgentSystem.sendMsg(s, outMessage, addressOfServer, port);
        } catch (SocketException ex) {
            Logger.getLogger(PingAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
