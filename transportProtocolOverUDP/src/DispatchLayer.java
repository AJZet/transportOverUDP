import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class FileReceiver implements Layer {

	  private final Layer subLayer;
	  File file = null;
	  PrintWriter writer = null;
	  boolean closed = false;
	  int packetNo = 0;
	  
	  private Lock lock = new ReentrantLock();
	  Condition isFinished = this.lock.newCondition();

	  public FileReceiver(String destinationHost, int destinationPort,
	      int connectionId) {
	    subLayer = new ConnectedLayer(destinationHost, destinationPort,
	        connectionId);
	    subLayer.deliverTo(this);
	    
	    
	  }

public class DispatchLayer implements Layer {

  private static Map<Integer, Layer> table = new HashMap<Integer, Layer>();
  private static Layer dispatcher = null;

  public static synchronized void start() {
    if (dispatcher == null)
      dispatcher = new DispatchLayer();
    GroundLayer.deliverTo(dispatcher);
  }

  //detect no connection (sessionId) and launch new file receiver
  @SuppressWarnings("boxing")
  public static synchronized void register(Layer layer, int sessionId) {
    if (dispatcher != null) {
    	//match sessionId of the client to the layer
      table.put(sessionId, layer);
      GroundLayer.deliverTo(dispatcher);
    } else
      GroundLayer.deliverTo(layer);
  }

  private DispatchLayer() { // singleton pattern, restricts the instantiation of a class to one object.
  }

  @Override
  public void send(String payload) {
    throw new UnsupportedOperationException("don't use this for sending");
  }

  @Override
  //handle the incoming packets
  //mapping incoming connectionIDs to connected layers
  //for existing -> look up enough to deliver a packet
  //for new --> build file receiver and register its connected layer
  public void receive(String payload, String source) {
	  
	  if (payload == "--HELLO--") {
		  Thread t = new Thread(new FileReceiver());
		  
	  }
	  
  }

  @Override
  public void deliverTo(Layer above) {
    throw new UnsupportedOperationException(
        "don't support a single Layer above");
  }

  @Override
  public void close() { // nothing
  }

}