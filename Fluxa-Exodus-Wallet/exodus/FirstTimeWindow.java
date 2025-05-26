import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import java.awt.Window.Type;

public class FirstTimeWindow {

	private JFrame frmEvault;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FirstTimeWindow window = new FirstTimeWindow();
					window.frmEvault.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FirstTimeWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmEvault = new JFrame();
		frmEvault.setTitle("EVault");
		frmEvault.setIconImage(Toolkit.getDefaultToolkit().getImage(FirstTimeWindow.class.getResource("/images/256x256.png")));
		frmEvault.setBounds(100, 100, 450, 300);
		frmEvault.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblWelcomeToEvault = new JLabel("Welcome To EVault");
		lblWelcomeToEvault.setHorizontalAlignment(SwingConstants.CENTER);
		frmEvault.getContentPane().add(lblWelcomeToEvault, BorderLayout.NORTH);
	}
}
