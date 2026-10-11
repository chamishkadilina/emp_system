package empsystem.ui;

import empsystem.dao.AdminDao;
import java.sql.SQLException;
import java.util.Optional;

/** Login screen; the first window shown. */
public class LoginFrame extends javax.swing.JFrame {

    private final AdminDao adminDao = new AdminDao();

    public LoginFrame() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverSettingsButton = new javax.swing.JLabel();
        adminSettingsButton = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        userIdField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        serverSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/login/link.png"))); // NOI18N
        serverSettingsButton.setText("jLabel8");
        serverSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serverSettingsButtonMouseClicked(evt);
            }
        });
        getContentPane().add(serverSettingsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(438, 431, 30, 30));

        adminSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/login/settings.png"))); // NOI18N
        adminSettingsButton.setText("jLabel8");
        adminSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                adminSettingsButtonMouseClicked(evt);
            }
        });
        getContentPane().add(adminSettingsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 430, 30, 30));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 51, 255));
        jLabel1.setText("Log In");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 120, 80, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Welcome Back");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, -1, -1));

        jLabel3.setText("User ID");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 180, -1, -1));

        jLabel4.setText("Password");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 250, -1, -1));

        userIdField.setBackground(new java.awt.Color(102, 204, 255));
        userIdField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 204, 255), 3));
        getContentPane().add(userIdField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 200, 230, 30));

        passwordField.setBackground(new java.awt.Color(102, 204, 255));
        passwordField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 204, 255), 3));
        getContentPane().add(passwordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 270, 230, 30));

        loginButton.setBackground(new java.awt.Color(0, 102, 255));
        loginButton.setForeground(new java.awt.Color(255, 255, 255));
        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        getContentPane().add(loginButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 350, 103, 38));

        errorLabel.setBackground(new java.awt.Color(255, 0, 51));
        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        getContentPane().add(errorLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 310, 240, 20));

        closeButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        closeButton.setText("x");
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeButtonMouseClicked(evt);
            }
        });
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 0, 20, 40));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/login/log.png"))); // NOI18N
        backgroundLabel.setText("jLabel5");
        getContentPane().add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 510, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        String userName = userIdField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());
        try {
            Optional<String> adminName = adminDao.login(userName, password);
            if (adminName.isPresent()) {
                new HomeFrame(adminName.get()).setVisible(true);
                dispose();
                return;
            }
            errorLabel.setText("Incorrect User ID Or Password");
        } catch (SQLException e) {
            errorLabel.setText("Server Error.. Check Host Address");
        }
        userIdField.setText("");
        passwordField.setText("");
    }//GEN-LAST:event_loginButtonActionPerformed

    private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMouseClicked
        System.exit(0);
    }//GEN-LAST:event_closeButtonMouseClicked

    private void adminSettingsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_adminSettingsButtonMouseClicked
        new AdminUsersFrame().setVisible(true);
        dispose();
    }//GEN-LAST:event_adminSettingsButtonMouseClicked

    private void serverSettingsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serverSettingsButtonMouseClicked
        new ServerSettingsFrame().setVisible(true);
        dispose();
    }//GEN-LAST:event_serverSettingsButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel closeButton;
    private javax.swing.JLabel serverSettingsButton;
    private javax.swing.JLabel adminSettingsButton;
    private javax.swing.JTextField userIdField;
    private javax.swing.JPasswordField passwordField;
    // End of variables declaration//GEN-END:variables
}
