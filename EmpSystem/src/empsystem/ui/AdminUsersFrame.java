package empsystem.ui;

import empsystem.dao.AdminDao;
import empsystem.util.Dialogs;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.JPanel;

/** Adds and deletes admin accounts. Unlocked with {@link #MASTER_PASSWORD}. */
public class AdminUsersFrame extends javax.swing.JFrame {

    private static final String MASTER_PASSWORD = "123";

    private final AdminDao adminDao = new AdminDao();

    public AdminUsersFrame() {
        initComponents();
    }

    private void showPage(JPanel page) {
        for (JPanel p : new JPanel[] {passwordPage, menuPage, addUserPage, deleteUserPage}) {
            p.setVisible(p == page);
        }
    }

    private void backToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cardPanel = new javax.swing.JPanel();
        passwordPage = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        masterPasswordField = new javax.swing.JPasswordField();
        enterButton = new javax.swing.JButton();
        passwordPageCloseButton = new javax.swing.JLabel();
        menuPage = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        menuPageCloseButton = new javax.swing.JLabel();
        deleteUserTile = new javax.swing.JLabel();
        addUserTile = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        addUserPage = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        userNameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        addUserButton = new javax.swing.JButton();
        addUserCloseButton = new javax.swing.JLabel();
        addUserBackButton = new javax.swing.JLabel();
        deleteUserPage = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        deleteUserNameField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        deleteUserButton = new javax.swing.JButton();
        deleteUserCloseButton = new javax.swing.JLabel();
        deleteUserBackButton = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cardPanel.setBackground(new java.awt.Color(255, 255, 255));
        cardPanel.setLayout(new java.awt.CardLayout());

