package com.example.dictionary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class loginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private SharedPreferences mSharedPreference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profileImage;
    private ImageButton btnChangeAvatar;
    private Uri avatarUri;

    // 预设头像资源ID
    private final int[] avatarResources = {
            R.mipmap.login_picture,  // 默认头像
            R.mipmap.login_cat_picture,  // 猫头像
            R.mipmap.login_dog_picture   // 狗头像
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // 初始化视图
        profileImage = findViewById(R.id.profile_image);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);

        // 设置头像点击事件 - 显示头像选择对话框
        btnChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarSelectionDialog();
            }
        });

        // 设置头像图片本身的点击事件
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarSelectionDialog();
            }
        });

        //获取用户名和密码
        mSharedPreference = getSharedPreferences("user", MODE_PRIVATE);

        //初始化控件
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        //点击注册
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到注册页面
                Intent intent = new Intent(loginActivity.this, registerActivity.class);
                startActivity(intent);
            }
        });

        //登录
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString();
                String passsword = et_password.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(passsword)) {
                    Toast.makeText(loginActivity.this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
                } else {
                    String name = mSharedPreference.getString("username", null);
                    String pwd = mSharedPreference.getString("password", null);

                    if (username.equals(name) && passsword.equals(pwd)) {
                        //登陆成功
                        Intent intent = new Intent(loginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(loginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 显示头像选择对话框
    private void showAvatarSelectionDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择头像");

        // 对话框选项
        String[] options = {"默认头像", "猫咪头像", "狗狗头像", "从相册选择"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 默认头像
                    setAvatarFromResource(avatarResources[0]);
                    break;
                case 1: // 猫咪头像
                    setAvatarFromResource(avatarResources[1]);
                    break;
                case 2: // 狗狗头像
                    setAvatarFromResource(avatarResources[2]);
                    break;
                case 3: // 从相册选择
                    openImageChooser();
                    break;
            }
        });

        // 显示对话框
        builder.show();
    }

    // 从资源设置头像
    private void setAvatarFromResource(int resourceId) {
        try {
            // 从资源获取Bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);

            // 设置头像
            profileImage.setImageBitmap(bitmap);

            // 保存头像到本地
            saveAvatar(bitmap);

            Toast.makeText(this, "头像已更换", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "更换头像失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageChooser() {
        // 使用 ACTION_OPEN_DOCUMENT 而不是 ACTION_GET_CONTENT
        // ACTION_OPEN_DOCUMENT 提供更持久的访问权限
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            avatarUri = data.getData();

            // 获取内容的持久权限
            getContentResolver().takePersistableUriPermission(avatarUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 使用 ContentResolver 直接读取图像
            try (InputStream inputStream = getContentResolver().openInputStream(avatarUri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);

                // 保存头像到本地存储
                saveAvatar(bitmap);

                Toast.makeText(this, "头像已更换", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "无法加载图像", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAvatar(Bitmap bitmap) {
        // 保存头像到应用内部存储
        try {
            FileOutputStream outputStream = openFileOutput("avatar.png", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSavedAvatar() {
        // 加载已保存的头像
        try {
            FileInputStream inputStream = openFileInput("avatar.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            profileImage.setImageBitmap(bitmap);
            inputStream.close();
        } catch (Exception e) {
            // 没有保存的头像，使用默认头像
            setAvatarFromResource(avatarResources[0]);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedAvatar();
    }
}