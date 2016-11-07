package connector;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;

enum ConnectMonitor {
	connet;

	/**
	 * execute request throw the monitor if connextor is SOCKS connector with
	 * userName/pass
	 * 
	 * @param connector
	 *            - input connector http/socks
	 * @return - response/answer
	 * @throws IOException
	 */
	public CloseableHttpResponse execute(Connector connector, HttpHost target,
			HttpRequest request) throws IOException {
		return connector.executeRequest(target, request);
	}

}
