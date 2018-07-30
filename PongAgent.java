
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PongAgent class that supports sending "pong" when it is "pinged" from a
 * PingAgent. Note that messages have the following format: "message + : +
 * agentUID".
 */
public class PongAgent extends Agent {

    /**
     * Constructor for PongAgent.
     */
    public PongAgent() {
        super("PongAgent");
    }

    /**
     * Starts up this agent by waiting to be "pinged" by a PingAgent and
     * responding with a "pong".
     *
     * @return boolean representing completion of the method.
     */
    @Override
    public boolean start() {
        try {
            boolean stop = false;
            System.out.println("PongAgent[id=" + getUID() + "]: Waiting for pings...");
            byte[] buf = new byte[1000];
            int PORT = AgentSystem.getPort();
            DatagramPacket dgp = new DatagramPacket(buf, buf.length);
            DatagramSocket sk = new DatagramSocket(PORT);

            // While this agent has not been pinged, continue.
            while (!stop) {
                SocketPacketPackage pack = receiveMsg(sk, dgp);
                String str = new String(pack.packet.getData(), 0, pack.packet.getLength());
                String[] split = str.split(":");
                sendPong(pack, split);
                if (split.length > 1) {
                    if (split[1].equals("ping")) {
                        stop = true;
                    }
                }
            }
            return true; // Successful completion.
        } catch (SocketException ex) {
            Logger.getLogger(PongAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; // Failed completion.
    }

    /**
     * Sends a "pong" message to the PingAgent that "pinged" it.
     *
     * @param pack SocketPacketPackage containing the socket/packet necessary to
     * send a message.
     * @param split String array containing the message previously received from
     * the client, where the original string was split with delimiter ":".
     */
    private void sendPong(SocketPacketPackage pack, String[] split) {
        String outMessage;
        // If the received message was the "looking for pongs" broadcast message, respond accordingly
        // to establish a connection.
        if (split[0].equals("Looking for pongs")) {
            outMessage = "IAMPongAgent:" + getUID();
        } else {
            // Connection already established: respond with a "pong".
            outMessage = "pong:" + getUID();
            System.out.println("PongAgent[id=" + getUID() + "]: Received ping from PingAgent[id=" + split[0] + "]");
            System.out.println("PongAgent[id=" + getUID() + "]: Sending pong to PingAgent[id=" + split[0] + "]");
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
    private SocketPacketPackage receiveMsg(DatagramSocket socket, DatagramPacket dgPacket) {
        try {
            socket.receive(dgPacket);
            String str = new String(dgPacket.getData(), 0, dgPacket.getLength());
            return (new SocketPacketPackage(socket, dgPacket));
        } catch (SocketException ex) {
            Logger.getLogger(PongAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PongAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; // Error occurred.
    }
}
