package com.example.smartcrop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcrop.MainActivity;
import com.example.smartcrop.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // KIỂM TRA TÀI KHOẢN ADMIN CỨNG
        if (email.equalsIgnoreCase("12345N") && password.equals("12345")) {
            getSharedPreferences("SmartCropPrefs", MODE_PRIVATE).edit()
                    .putBoolean("is_admin", true)
                    .putString("admin_uid", "12345N")
                    .apply();
            Toast.makeText(this, "Chào mừng Quản trị viên!", Toast.LENGTH_SHORT).show();
            // Chuyển hướng đến Admin Dashboard (Sẽ tạo sau)
            // startActivity(new Intent(this, AdminDashboardActivity.class));
            startActivity(new Intent(this, com.example.smartcrop.MainActivity.class));
            finish();
            return;
        }

        // Reset Admin flag if normal user logs in
        getSharedPreferences("SmartCropPrefs", MODE_PRIVATE).edit().putBoolean("is_admin", false).apply();

        // Hien thi trang thai dang tai
        binding.btnLogin.setVisibility(View.GONE);
        binding.pbLogin.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Đang xác thực tài khoản...", Toast.LENGTH_SHORT).show();

        final String formattedEmail = email.toLowerCase();

        mAuth.signInWithEmailAndPassword(formattedEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            // Tự động khởi tạo và đăng ký tài khoản mới trên Firebase nếu tài khoản Gmail chưa tồn tại
                            mAuth.createUserWithEmailAndPassword(formattedEmail, password)
                                    .addOnCompleteListener(this, createTaskId -> {
                                        if (createTaskId.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Tự động kích hoạt tài khoản và đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            checkSqlUserFallback(formattedEmail, "Tài khoản Gmail chưa được đăng ký trên Firebase");
                                        }
                                    });
                        } else {
                            String errorDetail = "Mật khẩu hoặc email nhập chưa đúng";
                            if (exception instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                errorDetail = "Mật khẩu nhập chưa đúng. Vui lòng kiểm tra lại.";
                            } else if (exception != null && exception.getMessage() != null) {
                                errorDetail = exception.getMessage();
                            }

                            final String userErrMsg = errorDetail;
                            checkSqlUserFallback(formattedEmail, userErrMsg);
                        }
                    }
                });
    }

    private void checkSqlUserFallback(String email, String defaultErrorMsg) {
        com.example.smartcrop.api.ApiService apiService = com.example.smartcrop.api.RetrofitClient.getSqlService();
        apiService.getUserByEmail(email).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<java.util.Map<String, Object>> call, @androidx.annotation.NonNull retrofit2.Response<java.util.Map<String, Object>> response) {
                binding.btnLogin.setVisibility(View.VISIBLE);
                binding.pbLogin.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().get("status"))) {
                    String uid = (String) response.body().get("uid");
                    String name = (String) response.body().get("displayName");
                    Boolean isAdmin = (Boolean) response.body().get("isAdmin");

                    getSharedPreferences("SmartCropPrefs", MODE_PRIVATE).edit()
                            .putBoolean("is_admin", Boolean.TRUE.equals(isAdmin))
                            .putString("user_uid", uid)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Chào mừng " + (name != null ? name : "bạn"), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + defaultErrorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<java.util.Map<String, Object>> call, @androidx.annotation.NonNull Throwable t) {
                binding.btnLogin.setVisibility(View.VISIBLE);
                binding.pbLogin.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + defaultErrorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
