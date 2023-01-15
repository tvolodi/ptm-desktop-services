
import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
	

	private static String baseAuthUrl = "http://localhost:8180";
	private static String baseAppUrl = "http://localhost:5098";
			
	
	private static String accessToken = "";
	private static String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0MTZmYTM2MC1lMTM4LTQyNTEtOGUyOC1mYTNlMWIxYjkzODQifQ.eyJleHAiOjE2NzM3NjU4MDgsImlhdCI6MTY3Mzc2NDAwOCwianRpIjoiZTg1ZmZmYmMtNmQ5NC00NjRiLThmMzktYWY4ZTAwOWQzNTY1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL2F1dGgvcmVhbG1zL3R3bSIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODE4MC9hdXRoL3JlYWxtcy90d20iLCJzdWIiOiI3Y2YzODYzMC1jNzQ5LTQ1OWEtYTFjNi01NzM5MTcxMzg0MjUiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoidHdtLWNsaWVudCIsInNlc3Npb25fc3RhdGUiOiJiNzdiZDYxNi00Yjk4LTRjYTEtODRhOS00NmE4NmZjYzcyODEiLCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiJiNzdiZDYxNi00Yjk4LTRjYTEtODRhOS00NmE4NmZjYzcyODEifQ.WwTM2bvAwjqOkljB225rk2P5fFO5-rihr_ShJaYnhZY";
	
	private static CloseableHttpClient httpClient = HttpClients.createDefault();
	
	public static HttpServer httpServer;

	public static void main(String[] args) throws Exception {
		System.out.println("Hi!");
		
		httpServer = HttpServer.create(new InetSocketAddress(8991), 0);
		httpServer.createContext("/test", new MyHandler());
		httpServer.createContext("/shutdown", new ShutdownHandler());
		httpServer.createContext("/testshot", new TestshotHandler());
		httpServer.createContext("/testRefreshToken", new TestRefreshTokenHandler());
		httpServer.createContext("/setTokens", new SetAccessTokensHandler());
		httpServer.setExecutor(null);
		httpServer.start();
	}

	// Set token from request from the web client after authorization
	static class SetAccessTokensHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			
			InputStream reqBodyIS = httpExchange.getRequestBody();
			
			String reqBodyString = new String(reqBodyIS.readAllBytes(), StandardCharsets.UTF_8);
			
			JSONObject jsonObj = new JSONObject(reqBodyString);
			accessToken = jsonObj.getString("AccessToken");
			refreshToken = jsonObj.getString("RefreshToken");
			
			String response = "Token are set";
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}	
	
	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "My response";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	
	static class TestshotHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			
			createScreenShot();
				
			String response = "TestFinished";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	
	static class TestRefreshTokenHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			
			refreshToken();
			
			String response = "Created";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	
	
	public static void createScreenShot()
	{
		int width = 0;
		int height = 0;
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gdArr = ge.getScreenDevices();
		for(GraphicsDevice gd : gdArr) {
			DisplayMode dMode = gd.getDisplayMode();
			width += dMode.getWidth();
			height += dMode.getHeight();
		}
		
		Rectangle screenRect = new Rectangle(0, 0, width, height);
		BufferedImage capture;
		try {
			capture = new Robot().createScreenCapture(screenRect);
			ImageIO.write(capture, "png", new File("printscreen.png"));
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void shutdown()
	{
		 httpServer.stop(0); 
//		 httpThreadPool.shutdown(); 
//		 try { 
//			 httpThreadPool.awaitTerminator(2, TimeUnit.HOURS); 
//		 } 
//		 catch (Exception e) {...shrieeeeek!...}
	}
	
	static class ShutdownHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange t) throws IOException {
			shutdown();
		}
	}
	
	
	// Get access token by refresh token from auth server (KeyCloak for now)
	public static void refreshToken()
	{
		
		// TODO url -> settings
		HttpPost httpPost = new HttpPost("http://localhost:8180/auth/realms/twm/protocol/openid-connect/token");
		
		// Form data for request
		List <NameValuePair> formData = new ArrayList<NameValuePair>();
		formData.add(new BasicNameValuePair("client_id", "twm-client"));
		formData.add(new BasicNameValuePair("grant_type", "refresh_token"));
		formData.add(new BasicNameValuePair("refresh_token", refreshToken));
		httpPost.setEntity(new UrlEncodedFormEntity(formData));
		try {
			// Make request
			CloseableHttpResponse response = httpClient.execute(httpPost);
			
			// Convert to json.
			InputStream contentInStream = response.getEntity().getContent();
			
			String contentString = new String(contentInStream.readAllBytes(), StandardCharsets.UTF_8);
			JSONObject jsonObj = new JSONObject(contentString);
			
			// Updating tokens.
			accessToken = jsonObj.getString("access_token");
			refreshToken = jsonObj.getString("refresh_token");
			
			// TODO add processing on aged refresh token
			
			// Free resources.
			response.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		
	}
}
