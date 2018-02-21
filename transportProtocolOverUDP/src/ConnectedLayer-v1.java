
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
	
  
  //a layer above
  static Layer above = null;

  public void receive(String payload, String source) {
	  //System.out.println("recive packet: " + payload + " from " + source);
	  String[] a = payload.split(";", 3);
	  payload = a[2];
	  if (above != null && !payload.equals("--ACK--")) {
		  if (!payload.equals("--HELLO--")) {
			  above.receive(payload, this.toString());
		  }
		  String conID = a[0];
		  String packNo = a[1];
		  String answer = conID + ";" + packNo + ";" + "--ACK--";
		  //System.out.println("send answer: " + answer);
		  GroundLayer.send(answer, destinationHost, destinationPort);
		}
	  else {
		  //System.out.println("recive : " + payload);
	  }

  }
  
  //stores reference to the Layer to which payload of incoming packets is passed
  public void deliverTo(Layer layer) {
	  above = layer;
	  
  }

  //makes the call to GroundLayer.send with suitable parameters.
  //It must also increment the packet number for each new payload.
  volatile int packetNumber = 0;
  public void send(String payload) {
	  String message = connectionID + ";" + packetNumber + ";" + payload;
	  //System.out.println("send packet: " + message);
	  GroundLayer.send(message, destinationHost, destinationPort);
	  packetNumber++;
  }
  

  public void close() {

    System.err.println("ConnectedLayer closed");
  }

}