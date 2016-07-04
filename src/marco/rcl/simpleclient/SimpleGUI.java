package marco.rcl.simpleclient;


import marco.rcl.shared.Errors;
import marco.rcl.shared.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import static marco.rcl.shared.Errors.UserNotLogged;
import static marco.rcl.shared.Errors.noErrors;
import static marco.rcl.simpleclient.SimpleGUI.buttonNames.*;

/**
 * This class implements the GUI showed to the users
 */
public class SimpleGUI {

    private static boolean isStarted = false;
    private static Dimension viewDimension = new Dimension(620,500);
    private static JFrame window = new JFrame("Simple-Chat");
    private static JTextArea messageLabel = new JTextArea(1,1);
    private static JTextField sendMessage = new JTextField(1);
    private static JButton sendButton = new JButton("Send");
    private static JPanel containerView = new JPanel();
    private static JPanel initialView = new JPanel();
    private static JPanel chatView = new JPanel();
    private static JScrollPane messagesView = new JScrollPane(messageLabel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private static CardLayout cardLayout = new CardLayout();
    private static HashMap<String,JButton> topButtons = new HashMap<>(6);
    private static String[] viewNames = {"initialView","chatView"};
    private static ExecutorService ex = Client.getExecutorService();
    private static buttonNames btn = new buttonNames();


    static class buttonNames{
        static final String Logout = "Logout";
        static final String SearchUser = "Search User";
        static final String AddFriend = "Add Friend";
        static final String FriendList = "Friend List";
        static final String FollowFriend = "Follow Friend";
        static final String FriendRequests = "Friend Requests";

        static String[] getNames(buttonNames names){
            if (names==null) return null;
            ArrayList<String> fields = new ArrayList<>();
            for (Field field : names.getClass().getDeclaredFields()) {
                try {
                    fields.add((String) field.get(field));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return fields.toArray(new String[fields.size()]);
        }
    }

    static {
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Client.close();
                super.windowClosing(e);
            }
        });
    }

    public static void addMessage(String text){
        SwingUtilities.invokeLater(() -> {
            messageLabel.append(text);
            messageLabel.repaint();
        });
    }

    private static void showInitialView(){
        window.setMinimumSize(new Dimension(250,100));
        window.setSize(250,100);
        containerView.setMinimumSize(new Dimension(250,100));
        containerView.setSize(250,100);
        cardLayout.show(containerView,viewNames[0]);
    }
    private static void showChatView(){
        window.setMinimumSize(viewDimension);
        containerView.setMinimumSize(viewDimension);
        cardLayout.show(containerView,viewNames[1]);
    }


    private static void setMessageView(){
        messageLabel.setEditable(false);
        messageLabel.setBackground(Color.WHITE);
    }


    private static void setLayout(){
        initialView.setLayout(new FlowLayout());
        initialView.setBackground(Color.lightGray);
        containerView.setLayout(cardLayout);
        containerView.setSize(viewDimension);
        window.setSize(300,100);
        window.setLocationRelativeTo(null);
        window.add(containerView);
        window.setVisible(true);
    }

    private static void setViews(){
        containerView.add(viewNames[0], initialView);
        containerView.add(viewNames[1], chatView);
    }

    private static void setChatView(){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        chatView.setLayout(layout);
        chatView.setBackground(Color.lightGray);
        initButtons();
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridx=0;
        constraints.gridy=0;
        topButtons.forEach((name,button) -> {
            chatView.add(button,constraints);
            constraints.gridx = GridBagConstraints.RELATIVE;
        });
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = topButtons.size();
        constraints.weightx = 1;
        constraints.weighty = 1;
        chatView.add(messagesView,constraints);
        constraints.weighty = 0;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = topButtons.size()-1;
        constraints.gridheight = 1;
        chatView.add(sendMessage,constraints);
        constraints.weightx = 0;
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        chatView.add(sendButton,constraints);
    }

    private static void sendListener(){
        String content = sendMessage.getText();
        if (content==null || content.equals(""))return;
        content += '\n';
        sendMessage.setText("");
        Client.publish(content);
        messageLabel.append("me: " + content);
    }

