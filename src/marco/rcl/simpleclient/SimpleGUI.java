package marco.rcl.simpleclient;


import javax.naming.InitialContext;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by marko on 21/06/2016.
 */
public class SimpleGUI {

    private static JFrame window = new JFrame("Simple-Chat");
    private static JPanel containerView = new JPanel();
    private static JPanel initialView = new JPanel();
    private static JPanel chatView = new JPanel();
    private static JPanel buttonsView = new JPanel();
    private static JPanel messagesVies = new JPanel();
    private static JPanel sendView = new JPanel();
    private static CardLayout cardLayout = new CardLayout();
    private static HashMap<String,JButton> topButtons = new HashMap<>(5);
    private static String[] viewNames = {"initialView","chatView"};
    private static boolean isStarted = false;
    private static Dimension buttonDimensions = new Dimension(400,20);

    private abstract static class buttonNames{
        static final String Logout = "Logout";
        static final String SearchUser = "Search User";
        static final String AddFriend = "Add Friend";
        static final String FriendList = "Friend List";
        static final String FollowFriend = "Follow Friend";
        static String[] getNames(){
            return new String[]{Logout,SearchUser,AddFriend,FriendList,FollowFriend};
        }
    }

    private static void initButtons(){
        for (String name : buttonNames.getNames()){
            JButton button = new JButton(name);
            button.setSize(buttonDimensions);
            buttonsView.add(name,button);
            topButtons.put(name,button);
        }
    }

    private static void showInitialView(){
        cardLayout.show(containerView,viewNames[0]);
        window.setSize(1000,50);
    }
    private static void showChatView(){
        cardLayout.show(chatView,viewNames[1]);
    }


    private static void setLayout(){
        containerView.setLayout(cardLayout);
        chatView.setLayout(new GridLayout(4,1));
        buttonsView.setLayout(new FlowLayout());
        messagesVies.setLayout(new FlowLayout());
        sendView.setLayout(new FlowLayout());
        initialView.setLayout(new FlowLayout());
    }

    private static void setViews(){
        containerView.add(viewNames[0], initialView);
        containerView.add(viewNames[1], chatView);
    }

    private static void setInitialView(){
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        loginButton.setSize(buttonDimensions);
        registerButton.setSize(buttonDimensions);
    }

    private void setListeners(){
        topButtons.forEach((name,button) -> {
            switch (name) {
                case buttonNames.Logout:
                    Client.logout();
                    showInitialView();
                    break;
            }
        });
    }


    public static void startView(){
        if (isStarted)return;
        isStarted=true;
        initButtons();
        setLayout();
        setViews();
        showChatView();
    }


}
