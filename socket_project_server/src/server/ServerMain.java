package server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import server.entity.Room;

import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import java.awt.Color;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ServerMain extends JFrame {

	//ip / port설정
	String ip = "127.0.0.1";
	int port = 8000;
	String portString = Integer.toString(port);
	
	//서버 GUI 출력메서드
	public void sysoutGUI (String print) {
		serverNotiTextArea.append(print + "\n");
		System.out.println(print);
	}
	
	//필드
	private JPanel mainPanel;
	private JTextArea serverNotiTextArea;
	private ServerSocket serverSocket;
	private Socket socket;
	
	public static List<ConnectedSocket> connectedSocketList = new ArrayList<>();
	public static List<Room> roomList = new ArrayList<>();

	public static void main(String[] args) {
		
		//GUI표시
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerMain frame = new ServerMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public ServerMain() {
		
		// <<<GUI기본 Panel 설정>>>
		setBackground(new Color(128, 128, 128));
		setFont(new Font("나눔바른고딕", Font.PLAIN, 14));
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\jusg0\\OneDrive\\사진\\Samsung Gallery\\DCIM\\Screenshots\\Screenshot_20230710_225559_Whale.jpg"));
		setTitle("ServerTool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(1500, 100, 350, 600);
		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPanel);
		mainPanel.setLayout(null);
		
		//<<< 알림창 영역>>>
		JScrollPane serverNotiScrollPane = new JScrollPane();
		serverNotiScrollPane.setBounds(12, 81, 310, 470);
		mainPanel.add(serverNotiScrollPane);
		
		serverNotiTextArea = new JTextArea();
		serverNotiTextArea.setText("소켓채팅 서버에 오신걸 환영합니다.");
		serverNotiScrollPane.setViewportView(serverNotiTextArea);
		
		// <<< 서버시작 버튼 >>>
		JToggleButton ServerStartButton = new JToggleButton("서버 시작", false);
		ServerStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ServerStartButton.isSelected()) {
					startServer();
					
				} else if(!ServerStartButton.isSelected()){
					stopServer();
				}
				
			}
		});
		ServerStartButton.setFont(new Font("나눔고딕 ExtraBold", Font.PLAIN, 15));
		ServerStartButton.setBounds(26, 20, 94, 39);
		mainPanel.add(ServerStartButton);
		
		// <<< 라벨 >>>
		JLabel ipLabel = new JLabel("IP");
		ipLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 12));
		ipLabel.setBounds(163, 10, 22, 15);
		mainPanel.add(ipLabel);
		JLabel portLabel = new JLabel("Port");
		portLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 12));
		portLabel.setBounds(163, 33, 57, 15);
		mainPanel.add(portLabel);
		JLabel userNumLabel = new JLabel("접속자");
		userNumLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 12));
		userNumLabel.setBounds(163, 56, 57, 15);
		mainPanel.add(userNumLabel);
		
		//<<< 기본정보표시 >>>
		JTextArea ipArea = new JTextArea();
		ipArea.setEditable(false);
		ipArea.setFont(new Font("나눔고딕", Font.PLAIN, 12));
		ipArea.setForeground(Color.WHITE);
		ipArea.setBackground(Color.DARK_GRAY);
		ipArea.setBounds(208, 10, 56, 15);
		mainPanel.add(ipArea);
		ipArea.setText(ip); //ip 변수set
		JTextArea portArea = new JTextArea();
		portArea.setEditable(false);
		portArea.setFont(new Font("나눔고딕", Font.PLAIN, 12));
		portArea.setForeground(Color.WHITE);
		portArea.setBackground(Color.DARK_GRAY);
		portArea.setBounds(208, 33, 56, 15);
		mainPanel.add(portArea);
		portArea.setText(portString); //port변수
		JTextArea userNumArea = new JTextArea();
		userNumArea.setEditable(false);
		userNumArea.setFont(new Font("나눔고딕", Font.PLAIN, 13));
		userNumArea.setForeground(Color.WHITE);
		userNumArea.setBackground(Color.DARK_GRAY);
		userNumArea.setBounds(208, 56, 56, 15);
		mainPanel.add(userNumArea);
		userNumArea.setText("구현해야함"); //접속자수 표시
		
	}
	
	// <<< 서버시작기능 >>>
    private void startServer() {
    	try {
            // 서버 소켓 생성 및 클라이언트 연결 대기
            sysoutGUI("클라이언트 연결을 시도합니다.");
            this.serverSocket = new ServerSocket(port);
            sysoutGUI("서버 시작: "+ portString +"포트에서 클라이언트 연결을 시도합니다.");
           
            while(true) {
				sysoutGUI("accept 전");
            	this.socket = serverSocket.accept();
            	sysoutGUI("accept 성공");
				ConnectedSocket connectedSocket = new ConnectedSocket(socket);
				connectedSocket.start();
				connectedSocketList.add(connectedSocket);
            }
        } catch (IOException e) {
            sysoutGUI("서버 시작 실패: " + e.getMessage());
        }
    }
    
    // <<< 서버끄기 기능>>>
    private void stopServer() {
    	//클라이언트 소켓을 닫고 서버 소켓을 닫아서 클라이언트의 연결을 중단
    	sysoutGUI("서버 종료 로직을 구현해야합니다.\n");
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            sysoutGUI("서버가 종료되었습니다.\n");
        } catch (IOException e) {
            System.out.println("서버 종료에 실패: " + e.getMessage());
        }
    }
	
}
