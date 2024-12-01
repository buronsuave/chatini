package frontend;

import app.ChatiniApp;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;

public class GraphicApp extends JFrame {
    private final ChatiniApp context;
    private JPanel messageContainer;
    private JTextArea messageInputArea;
    private JButton sendButton;
    private JTextField ipInputField;

    public GraphicApp(ChatiniApp context) {
        this.context = context;

        // Set up JFrame properties
        setTitle("Chatini App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLayout(new BorderLayout());

        // Create a toolbar
        JToolBar toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Create message container
        messageContainer = createMessageContainer();
        JScrollPane pane = new JScrollPane(messageContainer);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(pane, BorderLayout.CENTER);

        // Create input area and send button
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);

        // Show JFrame
        setVisible(true);
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Text field for IP input
        ipInputField = new JTextField("Enter IP here...");
        ipInputField.setPreferredSize(new Dimension(200, 30));
        toolbar.add(ipInputField);

        // Spacer to push the button to the right
        toolbar.add(Box.createHorizontalGlue());

        // Load chat button
        JButton loadButton = new JButton("Load Chat");
        loadButton.addActionListener(actionEvent -> refreshMessages());
        toolbar.add(loadButton);

        return toolbar;
    }

    private JPanel createMessageContainer() {
        messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(Color.WHITE);
        messageContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Top, Left, Bottom, Right
        return messageContainer;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);

        // Create the message input area
        messageInputArea = new JTextArea();
        messageInputArea.setPreferredSize(new Dimension(400, 50));
        messageInputArea.setLineWrap(true);
        messageInputArea.setWrapStyleWord(true);
        messageInputArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Create send button
        sendButton = new JButton("Send Message");
        sendButton.setEnabled(false); // Initially disabled until chat is loaded
        sendButton.addActionListener(actionEvent ->  {
            if (ipInputField.getText().equals("broadcast")) {
                context.sendBroadcastMessage(messageInputArea.getText());
            } else {
                context.sendMessage(ipInputField.getText(), messageInputArea.getText());
            }
        });

        inputPanel.add(messageInputArea, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        return inputPanel;
    }

    public void refreshMessages() {
        int chatIndex = context.findChat(ipInputField.getText());
        loadMessages(chatIndex);
    }

    private void loadMessages(int chatIndex) {
        // Clear container
        messageContainer.removeAll();
        // Enable the send button after loading a chat
        sendButton.setEnabled(true);

        if (chatIndex == -1) {
            // Refresh the messageContainer to display no components
            messageContainer.revalidate();
            messageContainer.repaint();
            return;
        }

        // Add each message as a MessageComponent
        JsonArray chat = context.getMessages().get(chatIndex)
                .getAsJsonObject().get("record").getAsJsonArray();

        for (int i = 0; i < chat.size(); i++) {
            JsonObject message = chat.get(i).getAsJsonObject();
            String content = message.get("content").getAsString();
            String sender = message.get("sender").getAsString();
            boolean isUserMessage = sender.equals(context.getHostname());

            // Add a MessageComponent to the container
            messageContainer.add(new MessageComponent(content, sender, isUserMessage));

            // Add vertical spacing after each message (except the last one)
            if (i < chat.size() - 1) {
                messageContainer.add(Box.createVerticalStrut(10)); // 10px spacing
            }

        }

        // Refresh the messageContainer to display the new components
        messageContainer.revalidate();
        messageContainer.repaint();
    }

    public void run() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
