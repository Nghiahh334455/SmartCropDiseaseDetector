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
     * Chuyển đổi Uri ảnh sang chuỗi Base64 (Có nén để tiết kiệm dung lượng DB)
     */
    public static String uriToBase64(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmapToBase64(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Chuyển đổi Bitmap sang chuỗi Base64
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Nén ảnh xuống 70% để cân bằng giữa chất lượng và dung lượng DB
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Chuyển đổi chuỗi Base64 ngược lại thành Byte Array để Glide hiển thị
     */
    public static byte[] base64ToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            return Base64.decode(base64String, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
}
