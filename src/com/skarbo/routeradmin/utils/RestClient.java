package com.skarbo.routeradmin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class RestClient {

	public enum RequestMethod {
		GET, POST
	}

	private static final String TAG = RestClient.class.getSimpleName();

	private ArrayList<NameValuePair> data;
	private ArrayList<NameValuePair> headers;
	private ArrayList<BasicClientCookie> cookies;

	private String url;
	private String authUser;
	private String authPassword;

	public RestClient(String url) {
		this.url = url;
		this.data = new ArrayList<NameValuePair>();
		this.headers = new ArrayList<NameValuePair>();
		this.cookies = new ArrayList<BasicClientCookie>();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setAuthenticate(String user, String password) {
		this.authUser = user;
		this.authPassword = password;
	}

	public void addData(String name, String value) {
		data.add(new BasicNameValuePair(name, value));
	}

	public void addData(Map<String, String> dataMap) {
		for (Entry<String, String> entry : dataMap.entrySet())
			data.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
	}

	public void addHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	public void addCookie(String name, String value) {
		addCookie(name, value, null, null);
	}

	public void addCookie(String name, String value, String domain, String path) {
		BasicClientCookie cookie = new BasicClientCookie(name, value);
		if (domain != null)
			cookie.setDomain(domain);
		if (path != null)
			cookie.setPath(path);
		cookies.add(cookie);
	}

	public Response execute(RequestMethod method) throws Exception {
		switch (method) {
		case GET: {
			HttpGet request = new HttpGet(url);
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}
			return executeRequest(request, url);
		}
		case POST: {
			HttpPost request = new HttpPost(url);
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}
			if (!data.isEmpty()) {
				request.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
			}
			return executeRequest(request, url);
		}
		}
		return null;
	}

	private Response executeRequest(HttpUriRequest request, String url) throws IOException {
		DefaultHttpClient client = new DefaultHttpClient();

		// Cookies
		for (BasicClientCookie cookie : this.cookies) {
			client.getCookieStore().addCookie(cookie);
		}

		// Authenticate
		if (this.authUser != null && this.authPassword != null) {
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(this.authUser, this.authPassword);
			client.getCredentialsProvider()
					.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
		}

		Response response = new Response();
		HttpResponse httpResponse;

		try {
			Log.d(TAG, "Client execute: " + request.getURI().toString());
			httpResponse = client.execute(request);

			response.setResponseCode(httpResponse.getStatusLine().getStatusCode());
			response.setMessage(httpResponse.getStatusLine().getReasonPhrase());

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				response.setResponse(convertStreamToString(instream));
				instream.close();
			}
		} finally {
			client.getConnectionManager().shutdown();
		}

		return response;
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// ... CLASS

	public static class Response {

		private int responseCode;
		private String response;
		private String message;

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public String getResponse() {
			return response;
		}

		public void setResponse(String response) {
			this.response = response;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
		@Override
		public String toString() {
			return String.format("Code: %d, Message: %s, Response: %d", this.getResponseCode(), this.getMessage(), this.getResponse().length());
		}

	}

	// ... /CLASS
}