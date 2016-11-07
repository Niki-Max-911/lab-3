package connector;

import java.net.InetSocketAddress;

import org.apache.http.HttpHost;

public class ConnectionFactory {
	public static Connector getConnector() {
		return new LocalhostConnector();
	}

	public static Connector getConnector(final HttpHost proxy,
			final String userName, final String password)
			throws BadProxyException {

		Connector httpConnector = new HttpProxyConnector(proxy, userName,
				password);
		if (httpConnector.checkConnection()) {
//			System.out.println("Luck");
			return httpConnector;
		} else {

			InetSocketAddress inetSocketProxy = new InetSocketAddress(
					proxy.getHostName(), proxy.getPort());
			Connector socksConnector = new SocksProxyConnector(inetSocketProxy,
					userName, password);
			if (socksConnector.checkConnection()) {
//				System.out.println("Luck");
				return socksConnector;
			}
		}
		throw new BadProxyException();
	}

	public static Connector getConnector(final HttpHost proxy)
			throws BadProxyException {
		return getConnector(proxy, "", "");
	}
}
