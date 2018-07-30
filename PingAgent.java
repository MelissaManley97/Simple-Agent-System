
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PingAgent class that supports looking for PongAgents and sending them pings.
 * ChatClientAgent. Note that messages have the following format: "message + : +
 * agentUID".
 */
public class PingAgent extends Agent {

    private InetAddress addressOfPong; // Pong's IP address.

    /**
     * Constructor for PingAgent.
     */
    public PingAgent() {
        super("PingAgent");
        try {
            addressOfPong = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(PingAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts up this agent by looking for PongAgents, sending them pings, and
     * receiving back pongs.
     *
     * @return boolean representing completion of the method.
     */
    @Override
    public boolean start() {
        String pongID = lookForPongs();
        SocketPacketPackage socketPack = sendPing(pongID);
        receiveMsg(socketPack, pongID); // Once ping receives a pong -> no further messaging.
        return true;
    }

    /**
     * Queries the AgentSystem for available PongAgents.
     *
     * @return String representing the UID of the found agent.
     */
    private String lookForPongs() {
        System.out.println("PingAgent[id=" + getUID() + "]: Looking for PongAgents...");
        String foundID = "";
        String outMessage = "Looking for pongs";
        DatagramPacket pack;

        // While a PongAgent has not been found, query the AgentSystem.
        while (foundID.equals("")) {
            pack = AgentSystem.findAgent(outMessage);
            String str = new String(pack.getData(), 0, pack.getLength());
            String[] split = str.split(":");
            if (!split[0].equals("pong")) {
                foundID = split[1];
                addressOfPong = pack.getAddress();
            }
        }

        // Connection established.
        System.out.println("PingAgent[id=" + getUID() + "]: Found PongAgent[id=" + foundID + "]");
        return foundID;
    }

    /**
     * Sends a message to the server agent.
     *
     * @param pongID String representing the UID of the server agent.
     * @return SocketPacketPackage representing the datagram socket and datagram
     * packet used to send the message.
     */
    private SocketPacketPackage sendPing(String pongID) {
        try {
            DatagramSocket s = new DatagramSocket();
            String outMessage = getUID() + ":ping";
            byte[] buf = outMessage.getBytes();
            int port = AgentSystem.getPort();
            DatagramPacket out = new DatagramPacket(buf, buf.length, addressOfPong, port);

            System.out.println("PingAgent[id=" + getUID() + "]: Sending ping to PongAgent[id=" + pongID + "]");

            // Have the agent system send the message.
            AgentSystem.sendMsg(s, outMessage, addressOfPong, port);
            return new SocketPacketPackage(s, out);
        } catch (SocketException ex) {
            Logger.getLogger(PingAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; // Error occurred.
    }

    /**
     * Receives a message from the server.
     *
     * @param pack SocketPacketPackage representing the socket/packet pair that
     * is used to receive the message.
     * @param pongID String representing the UID of the server agent.
     * @return String representing the message received.
     */
    private void receiveMsg(SocketPacketPackage pack, String pongID) {
        try {
            pack.socket.receive(pack.packet); // Receive the datagram packet.

            // Process the received message.
            String str = new String(pack.packet.getData(), 0, pack.packet.getLength());
            System.out.println("PingAgent[id=" + getUID() + "]: Received pong from PongAgent[id=" + pongID + "]" + str);
        } catch (IOException ex) {
            Logger.getLogger(PingAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
