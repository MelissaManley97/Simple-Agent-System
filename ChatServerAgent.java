
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatServerAgent class that supports sending and receiving messages from a
 * ChatClientAgent. Note that messages have the following format: "message + : +
 * agentUID".
 */
public class ChatServerAgent extends Agent {

    private boolean establishedConnection = false; // Whether this server has connected to a client.


    /**
     * Constructor for ChatServerAgent.
     */
    public ChatServerAgent() {
        super("ChatServerAgent");
    }

    /**
     * Starts up this agent by looking for a server and then supporting chatting
     * until the chat has ended.
     *
     * @return boolean representing completion of the method.
     */
    @Override
    public boolean start() {
        try {
            boolean endChat = false;
            System.out.println("ChatServerAgent[id=" + getUID() + "]: Waiting for clients...");
            byte[] buf = new byte[1000];
            DatagramPacket dgp = new DatagramPacket(buf, buf.length);
            DatagramSocket sk = new DatagramSocket(AgentSystem.getPort());

            // While the chat has not ended, send and receive messages.
            while (!endChat) {
                SocketPacketPackage pack = receiveMessage(sk, dgp); // Receive message from client.

                // Process received msg
                String str = new String(pack.packet.getData(), 0, pack.packet.getLength());
                String[] split = str.split(":");
                if (split[0].toLowerCase().equals("end chat")) {
                    System.out.println("...ending chat");
                    endChat(pack);
                    endChat = true;
                } else {
                    sendMessage(pack, split);
                }
            }
            return true; // Successful completion.
        } catch (SocketException ex) {
            Logger.getLogger(PongAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatServerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; // Failed completion.
    }

    /**
     * Sends a message to the client agent.
     *
     * @param pack    SocketPacketPackage containing the socket/packet necessary to
     *                send a message.
     * @param split   String array containing the message previously received from
     *                the client, where the original string was split with delimiter ":".
     * @throws IOException
     */
    private void sendMessage(SocketPacketPackage pack, String[] split) throws IOException {
        String outMessage;

        // Client's initial broadcast message received. Prepare to connect.
        if (split[0].equals(ChatClientAgent.getBroadcastMsg())) {
            outMessage = "IAMChatServerAgent:" + getUID();
        } else {
            // Connection already established. outMessage is the user input.
            System.out.print("Enter message: ");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            outMessage = stdin.readLine();
            outMessage += ":" + getUID();
        }
        byte[] buf = outMessage.getBytes();
        DatagramPacket out = new DatagramPacket(buf, buf.length, pack.packet.getAddress(), pack.packet.getPort());

        // Have the agent system send the message.
        AgentSystem.sendMsg(pack.socket, outMessage, pack.packet.getAddress(), pack.packet.getPort());
    }

    /**
     * Receives a message from the client.
     *
     * @param socket DatagramSocket that will receive the message.
     * @param dgPacket DatagramPacket that will contain the received message.
     * @return SocketPacketPackage representing the socket and packet that
     * contain data about the message received.
     */
    private SocketPacketPackage receiveMessage(DatagramSocket socket, DatagramPacket dgPacket) {
        try {
            socket.receive(dgPacket); // Receive message.
            String str = new String(dgPacket.getData(), 0, dgPacket.getLength());
            String[] split = str.split(":");

            if(!establishedConnection) { // Broadcast message
                // Establish connection.
                System.out.println("Connection granted: Wait for client to send message. Type 'end chat' to end the chat.");
                establishedConnection = true;
            } else {
              System.out.println("Received: " + split[0]);
            }
            return (new SocketPacketPackage(socket, dgPacket));
        } catch (SocketException ex) {
            Logger.getLogger(PongAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PongAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; // Error occurred.
    }

    /**
     * Ends a chat between the client and server agents.
     *
     * @param pack SocketPacketPackage containing the socket/packet necessary to
     * send a message.
     */
    private void endChat(SocketPacketPackage pack) {
        String outMessage = "end chat";
        byte[] buf = outMessage.getBytes();
        DatagramPacket out = new DatagramPacket(buf, buf.length, pack.packet.getAddress(), pack.packet.getPort());

        // Have the agent system send the "end chat" message.
        AgentSystem.sendMsg(pack.socket, outMessage, pack.packet.getAddress(), pack.packet.getPort());
    }
}
