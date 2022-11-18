package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *Create an account or login
 */
public class CreateAccountOrLogin {
    StorylineInterface controller;

    public void BuildCreateAccountOrLoginScreen() {
        JFrame frame = new JFrame("Create an account or login?");
        frame.setSize(600, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());

        JButton createAccount = new JButton("Create an account");
        JButton login = new JButton("Login");

        createAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.BuildRegisterScreen(); //jump to RegisterScreen
            }
        });
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.BuildLoginScreen(); //jump to LoginScreen
            }
        });

        frame.add(createAccount);
        frame.add(login);
        frame.setVisible(true);
    }
}
