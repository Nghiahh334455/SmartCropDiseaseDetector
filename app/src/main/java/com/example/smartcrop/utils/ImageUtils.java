package com.example.smartcrop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Chuyển đổi Uri ảnh sang chuỗi Base64 (Có nén và thu nhỏ để tiết kiệm dung lượng DB)
     */
    public static String uriToBase64(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            
            // Lấy kích thước ảnh gốc
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) inputStream.close();
            
            // Tính toán tỷ lệ thu nhỏ (Scale)
            int scale = 1;
            int maxSide = 1024; // Kích thước tối đa cho 1 cạnh
            if (options.outHeight > maxSide || options.outWidth > maxSide) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxSide / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
            }
            
            BitmapFactory.Options outOptions = new BitmapFactory.Options();
            outOptions.inSampleSize = scale;
            
            inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, outOptions);
            if (inputStream != null) inputStream.close();
            
            return bitmapToBase64(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Chuyển đổi Bitmap sang chuỗi Base64 (Có thu nhỏ)
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        
        // Thu nhỏ Bitmap nếu quá lớn
        int maxSide = 800;
        if (bitmap.getWidth() > maxSide || bitmap.getHeight() > maxSide) {
            float scale = Math.min((float) maxSide / bitmap.getWidth(), (float) maxSide / bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale), Math.round(bitmap.getHeight() * scale), true);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Nén ảnh xuống 70%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Chuyển đổi chuỗi Base64 ngược lại thành Byte Array để Glide hiển thị
     */
    public static byte[] base64ToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        
        // Loại bỏ tiền tố BASE64: nếu có
        if (base64String.startsWith("BASE64:")) {
            base64String = base64String.substring(7);
        }
        
        try {
            return Base64.decode(base64String, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
}
