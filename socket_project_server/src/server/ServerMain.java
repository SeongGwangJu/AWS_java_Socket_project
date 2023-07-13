package server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.ImageIcon;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ServerMain extends JFrame {

	private JPanel server;
	private JTextArea serverNotiTextArea;
	private ServerSocket serverSocket;
	private Socket socket;
	String ip = "127.0.0.1";
	int portInt = 8000;
	String portString = Integer.toString(portInt);
	
	public void sysoutTool (String print) {
		serverNotiTextArea.append(print);
	}

	public static void main(String[] args) {
		
		/*
		 ServerMain server = new ServerMain();
		server.setVisible(true);
		*/
		
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
		

		setBackground(new Color(128, 128, 128));
		setFont(new Font("나눔바른고딕", Font.PLAIN, 14));
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\jusg0\\OneDrive\\사진\\Samsung Gallery\\DCIM\\Screenshots\\Screenshot_20230710_225559_Whale.jpg"));
		setTitle("ServerTool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(1500, 100, 350, 600);
		server = new JPanel();
		server.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(server);
		server.setLayout(null);
		
		JScrollPane serverNotiScrollPane = new JScrollPane();
		serverNotiScrollPane.setBounds(12, 81, 310, 470);
		server.add(serverNotiScrollPane);
		
		serverNotiTextArea = new JTextArea();
		serverNotiTextArea.setText("소켓채팅 서버에 오신걸 환영합니다.");
		serverNotiScrollPane.setViewportView(serverNotiTextArea);
		
		//서버시작
		JToggleButton ServerStartButton = new JToggleButton("서버 시작", false);
		ServerStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ServerStartButton.isSelected()) {
					sysoutTool("\n서버를 시작합니다\n");
			       startServer();
				} else if(!serverSocket.isClosed()) {
					stopServer();
				}
				
			}
		});
		
		ServerStartButton.setFont(new Font("나눔고딕 ExtraBold", Font.PLAIN, 15));
		ServerStartButton.setBounds(26, 20, 94, 39);
		server.add(ServerStartButton);
		
		JLabel ipLabel = new JLabel("IP");
		ipLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 12));
		ipLabel.setBounds(163, 10, 22, 15);
		server.add(ipLabel);
		
		JLabel portLabel = new JLabel("Port");
		portLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 12));
		portLabel.setBounds(163, 33, 57, 15);
		server.add(portLabel);
		
		JLabel userNumLabel = new JLabel("접속자");
		userNumLabel.setFont(new Font("나눔바른고딕", Font.PLAIN, 12));
		userNumLabel.setBounds(163, 56, 57, 15);
		server.add(userNumLabel);
		
		JTextArea ipArea = new JTextArea();
		ipArea.setEditable(false);
		ipArea.setFont(new Font("나눔고딕", Font.PLAIN, 12));
		ipArea.setForeground(Color.WHITE);
		ipArea.setBackground(Color.DARK_GRAY);
		ipArea.setBounds(208, 10, 56, 15);
		server.add(ipArea);
		
		ipArea.setText(ip);
		
		JTextArea portArea = new JTextArea();
		portArea.setEditable(false);
		portArea.setFont(new Font("나눔고딕", Font.PLAIN, 12));
		portArea.setForeground(Color.WHITE);
		portArea.setBackground(Color.DARK_GRAY);
		portArea.setBounds(208, 33, 56, 15);
		server.add(portArea);
		
		portArea.setText(portString);
		
		JTextArea userNumArea = new JTextArea();
		userNumArea.setEditable(false);
		userNumArea.setFont(new Font("나눔고딕", Font.PLAIN, 13));
		userNumArea.setForeground(Color.WHITE);
		userNumArea.setBackground(Color.DARK_GRAY);
		userNumArea.setBounds(208, 56, 56, 15);
		server.add(userNumArea);
		//접속자수 표시
		userNumArea.setText("추가해야함");
		
	}
	
    private void startServer() {
    	try {
            // 서버 소켓 생성 및 클라이언트 연결 대기
            sysoutTool("서버 시작: "+ portString +"포트에서 클라이언트 연결 전\n");

            ServerSocket serverSocket = new ServerSocket(portInt);
            sysoutTool("서버 시작: "+ portString +"포트에서 클라이언트와 연결 완료.\n");
            System.out.println("서버 시작: " + portString + "포트에서 클라이언트 연결 대기 중...");
            
            while(true) {
            	Socket socket = serverSocket.accept();
            	serverNotiTextArea.append("접속");
            	System.out.println("누군가 접속");
            	sysoutTool("드디어 누군가 접속");
            }
        } catch (IOException e2) {
            System.out.println("서버 시작 실패: " + e2.getMessage());
        }
    }

    private void stopServer() {
    	//클라이언트 소켓을 닫고 서버 소켓을 닫아서 클라이언트의 연결을 중단
    	serverNotiTextArea.append("서버 종료 로직을 구현해야합니다.\n");
        try {
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            serverNotiTextArea.append("서버가 종료되었습니다.\n");
        } catch (IOException e) {
            System.out.println("서버 종료에 실패: " + e.getMessage());
        }
    }
	
}
