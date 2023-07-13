package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lombok.Getter;

import java.awt.CardLayout;

@Getter
public class ClientMain extends JFrame {

	private JPanel mainCardPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMain frame = new ClientMain();
					frame.setVisible(true);
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 290, 520);
		mainCardPanel = new JPanel();
		mainCardPanel.setBorder(null);

		setContentPane(mainCardPanel);
		mainCardPanel.setLayout(new CardLayout(0, 0));
	}

}
