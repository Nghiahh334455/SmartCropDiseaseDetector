package com.example.smartcrop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcrop.MainActivity;
import com.example.smartcrop.databinding.ActivityRegisterBinding;
import com.example.smartcrop.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseUtils.getAuth();

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = binding.etEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth == null) {
            Toast.makeText(this, "Firebase chưa được cấu hình.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Lỗi đăng ký";
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + err,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (binding == null) return;
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? "" : "ĐĂNG KÝ NGAY");
        binding.pbRegister.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }
}
