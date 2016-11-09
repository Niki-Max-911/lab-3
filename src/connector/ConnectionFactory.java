package connector;

public class ConnectionFactory {
	public static Connector getConnector() {
		return new LocalhostConnector();
	}
}
