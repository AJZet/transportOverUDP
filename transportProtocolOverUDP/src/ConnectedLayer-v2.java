import java.util.TimerTask;

public class ConnectedLayer implements Layer {
	
	//constructor
	//the host and port which identify the destination for all packets send through this ConnectedLayer, 
	//and an id for the connection, here picked as random
	String destinationHost = null;
	int destinationPort;
	int connectionID;
	public ConnectedLayer (String host, int port, int id) {
		this.destinationHost = host;
		this.destinationPort = port;
		this.connectionID = id;
		GroundLayer.deliverTo(this);
		this.send("--HELLO--");
	}

	// a flag
  volatile boolean notify = false;
  
  //a layer above
  static Layer above = null;
  
  //number of sent packet
  volatile int packetNumber = 0;
  
  //to check if packet was received again, lost ACK?
  volatile int recentPackNo = 0;
  volatile boolean sentAgain = false;

  public synchronized void receive(String payload, String source) {
	  //System.out.println("recive packet: " + payload + " from " + source);
	  String[] a = payload.split(";", 3);
	  payload = a[2];
	  String conID = a[0];
	  String packNo = a[1];
	  if (Integer.parseInt(packNo) == recentPackNo) {
		  sentAgain = true;
	  }
	  recentPackNo = Integer.parseInt(packNo);
	  if (above != null && !payload.equals("--ACK--")) {
		  if (!payload.equals("--HELLO--") && !sentAgain) {
			  above.receive(payload, this.toString());
		  }
		  sentAgain = false;
		  String answer = conID + ";" + packNo + ";" + "--ACK--";
		  //System.out.println("send answer: " + answer);
		  GroundLayer.send(answer, destinationHost, destinationPort);
		}
	  //ACK recived
	  else if(payload.equals("--ACK--")){
		  if( Integer.parseInt(conID) == connectionID && (Integer.parseInt(packNo) + 1) == packetNumber) {
			  //synchronized(this) {
			  notify = true;
			  notifyAll();
		  //}
		  }
	  }

  }
  
  //stores reference to the Layer to which payload of incoming packets is passed
  public void deliverTo(Layer layer) {
	  above = layer;
	  
  }
  
  //Timer timer = new Timer (true);

  //makes the call to GroundLayer.send with suitable parameters.
  //It must also increment the packet number for each new payload.
  public void send(String payload) {
	  String message = connectionID + ";" + packetNumber + ";" + payload;

	  /*TimerTask task = new TimerTask() {
		  @Override
		    public void run() {
			  	System.out.println("send packet: " + message);
		    		GroundLayer.send(message, destinationHost, destinationPort);

		    }
	  };
	  synchronized(this) {
		  timer.schedule(task, Long.parseLong("0"), Long.parseLong("4000"));
	  }*/
	  new java.util.Timer().schedule(new TimerTask(){
	        @Override
	        public synchronized void run() {
		        	while (notify == false) {
			        	//System.out.println("send packet: " + message);
		        		GroundLayer.send(message, destinationHost, destinationPort);
		        		try {
							wait(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        	}
		        	notify = false;		
	        }
	  }, 0);
	    //} ,0 ,1000*5);
	  packetNumber++;
  }
  

  public void close() {

    System.err.println("ConnectedLayer closed");
  }

}