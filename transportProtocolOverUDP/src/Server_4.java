import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

  public Layer getSubLayer() {
    return subLayer;
  }

  @Override
  public void send(String payload) {
    throw new UnsupportedOperationException(
        "don't support any send from above");
  }

  @Override
  public void receive(String payload, String sender) {
	  lock.lock();

	  	//close the layer
  		if (payload.startsWith("**CLOSE**")) {
  			
				writer.close();

  			//System.out.println("SIGNAL " + payload);
  			isFinished.signal();	
  		}
  		else
	    //create file
  		if (payload.startsWith("SEND ")) {
  			String a [] = payload.split(" ");
	    		//int length = income.length();
	    		//income = income.substring(8, length);
	    		String income = a[a.length - 1];
	        //System.out.println("name the file _received_" + income);
	    		file = new File("_received_" + income);
	    		
	    		//create filewriter
	    		try {
					writer = new PrintWriter(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    }
	    //write to file
	    else {
	    		if(packetNo > 1) {
	    			writer.println();
	    		}
	        //System.out.println("write to file " + payload);
			writer.print(payload);

	    }
  		packetNo ++;
	  lock.unlock();
  }

  @Override
  public void deliverTo(Layer above) {
    throw new UnsupportedOperationException("don't support any Layer above");
  }

  @Override
  public void close() {
	lock.lock();
    // here, first wait for completion
	try {
		//System.out.println("waiting to close");
		isFinished.await();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    System.out.println("closing");
    subLayer.close();
    lock.unlock();
  }

}

public class Server_4 {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(
          "syntax : java Server_4 myPort destinationHost destinationPort");
      return;
    }
    if (GroundLayer.start(Integer.parseInt(args[0]))) {
      // GroundLayer.RELIABILITY = 0.5;
      FileReceiver receiver = new FileReceiver(args[1],
          Integer.parseInt(args[2]), (int) (Math.random() * Integer.MAX_VALUE));
      receiver.close();
      GroundLayer.close();
    }
  }
}