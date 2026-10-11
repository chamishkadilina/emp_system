package empsystem.ui;

import com.itextpdf.text.DocumentException;
import empsystem.dao.AdminDao;
import empsystem.dao.AttendanceDao;
import empsystem.dao.EmployeeDao;
import empsystem.dao.LeaveDao;
import empsystem.model.Employee;
import empsystem.model.EmployeeLogin;
import empsystem.report.EmployeePdfReport;
import empsystem.util.Dialogs;
import empsystem.util.Images;
import empsystem.util.Validation;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main window shown after login. The side menu switches between the home, employees, edit,
 * attendance and leave pages; the edit page leads to the add, update and delete pages.
 */
public class HomeFrame extends javax.swing.JFrame {

    private static final ImageIcon ADD_PHOTO_ICON = Images.icon(Images.ADD_PHOTO);
    private static final ImageIcon DEFAULT_PHOTO_ICON = Images.icon(Images.DEFAULT_EMPLOYEE_PHOTO);

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final AdminDao adminDao = new AdminDao();
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final LeaveDao leaveDao = new LeaveDao();

    /** Photo chosen on the add-employee page, or null. */
    private byte[] newEmployeePhoto;

    public HomeFrame(String adminName) {
        initComponents();
        adminNameLabel.setText(adminName);
        exportPdfButton.setVisible(false);
        showHomePage();
        loadEmployees();
        loadPendingLeaves();
    }

    // ---------------------------------------------------------------- navigation

    /** Shows one page and highlights its side menu item. */
    private void showPage(JPanel page, JLabel activeMenuItem) {
        JPanel[] pages = {homePage, employeesPage, editMenuPage, addEmployeePage,
            updateEmployeePage, deleteEmployeePage, attendancePage, leavePage};
        for (JPanel p : pages) {
            p.setVisible(p == page);
        }
        JLabel[] activeMenuItems = {navHomeActive, navEmployeesActive, navEditActive,
            navAttendanceActive, navLeaveActive};
        for (JLabel item : activeMenuItems) {
            item.setVisible(item == activeMenuItem);
        }
    }

