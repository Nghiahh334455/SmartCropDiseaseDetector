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
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.Map;
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
        
        // Gọi AI qua Port 8000 (VS Code)
        ApiService apiService = RetrofitClient.getAiService();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null && user.getEmail() != null) ? user.getEmail() : "unknown@example.com";

        RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
        RequestBody emailPart = RequestBody.create(userEmail, MediaType.parse("text/plain"));

        apiService.predictDisease(body).enqueue(new retrofit2.Callback<PredictResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<PredictResponse> call, @NonNull retrofit2.Response<PredictResponse> response) {
                runOnUiThread(() -> showLoading(false));
                if (response.isSuccessful() && response.body() != null) {
                    PredictResponse result = response.body();
                    
                    // Cập nhật kết quả lên UI
                    binding.tvDiseaseName.setText(result.getDiseaseName()); // Giả sử model có phương thức này, cần check
                    // Nếu Model cũ trả về disease_name_vi, ta dùng field đó
                    
                    // ... Cập nhật các thông tin khác ...
                    // Lưu ý: Tôi sẽ tinh chỉnh lại để khớp với PredictResponse thật của bạn
                    processAiResult(result);
                } else {
                    runOnUiThread(() -> Toast.makeText(DiagnosisActivity.this, "Lỗi Server AI (404/500): " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<PredictResponse> call, @NonNull Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(DiagnosisActivity.this, "Lỗi kết nối AI: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void processAiResult(PredictResponse result) {
        String name = result.getDiseaseName() != null ? result.getDiseaseName() : "Bệnh hại";
        double confidence = result.getConfidence();
        
        binding.tvDiseaseName.setText(name);
        binding.tvConfidence.setText(String.format("Độ tin cậy: %.2f%%", confidence));
        
        currentDiseaseName = name;
        binding.btnPostForum.setVisibility(View.VISIBLE);

        Map<String, String> details = result.getTreatmentDetails();
        if (details != null) {
            String severity = details.get("severity");
            binding.tvSeverity.setText("Mức độ: " + severity);
            updateSeverityUI(severity != null ? severity : "");
            
            binding.tvSymptoms.setText(details.get("symptoms"));
            binding.tvBioTreatment.setText(details.get("biological_treatment"));
            binding.tvChemTreatment.setText(details.get("chemical_treatment"));
            binding.tvPrevention.setText(details.get("prevention"));
        }
        
        binding.tvAiAdvice.setText(result.getAiExpertAdvice());

        // Draw BBox
        Map<String, Integer> bbox = result.getBbox();
        if (bbox != null && bbox.containsKey("x1")) {
            drawBoundingBoxOnImage(bbox.get("x1"), bbox.get("y1"), bbox.get("x2"), bbox.get("y2"));
        }

        // Stats & History - Upload image for stats
        boolean isHealthy = name.toLowerCase().contains("khỏe mạnh") || name.toLowerCase().contains("healthy");
        if (!isHealthy && originalBitmap != null) {
            uploadDiagnosisImageForStats(name);
        }
        
        saveToHistory(name, confidence, "Chẩn đoán bởi AI");
    }

    private void uploadDiagnosisImageForStats(String diseaseName) {
        String fileName = "stats_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("diagnosis_images/" + fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data).continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return ref.getDownloadUrl();
        }).addOnSuccessListener(uri -> {
            incrementDiseaseCount(diseaseName, uri.toString());
        });
    }

    private void incrementDiseaseCount(String diseaseName, String imageUrl) {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.incrementDiseaseCount(diseaseName, imageUrl).enqueue(new retrofit2.Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<java.util.Map<String, String>> call, @NonNull retrofit2.Response<java.util.Map<String, String>> response) {
                // Success
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                // Log error
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
        // Da loai bo thong bao Gmail gay phien
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


    private void showPostDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_share_post, null);
        dialog.setContentView(view);

        com.google.android.material.imageview.ShapeableImageView ivPreview = view.findViewById(R.id.ivSharePreview);
        android.widget.TextView tvName = view.findViewById(R.id.tvShareDiseaseName);
        android.widget.EditText etStatus = view.findViewById(R.id.etShareStatus);

        tvName.setText(currentDiseaseName);
        if (originalBitmap != null) {
            ivPreview.setImageBitmap(originalBitmap);
        }

        view.findViewById(R.id.btnConfirmShare).setOnClickListener(v -> {
            String question = etStatus.getText().toString().trim();
            dialog.dismiss();
            postToForum(question);
        });

        dialog.show();
    }

    private void postToForum(String question) {
        if (originalBitmap == null) return;
        showLoading(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showLoading(false);
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String userName = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Người dùng Thần Nông AI";
        String userPhotoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

        // 1. Upload ảnh lên Firebase Storage
        String fileName = "forum_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("forum_images/" + fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

                storageRef.putBytes(data)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            // 2. Lưu thông tin vào SQL Server qua Port 8001
                            ApiService apiService = RetrofitClient.getSqlService();
                            apiService.createPost(uid, userName, question, userPhotoUrl, downloadUri.toString(), currentDiseaseName)
                                    .enqueue(new retrofit2.Callback<Map<String, String>>() {
                                        @Override
                                        public void onResponse(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull retrofit2.Response<Map<String, String>> response) {
                                            showLoading(false);
                                            if (response.isSuccessful()) {
                                                Toast.makeText(DiagnosisActivity.this, "Đã đăng bài thành công lên diễn đàn!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(DiagnosisActivity.this, "Lỗi server SQL: " + response.code(), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull Throwable t) {
                                            showLoading(false);
                                            Toast.makeText(DiagnosisActivity.this, "Lỗi kết nối SQL Server", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            showLoading(false);
                            Toast.makeText(this, "Lỗi tải ảnh: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
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
