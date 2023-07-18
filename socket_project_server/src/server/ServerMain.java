package server;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;

import lombok.Data;
import lombok.Setter;
import server.entity.Room;

@Data
public class ServerMain extends JFrame {
	
	private static ServerMain instance;
	public static ServerMain getInstance() {
	    if (instance == null) {
	        instance = new ServerMain();
	    }
	    return instance;
	}

	//ip / port설정
	String ip = "127.0.0.1";
	int port = 8000;
	int userNum = 0;
	String userNumString = Integer.toString(userNum);
	
	//서버 GUI 출력 메서드 
	public static void sysoutGUI (String print) {
		serverNotiTextArea.append(print + "\n");
		System.out.println(print);
		
	}
	
	//필드
	private JPanel mainPanel;
	private static JTextArea serverNotiTextArea;
	private ServerSocket serverSocket;
	private Socket socket;
	private JTextArea userNumArea;
	private Gson gson;
	
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
		setResizable(false);
		setAlwaysOnTop(true);
		
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
		sysoutGUI("소켓채팅 서버에 오신걸 환영합니다.\n");
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
		ServerStartButton.setBounds(26, 20, 108, 39);
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
		portArea.setText(""+port); //port변수
		
		userNumArea = new JTextArea();
		userNumArea.setFont(new Font("나눔고딕", Font.PLAIN, 13));
		userNumArea.setForeground(Color.WHITE);
		userNumArea.setBackground(Color.DARK_GRAY);
		userNumArea.setBounds(208, 56, 56, 15);
		mainPanel.add(userNumArea);
		userNumArea.setText("" + userNum); //접속자수 표시
		
	}
	
	// <<< 서버시작기능 >>>
    private void startServer() {
    	//백그라운드에서 무한루프 돌리기 위해 Thread사용.
    	Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
		            serverSocket = new ServerSocket(port);
		            sysoutGUI("포트 "+ port +"번으로 서버를 열었습니다.");

		        	userNumArea.setText("" + userNum);
		            while(true) {
		            	socket = serverSocket.accept();
						ConnectedSocket connectedSocket = new ConnectedSocket(socket);
						connectedSocket.start();
						connectedSocketList.add(connectedSocket);
		            	userNum += 1;
		            	
		            	//if(!ConnectedSocket.get  == null)
						//sysoutGUI("클라이언트" +userNum+" 연결됨" ); //나중에 username으로 수정해야함 완성후!
						
		            	//접속자 수 업데이트, EDT(EventDispatchThread)사용.
	                    EventQueue.invokeLater(new Runnable() {
	                        public void run() {
	                            userNumArea.setText("" + userNum); // userNumArea에 접속자 수 반영
	                        }
	                    });
		            }
				} catch (BindException e){ 
					sysoutGUI("서버시작 실패 : 서버가 이미 실행중입니다.");
		        } catch (IOException e) {

		        }
			}
			
		});
    	thread.start();
    }
    
    // <<< 서버끄기 기능>>>
    private void stopServer() {
    	//클라이언트 소켓을 닫고 서버 소켓을 닫아서 클라이언트의 연결을 중단
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            sysoutGUI("서버가 종료되었습니다.");
        } catch (IOException e) {
            //sysoutGUI("서버 종료에 실패: " + e.getMessage());
        } 
    }
}
