package frontend;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class MessageComponent extends JPanel {

    public MessageComponent(String content, String sender, boolean isUserMessage) {
        // Set up panel properties
        setLayout(new BorderLayout());
        setBorder(new RoundedBorder(15, isUserMessage ? new Color(0, 200, 100) : new Color(100, 150, 255)));
        setBackground(isUserMessage ? new Color(220, 255, 220) : new Color(220, 230, 255));
        setOpaque(true);

        // Add margins inside the card
        setBorder(BorderFactory.createCompoundBorder(
                getBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Message content
        JLabel contentLabel = new JLabel("<html>" + content + "</html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(contentLabel, BorderLayout.CENTER);

        // Sender info
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        senderLabel.setForeground(Color.DARK_GRAY);
        add(senderLabel, BorderLayout.SOUTH);

        // Add space between cards
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
    }
}

// RoundedBorder Class
class RoundedBorder extends AbstractBorder {
    private final int radius;
    private final Color borderColor;

    public RoundedBorder(int radius, Color borderColor) {
        this.radius = radius;
        this.borderColor = borderColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius, radius, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = radius;
        insets.right = radius;
        insets.top = radius;
        insets.bottom = radius;
        return insets;
    }
}
