package walk.around.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.net.Uri.Builder;

public class Http {

	public static final HttpClient client = new DefaultHttpClient();

	public static String get(String url, List<NameValuePair> params) {
		InputStream instream = null;
		StringBuilder builder = new StringBuilder();
		try {
			Builder uriBuilder = Uri.parse(url).buildUpon();
			for (NameValuePair param : params) {
				uriBuilder.appendQueryParameter(param.getName(), param.getValue());
			}
			HttpGet getRequest = new HttpGet(uriBuilder.toString());
			
			HttpResponse getResponse = client.execute(getRequest);
			HttpEntity entity = getResponse.getEntity();
			
			if (entity != null) {
				instream = entity.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));

				String line = null;

				while ((line = reader.readLine()) != null) {
					builder.append(line + "\n");
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (instream != null) {
				try {
					instream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return builder.toString();
	}


	// Other HTTP methods unnecessary right now
}
