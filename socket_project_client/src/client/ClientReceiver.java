package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;
import client.dto.RequestBodyDto;

public class ClientReceiver extends Thread {
	
	@Override
	public void run() {
		ClientMain clientMain = ClientMain.getInstance();
		while(true) {
			try {
				BufferedReader bufferedReader = 
						new BufferedReader(new InputStreamReader(clientMain.getSocket().getInputStream()));
				String requestBody = bufferedReader.readLine();
				
				requestController(requestBody);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void requestController(String requestBody) {
		Gson gson = new Gson();
		
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();
		
		switch(resource) {
			case "updateRoomList" :
				List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ClientMain.getInstance().getRoomListModel().clear();
				ClientMain.getInstance().getRoomListModel().addAll(roomList);
				break;
				
			case "showMessage":
				String messageContent = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ClientMain.getInstance().getChattingTextArea().append(messageContent + "\n");
				break;
				
			case "updateUserList":
				List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ClientMain.getInstance().getUserListModel().clear();
				ClientMain.getInstance().getUserListModel().addAll(usernameList);
				break;
		}
	}
	

}