    private void showHomePage() {
        showPage(homePage, navHomeActive);
        try {
            totalEmployeesLabel.setText(String.valueOf(employeeDao.count(EmployeeDao.ALL_DEPARTMENTS)));
            totalAdminsLabel.setText(String.valueOf(adminDao.count()));
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void showEditPage(JPanel page) {
        showPage(page, navEditActive);
    }

    // ---------------------------------------------------------------- employees page

    /** Fills the table and the count for the department chosen in the filter. */
    private void loadEmployees() {
        String department = (String) departmentFilterCombo.getSelectedItem();
        try {
            employeeTable.setModel(employeeDao.list(department));
            departmentCountLabel.setText(String.valueOf(employeeDao.count(department)));
            employeePhotoLabel.setIcon(DEFAULT_PHOTO_ICON);
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    /** Filters the table; an exact employee ID also shows the photo and the PDF button. */
    private void searchEmployees() {
        String text = employeeSearchField.getText().trim();
        try {
            employeeTable.setModel(employeeDao.search(text));
            Optional<Employee> match = findEmployee(text);
            exportPdfButton.setVisible(match.isPresent());
            if (match.isPresent()) {
                departmentCountLabel.setText("");
            }
            employeePhotoLabel.setIcon(photoOf(match, DEFAULT_PHOTO_ICON));
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void showSelectedEmployeePhoto() {
        int row = employeeTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        try {
            String id = employeeTable.getValueAt(row, 0).toString();
            employeePhotoLabel.setIcon(photoOf(findEmployee(id), DEFAULT_PHOTO_ICON));
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void exportEmployeePdf() {
        String id = employeeSearchField.getText().trim();
        try {
            Optional<Employee> employee = findEmployee(id);
            if (employee.isEmpty()) {
                Dialogs.showInfo(this, "Employee not found");
                return;
            }
            Optional<EmployeeLogin> login = employeeDao.findLogin(id);

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save PDF");
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getPath() + ".pdf");
            }
            EmployeePdfReport.write(employee.get(), login.orElse(null), file);
            Dialogs.showInfo(this, "PDF saved successfully to " + file.getAbsolutePath());
        } catch (SQLException | IOException | DocumentException e) {
            Dialogs.showError(this, e);
        }
    }

    // ---------------------------------------------------------------- add / update / delete pages

    private void validateAddForm() {
        String error = formError(addFirstNameField.getText(), addLastNameField.getText(),
                addPositionField.getText(), addContactField.getText());
        addEmployeeError.setText(error);
        addEmployeeButton.setVisible(error == null);
    }

    private void validateUpdateForm() {
        boolean validId = Validation.isEmployeeId(updateEmpIdField.getText().trim());
        String error = validId
                ? formError(updateFirstNameField.getText(), updateLastNameField.getText(),
                        updatePositionField.getText(), updateContactField.getText())
                : "Please Enter Correct Employee ID";
        updateError.setText(error);
        findEmployeeButton.setVisible(validId);
        updateEmployeeButton.setVisible(error == null);
    }

    /** Returns the message for the first invalid field, or null when all are valid. */
    private static String formError(String firstName, String lastName, String position, String contact) {
        if (!Validation.isName(firstName) || !Validation.isName(lastName)) {
            return "Name must contain letters only";
        }
        if (!Validation.isName(position)) {
            return "Position must contain letters only";
        }
        if (!Validation.isContact(contact)) {
            return "Contact Number is Incorrect";
        }
        return null;
    }

    private void addEmployee() {
        if (addDobChooser.getDate() == null) {
            Dialogs.showInfo(this, "Please select the date of birth");
            return;
        }
        Employee employee = new Employee(null,
                addFirstNameField.getText(),
                addLastNameField.getText(),
                (String) addGenderCombo.getSelectedItem(),
                addContactField.getText(),
                toLocalDate(addDobChooser.getDate()),
                addPositionField.getText(),
                (String) addDepartmentCombo.getSelectedItem(),
                newEmployeePhoto);
        try {
            employeeDao.add(employee);
            clearAddForm();
            Dialogs.showInfo(this, "Employee added successfully");
            showEditPage(editMenuPage);
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void clearAddForm() {
        addFirstNameField.setText("");
        addLastNameField.setText("");
        addContactField.setText("");
        addPositionField.setText("");
        addDobChooser.setDate(null);
        addPhotoLabel.setIcon(ADD_PHOTO_ICON);
        addEmployeeError.setText(null);
        addEmployeeButton.setVisible(true);
        newEmployeePhoto = null;
    }

    private void chooseNewEmployeePhoto() {
        File file = choosePhotoFile();
        if (file == null) {
            return;
        }
        try {
            newEmployeePhoto = Images.readFile(file);
            addPhotoLabel.setIcon(Images.fromBytes(newEmployeePhoto, ADD_PHOTO_ICON));
        } catch (IOException e) {
            Dialogs.showError(this, e);
        }
    }

    /** Loads the employee with the entered ID into the update form. */
    private void loadEmployeeForUpdate() {
        String id = updateEmpIdField.getText().trim();
        try {
            Optional<Employee> found = findEmployee(id);
            if (found.isEmpty()) {
                Dialogs.showInfo(this, "Employee not found");
                return;
            }
            Employee employee = found.get();
            updateFirstNameField.setText(employee.firstName());
            updateLastNameField.setText(employee.lastName());
            updateContactField.setText(employee.contact());
            updatePositionField.setText(employee.position());
            updateGenderCombo.setSelectedItem(employee.gender());
            updateDepartmentCombo.setSelectedItem(employee.department());
            updateDobChooser.setDate(toDate(employee.dateOfBirth()));
            updatePhotoLabel.setIcon(photoOf(found, ADD_PHOTO_ICON));
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void updateEmployee() {
        String id = updateEmpIdField.getText().trim();
        if (id.isEmpty()) {
            Dialogs.showInfo(this, "Please enter an employee ID");
            return;
        }
        Employee employee = new Employee(id,
                updateFirstNameField.getText(),
                updateLastNameField.getText(),
                (String) updateGenderCombo.getSelectedItem(),
                updateContactField.getText(),
                toLocalDate(updateDobChooser.getDate()),
                updatePositionField.getText(),
                (String) updateDepartmentCombo.getSelectedItem(),
                null);
        try {
            if (!employeeDao.update(employee)) {
                Dialogs.showInfo(this, "Employee not found");
                return;
            }
            Dialogs.showInfo(this, "Employee updated successfully");
            clearUpdateForm();
            showEditPage(editMenuPage);
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void clearUpdateForm() {
        updateEmpIdField.setText("");
        updateFirstNameField.setText("");
        updateLastNameField.setText("");
        updateContactField.setText("");
        updatePositionField.setText("");
        updateDobChooser.setDate(null);
        updatePhotoLabel.setIcon(ADD_PHOTO_ICON);
        updateError.setText(null);
        findEmployeeButton.setVisible(true);
        updateEmployeeButton.setVisible(true);
    }

    /** Asks for a new photo for the employee in the update form and saves it straight away. */
    private void changeEmployeePhoto() {
        String id = updateEmpIdField.getText().trim();
        if (id.isEmpty()) {
            Dialogs.showInfo(this, "Please enter an employee ID");
            return;
        }
        File file = choosePhotoFile();
        if (file == null) {
            return;
        }
        try {
            byte[] photo = Images.readFile(file);
            updatePhotoLabel.setIcon(Images.fromBytes(photo, ADD_PHOTO_ICON));
            if (!Dialogs.confirm(this, "Do you want to update this photo?")) {
                return;
            }
            employeeDao.updatePhoto(id, photo);
            Dialogs.showInfo(this, "Photo updated");
            showEditPage(editMenuPage);
        } catch (IOException | SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void validateDeleteForm() {
        boolean validId = Validation.isEmployeeId(deleteEmpIdField.getText().trim());
        deleteError.setText(validId ? null : "Please Enter Correct Employee ID");
        deleteEmployeeButton.setVisible(validId);
    }

    private void deleteEmployee() {
        String id = deleteEmpIdField.getText().trim();
        if (id.isEmpty() || !Dialogs.confirm(this, "Do you want to delete this employee?")) {
            return;
        }
        try {
            boolean deleted = employeeDao.delete(id);
            Dialogs.showInfo(this, deleted ? "Employee deleted" : "Employee not found");
            deleteEmpIdField.setText("");
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    // ---------------------------------------------------------------- attendance page

    private void loadAttendance() {
        String id = attendanceEmpIdField.getText().trim();
        boolean validId = Validation.isEmployeeId(id);
        attendanceError.setText(validId ? null : "Incorrect Employee ID");
        attendanceDatePanel.setVisible(validId);
        if (!validId) {
            attendanceEmployeeName.setText(null);
            return;
        }
        AttendanceDao.Range range = AttendanceDao.Range.values()[attendanceRangeCombo.getSelectedIndex()];
        try {
            attendanceInTable.setModel(attendanceDao.checkIns(id, range));
            attendanceOutTable.setModel(attendanceDao.checkOuts(id, range));
            attendanceEmployeeName.setText(findEmployee(id).map(Employee::fullName).orElse(null));
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void loadAttendanceOnDate() {
        String id = attendanceEmpIdField.getText().trim();
        String date = attendanceDateField.getText().trim();
        try {
            attendanceInTable.setModel(attendanceDao.checkInsOn(id, date));
            attendanceOutTable.setModel(attendanceDao.checkOutsOn(id, date));
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    // ---------------------------------------------------------------- leave page

    private void loadPendingLeaves() {
        try {
            leaveTable.setModel(leaveDao.listPending());
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void clearLeaveDetails() {
        leaveEmpIdLabel.setText("");
        leaveEmployeeName.setText("");
        leaveCommentArea.setText("");
        leaveDatesLabel.setText("");
    }

    private void showSelectedLeave() {
        int row = leaveTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        String id = leaveTable.getValueAt(row, 0).toString();
        String startDate = leaveTable.getValueAt(row, 2).toString();
        String endDate = leaveTable.getValueAt(row, 3).toString();
        try {
            leaveEmpIdLabel.setText(id);
            leaveEmployeeName.setText(findEmployee(id).map(Employee::fullName).orElse(""));
            leaveCommentArea.setText(leaveDao.findComment(id, startDate, endDate).orElse(""));
            leaveDatesLabel.setText(startDate + " to " + endDate);
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    private void decideSelectedLeave(boolean accept) {
        int row = leaveTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showInfo(this, "Please select a leave request first");
            return;
        }
        String question = accept ? "Do you want to accept this leave?" : "Do you want to reject this leave?";
        if (!Dialogs.confirm(this, question)) {
            return;
        }
        String id = leaveTable.getValueAt(row, 0).toString();
        String startDate = leaveTable.getValueAt(row, 2).toString();
        String endDate = leaveTable.getValueAt(row, 3).toString();
        try {
            leaveDao.decide(id, startDate, endDate, accept);
            loadPendingLeaves();
            clearLeaveDetails();
        } catch (SQLException e) {
            Dialogs.showError(this, e);
        }
    }

    // ---------------------------------------------------------------- helpers

    /** Looks up an employee only when {@code id} looks like an employee ID. */
    private Optional<Employee> findEmployee(String id) throws SQLException {
        if (id.isEmpty() || !Validation.isEmployeeId(id)) {
            return Optional.empty();
        }
        return employeeDao.findById(id);
    }

    private static ImageIcon photoOf(Optional<Employee> employee, ImageIcon fallback) {
        return Images.fromBytes(employee.map(Employee::photo).orElse(null), fallback);
    }

    /** Returns the chosen image file, or null when the user cancels. */
    private File choosePhotoFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "bmp"));
        return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private static void allowDigitsOnly(KeyEvent evt) {
        if (!Character.isDigit(evt.getKeyChar())) {
            evt.consume();
        }
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate date) {
        return date == null ? null : Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        sidePanel = new javax.swing.JPanel();
        welcome = new javax.swing.JLabel();
        photo = new javax.swing.JLabel();
        navEmployees = new javax.swing.JLabel();
        navEmployeesActive = new javax.swing.JLabel();
        navAttendance = new javax.swing.JLabel();
        navAttendanceActive = new javax.swing.JLabel();
        navEdit = new javax.swing.JLabel();
        navEditActive = new javax.swing.JLabel();
        navHome = new javax.swing.JLabel();
        navHomeActive = new javax.swing.JLabel();
        navLeave = new javax.swing.JLabel();
        navLeaveActive = new javax.swing.JLabel();
        logoutButton = new javax.swing.JLabel();
        adminNameLabel = new javax.swing.JLabel();
        blue = new javax.swing.JLabel();
        headerPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        closeButton = new javax.swing.JLabel();
        minimizeButton = new javax.swing.JLabel();
        contentPanel = new javax.swing.JPanel();
        homePage = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        totalAdminsLabel = new javax.swing.JLabel();
        totalEmployeesLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        adminSettingsButton = new javax.swing.JLabel();
        employeesPage = new javax.swing.JPanel();
        departmentFilterCombo = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        employeeTable = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        departmentCountLabel = new javax.swing.JLabel();
        employeeSearchField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        employeePhotoLabel = new javax.swing.JLabel();
        exportPdfButton = new javax.swing.JButton();
        editMenuPage = new javax.swing.JPanel();
        addEmployeeTile = new javax.swing.JLabel();
        updateEmployeeTile = new javax.swing.JLabel();
        deleteEmployeeTile = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        addEmployeePage = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        addFirstNameField = new javax.swing.JTextField();
        addLastNameField = new javax.swing.JTextField();
        addContactField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        clearAddFormButton = new javax.swing.JButton();
        addEmployeeButton = new javax.swing.JButton();
        addPhotoLabel = new javax.swing.JLabel();
        addChoosePhotoButton = new javax.swing.JButton();
        addGenderCombo = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        addPositionField = new javax.swing.JTextField();
        addDepartmentCombo = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        addDobChooser = new com.toedter.calendar.JDateChooser();
        jLabel13 = new javax.swing.JLabel();
        addBackButton = new javax.swing.JLabel();
        addEmployeeError = new javax.swing.JLabel();
        deleteEmployeePage = new javax.swing.JPanel();
        deleteEmpIdField = new javax.swing.JTextField();
        deleteBackButton = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        deleteEmployeeButton = new javax.swing.JButton();
        deleteError = new javax.swing.JLabel();
        updateEmployeePage = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        updateDobChooser = new com.toedter.calendar.JDateChooser();
        updateDepartmentCombo = new javax.swing.JComboBox<>();
        updatePositionField = new javax.swing.JTextField();
        updatePhotoLabel = new javax.swing.JLabel();
        updateChoosePhotoButton = new javax.swing.JButton();
        updateContactField = new javax.swing.JTextField();
        updateGenderCombo = new javax.swing.JComboBox<>();
        updateLastNameField = new javax.swing.JTextField();
        updateFirstNameField = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        updateBackButton = new javax.swing.JLabel();
        updateEmployeeButton = new javax.swing.JButton();
        clearUpdateFormButton = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        updateEmpIdField = new javax.swing.JTextField();
        findEmployeeButton = new javax.swing.JButton();
        updateError = new javax.swing.JLabel();
        attendancePage = new javax.swing.JPanel();
        attendanceEmpIdField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        attendanceOutTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        attendanceInTable = new javax.swing.JTable();
        attendanceEmployeeName = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        attendanceRangeCombo = new javax.swing.JComboBox<>();
        attendanceDatePanel = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        attendanceDateField = new javax.swing.JTextField();
        attendanceDateSearchButton = new javax.swing.JButton();
        attendanceError = new javax.swing.JLabel();
        leavePage = new javax.swing.JPanel();
        jLabel42 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        leaveTable = new javax.swing.JTable();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        leaveEmpIdLabel = new javax.swing.JLabel();
        leaveEmployeeName = new javax.swing.JLabel();
        leaveDatesLabel = new javax.swing.JLabel();
        acceptLeaveButton = new javax.swing.JLabel();
        rejectLeaveButton = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        leaveCommentArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        sidePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        welcome.setBackground(new java.awt.Color(255, 255, 255));
        welcome.setFont(new java.awt.Font("Apex New Bold", 1, 36)); // NOI18N
        welcome.setForeground(new java.awt.Color(255, 255, 255));
        welcome.setText("Welcome");
        sidePanel.add(welcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 200, 160, 50));

        photo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/home/person_.png"))); // NOI18N
        sidePanel.add(photo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 160, 170));

        navEmployees.setBackground(new java.awt.Color(204, 204, 204));
        navEmployees.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        navEmployees.setForeground(new java.awt.Color(255, 255, 255));
        navEmployees.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navEmployees.setText("Employees");
        navEmployees.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navEmployeesMouseClicked(evt);
            }
        });
        sidePanel.add(navEmployees, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 355, 220, 40));

        navEmployeesActive.setBackground(new java.awt.Color(204, 204, 204));
        navEmployeesActive.setFont(new java.awt.Font("Apex New Bold", 0, 12)); // NOI18N
        navEmployeesActive.setForeground(new java.awt.Color(255, 255, 255));
        navEmployeesActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navEmployeesActive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/navigation/side_button.png"))); // NOI18N
        sidePanel.add(navEmployeesActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 355, 220, 40));

        navAttendance.setBackground(new java.awt.Color(255, 255, 255));
        navAttendance.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        navAttendance.setForeground(new java.awt.Color(255, 255, 255));
        navAttendance.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navAttendance.setText("View Attendance");
        navAttendance.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navAttendanceMouseClicked(evt);
            }
        });
        sidePanel.add(navAttendance, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 453, 220, 40));

        navAttendanceActive.setBackground(new java.awt.Color(255, 255, 255));
        navAttendanceActive.setFont(new java.awt.Font("Apex New Bold", 0, 12)); // NOI18N
        navAttendanceActive.setForeground(new java.awt.Color(255, 255, 255));
        navAttendanceActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navAttendanceActive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/navigation/side_button.png"))); // NOI18N
        sidePanel.add(navAttendanceActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 453, 220, 40));

        navEdit.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        navEdit.setForeground(new java.awt.Color(255, 255, 255));
        navEdit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navEdit.setText("Edit Employees");
        navEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navEditMouseClicked(evt);
            }
        });
        sidePanel.add(navEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 405, 220, 40));

        navEditActive.setFont(new java.awt.Font("Apex New Bold", 0, 12)); // NOI18N
        navEditActive.setForeground(new java.awt.Color(255, 255, 255));
        navEditActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navEditActive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/navigation/side_button.png"))); // NOI18N
        sidePanel.add(navEditActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 405, 220, 40));

        navHome.setBackground(new java.awt.Color(204, 204, 204));
        navHome.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        navHome.setForeground(new java.awt.Color(255, 255, 255));
        navHome.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navHome.setText("Home");
        navHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navHomeMouseClicked(evt);
            }
        });
        sidePanel.add(navHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 305, 220, 40));

        navHomeActive.setBackground(new java.awt.Color(204, 204, 204));
        navHomeActive.setFont(new java.awt.Font("Apex New Bold", 0, 12)); // NOI18N
        navHomeActive.setForeground(new java.awt.Color(255, 255, 255));
        navHomeActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navHomeActive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/navigation/side_button.png"))); // NOI18N
        sidePanel.add(navHomeActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 305, 220, 40));

