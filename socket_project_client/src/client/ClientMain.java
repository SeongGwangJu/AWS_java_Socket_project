package client;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import client.dto.RequestBodyDto;
import client.dto.SendMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientMain extends JFrame {

	private static ClientMain instance;

	public static ClientMain getInstance() {
		if (instance == null) {
			instance = new ClientMain();
		}
		return instance;
	}

	private JLabel roomOwnerLabel;
	private String roomName;
	private String username;
	private Socket socket;
	private boolean isOwner; //방만들면 true, join하면 false
	
	private CardLayout mainCardLayout;
	private JPanel mainCardPanel;

	private JPanel chattingRoomListPanel;
	private JScrollPane roomListScrollPanel;
	private DefaultListModel<String> roomListModel;
	private JList roomList;

	private JTextField usernameTextField;
	private JLabel userIcon;
	private JTextField roomNameTextField;
	private JLabel roomNameIcon;

	private JPanel chattingRoomPanel;
	private JTextField messageTextField;
	private JTextArea chattingTextArea;
	private JScrollPane userListScrollPane;
	private DefaultListModel<String> userListModel;
	private JList userList;
	private JLabel ipLabel;
	private JButton sendButton;
	private JButton btnNewButton;
	private JLabel btnNewJLabel;
	String ip = "127.0.0.1";
	int port = 8000;

	// <<< 메인메서드 : GUI 표시 >>>
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMain frame = ClientMain.getInstance();
					frame.setVisible(true);

					ClientReceiver clientReceiver = new ClientReceiver();
					clientReceiver.start();

					// 연결시 : "connection" 전송(username)
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("connection", frame.username);
					ClientSender.getInstance().send(requestBodyDto);

	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientMain() {
		setAlwaysOnTop(true);

		// <<< 아이디 입력창 및 연결부 >>>
		username = JOptionPane.showInputDialog(null, "아이디를 입력하세요.");
		if (Objects.isNull(username)) {
			System.exit(0);
		}
		if (username.isBlank()) {
			System.exit(0);
		}
		//아이디 중복 검증 :방장을 id로 식별하기때문에 검증 해야 방장 중복을 피할 수 있음. 근데 서버 통해야함..
//		if (username.equals) { 
		
		// 소켓 접속
		try {
			socket = new Socket(ip, port);
		}catch(ConnectException e) {
			System.out.println("서버를 시작해주세요.");
		}catch (IOException e) {
			e.printStackTrace();
		}

	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 320, 600);

		mainCardLayout = new CardLayout();
		mainCardPanel = new JPanel();
		mainCardPanel.setLayout(mainCardLayout);
		setContentPane(mainCardPanel);

		// <<< 대기실 >>>
		chattingRoomListPanel = new JPanel();
		chattingRoomListPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomListPanel.setLayout(null);
		mainCardPanel.add(chattingRoomListPanel, "chattingRoomListPanel");

		// 방만들기 버튼
		JButton createRoomButton = new JButton("방만들기");
		createRoomButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		createRoomButton.setBounds(66, 82, 170, 40);

		createRoomButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				//방만들 때 검증
				roomName = JOptionPane.showInputDialog(chattingRoomListPanel, "방제목을 입력하세요.");
				
				if (Objects.isNull(roomName)) {
					return;
				}
				if (roomName.isBlank()) {
					JOptionPane.showMessageDialog(chattingRoomListPanel, "방제목을 입력하세요.", "방만들기 실패",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
		        
		        
				for(int i = 0; i < roomListModel.size(); i++) {
					if(roomListModel.get(i).equals(roomName)) {
						JOptionPane.showMessageDialog(chattingRoomListPanel, "이미 존재하는 방제목입니다.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);

						return;
					}
				}
				
				// 방만들면 "createRoom" : 룸리스트 업데이트, "join" : 유저리스트 업데이트
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("createRoom", roomName); 
				ClientSender.getInstance().send(requestBodyDto);
				isOwner = true;

				// 방제목 표시(방장용)
				roomNameTextField.setText(roomName);
				
				mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
				requestBodyDto = new RequestBodyDto<String>("join", roomName);
				ClientSender.getInstance().send(requestBodyDto);
			}
		});

		chattingRoomListPanel.add(createRoomButton);
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(17, 150, 270, 340);
		chattingRoomListPanel.add(roomListScrollPanel);

		// 방 입장시 "join" (roomName)
		roomListModel = new DefaultListModel<String>();
		roomList = new JList(roomListModel);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					isOwner = false; //join 시에는 방장이 아님
					
					//roomName을 서버에 전송
					roomName = roomListModel.get(roomList.getSelectedIndex());
					//ClientMain.getInstance().getUserList().removeAll();
					
					mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", roomName);
					ClientSender.getInstance().send(requestBodyDto);
					// 방제목 표시 및 채팅방 초기화
					roomNameTextField.setText(roomName);
					
					getChattingTextArea().setText("");
				}
			}
		});
		roomListScrollPanel.setViewportView(roomList);

		// 대기실 유저 이름표시
		usernameTextField = new JTextField();
		usernameTextField.setHorizontalAlignment(SwingConstants.LEFT);
		usernameTextField.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		usernameTextField.setBorder(new EmptyBorder(0, 0, 0, 0));
		usernameTextField.setEditable(false);
		usernameTextField.setText(username);

		usernameTextField.setBounds(52, 5, 53, 40);
		chattingRoomListPanel.add(usernameTextField);
		usernameTextField.setColumns(10);

		// 유저 아이콘
		userIcon = new JLabel("");
		userIcon.setIcon(
				new ImageIcon(ClientMain.class.getResource("/icon/userIcon.png")));

		userIcon.setBounds(12, 8, 35, 35);
		chattingRoomListPanel.add(userIcon);

		// <<< 채팅방 >>>
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomPanel.setLayout(null);
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");

		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(17, 150, 270, 340);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);

		// 채팅창
		chattingTextArea = new JTextArea();
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);

		// 채팅 입력시 : sendMessage(발신자,메시지 내용)
		messageTextField = new JTextField();
		messageTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {

					SendMessage sendMessage = SendMessage.builder().fromUsername(username)
							.messageBody(messageTextField.getText()).build();

					RequestBodyDto<SendMessage> requestBodyDto = new RequestBodyDto<>("sendMessage", sendMessage);

					ClientSender.getInstance().send(requestBodyDto);
					messageTextField.setText("");
				}
			}
		});
		messageTextField.setBounds(17, 505, 195, 40);
		chattingRoomPanel.add(messageTextField);
		messageTextField.setColumns(10);

		// 채팅방 상단 접속자리스트
		userListScrollPane = new JScrollPane();
		userListScrollPane.setBounds(17, 55, 270, 80);
		chattingRoomPanel.add(userListScrollPane);

		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String username = userListModel.get(userList.getSelectedIndex());
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", username);
					ClientSender.getInstance().send(requestBodyDto);
				}
			}
		});
		userListScrollPane.setViewportView(userList);

		// 채팅방 상단 방제표시
		roomNameTextField = new JTextField();
		//roomNameTextField.setText("방제목");
		roomNameTextField.setHorizontalAlignment(SwingConstants.LEFT);
		roomNameTextField.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		roomNameTextField.setEditable(false);
		roomNameTextField.setColumns(10);
		roomNameTextField.setBorder(new EmptyBorder(0, 0, 0, 0));
		roomNameTextField.setBounds(52, 5, 130, 40);
		chattingRoomPanel.add(roomNameTextField);

		// 아이콘
		roomNameIcon = new JLabel("");
		roomNameIcon.setIcon(
				new ImageIcon("socket_project_client\\src\\userIcon.png"));
		roomNameIcon.setBounds(12, 8, 35, 35);
		chattingRoomPanel.add(roomNameIcon);

		ipLabel = new JLabel();
		ipLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		ipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ipLabel.setBounds(180, 6, 57, 35);
		chattingRoomPanel.add(ipLabel);
		ipLabel.setText(ip);

		sendButton = new JButton("전송");
		sendButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
		sendButton.setBounds(219, 504, 68, 40);
		chattingRoomPanel.add(sendButton);

		// 방 나가기 버튼 : exitRoom(
		btnNewButton = new JButton("X");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if(e.getClickCount() == 1) {
					
					RequestBodyDto<String> requestBodyDto = null;
					//방장이 나가는 경우
					if(isOwner) {
						if(JOptionPane.showConfirmDialog(chattingRoomPanel, 
								"방이 사라집니다. 정말 나가시겠습니까?", "방 나가기(방장)", JOptionPane.YES_NO_OPTION) == 0) {
							requestBodyDto = new RequestBodyDto<String>("ownerExitRoom", roomName);
							ClientSender.getInstance().send(requestBodyDto);
							mainCardLayout.show(mainCardPanel, "chattingRoomListPanel");
							
						} return;
						
					//방장이 아닌경우
					} else {
							mainCardLayout.show(mainCardPanel, "chattingRoomListPanel");
							requestBodyDto = new RequestBodyDto<String>("exitRoom", roomName);
							ClientSender.getInstance().send(requestBodyDto);
					}	
					 //서버로 roomName 전송
					getChattingTextArea().setText("");
				}

					
			}
		});
		btnNewButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		btnNewButton.setBounds(247, 8, 45, 35);
		chattingRoomPanel.add(btnNewButton);

		roomOwnerLabel = new JLabel();
		roomOwnerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		roomOwnerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		roomOwnerLabel.setBounds(180, 6, 57, 35);
		chattingRoomPanel.add(roomOwnerLabel);
		//종료시 예외처리
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				RequestBodyDto<String> quitDto = new RequestBodyDto<String>("quitWindow", username);
				ClientSender.getInstance().send(quitDto);	
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});

	}

	public void setRoomCreator(boolean b) {
	}

}
