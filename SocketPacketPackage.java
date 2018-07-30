
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * SocketPacketPackage class consisting of a DatagramSocket and
 * DatagramPacket pair.
 */
public class SocketPacketPackage {

    DatagramSocket socket;
    DatagramPacket packet;

    /**
     * Constructor to initialize this SocketPacketPackage to the given values.
     * @param s     Value that will be given to the DatagramSocket "socket"
     * @param p     Value that will be given to the DatagramPacket "packet"
     */
    public SocketPacketPackage(DatagramSocket s, DatagramPacket p) {
        socket = s;
        packet = p;
    }
}
