import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class TransferingDialog extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -949816392934951638L;

	public TransferingDialog(String message, String title) {
	setSize(500,70);
	setLocationByPlatform(true);
	setTitle(title);
	final JLabel messageLabel = new JLabel(message);
	messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
	getContentPane().add(messageLabel,BorderLayout.NORTH);
	//final JButton button = new JButton("Click Me");
    //getContentPane().add(button,BorderLayout.SOUTH);
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	setVisible(true);
	}
	



}
