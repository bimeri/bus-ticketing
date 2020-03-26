package net.gowaka.gowaka.domain.service.utilities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 10:44 AM <br/>
 */
public class QRCodeProvider {


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
}
