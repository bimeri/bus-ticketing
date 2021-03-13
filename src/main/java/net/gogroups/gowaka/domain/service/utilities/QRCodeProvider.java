package net.gogroups.gowaka.domain.service.utilities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 10:44 AM <br/>
 */
public class QRCodeProvider {

    public static final String STORAGE_FOLDER = "QR_Code";
    public static final String STORAGE_FILE_FORMAT = "png";

    public static BufferedImage generateQRCodeImage(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static String getQRCodeBase64EncodedImage(String code) {
        try {
            BufferedImage bufferedImage = QRCodeProvider.generateQRCodeImage(code);
            String pathname = "image" + UUID.randomUUID() + ".png";
            File file = new File(pathname);
            ImageIO.write(bufferedImage, "png", file);
            byte[] imageBytes = Files.readAllBytes(Paths.get(pathname));
            file.delete();
            Base64.Encoder encoder = Base64.getEncoder();
            return "data:image/png;base64," + encoder.encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