    private static void setInitialView(){
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        loginButton.setBackground(Color.WHITE);
        registerButton.setBackground(Color.white);
        loginButton.addActionListener( e -> {
                JTextField username = new JTextField();
                JTextField password = new JTextField();
                Object[] message = {"Username:", username, "Password", password};
                int option = JOptionPane.showConfirmDialog(initialView, message, "Login", JOptionPane.DEFAULT_OPTION);
                if (option != JOptionPane.OK_OPTION) return;
                Errors error = Client.login(username.getText(), password.getText());
                if (error == Errors.noErrors) showChatView();
                else JOptionPane.showMessageDialog(initialView, Errors.getError(error),
                        "Login Error", JOptionPane.ERROR_MESSAGE);
        });
        registerButton.addActionListener( e -> {
                JTextField username = new JTextField();
                JTextField password = new JTextField();
                Object[] message = {"Username:", username, "Password", password};
                int option = JOptionPane.showConfirmDialog(initialView, message,
                        "Register", JOptionPane.DEFAULT_OPTION);
                if (option != JOptionPane.OK_OPTION) return;
                Errors error = Client.register(username.getText(), password.getText());
                if (error == Errors.noErrors) showChatView();
                else JOptionPane.showMessageDialog(initialView, Errors.getError(error),
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
        });

        initialView.add(loginButton);
        initialView.add(registerButton);
    }

    private static void setListeners(){
        topButtons.forEach((name,button) -> {
            switch (name) {
                case Logout:
                    button.addActionListener( e -> {
                            messageLabel.setText("");
                            Client.logout();
                            JOptionPane.showMessageDialog(chatView, "Good bye!!");
                            showInitialView();
                    });
                    break;
                case SearchUser:
                    button.addActionListener( e -> {
                            String user = JOptionPane.showInputDialog("Please insert the username");
                            if (user == null || user.equals("")) return;
                            Response response = Client.searchUser(user);
                            if (response.getError() == UserNotLogged) {
                                showInitialView();
                                return;
                            }
                            if (response.getError() == noErrors) {
                                if (response.getUserList() == null) {
                                    JOptionPane.showMessageDialog(chatView, "User not found");
                                } else JOptionPane.showMessageDialog(chatView, response.getUserList());
                            } else JOptionPane.showMessageDialog(chatView, Errors.getError(response.getError()),
                                    "Search Error", JOptionPane.ERROR_MESSAGE);
                    });
                    break;
                case AddFriend:
                    button.addActionListener( e -> {
                            String user = JOptionPane.showInputDialog("Please insert the username");
                            if (user == null || user.equals("")) return;
                            Errors error = Client.addFriend(user);
                            if (error == UserNotLogged) {
                                showInitialView();
                                return;
                            }
                            if (error == noErrors) JOptionPane.showMessageDialog(chatView, "Request sent");
                            else JOptionPane.showMessageDialog(chatView, Errors.getError(error),
                                    "Add Friend", JOptionPane.ERROR_MESSAGE);
                    });
                    break;
                case FriendList:
                    button.addActionListener( e -> {
                            Response response = Client.friendList();
                            if (response.getError() == UserNotLogged) {
                                showInitialView();
                                return;
                            }
                            if (response.getError() != noErrors) {
                                JOptionPane.showMessageDialog(chatView, Errors.getError(response.getError()),
                                        "Friend List", JOptionPane.ERROR_MESSAGE);
                            } else {
                                if (response.getFriendList() != null)
                                    JOptionPane.showMessageDialog(chatView, response.getFriendList());
                                else
                                    JOptionPane.showMessageDialog(chatView, "Empty friend list");
                            }
                        });
                    break;
                case FollowFriend:
                    button.addActionListener( e -> {
                            String user = JOptionPane.showInputDialog("Please insert the username");
                            if (user == null || user.equals("")) return;
                            Errors error = Client.followFriend(user);
                            if (error == UserNotLogged) {
                                showInitialView();
                                return;
                            }
                            if (error == noErrors) JOptionPane.showMessageDialog(chatView, "User followed");
                            else JOptionPane.showMessageDialog(chatView, Errors.getError(error),
                                    "Follow Friend", JOptionPane.ERROR_MESSAGE);
                        });
                    break;
                case FriendRequests:
                    button.addActionListener(e -> {
                            Response response = Client.friendRequests();
                            if (response.getError() == UserNotLogged) {
                                showInitialView();
                                return;
                            }
                            if (response.getUserList() == null) {
                                JOptionPane.showMessageDialog(chatView, "You have no requests!",
                                        "Follow Friend", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                for (String request : response.getUserList()) {
                                    int option = JOptionPane.showConfirmDialog(chatView, request + " wants to be your friend",
                                            "Friend request", JOptionPane.YES_NO_OPTION);
                                    if (option == JOptionPane.OK_OPTION) {
                                        Client.confirmRequest(request);
                                    } else Client.ignoreRequest(request);
                                }
                            }
                        });
                    break;
            }
        });
        sendButton.addActionListener(e -> SimpleGUI.sendListener());
        sendMessage.addActionListener(e -> SimpleGUI.sendListener());

    }

    private static void initButtons(){
        for (String name : buttonNames.getNames(btn)){
            JButton button = new JButton(name);
            topButtons.put(name,button);
        }
        setListeners();
    }


    public static void startView(){
        if (isStarted)return;
        isStarted=true;
        setLayout();
        setViews();
        setInitialView();
        showInitialView();
        setMessageView();
        setChatView();
    }


}