        passwordPage.setBackground(new java.awt.Color(255, 255, 255));
        passwordPage.setPreferredSize(new java.awt.Dimension(593, 466));
        passwordPage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        passwordPage.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(561, 0, 32, -1));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        passwordPage.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 29, -1));

        jLabel10.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("PLEASE ENTER  PASSWORD");
        passwordPage.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 140, 290, -1));

        jLabel12.setText("Password");
        passwordPage.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 220, -1, -1));
        passwordPage.add(masterPasswordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 220, 197, -1));

        enterButton.setText("ENTER");
        enterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterButtonActionPerformed(evt);
            }
        });
        passwordPage.add(enterButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 300, -1, 30));

        passwordPageCloseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/close2.png"))); // NOI18N
        passwordPageCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                passwordPageCloseButtonMouseClicked(evt);
            }
        });
        passwordPage.add(passwordPageCloseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 30, 30));

        cardPanel.add(passwordPage, "card3");

        menuPage.setBackground(new java.awt.Color(255, 255, 255));
        menuPage.setPreferredSize(new java.awt.Dimension(593, 466));
        menuPage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        menuPage.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 0, 32, -1));

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        menuPage.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 29, -1));

        menuPageCloseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/close2.png"))); // NOI18N
        menuPageCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuPageCloseButtonMouseClicked(evt);
            }
        });
        menuPage.add(menuPageCloseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 30, 30));

        deleteUserTile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/du.png"))); // NOI18N
        deleteUserTile.setText("jLabel14");
        deleteUserTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteUserTileMouseClicked(evt);
            }
        });
        menuPage.add(deleteUserTile, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, 110, 120));

        addUserTile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/anu.png"))); // NOI18N
        addUserTile.setText("jLabel14");
        addUserTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addUserTileMouseClicked(evt);
            }
        });
        menuPage.add(addUserTile, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 110, 120));

        jLabel14.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        jLabel14.setText("DELETE USER ACCOUNT");
        menuPage.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 180, -1, -1));

        jLabel15.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        jLabel15.setText("CREATE USER ACCOUNT");
        menuPage.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 180, -1, -1));

        cardPanel.add(menuPage, "card13");

        addUserPage.setBackground(new java.awt.Color(255, 255, 255));
        addUserPage.setPreferredSize(new java.awt.Dimension(593, 466));
        addUserPage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Name");
        addUserPage.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(159, 211, -1, -1));

        jLabel2.setText("User Name");
        addUserPage.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 256, -1, -1));

        jLabel3.setText("Password");
        addUserPage.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 299, -1, -1));

        addUserPage.add(nameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(208, 208, 197, -1));
        addUserPage.add(userNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(208, 253, 197, -1));

        addUserPage.add(passwordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(208, 296, 197, -1));

        jLabel4.setBackground(new java.awt.Color(0, 204, 204));
        jLabel4.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("ADD NEW USER");
        addUserPage.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(215, 124, 190, 36));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        addUserPage.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 0, 32, -1));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        addUserPage.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 29, -1));

        addUserButton.setText("ADD");
        addUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUserButtonActionPerformed(evt);
            }
        });
        addUserPage.add(addUserButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(267, 336, 82, -1));

        addUserCloseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/close2.png"))); // NOI18N
        addUserCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addUserCloseButtonMouseClicked(evt);
            }
        });
        addUserPage.add(addUserCloseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 30, 30));

        addUserBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/back_small.png"))); // NOI18N
        addUserBackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addUserBackButtonMouseClicked(evt);
            }
        });
        addUserPage.add(addUserBackButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 40, 30));

        cardPanel.add(addUserPage, "card13");

        deleteUserPage.setBackground(new java.awt.Color(255, 255, 255));
        deleteUserPage.setPreferredSize(new java.awt.Dimension(593, 466));
        deleteUserPage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setText("User Name");
        deleteUserPage.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 210, -1, -1));

        deleteUserPage.add(deleteUserNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(208, 208, 197, -1));

        jLabel23.setBackground(new java.awt.Color(0, 204, 204));
        jLabel23.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(0, 0, 0));
        jLabel23.setText("DELETE  USER  ACCOUNT");
        deleteUserPage.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 130, 230, 36));

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        deleteUserPage.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 0, 32, -1));

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/sidePane.png"))); // NOI18N
        deleteUserPage.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 29, -1));

        deleteUserButton.setText("DELETE");
        deleteUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUserButtonActionPerformed(evt);
            }
        });
        deleteUserPage.add(deleteUserButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(267, 250, 82, -1));

        deleteUserCloseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/close2.png"))); // NOI18N
        deleteUserCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteUserCloseButtonMouseClicked(evt);
            }
        });
        deleteUserPage.add(deleteUserCloseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 30, 30));

        deleteUserBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/admin/back_small.png"))); // NOI18N
        deleteUserBackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteUserBackButtonMouseClicked(evt);
            }
        });
        deleteUserPage.add(deleteUserBackButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 40, 30));

        cardPanel.add(deleteUserPage, "card13");

        getContentPane().add(cardPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 450));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void enterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enterButtonActionPerformed
        if (MASTER_PASSWORD.equals(new String(masterPasswordField.getPassword()))) {
            showPage(menuPage);
        } else {
            Dialogs.showInfo(this, "Incorrect password");
            backToLogin();
        }
    }//GEN-LAST:event_enterButtonActionPerformed

    private void addUserTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addUserTileMouseClicked
        showPage(addUserPage);
    }//GEN-LAST:event_addUserTileMouseClicked

    private void deleteUserTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteUserTileMouseClicked
        showPage(deleteUserPage);
    }//GEN-LAST:event_deleteUserTileMouseClicked

    private void addUserBackButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addUserBackButtonMouseClicked
        showPage(menuPage);
    }//GEN-LAST:event_addUserBackButtonMouseClicked

    private void deleteUserBackButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteUserBackButtonMouseClicked
        showPage(menuPage);
    }//GEN-LAST:event_deleteUserBackButtonMouseClicked

    private void addUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUserButtonActionPerformed
        String userName = userNameField.getText().trim().toLowerCase();
        if (userName.isEmpty()) {
            Dialogs.showInfo(this, "Please enter a user name");
            return;
        }
        if (!Dialogs.confirm(this, "Do you want to add this user?")) {
            return;
        }
        try {
            adminDao.add(nameField.getText(), userName, passwordField.getText());
            Dialogs.showInfo(this, "User added successfully");
            backToLogin();
        } catch (SQLIntegrityConstraintViolationException e) {
            Dialogs.showInfo(this, "This user name already exists");
            nameField.setText("");
            userNameField.setText("");
            passwordField.setText("");
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }//GEN-LAST:event_addUserButtonActionPerformed

    private void deleteUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteUserButtonActionPerformed
        String userName = deleteUserNameField.getText().trim();
        if (userName.isEmpty() || !Dialogs.confirm(this, "Do you want to delete this user?")) {
            return;
        }
        try {
            boolean deleted = adminDao.delete(userName);
            Dialogs.showInfo(this, deleted ? "User deleted" : "No user with this user name");
            deleteUserNameField.setText("");
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }//GEN-LAST:event_deleteUserButtonActionPerformed

    private void passwordPageCloseButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_passwordPageCloseButtonMouseClicked
        backToLogin();
    }//GEN-LAST:event_passwordPageCloseButtonMouseClicked

    private void menuPageCloseButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuPageCloseButtonMouseClicked
        backToLogin();
    }//GEN-LAST:event_menuPageCloseButtonMouseClicked

    private void addUserCloseButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addUserCloseButtonMouseClicked
        backToLogin();
    }//GEN-LAST:event_addUserCloseButtonMouseClicked

    private void deleteUserCloseButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteUserCloseButtonMouseClicked
        backToLogin();
    }//GEN-LAST:event_deleteUserCloseButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField masterPasswordField;
    private javax.swing.JButton addUserButton;
    private javax.swing.JButton deleteUserButton;
    private javax.swing.JPanel addUserPage;
    private javax.swing.JPanel passwordPage;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField passwordField;
    private javax.swing.JTextField userNameField;
    private javax.swing.JTextField deleteUserNameField;
    private javax.swing.JPanel deleteUserPage;
    private javax.swing.JButton enterButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel passwordPageCloseButton;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel deleteUserTile;
    private javax.swing.JLabel addUserTile;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel menuPageCloseButton;
    private javax.swing.JLabel deleteUserBackButton;
    private javax.swing.JLabel addUserBackButton;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel deleteUserCloseButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel addUserCloseButton;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JPanel menuPage;
    // End of variables declaration//GEN-END:variables
}
