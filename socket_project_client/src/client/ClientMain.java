package client;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
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

import client.ClientReceiver;
import client.ClientSender;
import client.dto.RequestBodyDto;
import client.dto.SendMessage;
import lombok.Getter;

@Getter
public class ClientMain extends JFrame {

	private static ClientMain instance;
	public static ClientMain getInstance() {
		if(instance == null) {
			instance = new ClientMain();
		}
		return instance;
	}
	
	private String username;
	private Socket socket;
	
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

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
//					ClientMain frame = new ClientMain();
//					frame.setVisible(true);
					ClientMain frame = ClientMain.getInstance();
					frame.setVisible(true);
					
					ClientReceiver clientReceiver = new ClientReceiver();
					clientReceiver.start();
					
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("connection", frame.username);
					ClientSender.getInstance().send(requestBodyDto);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	
	public ClientMain() {
		
		username = JOptionPane.showInputDialog(chattingRoomPanel, "아이디를 입력하세요.");			
		
		if(Objects.isNull(username)) {
			System.exit(0);
		}
		
		if(username.isBlank()) {
			System.exit(0);
		}
		
		try {
			socket = new Socket("127.0.0.1", 8000);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 320, 600);

		mainCardLayout = new CardLayout();
		mainCardPanel = new JPanel();
		mainCardPanel.setLayout(mainCardLayout);
		setContentPane(mainCardPanel);
		
		chattingRoomListPanel = new JPanel();
		chattingRoomListPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomListPanel.setLayout(null);
		mainCardPanel.add(chattingRoomListPanel, "chattingRoomListPanel");
		
		
		
		JButton createRoomButton = new JButton("방만들기");
		createRoomButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		createRoomButton.setBounds(66, 82, 170, 40);
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String roomName = JOptionPane.showInputDialog(chattingRoomListPanel, "방제목을 입력하세요.");
				if(Objects.isNull(roomName)) {
					return;
				}
				if(roomName.isBlank()) {
					JOptionPane.showMessageDialog(chattingRoomListPanel, "방제목을 입력하세요.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				for(int i = 0; i < roomListModel.size(); i++) {
					if(roomListModel.get(i).equals(roomName)) {
						JOptionPane.showMessageDialog(chattingRoomListPanel, "이미 존재하는 방제목입니다.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
		        
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("createRoom", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				mainCardLayout.show(mainCardPanel, "chattingRoomPanel"); 
				requestBodyDto = new RequestBodyDto<String>("join", roomName);
				ClientSender.getInstance().send(requestBodyDto);
			}
		});
		chattingRoomListPanel.add(createRoomButton);
		
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(17, 150, 270, 340);
		chattingRoomListPanel.add(roomListScrollPanel);
		
		roomListModel = new DefaultListModel<String>();
		roomList = new JList(roomListModel);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String roomName = roomListModel.get(roomList.getSelectedIndex());
					mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", roomName);
					ClientSender.getInstance().send(requestBodyDto);
				}
			}
		});
		roomListScrollPanel.setViewportView(roomList);
        
		usernameTextField = new JTextField();
		usernameTextField.setHorizontalAlignment(SwingConstants.LEFT);
		usernameTextField.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		usernameTextField.setBorder(new EmptyBorder(0, 0, 0, 0));
		usernameTextField.setEditable(false);
		usernameTextField.setText(username);

		usernameTextField.setBounds(52, 5, 53, 40);
		chattingRoomListPanel.add(usernameTextField);
		usernameTextField.setColumns(10);
	
		userIcon = new JLabel("");
		userIcon.setIcon(new ImageIcon("C:\\aws\\java\\workspace\\socket_project\\socket_project_client\\src\\userIcon.png"));

		userIcon.setBounds(12, 8, 35, 35);
		chattingRoomListPanel.add(userIcon);
		
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomPanel.setLayout(null);
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
		
		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(17, 150, 270, 340);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);
		
		chattingTextArea = new JTextArea();
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);
		
		messageTextField = new JTextField();
		messageTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					
					SendMessage sendMessage = SendMessage.builder()
							.fromUsername(username)
							.messageBody(messageTextField.getText())
							.build();
					
					RequestBodyDto<SendMessage> requestBodyDto = 
							new RequestBodyDto<>("sendMessage", sendMessage); 
					
					ClientSender.getInstance().send(requestBodyDto);
					messageTextField.setText("");
				}
			}
		});
		messageTextField.setBounds(17, 505, 195, 40);
		chattingRoomPanel.add(messageTextField);
		messageTextField.setColumns(10);
		
		userListScrollPane = new JScrollPane();
		userListScrollPane.setBounds(17, 55, 270, 80);
		chattingRoomPanel.add(userListScrollPane);
		
		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userListScrollPane.setViewportView(userList);
		
		roomNameTextField = new JTextField();
		roomNameTextField.setText("임시용"); //
		roomNameTextField.setHorizontalAlignment(SwingConstants.LEFT);
		roomNameTextField.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		roomNameTextField.setEditable(false);
		roomNameTextField.setColumns(10);
		roomNameTextField.setBorder(new EmptyBorder(0, 0, 0, 0));
		roomNameTextField.setBounds(52, 5, 130, 40);
		chattingRoomPanel.add(roomNameTextField);
		
		roomNameIcon = new JLabel("");
		roomNameIcon.setIcon(new ImageIcon("C:\\aws\\java\\workspace\\socket_project\\socket_project_client\\src\\userIcon.png"));
		roomNameIcon.setBounds(12, 8, 35, 35);
		chattingRoomPanel.add(roomNameIcon);
		
		ipLabel = new JLabel();
		ipLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		ipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ipLabel.setBounds(180, 6, 57, 35);
		chattingRoomPanel.add(ipLabel);
		
		sendButton = new JButton("전송");
		sendButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
		sendButton.setBounds(219, 504, 68, 40);
		chattingRoomPanel.add(sendButton);
		
		btnNewJLabel = new JLabel("X");
		btnNewJLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mainCardLayout.show(mainCardPanel, "chattingRoomListPanel");
			}
		});
		btnNewJLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		btnNewJLabel.setBounds(247, 8, 45, 35);
		chattingRoomPanel.add(btnNewJLabel);
		
		
	}

}
