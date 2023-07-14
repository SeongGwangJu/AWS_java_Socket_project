package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

import server.dto.RequestBodyDto;

public class ServerSender {
	
	private Gson gson;
	
	private static ServerSender instance;
	
	private ServerSender() {
		gson = new Gson();
	}
	
	public static ServerSender getInstance() {
		if(instance == null) {
			instance = new ServerSender();
		}
		return instance;
	}
	
	public void send(Socket socket, RequestBodyDto<?> requestBodyDto) {
		try {
			PrintWriter printWriter = 
					new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(gson.toJson(requestBodyDto));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}













