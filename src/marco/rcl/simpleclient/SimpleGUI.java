package marco.rcl.simpleclient;


import marco.rcl.shared.Errors;
import marco.rcl.shared.Response;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Enum.valueOf;
import static marco.rcl.shared.Errors.noErrors;
import static marco.rcl.simpleclient.SimpleGUI.buttonNames.*;

/**
 * Created by marko on 21/06/2016.
 */
public class SimpleGUI {

    private static boolean isStarted = false;
    private static Dimension buttonDimensions = new Dimension(400,20);
    private static Dimension viewDimension = new Dimension(600,500);
    private static JFrame window = new JFrame("Simple-Chat");
    private static JTextArea messageLabel = new JTextArea(1,1);
    private static JTextField sendMessage = new JTextField(1);
    private static JButton sendButton = new JButton("Send");
    private static JPanel containerView = new JPanel();
    private static JPanel initialView = new JPanel();
    private static JPanel chatView = new JPanel();
    private static JPanel buttonsView = new JPanel();
    private static JScrollPane messagesView = new JScrollPane(messageLabel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private static JPanel sendView = new JPanel();
    private static CardLayout cardLayout = new CardLayout();
    private static HashMap<String,JButton> topButtons = new HashMap<>(5);
    private static String[] viewNames = {"initialView","chatView"};

    private static buttonNames btn = new buttonNames();

    static class buttonNames{
        static final String Logout = "Logout";
        static final String SearchUser = "Search User";
        static final String AddFriend = "Add Friend";
        static final String FriendList = "Friend List";
        static final String FollowFriend = "Follow Friend";

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

    public static void addMessage(String text){
        messageLabel.append(text);
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

    private static void setButtonsView(){
        buttonsView.setLayout(new FlowLayout());
    }

    private static void setMessageView(){
        messageLabel.setEditable(false);
        messageLabel.setBackground(Color.WHITE);
    }


    private static void setSendView(){
        sendView.setLayout(new FlowLayout());
        sendMessage.setSize(viewDimension.width-buttonDimensions.width,buttonDimensions.height);
        sendView.setSize(viewDimension.width,buttonDimensions.height);
        sendMessage.setSize(viewDimension.width - buttonDimensions.width,buttonDimensions.height);
        sendMessage.setBackground(Color.white);
        sendView.add(sendMessage);
        sendView.add(sendButton);
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
//        chatView.setMinimumSize(viewDimension);
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

    private static void setInitialView(){
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        loginButton.setSize(buttonDimensions);
        registerButton.setSize(buttonDimensions);
        loginButton.setBackground(Color.WHITE);
        registerButton.setBackground(Color.white);
        loginButton.addActionListener( e -> {
            JTextField username = new JTextField();
            JTextField password = new JTextField();
            Object[] message = {"Username:", username, "Password", password};
            int option =  JOptionPane.showConfirmDialog(initialView,message,"Login",JOptionPane.DEFAULT_OPTION);
            if (option!=JOptionPane.OK_OPTION) return;
            Errors error = Client.login(username.getText(),password.getText());
            if (error == Errors.noErrors)showChatView();
            else JOptionPane.showMessageDialog(initialView, Errors.getError(error),
                    "Login Error",JOptionPane.ERROR_MESSAGE);
        });
        registerButton.addActionListener( e -> {
            JTextField username = new JTextField();
            JTextField password = new JTextField();
            Object[] message = {"Username:", username, "Password", password};
            int option = JOptionPane.showConfirmDialog(initialView,message,
                    "Register",JOptionPane.DEFAULT_OPTION);
            if (option!=JOptionPane.OK_OPTION) return;
            Errors error = Client.register(username.getText(),password.getText());
            if (error == Errors.noErrors)showChatView();
            else JOptionPane.showMessageDialog(initialView, Errors.getError(error),
                    "Registration Error",JOptionPane.ERROR_MESSAGE);
        });
        initialView.add(loginButton);
        initialView.add(registerButton);
    }

    private static void setListeners(){
        topButtons.forEach((name,button) -> {
            switch (name) {
                case Logout:
                    button.addActionListener( e -> {
                        Client.logout();
                        JOptionPane.showMessageDialog(chatView,"Good bye!!");
                        showInitialView();
                    });
                    break;
                case SearchUser:
                    button.addActionListener( e -> {
                        String user = JOptionPane.showInputDialog("Please insert the username");
                        Response response = Client.searchUser(user);
                        if (response.getError()==noErrors)
                            JOptionPane.showMessageDialog(chatView,response.getUserList());
                        else JOptionPane.showInputDialog(chatView,Errors.getError(response.getError()),
                                "Search Error",JOptionPane.ERROR_MESSAGE);
                    });
                    break;
                case AddFriend:
                    button.addActionListener( e -> {
                        String user = JOptionPane.showInputDialog("Please insert the username");
                        Errors error = Client.addFriend(user);
                        if (error==noErrors) JOptionPane.showConfirmDialog(chatView,"Request sent");
                        else JOptionPane.showInputDialog(chatView,Errors.getError(error),
                                "Add Friend",JOptionPane.ERROR_MESSAGE);
                    });
                    break;
                case FriendList:
                    button.addActionListener( e -> {
                        Response response = Client.friendList();
                        if (response.getError()!= noErrors){
                            JOptionPane.showMessageDialog(chatView,Errors.getError(response.getError()),
                                    "Friend List",JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(chatView,response.getFriendList());
                        }
                    });
                    break;
                case FollowFriend:
                    button.addActionListener( e -> {
                        String user = JOptionPane.showInputDialog("Please insert the username");
                        Errors error = Client.followFriend(user);
                        if (error == noErrors) JOptionPane.showConfirmDialog(chatView, "User followed");
                        else JOptionPane.showInputDialog(chatView, Errors.getError(error),
                                "Follow Friend", JOptionPane.ERROR_MESSAGE);
                    });
            }
        });
    }

    private static void initButtons(){
        for (String name : buttonNames.getNames(btn)){
            JButton button = new JButton(name);
            button.setSize(buttonDimensions);
            buttonsView.add(name,button);
            topButtons.put(name,button);
        }
        setListeners();
        sendButton.setSize(buttonDimensions);
    }


    public static void startView(){
        if (isStarted)return;
        isStarted=true;
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout();
        setViews();
        setInitialView();
        showInitialView();
        setButtonsView();
        setMessageView();
        setSendView();
        setChatView();
    }


}
