import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class DarkButton extends JButton {

    private static final Color NORMAL_COLOR = new Color(60, 60, 60); //DarkGray
    private static final Color HOVER_COLOR = new Color(40, 40, 40); //DarkerGray

    public DarkButton(String text) {
        super(text);
        setBackground(NORMAL_COLOR);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(0, 0, 0, 0)); // This helps avoid extra padding
        setFont(new Font("SansSerif", Font.BOLD, 13));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                setBackground(HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                setBackground(NORMAL_COLOR);
            }
        });
    }
}
