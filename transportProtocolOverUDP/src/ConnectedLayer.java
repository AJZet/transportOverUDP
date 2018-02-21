public class ConnectedLayer implements Layer {

  private static final String HELLO = "--HELLO--";
  private static final String ACK = "--ACK--";

  private final String destinationHost;
  private final int destinationPort;
  private final int sessionId;
  private int number;
  private int remoteId;
  private int remoteNum;
  private Layer aboveLayer;

  public ConnectedLayer(String host, int port, int id) {
    destinationHost = host;
    destinationPort = port;
    sessionId = id;
    number = 0;
    remoteId = -1;
    remoteNum = -1;
    aboveLayer = null;
    //GroundLayer.deliverTo(this);
    DispatchLayer.register(this, sessionId);
    send(HELLO);
  }

  public void send(final String payload) {
    GroundLayer.send(sessionId + ";" + number + ';' + payload, destinationHost,
        destinationPort);
    ++number;
  }

  private static int convertSafe(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) { // nothing
      return -1;
    }
  }

  public void handleAck(int id, int num) {
  }

  public void handleHello(int id, int num) {
    if (remoteId == -1 && num == 0) {
      remoteId = id;
      remoteNum = 0;
    }
    if (id == remoteId)
      GroundLayer.send(id + ";" + num + ';' + ACK, destinationHost,
          destinationPort);
  }

  public void receive(String payload, String sender) {
    String[] packet = payload.split(";", 3);
    if (packet.length < 3) {
      if (aboveLayer != null)
        aboveLayer.receive(payload, sender); // may also want to see raw messages
    } else {
      int id = convertSafe(packet[0]);
      int num = convertSafe(packet[1]);
      if (ACK.equals(packet[2]))
        handleAck(id, num);
      else if (HELLO.equals(packet[2]))
        handleHello(id, num);
      else if (id == remoteId) {
        if (num == remoteNum + 1) {
          if (aboveLayer != null)
            aboveLayer.receive(packet[2],
                sender + '#' + packet[0] + '(' + packet[1] + ')');
          remoteNum = num;
        }
        if (aboveLayer != null)
          GroundLayer.send(packet[0] + ";" + remoteNum + ';' + ACK,
              destinationHost, destinationPort);
      }
    }
  }

  public void deliverTo(Layer above) {
    aboveLayer = above;
  }

  public void close() {
    aboveLayer = null;
  }
}
