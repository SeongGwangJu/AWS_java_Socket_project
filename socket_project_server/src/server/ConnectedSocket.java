package server;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.RequiredArgsConstructor;
import server.dto.RequestBodyDto;
import server.dto.SendMessage;
import server.entity.Room;
@RequiredArgsConstructor
public class ConnectedSocket extends Thread {
	private Gson gson;
	private final Socket socket;
	private String username;
	private boolean isOwner = false;  
	List<String> sameUserNameTestList = new ArrayList<>();
	@Override
	public void run() {
		
	gson = new Gson();
		//예외처리, 나중에 수정 필요
		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String requestBody = null;
				try {
					requestBody = bufferedReader.readLine();
					requestController(requestBody);
					
				}catch (SocketException e) { //클라이언트가 소켓이 닫히지 않은 채 종료할 경우
					e.printStackTrace();
					break; //예외처리를 해도 예외가 발생하기때문에 먹통됨
				} catch (NullPointerException e) {
					sysoutGUI("널포인터 익셉션 at readLine");
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				} 
			} catch (java.net.SocketException e) {
				sysoutGUI("클라이언트의 닫혔습니다.");
				e.printStackTrace();
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} catch (NullPointerException e) {
				e.printStackTrace();
				sysoutGUI("널포인터익셉션 at BufferedReader");
				break;
				
			}
		}
	}

	// <<< Resource에 따른 case 설정 >>>
	private void requestController(String requestBody) {
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();

		// RequestBodyDto<?> requestBodyDto = gson.fromJson(requestBody,
		// RequestBodyDto.class);
		/*
		 * TypeToken<RequestBodyDto<SendMessage>> token = new
		 * TypeToken<RequestBodyDto<SendMessage>>( ) { }; RequestBodyDto<SendMessage>
		 * requestBodyDto2 = gson.fromJson(requestBody, token.getType());
		 */

		switch (resource) {
		case "connection":
			connection(requestBody);
			break;

		case "createRoom":
			createRoom(requestBody);
			break;
			
		case "join": 
			join(requestBody);
			break;
		
		case "exitRoom":
			exitRoom(requestBody);
			break;
			
		case "ownerExitRoom" :
			ownerExitRoom(requestBody);
			break;
			
		case "sendWhisper": //미완성
			sendMessage(requestBody);
			break;
			
		case "sendMessage": // 밑에 내용 개중요함. 모든 케이스를 다 적어야함.
			sendMessage(requestBody);
			break;
			
		case "quitWindow" :
			quitWindow(requestBody);
			break;
		
		}
	}
	
	// <<< 위 case에 따른 행동 정의>>>
	
	//연결되었을때 대기실의 룸리스트 반환
	private void connection(String requestBody) {
		username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		sysoutGUI(username + "님이 서버에 연결되었습니다.");
		
		//방 제목만 있는 리스트 생성 + 유저네임 검증용 리스트 생성.
		List<String> roomNameList = new ArrayList<>();
		 //중복
		sameUserNameTestList.add(username);
		
		ServerMain.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
			
		});
		RequestBodyDto<List<String>> updateRoomListRequestBodyDto =
				new RequestBodyDto<List<String>>("updateRoomList", roomNameList);
		
		RequestBodyDto<List<String>> sameUserNameTestListRequestBodyDto =
				new RequestBodyDto<List<String>>("duplicationTest", sameUserNameTestList);
		
		ServerSender.getInstance().send(socket, updateRoomListRequestBodyDto);
		ServerSender.getInstance().send(socket, sameUserNameTestListRequestBodyDto);
		
	}

	//방 만들었을 때 룸리스트 반환
	private void createRoom(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		sysoutGUI(username + "님이" + roomName + " 방을 생성했습니다");
		//룸리스트에 추가
		Room newRoom = Room.builder()
				.roomName(roomName)
				.owner(username)
				.userList(new ArrayList<ConnectedSocket>())
				.build();
		ServerMain.roomList.add(newRoom);
		
		List<String> roomNameList = new ArrayList<>();

		ServerMain.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});
		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>(
				"updateRoomList", roomNameList);
		ServerMain.connectedSocketList.forEach(con -> {
			ServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
		});
	}
    
	//방에 들어왔을 때 유저리스트와 join메시지를 반환
	private void join(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		sysoutGUI(username +"님이" + roomName + " 방에 접속");
		clearChat();
		
		ServerMain.roomList.forEach(room -> {
			if(room.getRoomName().equals(roomName)) {
				room.getUserList().add(this);
				
				List<String> usernameList = new ArrayList<>();
				
				
				room.getUserList().forEach(con -> {
					usernameList.add(con.username + (con.username.equals(room.getOwner()) ? " (방장)" : "")); //오너와 유저네임이 같은 유저에게만 방장 표시
					
				});
				
				//유저리스트 업데이트, 접속알림 데이터 생성
				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<List<String>> updateUserListDto
							= new RequestBodyDto<List<String>>("updateUserList", usernameList);
	
					RequestBodyDto<String> joinMessageDto
							= new RequestBodyDto<String>("showMessage", username + "님이 채팅방에 접속했습니다.");
					
					//클라이언트에게 데이터 보냄.
					ServerSender.getInstance().send(connectedSocket.socket, updateUserListDto);
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					ServerSender.getInstance().send(connectedSocket.socket, joinMessageDto);

				});
			}
		});
	}

	//(방장 아닌 사람이) 방 나갈 때 유저리스트와 exitRoom 메시지 반환
	private void exitRoom(String requestBody) { 
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		sysoutGUI(username +"님이" + roomName + " 방을 나감");
		clearChat();
		
		ServerMain.connectedSocketList.forEach(connectedSocket -> {
		});
		
		ServerMain.roomList.forEach(room -> {
			if(room.getRoomName().equals(roomName)) { //같은 방에서
				room.getUserList().remove(this);
				
				List<String> usernameList = new ArrayList<>();
				
				room.getUserList().forEach(con -> {
					usernameList.add(con.username + (con.username.equals(room.getOwner()) ? " (방장)" : ""));
				});
				
				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<List<String>> updateUserListDto
							= new RequestBodyDto<List<String>>("updateUserList", usernameList);
	
					RequestBodyDto<String> exitMessageDto
							= new RequestBodyDto<String>("showMessage", username + "님이 채팅방을 나갔습니다.");
					
					//클라이언트에게 데이터 보냄.
					ServerSender.getInstance().send(connectedSocket.socket, updateUserListDto);
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					ServerSender.getInstance().send(connectedSocket.socket, exitMessageDto);

				});
			}
		});
	}
	
	//방장이 나가는 경우
	private void ownerExitRoom(String requestBody) {

		//여기서부터 주석 전까지 강사님이 작성해주신 코드
//		RequestBodyDto<?> requestBodyDto = gson.fromJson(requestBody, RequestBodyDto.class);
		
//		for(Room room : ServerMain.roomList) {
//			if(room.getRoomName().equals((String) requestBodyDto.getBody())) {
//				room.getUserList().forEach(con -> {
//					RequestBodyDto<Object> exitRoomReqDto = new RequestBodyDto<Object>("notiRoomClosure", null);
//					ServerSender.getInstance().send(con.socket, exitRoomReqDto);
//				});
//				
//				ServerMain.roomList.remove(room);
//			}
//		};
//		
//		List<String> roomNameList = new ArrayList<>();
//		
//		ServerMain.roomList.forEach(room -> {
//			roomNameList.add(room.getRoomName());
//		});
//		
//		RequestBodyDto<List<String>> updateRoomListRequestBodyDto 
//			= new RequestBodyDto<List<String>>("updateRoomList", roomNameList);
//		ServerMain.connectedSocketList.forEach(con -> {
//			ServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
//		});
//===============================================

		//참고해서 재작성
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
	    sysoutGUI(roomName + "방의 방장 " + username + "이 방을 나감, 방이 사라집니다.");

	    List<Room> roomsToRemove = new ArrayList<>(); // 제거할 방 목록을 저장할 리스트
	    
	    //ConcurrentModificationException 발생해서
	    // 방 목록을 순회하며 나가면서 폭파될 방을 roomsToRemove에 추가
	    for (Room room : ServerMain.roomList) {
	    	sysoutGUI("room.getOwner : " + room.getOwner() + "username : " + username);
	        if (room.getRoomName().equals(roomName)) { //같은 방이고
	        	if(room.getOwner().equals(username)) { //방장이면(명령을 요청한 username과 방장이 같으면)
	        		room.getUserList().remove(this); //유저리스트에 자기 자신을 지운다.
	        	} //따라서 방장이 아닌 접속자들에만 알림창이 뜨도록 한다.
		        	room.getUserList().forEach(con -> {
						RequestBodyDto<Object> exitRoomReqDto = new RequestBodyDto<Object>("notiRoomClosure", null);
						ServerSender.getInstance().send(con.socket, exitRoomReqDto);
		        		});
	            roomsToRemove.add(room);
	        }
	    }
	    // roomsToRemove에 저장된 방을 roomList에서 제거
	    ServerMain.roomList.removeAll(roomsToRemove);

	    List<String> roomNameList = new ArrayList<>();
	    ServerMain.roomList.forEach(room -> {
	        roomNameList.add(room.getRoomName());
	    });

	    RequestBodyDto<List<String>> updateRoomListRequestBodyDto =
	            new RequestBodyDto<List<String>>("updateRoomList", roomNameList);
	    ServerMain.connectedSocketList.forEach(con -> {
	        ServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
	    });

		clearChat();
	}
	
	private void sendWhisper(String requestBody) { //미완성
		
	}
	
	//메시지 입력시 모든 접속자에게 반환
	private void sendMessage(String requestBody) {
		TypeToken<RequestBodyDto<SendMessage>> typeToken = new TypeToken<>() {
		};

		// 클라이언트한테 받은 Json데이터를, SendMessage객체로 변환
		RequestBodyDto<SendMessage> requestBodyDto = gson.fromJson(requestBody, typeToken.getType());
		SendMessage sendMessage = requestBodyDto.getBody();

		ServerMain.roomList.forEach(room -> {
			if(room.getUserList().contains(this)) {
				// 반복으로 모든 접속자에게 전송
				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<String> dto = new RequestBodyDto<String>("showMessage",
							sendMessage.getFromUsername() + ": " + sendMessage.getMessageBody());

					ServerSender.getInstance().send(connectedSocket.socket, dto);
				});	
			}
		});
	} 	//소켓이 창을 종료했을 경우
		private void quitWindow(String requestBody) {
			username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			sysoutGUI(username + "님이 종료하였습니다.");
			changeGUIUserNum();
			
		}
		// <<< 케이스 메서드 끝 >>>
	
	//방 나갈때, 들어올때 채팅창을 초기화하는 요청
	private void clearChat() {
		RequestBodyDto<String> clearChatDto = 
				new RequestBodyDto<String>("clearChat", null);
		ServerSender.getInstance().send(socket, clearChatDto);
	}
	
	private void sysoutGUI(String print) {
		EventQueue.invokeLater(new Runnable() {
            public void run() {
            	ServerMain.sysoutGUI(print);
            }
        });
	}
	
	private void changeGUIUserNum() { //작동 안함 이유는 모름
		ServerMain serverMain = new ServerMain();
		int userNum = (serverMain.getUserNum()-1);
		serverMain.setUserNum(userNum); //ServerMain에도 userNum을 변경
		EventQueue.invokeLater(new Runnable() {
            public void run() {
            	serverMain.userNumArea.setText("" + userNum);
            }
        });
	}
}
