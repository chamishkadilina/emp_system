package empsystem.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Loading icons from the resources folder and photos from bytes or files. */
public final class Images {

    public static final String ADD_PHOTO = "/empsystem/resources/icons/edit_employee/add.png";
    public static final String DEFAULT_EMPLOYEE_PHOTO = "/empsystem/resources/icons/employee/normal_emp.png";
    public static final String COMPANY_LOGO = "/empsystem/resources/images/metrologo.png";

    private Images() {
    }

    public static ImageIcon icon(String resourcePath) {
        return new ImageIcon(Images.class.getResource(resourcePath));
    }

    /** Returns the photo as an icon, or {@code fallback} when there is no readable image. */
    public static ImageIcon fromBytes(byte[] data, ImageIcon fallback) {
        if (data == null || data.length == 0) {
            return fallback;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            return image == null ? fallback : new ImageIcon(image);
        } catch (IOException e) {
            return fallback;
        }
    }

    public static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
