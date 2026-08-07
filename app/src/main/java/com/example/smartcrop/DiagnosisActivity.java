package com.example.smartcrop;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.database.AppDatabase;
import com.example.smartcrop.database.HistoryEntity;
import com.example.smartcrop.databinding.ActivityMainBinding;
import com.example.smartcrop.models.PredictResponse;
import com.example.smartcrop.utils.DiseaseProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;

public class DiagnosisActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Uri imageUri;
    private Bitmap originalBitmap;
    private String currentDiseaseName = "";
    private static final String CHANNEL_ID = "SmartCropWarning";

    // ActivityResultLauncher for Camera
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result && imageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        originalBitmap = BitmapFactory.decodeStream(inputStream);
                        binding.ivCropImage.setImageBitmap(originalBitmap);
                        uploadImageToFastAPI(uriToFile(imageUri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    // ActivityResultLauncher for Gallery
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        originalBitmap = BitmapFactory.decodeStream(inputStream);
                        binding.ivCropImage.setImageBitmap(originalBitmap);
                        uploadImageToFastAPI(uriToFile(uri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarDiagnosis);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarDiagnosis.setNavigationOnClickListener(v -> finish());

        createNotificationChannel();
        checkPermissions();

        binding.btnCamera.setOnClickListener(v -> openCamera());
        binding.btnGallery.setOnClickListener(v -> openGallery());
        binding.btnPostForum.setOnClickListener(v -> showPostDialog());

        // Handle intent from HomeFragment
        String action = getIntent().getStringExtra("action");
        if ("camera".equals(action)) {
            openCamera();
        } else if ("upload".equals(action)) {
            openGallery();
        }
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 100);
                break;
            }
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cameraLauncher.launch(imageUri);
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void uploadImageToFastAPI(File imageFile) {
        showLoading(true);
        OkHttpClient client = new OkHttpClient();

        String userEmail = "unknown@example.com";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            userEmail = user.getEmail();
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .addFormDataPart("user_email", userEmail)
                .build();

        // Địa chỉ IP máy tính chạy FastAPI
        // Ví dụ: 192.168.1.10 hoặc 10.0.2.2 nếu chạy Emulator
        // Lấy từ RetrofitClient.BASE_URL để đồng bộ
        String url = com.example.smartcrop.api.RetrofitClient.BASE_URL + "/predict";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    // Hiện lỗi chi tiết để debug
                    Toast.makeText(DiagnosisActivity.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                runOnUiThread(() -> showLoading(false));
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        double confidence = json.getDouble("confidence");
                        
                        // Parse treatment_details
                        org.json.JSONObject details = json.getJSONObject("treatment_details");
                        String diseaseNameVi = details.getString("disease_name_vi");
                        String severity = details.getString("severity");
                        String symptoms = details.getString("symptoms");
                        String bioTreatment = details.getString("biological_treatment");
                        String chemTreatment = details.getString("chemical_treatment");
                        String prevention = details.getString("prevention");
                        
                        // Parse Gemini Advice
                        String aiAdvice = json.getString("ai_expert_advice");

                        // Lấy tọa độ Bounding Box
                        org.json.JSONObject bbox = json.getJSONObject("bbox");
                        int x1 = bbox.getInt("x1");
                        int y1 = bbox.getInt("y1");
                        int x2 = bbox.getInt("x2");
                        int y2 = bbox.getInt("y2");

                        runOnUiThread(() -> {
                            // Cập nhật kết quả lên UI Dashboard
                            binding.tvDiseaseName.setText(diseaseNameVi);
                            binding.tvConfidence.setText(String.format("Độ tin cậy: %.2f%%", confidence));
                            binding.tvSeverity.setText("Mức độ: " + severity);

                            currentDiseaseName = diseaseNameVi;
                            binding.btnPostForum.setVisibility(View.VISIBLE);

                            // Cài đặt màu sắc theo mức độ nghiêm trọng
                            updateSeverityUI(severity);

                            binding.tvAiAdvice.setText(aiAdvice);
                            binding.tvSymptoms.setText(symptoms);
                            binding.tvBioTreatment.setText(bioTreatment);
                            binding.tvChemTreatment.setText(chemTreatment);
                            binding.tvPrevention.setText(prevention);

                            // Tiến hành vẽ Bounding Box lên Bitmap ảnh trên Android
                            drawBoundingBoxOnImage(x1, y1, x2, y2);

                            // Lưu vào lịch sử (Sử dụng tên tiếng Việt và gom các biện pháp lại)
                            String fullTreatment = "Sinh học: " + bioTreatment + "\nHóa học: " + chemTreatment;
                            saveToHistory(diseaseNameVi, confidence, fullTreatment);

                            // Logic cảnh báo khẩn cấp (Chỉ khi phát hiện BỆNH, không phải lá khỏe)
                            boolean isHealthy = diseaseNameVi.toLowerCase().contains("khỏe mạnh") || diseaseNameVi.toLowerCase().contains("an toàn");
                            
                            if (!isHealthy && (severity.contains("Rất cao") || confidence > 85)) {
                                // Tự động gửi email ngầm (không hiện AlertDialog gây phiền)
                                sendNotification(diseaseNameVi);
                            }

                            // Tăng bộ đếm bệnh thường gặp trên Firestore
                            incrementDiseaseCount(diseaseNameVi);
                        });
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(DiagnosisActivity.this, "Lỗi phân tích dữ liệu!", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(DiagnosisActivity.this, "Server trả về lỗi: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateSeverityUI(String severity) {
        if (severity.contains("Rất cao") || severity.contains("Nguy hiểm")) {
            binding.tvSeverity.setBackgroundResource(R.drawable.bg_badge_red);
        } else if (severity.contains("Trung bình")) {
            binding.tvSeverity.setBackgroundResource(R.drawable.bg_badge_orange);
        } else if (severity.contains("An toàn") || severity.contains("Khỏe mạnh")) {
            binding.tvSeverity.setBackgroundResource(R.drawable.bg_badge_green);
        } else {
            binding.tvSeverity.setBackgroundResource(R.drawable.bg_badge_gray);
        }
    }

    private void drawBoundingBoxOnImage(int x1, int y1, int x2, int y2) {
        if (originalBitmap == null) return;

        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);

        // Vẽ khung chữ nhật
        canvas.drawRect(x1, y1, x2, y2, paint);

        // Cập nhật Bitmap đã vẽ khung lên ImageView
        binding.ivCropImage.setImageBitmap(mutableBitmap);
    }

    private void showEmergencyDialog(String disease) {
        new AlertDialog.Builder(this)
                .setTitle("CẢNH BÁO NGUY HIỂM")
                .setMessage("Hệ thống phát hiện bệnh " + disease + " mức độ nghiêm trọng! Đã gửi email cảnh báo cho chủ vườn. Hãy cách ly vùng bệnh ngay.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }

    private void sendNotification(String disease) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Phát hiện bệnh hại: " + disease)
                .setContentText("Cảnh báo mức độ cao. Kiểm tra ứng dụng ngay!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnCamera.setEnabled(!isLoading);
        binding.btnGallery.setEnabled(!isLoading);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AI Warning", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Chuyển đổi Uri sang File để upload
    private File uriToFile(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "temp_upload.jpg");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return tempFile;
    }

    private void incrementDiseaseCount(String diseaseName) {
        FirebaseFirestore.getInstance().collection("disease_stats")
                .document(diseaseName)
                .update("count", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    // Nếu document chưa tồn tại, tạo mới
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("name", diseaseName);
                    data.put("count", 1);
                    FirebaseFirestore.getInstance().collection("disease_stats")
                            .document(diseaseName)
                            .set(data);
                });
    }

    private void showPostDialog() {
        android.widget.EditText etQuestion = new android.widget.EditText(this);
        etQuestion.setHint("Nhập câu hỏi của bạn về bệnh này...");
        etQuestion.setPadding(40, 40, 40, 40);

        new AlertDialog.Builder(this)
                .setTitle("Đăng bài lên diễn đàn")
                .setView(etQuestion)
                .setPositiveButton("Đăng bài", (dialog, which) -> {
                    String question = etQuestion.getText().toString().trim();
                    if (!question.isEmpty()) {
                        postToForum(question);
                    } else {
                        Toast.makeText(this, "Vui lòng nhập nội dung câu hỏi", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void postToForum(String question) {
        if (originalBitmap == null) return;
        showLoading(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "anonymous";
        String userName = user != null && user.getDisplayName() != null ? user.getDisplayName() : "Người dùng Thần Nông AI";

        // 1. Upload ảnh lên Firebase Storage
        String fileName = "forum_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("forum_images/" + fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        storageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // 2. Lưu thông tin vào Firestore
                    java.util.Map<String, Object> post = new java.util.HashMap<>();
                    post.put("uid", uid);
                    post.put("author", userName);
                    post.put("question", question);
                    post.put("imageUrl", uri.toString());
                    post.put("disease", currentDiseaseName);
                    post.put("timestamp", System.currentTimeMillis());
                    post.put("likes", 0);
                    post.put("commentsCount", 0);
                    post.put("likedBy", new java.util.ArrayList<String>());

                    FirebaseFirestore.getInstance().collection("forum_posts")
                            .add(post)
                            .addOnSuccessListener(documentReference -> {
                                showLoading(false);
                                Toast.makeText(this, "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Lỗi đăng bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToHistory(String disease, double confidence, String treatment) {
        Executors.newSingleThreadExecutor().execute(() -> {
            HistoryEntity history = new HistoryEntity(
                    disease,
                    confidence,
                    treatment,
                    imageUri != null ? imageUri.toString() : "",
                    System.currentTimeMillis()
            );
            AppDatabase.getInstance(this).historyDao().insert(history);
        });
    }
}
