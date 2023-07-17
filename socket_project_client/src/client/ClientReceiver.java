package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import client.dto.RequestBodyDto;

public class ClientReceiver extends Thread {
	
	@Override
	public void run() {
		ClientMain clientMain = ClientMain.getInstance();
		while(true) {
			try {
				BufferedReader bufferedReader = 
						new BufferedReader(new InputStreamReader(clientMain.getSocket().getInputStream()));
				String requestBody = null;
				try {
					requestBody = bufferedReader.readLine();
					
				}
				 catch (SocketException e) {
						System.out.println("서버소켓이 닫혔습니다.\n시스템을 종료합니다.");
						clientMain.dispose();
						System.exit(0);
						
				} catch (Exception e) {
					e.printStackTrace();
				}
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
				
			case "exitRoom":
				String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ClientMain.getInstance().getRoomListModel().clear();
			    break;
			    
			case "ownerExitRoom":
				
			    break;
		}
	}
	
	
	

}
