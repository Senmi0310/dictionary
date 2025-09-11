package com.example.dictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private SharedPreferences sharedPreferences;
    private static Bitmap currentUserAvatar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // 设置Toolbar并添加返回按钮功能
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建返回登录界面的Intent
                Intent intent = new Intent(MainActivity.this, loginActivity.class);

                // 添加标志清除返回栈，防止用户按返回键再次回到主界面
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
                finish(); // 结束当前Activity
            }
        });

        // 获取头像视图并设置点击事件
        profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理头像点击事件，打开好友页面
                Intent friendIntent = new Intent(MainActivity.this, friendActivity.class);
                startActivity(friendIntent);
            }
        });

        // 加载保存的头像
        loadSavedAvatar();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到页面时重新加载头像，确保一致性
        loadSavedAvatar();
    }

    private void loadSavedAvatar() {
        // 尝试从内部存储加载头像
        try {
            FileInputStream inputStream = openFileInput("avatar.png");
            currentUserAvatar = BitmapFactory.decodeStream(inputStream);
            profileImage.setImageBitmap(currentUserAvatar);
            inputStream.close();
        } catch (Exception e) {
            // 如果没有保存的头像，使用默认头像
            try {
                currentUserAvatar = BitmapFactory.decodeResource(getResources(), R.mipmap.login_picture);
                profileImage.setImageBitmap(currentUserAvatar);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 静态方法，供其他Activity获取当前用户头像
    public static Bitmap getCurrentUserAvatar() {
        return currentUserAvatar;
    }

    // 静态方法，供其他Activity设置当前用户头像
    public static void setCurrentUserAvatar(Bitmap avatar) {
        currentUserAvatar = avatar;
    }
}