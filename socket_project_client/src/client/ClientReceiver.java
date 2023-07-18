package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.List;

import javax.swing.JOptionPane;

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
				String requestBody = null;
				try {
					requestBody = bufferedReader.readLine();
					
				}
				 catch (SocketException e) { //소켓이 닫혀있는 경우
					 	System.out.println("SocketException. 클라이언트를 종료합니다.");
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
				  
			/* case "exitRoom":
				roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ClientMain.getInstance().getRoomListModel().clear();
				ClientMain.getInstance().getRoomListModel().addAll(roomList);
			    break; */
			    
			case "notiRoomClosure": //알림창 + 화면이동

				JOptionPane.showMessageDialog(ClientMain.getInstance().getChattingRoomPanel(), "방장이 나갔습니다.", "방나가짐", JOptionPane.ERROR_MESSAGE);
				ClientMain.getInstance().getMainCardLayout().show(ClientMain.getInstance().getMainCardPanel(), "chattingRoomListPanel");
			    break;
			
			case "sendWhisper":
				ClientMain.getInstance().getMessageTextField();
			    break;
			    
			case "clearChat" : 
				ClientMain.getInstance().getChattingTextArea().setText("");
				break;
			case "duplicationTest" :  //미완성
				System.out.println("duplicationTestStart"); //testprint
				List<String> duplicationTestList =(List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				int[] count = {0};
				duplicationTestList.forEach(userName -> {
					if(userName.equals(ClientMain.getInstance().getUsername())) {
						count[0] += +1;
						//count 값이 1이면 리스트와 겹치는게 하나니까 정상
						if(count[0] == 2) { //2개면 중복
							JOptionPane.showMessageDialog(ClientMain.getInstance().getChattingRoomPanel(), "아이디가 중복되었습니다.", "아이디 중복", JOptionPane.ERROR_MESSAGE);
							ClientMain.getInstance().setUsername(JOptionPane.showInputDialog(null, "아이디를 입력하세요."));
						}
					}
				});
		}
	}
	
	
	

}
