package server;

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
	@Override
	public void run() {

		ServerMain serverMain = ServerMain.getInstance();
		gson = new Gson();

		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String requestBody = null;
				try {
					requestBody = bufferedReader.readLine();
					
				}
				 catch (SocketException e) {
					 e.printStackTrace();
					 System.out.println("클라이언트가 나갔음"); // 수정필요
					serverMain.dispose();
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				requestController(requestBody);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (java.lang.NullPointerException e) {
				e.printStackTrace();
				System.out.println("널포인터익셉션");
				
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
			
		case "sendWhisper":
			sendMessage(requestBody);
			break;
			
		case "sendMessage": // 밑에 내용 개중요함. 모든 케이스를 다 적어야함.
			sendMessage(requestBody);
			break;
			
		
		}
	}
	
	// <<< 위 case에 따른 행동 정의>>>
	
	//연결되었을때 대기실의 룸리스트 반환
	private void connection(String requestBody) {
		username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		
		List<String> roomNameList = new ArrayList<>();

		server.ServerMain.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});

		RequestBodyDto<List<String>> updateRoomListRequestBodyDto =
				new RequestBodyDto<List<String>>("updateRoomList", roomNameList);

		ServerSender.getInstance().send(socket, updateRoomListRequestBodyDto);
		
	}
	
    public String getUsername() {
        return username;
    }
	//방 만들었을 때 룸리스트 반환
	private void createRoom(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();

		
		Room newRoom = Room.builder()
				.roomName(roomName)
				.owner(username)
				.userList(new ArrayList<ConnectedSocket>())
				.build();

		server.ServerMain.roomList.add(newRoom);

		List<String> roomNameList = new ArrayList<>();

		server.ServerMain.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});

		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>(
				"updateRoomList", roomNameList);

		server.ServerMain.connectedSocketList.forEach(con -> {

			ServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
		});

		
	}
	//방에 들어왔을 때 유저리스트와 join메시지를 반환
	private void join(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();

		//순서바꿔봄
		ServerMain.getInstance().sysoutGUI("join 정보 반환");


		System.out.println("연결된 소켓리스트에 저장된 데이터를  usernameList에 저장하고 값을 옮긴다.");
		ServerMain.connectedSocketList.forEach(connectedSocket -> {
			
			
		});

		ServerMain.roomList.forEach(room -> {
			if(room.getRoomName().equals(roomName)) {
				room.getUserList().add(this);
				
				List<String> usernameList = new ArrayList<>();
				
				
				room.getUserList().forEach(con -> {
					usernameList.add(con.username);
				});
				room.getUserList().forEach(connectedSocket -> {
					
					//System.out.println("유저리스트 업데이트 및 접속알림 데이터 생성");
					RequestBodyDto<List<String>> updateUserListDto
							= new RequestBodyDto<List<String>>("updateUserList", usernameList);
	
					RequestBodyDto<String> joinMessageDto
							= new RequestBodyDto<String>("showMessage", username + "님이 채팅방에 접속했습니다.");
					
					//클라이언트에게 데이터 보냄.
					//System.out.println("서버에서 send() 실행. update와 join 정보를 보낸다.");
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

	//방 나갈 때 유저리스트와 exitRoom 메시지 반환
	private void exitRoom(String requestBody) { 
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		//순서바꿔 봄
		ServerMain.getInstance().sysoutGUI("exitRoom 정보 반환");
	
		ServerMain.connectedSocketList.forEach(connectedSocket -> {
			
			
		});

		server.ServerMain.roomList.forEach(room -> {
			if(room.getRoomName().equals(roomName)) {
				room.getUserList().remove(this);
				
				List<String> usernameList = new ArrayList<>();
				
				
				room.getUserList().forEach(con -> {
					usernameList.add(con.username);
				});
				room.getUserList().forEach(connectedSocket -> {
					
					//System.out.println("유저리스트 업데이트 및 접속알림 데이터 생성");
					RequestBodyDto<List<String>> updateUserListDto
							= new RequestBodyDto<List<String>>("updateUserList", usernameList);
	
					RequestBodyDto<String> joinMessageDto
							= new RequestBodyDto<String>("showMessage", username + "님이 채팅방을 나갔습니다.");
					
					//클라이언트에게 데이터 보냄.
					//System.out.println("서버에서 send() 실행. update와 join 정보를 보낸다.");
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
	
	private void sendWhisper(String requestBody) {
		
	}
	
	//메시지 입력시 모든 접속자에게 반환
	private void sendMessage(String requestBody) {
		TypeToken<RequestBodyDto<SendMessage>> typeToken = new TypeToken<>() {
		};

		// 클라이언트한테 받은 Json데이터를, SendMessage객체로 변환
		RequestBodyDto<SendMessage> requestBodyDto = gson.fromJson(requestBody, typeToken.getType());
		SendMessage sendMessage = requestBodyDto.getBody();

		server.ServerMain.roomList.forEach(room -> {
			if(room.getUserList().contains(this)) {
				// 반복으로 모든 접속자에게 전송
				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<String> dto = new RequestBodyDto<String>("showMessage",
							sendMessage.getFromUsername() + ": " + sendMessage.getMessageBody());

					ServerSender.getInstance().send(connectedSocket.socket, dto);
				});	
			}
		});
		
		// 반복으로 모든 접속자에게 전송

		// <<< 케이스 메서드 끝 >>>
	}
}