        navLeave.setBackground(new java.awt.Color(204, 204, 204));
        navLeave.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        navLeave.setForeground(new java.awt.Color(255, 255, 255));
        navLeave.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navLeave.setText("Leave");
        navLeave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navLeaveMouseClicked(evt);
            }
        });
        sidePanel.add(navLeave, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 500, 220, 40));

        navLeaveActive.setBackground(new java.awt.Color(204, 204, 204));
        navLeaveActive.setFont(new java.awt.Font("Apex New Bold", 0, 12)); // NOI18N
        navLeaveActive.setForeground(new java.awt.Color(255, 255, 255));
        navLeaveActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        navLeaveActive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/navigation/side_button.png"))); // NOI18N
        sidePanel.add(navLeaveActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 500, 220, 40));

        logoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/LOGOUT.png"))); // NOI18N
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutButtonMouseClicked(evt);
            }
        });
        sidePanel.add(logoutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 570, 40, 40));

        adminNameLabel.setBackground(new java.awt.Color(0, 0, 0));
        adminNameLabel.setFont(new java.awt.Font("Apex New Medium", 0, 24)); // NOI18N
        adminNameLabel.setForeground(new java.awt.Color(255, 255, 255));
        adminNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        sidePanel.add(adminNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 240, 170, 30));

        blue.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/navigation/sidepane_.png"))); // NOI18N
        sidePanel.add(blue, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 630));

        jPanel1.add(sidePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 52, 250, 630));

        headerPanel.setBackground(new java.awt.Color(204, 204, 204));
        headerPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/emp_U.png"))); // NOI18N
        jLabel7.setText("jLabel7");
        headerPanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 50, 50));

        jLabel8.setFont(new java.awt.Font("Apex New Heavy", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Employee Management System");
        headerPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, 480, 30));

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/close.png"))); // NOI18N
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeButtonMouseClicked(evt);
            }
        });
        headerPanel.add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 0, 50, 50));

        minimizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/minimize.png"))); // NOI18N
        minimizeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minimizeButtonMouseClicked(evt);
            }
        });
        headerPanel.add(minimizeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1005, 0, 50, 50));

        jPanel1.add(headerPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, 1110, 50));

        contentPanel.setBackground(new java.awt.Color(255, 255, 255));
        contentPanel.setLayout(new java.awt.CardLayout());

        homePage.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        totalAdminsLabel.setBackground(new java.awt.Color(0, 0, 0));
        totalAdminsLabel.setFont(new java.awt.Font("Franklin Gothic Heavy", 1, 70)); // NOI18N
        totalAdminsLabel.setForeground(new java.awt.Color(0, 0, 0));
        totalAdminsLabel.setText("3");
        jPanel2.add(totalAdminsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 60, 120, 60));

        totalEmployeesLabel.setBackground(new java.awt.Color(0, 0, 0));
        totalEmployeesLabel.setFont(new java.awt.Font("Franklin Gothic Heavy", 1, 70)); // NOI18N
        totalEmployeesLabel.setForeground(new java.awt.Color(0, 0, 0));
        totalEmployeesLabel.setText("3");
        jPanel2.add(totalEmployeesLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 50, 130, 70));

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/home/to_.png"))); // NOI18N
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 360, 180));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/home/total_u.png"))); // NOI18N
        jPanel2.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 20, 360, 180));

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/home/metro_logo.png"))); // NOI18N

        adminSettingsButton.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        adminSettingsButton.setForeground(new java.awt.Color(255, 102, 102));
        adminSettingsButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        adminSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/home/settings.png"))); // NOI18N
        adminSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                adminSettingsButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout homePageLayout = new javax.swing.GroupLayout(homePage);
        homePage.setLayout(homePageLayout);
        homePageLayout.setHorizontalGroup(
            homePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, homePageLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(homePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(26, 26, 26))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, homePageLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(adminSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        homePageLayout.setVerticalGroup(
            homePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePageLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(adminSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        contentPanel.add(homePage, "card5");

        employeesPage.setBackground(new java.awt.Color(255, 255, 255));

        departmentFilterCombo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        departmentFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL", "HR", "IT", "SALARY", "SUPPLY" }));
        departmentFilterCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                departmentFilterComboActionPerformed(evt);
            }
        });

        employeeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "First Name", "Last Name", "Gender", "Contact No", "DOB", "Position", "Department"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        employeeTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        employeeTable.setName(""); // NOI18N
        employeeTable.setRowHeight(25);
        employeeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                employeeTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(employeeTable);
        if (employeeTable.getColumnModel().getColumnCount() > 0) {
            employeeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            employeeTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        }

        jLabel14.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        jLabel14.setText("Total Employees :");

        departmentCountLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        departmentCountLabel.setText("100");

        employeeSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                employeeSearchFieldKeyReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Apex New Heavy", 0, 18)); // NOI18N
        jLabel1.setText("Search");

        employeePhotoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/employee/normal_emp.png"))); // NOI18N
        employeePhotoLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        exportPdfButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        exportPdfButton.setText("Save As  PDF");
        exportPdfButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPdfButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout employeesPageLayout = new javax.swing.GroupLayout(employeesPage);
        employeesPage.setLayout(employeesPageLayout);
        employeesPageLayout.setHorizontalGroup(
            employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(employeesPageLayout.createSequentialGroup()
                .addGroup(employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(employeesPageLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 830, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(employeesPageLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(employeesPageLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(employeeSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(employeesPageLayout.createSequentialGroup()
                                .addComponent(departmentFilterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(departmentCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(38, 38, 38)
                        .addComponent(employeePhotoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(exportPdfButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        employeesPageLayout.setVerticalGroup(
            employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(employeesPageLayout.createSequentialGroup()
                .addGroup(employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(employeesPageLayout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addGroup(employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(departmentFilterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(departmentCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addGroup(employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(employeeSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, employeesPageLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(employeesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, employeesPageLayout.createSequentialGroup()
                                .addComponent(employeePhotoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, employeesPageLayout.createSequentialGroup()
                                .addComponent(exportPdfButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(80, 80, 80)))))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
        );

        contentPanel.add(employeesPage, "card2");

        editMenuPage.setBackground(new java.awt.Color(255, 255, 255));
        editMenuPage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addEmployeeTile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/edit_employee/P_ADD.png"))); // NOI18N
        addEmployeeTile.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addEmployeeTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addEmployeeTileMouseClicked(evt);
            }
        });
        editMenuPage.add(addEmployeeTile, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 210, -1, 180));

        updateEmployeeTile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/edit_employee/p_u.png"))); // NOI18N
        updateEmployeeTile.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        updateEmployeeTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateEmployeeTileMouseClicked(evt);
            }
        });
        editMenuPage.add(updateEmployeeTile, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 210, -1, 180));

        deleteEmployeeTile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/edit_employee/p_d.png"))); // NOI18N
        deleteEmployeeTile.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        deleteEmployeeTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteEmployeeTileMouseClicked(evt);
            }
        });
        editMenuPage.add(deleteEmployeeTile, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 210, -1, 180));

        jLabel22.setFont(new java.awt.Font("Apex New Heavy", 0, 18)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("ADD  EMPLOYEE");
        editMenuPage.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 400, 164, 37));

        jLabel23.setFont(new java.awt.Font("Apex New Heavy", 0, 18)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("REMOVE  EMPLOYEE");
        editMenuPage.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 400, 179, 37));

        jLabel24.setFont(new java.awt.Font("Apex New Heavy", 0, 18)); // NOI18N
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("UPDATE  EMPLOYEE");
        editMenuPage.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 400, 180, 37));

        jLabel26.setFont(new java.awt.Font("Apex New Bold", 1, 60)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("EDIT EMPLOYEES");
        editMenuPage.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 70, 550, 60));

        contentPanel.add(editMenuPage, "card6");

        addEmployeePage.setBackground(new java.awt.Color(255, 255, 255));
        addEmployeePage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("First Name");
        addEmployeePage.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Last Name");
        addEmployeePage.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("Gender");
        addEmployeePage.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 200, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Contact No");
        addEmployeePage.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 260, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Date of Birth");
        addEmployeePage.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 100, -1, -1));

        addFirstNameField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addFirstNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                addFirstNameFieldKeyReleased(evt);
            }
        });
        addEmployeePage.add(addFirstNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 100, 240, 33));

        addLastNameField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addLastNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                addLastNameFieldKeyReleased(evt);
            }
        });
        addEmployeePage.add(addLastNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 150, 240, 33));

        addContactField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addContactField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                addContactFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                addContactFieldKeyTyped(evt);
            }
        });
        addEmployeePage.add(addContactField, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 260, 240, 33));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Department");
        addEmployeePage.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 200, -1, -1));

        clearAddFormButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        clearAddFormButton.setText("Clear");
        clearAddFormButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAddFormButtonActionPerformed(evt);
            }
        });
        addEmployeePage.add(clearAddFormButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 470, 130, 40));

        addEmployeeButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addEmployeeButton.setText("Add");
        addEmployeeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEmployeeButtonActionPerformed(evt);
            }
        });
        addEmployeePage.add(addEmployeeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 470, 130, 40));

        addPhotoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/edit_employee/add.png"))); // NOI18N
        addPhotoLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addEmployeePage.add(addPhotoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 270, 120, 150));

        addChoosePhotoButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addChoosePhotoButton.setText("Select");
        addChoosePhotoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChoosePhotoButtonActionPerformed(evt);
            }
        });
        addEmployeePage.add(addChoosePhotoButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 430, 120, 40));

        addGenderCombo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addGenderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        addEmployeePage.add(addGenderCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 200, 240, 33));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("Position");
        addEmployeePage.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 150, -1, -1));

        addPositionField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addPositionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                addPositionFieldKeyReleased(evt);
            }
        });
        addEmployeePage.add(addPositionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 150, 240, 33));

        addDepartmentCombo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addDepartmentCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "HR", "IT", "SALARY", "SUPPLY" }));
        addEmployeePage.add(addDepartmentCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 200, 240, 33));
        addEmployeePage.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(88, 226, 127, -1));

        addDobChooser.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addEmployeePage.add(addDobChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 100, 240, 33));

        jLabel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        addEmployeePage.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 440, 370, 100));

        addBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/back.png"))); // NOI18N
        addBackButton.setText("jLabel25");
        addBackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addBackButtonMouseClicked(evt);
            }
        });
        addEmployeePage.add(addBackButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 40));

        addEmployeeError.setBackground(new java.awt.Color(255, 51, 51));
        addEmployeeError.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        addEmployeeError.setForeground(new java.awt.Color(255, 102, 102));
        addEmployeePage.add(addEmployeeError, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 390, 420, 40));

        contentPanel.add(addEmployeePage, "card3");

        deleteEmployeePage.setBackground(new java.awt.Color(255, 255, 255));
        deleteEmployeePage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        deleteEmpIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                deleteEmpIdFieldKeyReleased(evt);
            }
        });
        deleteEmployeePage.add(deleteEmpIdField, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 280, 254, 38));

        deleteBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/back.png"))); // NOI18N
        deleteBackButton.setText("jLabel25");
        deleteBackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteBackButtonMouseClicked(evt);
            }
        });
        deleteEmployeePage.add(deleteBackButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 40));

        jLabel27.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(0, 0, 0));
        jLabel27.setText("Employee ID");
        deleteEmployeePage.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 290, -1, -1));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        deleteEmployeeButton.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        deleteEmployeeButton.setText("Delete");
        deleteEmployeeButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        deleteEmployeeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEmployeeButtonActionPerformed(evt);
            }
        });

        deleteError.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        deleteError.setForeground(new java.awt.Color(255, 102, 102));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(60, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(deleteError, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteEmployeeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(97, 97, 97))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(161, Short.MAX_VALUE)
                .addComponent(deleteError, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(deleteEmployeeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );

        deleteEmployeePage.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 170, 520, 290));

        contentPanel.add(deleteEmployeePage, "card7");

        updateEmployeePage.setBackground(new java.awt.Color(255, 255, 255));
        updateEmployeePage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel28.setText("Department");
        updateEmployeePage.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 290, -1, -1));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel29.setText("Position");
        updateEmployeePage.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 240, -1, -1));

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setText("Date of Birth");
        updateEmployeePage.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 190, -1, -1));

        updateDobChooser.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateEmployeePage.add(updateDobChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 190, 240, 33));

        updateDepartmentCombo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateDepartmentCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "HR", "IT", "SALARY", "SUPPLY" }));
        updateEmployeePage.add(updateDepartmentCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 290, 240, 33));

        updatePositionField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updatePositionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updatePositionFieldKeyReleased(evt);
            }
        });
        updateEmployeePage.add(updatePositionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 240, 240, 33));

        updatePhotoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/edit_employee/add.png"))); // NOI18N
        updatePhotoLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        updateEmployeePage.add(updatePhotoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(657, 360, 120, 150));

        updateChoosePhotoButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        updateChoosePhotoButton.setText("Update Photo");
        updateChoosePhotoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateChoosePhotoButtonActionPerformed(evt);
            }
        });
        updateEmployeePage.add(updateChoosePhotoButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 520, 150, 40));

        updateContactField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateContactField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateContactFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                updateContactFieldKeyTyped(evt);
            }
        });
        updateEmployeePage.add(updateContactField, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 350, 240, 33));

        updateGenderCombo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateGenderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        updateEmployeePage.add(updateGenderCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 290, 240, 33));

        updateLastNameField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateLastNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateLastNameFieldKeyReleased(evt);
            }
        });
        updateEmployeePage.add(updateLastNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 240, 240, 33));

        updateFirstNameField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        updateFirstNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateFirstNameFieldKeyReleased(evt);
            }
        });
        updateEmployeePage.add(updateFirstNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, 240, 33));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel31.setText("First Name");
        updateEmployeePage.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 190, -1, -1));

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel32.setText("Last Name");
        updateEmployeePage.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 240, -1, -1));

        jLabel33.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel33.setText("Gender");
        updateEmployeePage.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 290, -1, -1));

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel34.setText("Contact No");
        updateEmployeePage.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 350, -1, -1));

        updateBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/common/back.png"))); // NOI18N
        updateBackButton.setText("jLabel25");
        updateBackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateBackButtonMouseClicked(evt);
            }
        });
        updateEmployeePage.add(updateBackButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 40));

        updateEmployeeButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        updateEmployeeButton.setText("Update");
        updateEmployeeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEmployeeButtonActionPerformed(evt);
            }
        });
        updateEmployeePage.add(updateEmployeeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 500, 130, 40));

        clearUpdateFormButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        clearUpdateFormButton.setText("Clear");
        clearUpdateFormButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearUpdateFormButtonActionPerformed(evt);
            }
        });
        updateEmployeePage.add(clearUpdateFormButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 500, 130, 40));

        jLabel37.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        updateEmployeePage.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 470, 370, 100));

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel36.setText("Employee ID");
        updateEmployeePage.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 70, 120, 40));

        updateEmpIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateEmpIdFieldKeyReleased(evt);
            }
        });
        updateEmployeePage.add(updateEmpIdField, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 80, 240, 33));

        findEmployeeButton.setText("Find");
        findEmployeeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        findEmployeeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findEmployeeButtonActionPerformed(evt);
            }
        });
        updateEmployeePage.add(findEmployeeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 83, 90, 30));

        updateError.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        updateError.setForeground(new java.awt.Color(255, 102, 102));
        updateEmployeePage.add(updateError, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 426, 310, 30));

        contentPanel.add(updateEmployeePage, "card8");

        attendancePage.setBackground(new java.awt.Color(255, 255, 255));

        attendanceEmpIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                attendanceEmpIdFieldKeyReleased(evt);
            }
        });

        attendanceOutTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        attendanceOutTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Attendance_Out"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        attendanceOutTable.setRowHeight(40);
        jScrollPane2.setViewportView(attendanceOutTable);

        attendanceInTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        attendanceInTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Attendence_in"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        attendanceInTable.setRowHeight(40);
        jScrollPane4.setViewportView(attendanceInTable);

        attendanceEmployeeName.setBackground(new java.awt.Color(0, 0, 0));
        attendanceEmployeeName.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel39.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel39.setText("Employee  :");

        jLabel40.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/attendance/in.png"))); // NOI18N
        jLabel40.setText("Clock IN");

        jLabel41.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/attendance/out.png"))); // NOI18N
        jLabel41.setText("Clock");

        attendanceRangeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Last Week", "Last Month", "All" }));
        attendanceRangeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendanceRangeComboActionPerformed(evt);
            }
        });

        jLabel38.setText("Date :");

        attendanceDateSearchButton.setText("Search");
        attendanceDateSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendanceDateSearchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout attendanceDatePanelLayout = new javax.swing.GroupLayout(attendanceDatePanel);
        attendanceDatePanel.setLayout(attendanceDatePanelLayout);
        attendanceDatePanelLayout.setHorizontalGroup(
            attendanceDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attendanceDatePanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(attendanceDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attendanceDateSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(attendanceDatePanelLayout.createSequentialGroup()
                        .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attendanceDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        attendanceDatePanelLayout.setVerticalGroup(
            attendanceDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attendanceDatePanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(attendanceDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attendanceDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(attendanceDateSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        attendanceError.setFont(new java.awt.Font("Apex New Bold", 0, 18)); // NOI18N
        attendanceError.setForeground(new java.awt.Color(255, 102, 102));

        javax.swing.GroupLayout attendancePageLayout = new javax.swing.GroupLayout(attendancePage);
        attendancePage.setLayout(attendancePageLayout);
        attendancePageLayout.setHorizontalGroup(
            attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attendancePageLayout.createSequentialGroup()
                .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attendancePageLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel39)
                        .addGap(18, 18, 18)
                        .addComponent(attendanceEmployeeName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attendancePageLayout.createSequentialGroup()
                        .addContainerGap(17, Short.MAX_VALUE)
                        .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(attendancePageLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(attendanceError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(attendanceDatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
            .addGroup(attendancePageLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attendancePageLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(132, 132, 132)
                        .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(81, 81, 81))
                    .addGroup(attendancePageLayout.createSequentialGroup()
                        .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attendanceEmpIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attendanceRangeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        attendancePageLayout.setVerticalGroup(
            attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attendancePageLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(attendanceEmpIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attendancePageLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(attendancePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(attendancePageLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(attendanceEmployeeName, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, attendancePageLayout.createSequentialGroup()
                            .addGap(34, 34, 34)
                            .addComponent(jLabel39))))
                .addGap(52, 52, 52))
            .addGroup(attendancePageLayout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addComponent(attendanceRangeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(141, 141, 141)
                .addComponent(attendanceDatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(attendanceError, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contentPanel.add(attendancePage, "card4");

        leavePage.setBackground(new java.awt.Color(255, 255, 255));

        jLabel42.setFont(new java.awt.Font("Apex New Bold", 0, 24)); // NOI18N
        jLabel42.setText("Employees Leaves");

        leaveTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Emp ID", "Reason", "Start Date", "End Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        leaveTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                leaveTableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(leaveTable);

        jLabel43.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        jLabel43.setText("Employee ID:");

        jLabel44.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        jLabel44.setText("Employee Name:");

        jLabel45.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        jLabel45.setText("Comment :");

        jLabel46.setFont(new java.awt.Font("Apex New Bold", 1, 14)); // NOI18N
        jLabel46.setText("Date :");

        leaveEmpIdLabel.setFont(new java.awt.Font("Apex New Medium", 0, 14)); // NOI18N

        leaveEmployeeName.setFont(new java.awt.Font("Apex New Medium", 0, 14)); // NOI18N

        leaveDatesLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        acceptLeaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/leave/accept.png"))); // NOI18N
        acceptLeaveButton.setText("jLabel51");
        acceptLeaveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                acceptLeaveButtonMouseClicked(evt);
            }
        });

        rejectLeaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empsystem/resources/icons/leave/reject.png"))); // NOI18N
        rejectLeaveButton.setText("jLabel53");
        rejectLeaveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rejectLeaveButtonMouseClicked(evt);
            }
        });

        leaveCommentArea.setColumns(20);
        leaveCommentArea.setRows(5);
        jScrollPane6.setViewportView(leaveCommentArea);

        javax.swing.GroupLayout leavePageLayout = new javax.swing.GroupLayout(leavePage);
        leavePage.setLayout(leavePageLayout);
        leavePageLayout.setHorizontalGroup(
            leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leavePageLayout.createSequentialGroup()
                .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(leavePageLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(leavePageLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(leavePageLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(acceptLeaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(rejectLeaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(leavePageLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel45, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(leavePageLayout.createSequentialGroup()
                                .addComponent(jLabel46, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(64, 64, 64)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(leaveEmpIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(leaveEmployeeName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(leavePageLayout.createSequentialGroup()
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leavePageLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(leaveDatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(21, 21, 21))
        );
        leavePageLayout.setVerticalGroup(
            leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leavePageLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(leavePageLayout.createSequentialGroup()
                        .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(leavePageLayout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel43)
                            .addComponent(leaveEmpIdLabel))
                        .addGap(18, 18, 18)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel44)
                            .addComponent(leaveEmployeeName))
                        .addGap(18, 18, 18)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel45)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(46, 46, 46)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel46)
                            .addComponent(leaveDatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addGroup(leavePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(acceptLeaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rejectLeaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(99, Short.MAX_VALUE))
        );

        contentPanel.add(leavePage, "card4");

        jPanel1.add(contentPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(251, 56, 860, 630));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 681, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // ---------------------------------------------------------------- header and side menu

    private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMouseClicked
        System.exit(0);
    }//GEN-LAST:event_closeButtonMouseClicked

    private void minimizeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeButtonMouseClicked
        setExtendedState(ICONIFIED);
    }//GEN-LAST:event_minimizeButtonMouseClicked

    private void logoutButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseClicked
        new LoginFrame().setVisible(true);
        dispose();
    }//GEN-LAST:event_logoutButtonMouseClicked

    private void adminSettingsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_adminSettingsButtonMouseClicked
        new AdminUsersFrame().setVisible(true);
        dispose();
    }//GEN-LAST:event_adminSettingsButtonMouseClicked

    private void navHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navHomeMouseClicked
        showHomePage();
    }//GEN-LAST:event_navHomeMouseClicked

    private void navEmployeesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navEmployeesMouseClicked
        showPage(employeesPage, navEmployeesActive);
        employeeSearchField.setText("");
        exportPdfButton.setVisible(false);
        loadEmployees();
    }//GEN-LAST:event_navEmployeesMouseClicked

    private void navEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navEditMouseClicked
        showEditPage(editMenuPage);
    }//GEN-LAST:event_navEditMouseClicked

    private void navAttendanceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navAttendanceMouseClicked
        showPage(attendancePage, navAttendanceActive);
    }//GEN-LAST:event_navAttendanceMouseClicked

    private void navLeaveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navLeaveMouseClicked
        showPage(leavePage, navLeaveActive);
        loadPendingLeaves();
        clearLeaveDetails();
    }//GEN-LAST:event_navLeaveMouseClicked

    // ---------------------------------------------------------------- employees page

    private void departmentFilterComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_departmentFilterComboActionPerformed
        loadEmployees();
    }//GEN-LAST:event_departmentFilterComboActionPerformed

    private void employeeSearchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_employeeSearchFieldKeyReleased
        searchEmployees();
    }//GEN-LAST:event_employeeSearchFieldKeyReleased

    private void employeeTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_employeeTableMouseClicked
        showSelectedEmployeePhoto();
    }//GEN-LAST:event_employeeTableMouseClicked

    private void exportPdfButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPdfButtonActionPerformed
        exportEmployeePdf();
    }//GEN-LAST:event_exportPdfButtonActionPerformed

    // ---------------------------------------------------------------- edit menu page

    private void addEmployeeTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addEmployeeTileMouseClicked
        showEditPage(addEmployeePage);
    }//GEN-LAST:event_addEmployeeTileMouseClicked

    private void updateEmployeeTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateEmployeeTileMouseClicked
        showEditPage(updateEmployeePage);
    }//GEN-LAST:event_updateEmployeeTileMouseClicked

    private void deleteEmployeeTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteEmployeeTileMouseClicked
        showEditPage(deleteEmployeePage);
    }//GEN-LAST:event_deleteEmployeeTileMouseClicked

    private void addBackButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addBackButtonMouseClicked
        showEditPage(editMenuPage);
    }//GEN-LAST:event_addBackButtonMouseClicked

    private void updateBackButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateBackButtonMouseClicked
        showEditPage(editMenuPage);
    }//GEN-LAST:event_updateBackButtonMouseClicked

    private void deleteBackButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteBackButtonMouseClicked
        showEditPage(editMenuPage);
    }//GEN-LAST:event_deleteBackButtonMouseClicked

    // ---------------------------------------------------------------- add employee page

    private void addFirstNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addFirstNameFieldKeyReleased
        validateAddForm();
    }//GEN-LAST:event_addFirstNameFieldKeyReleased

    private void addLastNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addLastNameFieldKeyReleased
        validateAddForm();
    }//GEN-LAST:event_addLastNameFieldKeyReleased

    private void addPositionFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addPositionFieldKeyReleased
        validateAddForm();
    }//GEN-LAST:event_addPositionFieldKeyReleased

    private void addContactFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addContactFieldKeyReleased
        validateAddForm();
    }//GEN-LAST:event_addContactFieldKeyReleased

    private void addContactFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addContactFieldKeyTyped
        allowDigitsOnly(evt);
    }//GEN-LAST:event_addContactFieldKeyTyped

    private void addChoosePhotoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChoosePhotoButtonActionPerformed
        chooseNewEmployeePhoto();
    }//GEN-LAST:event_addChoosePhotoButtonActionPerformed

    private void addEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEmployeeButtonActionPerformed
        addEmployee();
    }//GEN-LAST:event_addEmployeeButtonActionPerformed

    private void clearAddFormButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAddFormButtonActionPerformed
        clearAddForm();
    }//GEN-LAST:event_clearAddFormButtonActionPerformed

    // ---------------------------------------------------------------- update employee page

    private void updateEmpIdFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateEmpIdFieldKeyReleased
        validateUpdateForm();
    }//GEN-LAST:event_updateEmpIdFieldKeyReleased

    private void findEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findEmployeeButtonActionPerformed
        loadEmployeeForUpdate();
    }//GEN-LAST:event_findEmployeeButtonActionPerformed

    private void updateFirstNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateFirstNameFieldKeyReleased
        validateUpdateForm();
    }//GEN-LAST:event_updateFirstNameFieldKeyReleased

    private void updateLastNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateLastNameFieldKeyReleased
        validateUpdateForm();
    }//GEN-LAST:event_updateLastNameFieldKeyReleased

    private void updatePositionFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updatePositionFieldKeyReleased
        validateUpdateForm();
    }//GEN-LAST:event_updatePositionFieldKeyReleased

    private void updateContactFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateContactFieldKeyReleased
        validateUpdateForm();
    }//GEN-LAST:event_updateContactFieldKeyReleased

    private void updateContactFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updateContactFieldKeyTyped
        allowDigitsOnly(evt);
    }//GEN-LAST:event_updateContactFieldKeyTyped

    private void updateChoosePhotoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateChoosePhotoButtonActionPerformed
        changeEmployeePhoto();
    }//GEN-LAST:event_updateChoosePhotoButtonActionPerformed

    private void updateEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateEmployeeButtonActionPerformed
        updateEmployee();
    }//GEN-LAST:event_updateEmployeeButtonActionPerformed

    private void clearUpdateFormButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearUpdateFormButtonActionPerformed
        clearUpdateForm();
    }//GEN-LAST:event_clearUpdateFormButtonActionPerformed

    // ---------------------------------------------------------------- delete employee page

    private void deleteEmpIdFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deleteEmpIdFieldKeyReleased
        validateDeleteForm();
    }//GEN-LAST:event_deleteEmpIdFieldKeyReleased

    private void deleteEmployeeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEmployeeButtonActionPerformed
        deleteEmployee();
    }//GEN-LAST:event_deleteEmployeeButtonActionPerformed

    // ---------------------------------------------------------------- attendance page

    private void attendanceEmpIdFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_attendanceEmpIdFieldKeyReleased
        loadAttendance();
    }//GEN-LAST:event_attendanceEmpIdFieldKeyReleased

    private void attendanceRangeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendanceRangeComboActionPerformed
        loadAttendance();
    }//GEN-LAST:event_attendanceRangeComboActionPerformed

    private void attendanceDateSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendanceDateSearchButtonActionPerformed
        loadAttendanceOnDate();
    }//GEN-LAST:event_attendanceDateSearchButtonActionPerformed

    // ---------------------------------------------------------------- leave page

    private void leaveTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leaveTableMouseClicked
        showSelectedLeave();
    }//GEN-LAST:event_leaveTableMouseClicked

    private void acceptLeaveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_acceptLeaveButtonMouseClicked
        decideSelectedLeave(true);
    }//GEN-LAST:event_acceptLeaveButtonMouseClicked

    private void rejectLeaveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rejectLeaveButtonMouseClicked
        decideSelectedLeave(false);
    }//GEN-LAST:event_rejectLeaveButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEmployeeButton;
    private javax.swing.JButton updateEmployeeButton;
    private javax.swing.JLabel addEmployeeError;
    private javax.swing.JLabel adminSettingsButton;
    private javax.swing.JTable attendanceInTable;
    private javax.swing.JLabel attendanceEmployeeName;
    private javax.swing.JTable attendanceOutTable;
    private javax.swing.JTextField attendanceEmpIdField;
    private javax.swing.JComboBox<String> attendanceRangeCombo;
    private javax.swing.JLabel navAttendance;
    private javax.swing.JLabel navAttendanceActive;
    private javax.swing.JLabel attendanceError;
    private javax.swing.JLabel deleteBackButton;
    private javax.swing.JLabel blue;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JButton clearAddFormButton;
    private javax.swing.JButton clearUpdateFormButton;
    private javax.swing.JTextField addContactField;
    private javax.swing.JTextField updateContactField;
    private com.toedter.calendar.JDateChooser addDobChooser;
    private com.toedter.calendar.JDateChooser updateDobChooser;
    private javax.swing.JPanel deleteEmployeePage;
    private javax.swing.JTextField deleteEmpIdField;
    private javax.swing.JButton deleteEmployeeButton;
    private javax.swing.JLabel deleteError;
    private javax.swing.JComboBox<String> addDepartmentCombo;
    private javax.swing.JComboBox<String> updateDepartmentCombo;
    private javax.swing.JComboBox<String> departmentFilterCombo;
    private javax.swing.JLabel navEdit;
    private javax.swing.JLabel navEditActive;
    private javax.swing.JPanel editMenuPage;
    private javax.swing.JLabel navEmployees;
    private javax.swing.JLabel navEmployeesActive;
    private javax.swing.JLabel addPhotoLabel;
    private javax.swing.JLabel updatePhotoLabel;
    private javax.swing.JButton findEmployeeButton;
    private javax.swing.JTextField updateEmpIdField;
    private javax.swing.JTextField addFirstNameField;
    private javax.swing.JTextField updateFirstNameField;
    private javax.swing.JComboBox<String> addGenderCombo;
    private javax.swing.JComboBox<String> updateGenderCombo;
    private javax.swing.JTextField attendanceDateField;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel homePage;
    private javax.swing.JLabel navHome;
    private javax.swing.JLabel navHomeActive;
    private javax.swing.JPanel addEmployeePage;
    private javax.swing.JLabel employeePhotoLabel;
    private javax.swing.JButton attendanceDateSearchButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel logoutButton;
    private javax.swing.JLabel addEmployeeTile;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel updateEmployeeTile;
    private javax.swing.JLabel deleteEmployeeTile;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel addBackButton;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel updateBackButton;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel acceptLeaveButton;
    private javax.swing.JLabel rejectLeaveButton;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel closeButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextField addLastNameField;
    private javax.swing.JTextField updateLastNameField;
    private javax.swing.JTextArea leaveCommentArea;
    private javax.swing.JLabel leaveDatesLabel;
    private javax.swing.JPanel leavePage;
    private javax.swing.JLabel navLeave;
    private javax.swing.JLabel navLeaveActive;
    private javax.swing.JLabel leaveEmpIdLabel;
    private javax.swing.JLabel leaveEmployeeName;
    private javax.swing.JLabel adminNameLabel;
    private javax.swing.JTable leaveTable;
    private javax.swing.JLabel minimizeButton;
    private javax.swing.JPanel employeesPage;
    private javax.swing.JPanel attendancePage;
    private javax.swing.JButton exportPdfButton;
    private javax.swing.JLabel photo;
    private javax.swing.JTextField addPositionField;
    private javax.swing.JTextField updatePositionField;
    private javax.swing.JTextField employeeSearchField;
    private javax.swing.JPanel attendanceDatePanel;
    private javax.swing.JButton addChoosePhotoButton;
    private javax.swing.JButton updateChoosePhotoButton;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JLabel totalAdminsLabel;
    private javax.swing.JLabel totalEmployeesLabel;
    private javax.swing.JLabel departmentCountLabel;
    private javax.swing.JPanel updateEmployeePage;
    private javax.swing.JLabel updateError;
    private javax.swing.JTable employeeTable;
    private javax.swing.JLabel welcome;
    // End of variables declaration//GEN-END:variables
}
