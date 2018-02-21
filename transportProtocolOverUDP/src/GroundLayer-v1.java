import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroundLayer {

  /**
   * This {@code Charset} is used to convert between our Java native String
   * encoding and a chosen encoding for the effective payloads that fly over the
   * network.
   */
  private static final Charset CONVERTER = StandardCharsets.UTF_8;

  /**
   * This value is used as the probability that {@code send} really sends a
   * datagram. This allows to simulate the loss of packets in the network.
   */
  public static double RELIABILITY = 1.0;
  
  //a layer above
  static Layer above = null;
  //static volatile boolean closed = false;
  static Thread reciver = null;
  
  static DatagramSocket datagramSocket;



  //public static DatagramSocket datagramSocket = new DatagramSocket();

	private static class RecivePackets implements java.lang.Runnable {
		private DatagramSocket socket;
		byte[] buffer = new byte[2048];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);


		public RecivePackets (DatagramSocket datagramSocket) {
			this.socket = datagramSocket;
		}
		public void run() {
			//wait for incoming packets on the port
			while(!Thread.interrupted()) {
				try {
					socket.receive(packet);
					this.buffer = packet.getData();
					String payload = new String(buffer, 0, packet.getLength(), CONVERTER);
					if (above != null) {
						above.receive(payload, packet.getAddress().toString());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Thread.currentThread().interrupt();
					return;
				}
				
			}
			
			}
		}
  


  
  public static boolean start(int localPort) {
	//create a socket
	//DatagramSocket datagramSocket;
	try {
		datagramSocket = new DatagramSocket(localPort);
		//ExecutorService executor = Executors.newFixedThreadPool(30);
		reciver = new Thread(new RecivePackets(datagramSocket));
		reciver.start();
		//launch new thread
		//executor.submit(new RecivePackets(datagramSocket));
		return true;
	} catch (SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();

		//if there is a problem
	    return false;
	}

  }
  
  //stores reference to the Layer to which payload of incoming packets is passed
  public static void deliverTo(Layer layer) {
	  above = layer;
	  
  }
  //sends an UDP packet with the given payload to the specified destination. 
  //To simulate the loss of packets in the network, the value of RELIABILITY is 
  //used as the probability that send really sends a datagram. So, this method must do 
  //nothing with probability 1-RELIABILITY.
  public static void send(String payload, String destinationHost,
      int destinationPort) {

	  byte[] buffer = payload.getBytes(CONVERTER);
	  InetAddress address;
		try {
			//boolean val = true;
			boolean val = new Random().nextDouble() <= RELIABILITY;

			//DatagramSocket datagramSocket = new DatagramSocket();
			address = InetAddress.getByName(destinationHost);
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, destinationPort);
			if (val) {
				datagramSocket.send(packet);
			}
			  
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


  }
  

  public static void close() {
	//closed = true;
	reciver.interrupt();
	datagramSocket.close();
    System.err.println("GroundLayer closed");
  }

}