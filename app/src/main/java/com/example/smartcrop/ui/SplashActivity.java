package com.example.smartcrop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.view.animation.AlphaAnimation;

import com.example.smartcrop.MainActivity;
import com.example.smartcrop.R;
import com.example.smartcrop.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hiệu ứng Fade-in cho Logo và Tên app
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1200);
        findViewById(R.id.ivLogo).startAnimation(fadeIn);
        findViewById(R.id.tvAppName).startAnimation(fadeIn);

        // Kiểm tra trạng thái đăng nhập sau 2 giây
        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // Đã đăng nhập -> Vào thẳng màn hình chính
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Chưa đăng nhập -> Vào màn hình Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2000);
    }
}
