package hk.timeslogistics.wms.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class BarcodeCreate {
    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xFFFFFFFF;
    //private static final BarcodeFormat barcodeFormat= BarcodeFormat.QR_CODE;
    public static Bitmap createBarcode(String contents, int desiredWidth, int desiredHeight,BarcodeFormat barcodeFormat) {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result=null;
        try {
            result = writer.encode(contents, barcodeFormat, desiredWidth, desiredHeight);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;

    }
}
