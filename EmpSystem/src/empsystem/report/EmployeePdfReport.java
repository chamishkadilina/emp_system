package empsystem.report;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import empsystem.model.Employee;
import empsystem.model.EmployeeLogin;
import empsystem.util.Images;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Writes one employee's details and employee-app login to a PDF file. */
public final class EmployeePdfReport {

    private EmployeePdfReport() {
    }

    /** {@code login} may be null when the employee has no login. */
    public static void write(Employee employee, EmployeeLogin login, File file)
            throws IOException, DocumentException {
        Document document = new Document();
        try (FileOutputStream out = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, out);
            document.open();

            Image logo = Image.getInstance(EmployeePdfReport.class.getResource(Images.COMPANY_LOGO));
            logo.scaleToFit(150, 150);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);

            document.add(new Paragraph("Employee Details", FontFactory.getFont(FontFactory.HELVETICA, 18)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Employee Name: " + employee.fullName()));
            document.add(new Paragraph("Gender: " + employee.gender()));
            document.add(new Paragraph("Contact: " + employee.contact()));
            document.add(new Paragraph("Position: " + employee.position()));
            document.add(new Paragraph("Department: " + employee.department()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("User Name: " + (login == null ? "" : login.userName())));
            document.add(new Paragraph("Password: " + (login == null ? "" : login.password())));

            document.close();
        }
    }
}
