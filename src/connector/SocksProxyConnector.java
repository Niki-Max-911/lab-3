package connector;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class SocksProxyConnector extends Connector {
	static Registry<ConnectionSocketFactory> reg = RegistryBuilder
			.<ConnectionSocketFactory> create()
			.register("http", new MyConnectionSocketFactory())
			.register(
					"https",
					new MySSLConnectionSocketFactory(SSLContexts
							.createSystemDefault())).build();
	private boolean needVarified = false;

	public SocksProxyConnector(final InetSocketAddress proxy,
			final String userName, final String password) {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
				reg);

		config = DEF_REQUEST_CONFIG;
		cookieStore = new BasicCookieStore();
		browser = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(config)
				.setRetryHandler(MY_RETRY_HANDLER)
				.setDefaultSocketConfig(SOCKET_CONFIG).setUserAgent(USER_AGENT)
				.build();

		context = HttpClientContext.create();
		context.setAttribute("socks.address", proxy);
		context.setAttribute("Checker", false);

		if (userName != null) {
			context.setAttribute("socks.userName", userName);
			context.setAttribute("socks.password", password != null ? password
					: "");
		} else {
			context.setAttribute("socks.userName", "");
			context.setAttribute("socks.password", "");
		}
		needVarified = !userName.isEmpty();
	}

	@Override
	public CloseableHttpResponse getResp(HttpHost target, HttpRequest request)
			throws IOException {
		return needVarified ? getRespThroughMonitor(target, request)
				: executeRequest(target, request);
	}

	static class MySSLConnectionSocketFactory extends
			SSLConnectionSocketFactory {
		private String userName = "";
		private String password = "";

		public MySSLConnectionSocketFactory(SSLContext sslContext) {
			super(sslContext, ALLOW_ALL_HOSTNAME_VERIFIER);
		}

		@Override
		public Socket createSocket(HttpContext context) throws IOException {
			InetSocketAddress socksaddr = (InetSocketAddress) context
					.getAttribute("socks.address");

			userName = (String) context.getAttribute("socks.userName");
			password = (String) context.getAttribute("socks.password");

			Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
			return new Socket(proxy);
		}

		@Override
		public Socket connectSocket(int connectTimeout, Socket socket,
				HttpHost host, InetSocketAddress remoteAddress,
				InetSocketAddress localAddress, HttpContext context)
				throws IOException {
			InetSocketAddress unresolvedRemote = InetSocketAddress
					.createUnresolved(host.getHostName(),
							remoteAddress.getPort());

			Socket outSocket = null;
			if (userName != "")
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								MySSLConnectionSocketFactory.this.userName,
								MySSLConnectionSocketFactory.this.password
										.toCharArray());
					}
				});

			outSocket = super.connectSocket(connectTimeout, socket, host,
					unresolvedRemote, localAddress, context);

			if (userName != "")
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return null;
					}
				});
			return outSocket;
		}
	}

	static class MyConnectionSocketFactory implements ConnectionSocketFactory {
		private String userName = "";
		private String password = "";

		public Socket createSocket(final HttpContext context)
				throws IOException {
			InetSocketAddress socksaddr = (InetSocketAddress) context
					.getAttribute("socks.address");
			userName = (String) context.getAttribute("socks.userName");
			password = (String) context.getAttribute("socks.password");
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
			return new Socket(proxy);
		}

		public Socket connectSocket(final int connectTimeout,
				final Socket socket, final HttpHost host,
				final InetSocketAddress remoteAddress,
				final InetSocketAddress localAddress, final HttpContext context)
				throws IOException, ConnectTimeoutException {
			Socket sock;
			if (socket != null) {
				sock = socket;
			} else {
				sock = createSocket(context);
			}
			if (localAddress != null) {
				sock.bind(localAddress);
			}

			if (userName != "")
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								MyConnectionSocketFactory.this.userName,
								MyConnectionSocketFactory.this.password
										.toCharArray());
					}
				});

			try {
				sock.connect(remoteAddress, connectTimeout);
			} catch (SocketTimeoutException ex) {
				throw new ConnectTimeoutException(ex, host,
						remoteAddress.getAddress());
			} finally {
				if (userName != "")
					Authenticator.setDefault(new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return null;
						}
					});
			}
			return sock;
		}
	}
}
