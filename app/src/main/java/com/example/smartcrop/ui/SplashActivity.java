package com.example.smartcrop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcrop.MainActivity;
import com.example.smartcrop.R;
import com.example.smartcrop.ui.auth.LoginActivity;
import com.example.smartcrop.utils.FirebaseUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1200);
        findViewById(R.id.ivLogo).startAnimation(fadeIn);
        findViewById(R.id.tvAppName).startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            try {
                Intent nextIntent = FirebaseUtils.getCurrentUser() != null
                        ? new Intent(SplashActivity.this, MainActivity.class)
                        : new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(nextIntent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } catch (Exception e) {
                e.printStackTrace();
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 1800);
    }
}
