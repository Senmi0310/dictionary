package com.example.dictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class loginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private SharedPreferences mSharedPreference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profileImage;
    private ImageButton btnChangeAvatar;
    private Uri avatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        //获取用户名和密码
        mSharedPreference =getSharedPreferences("user",MODE_PRIVATE);

        //初始化控件
        et_username =findViewById(R.id.et_username);
        et_password =findViewById(R.id.et_password);



        //点击注册
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到注册页面
                Intent intent =new Intent(loginActivity.this,registerActivity.class);
                startActivity(intent);
            }
        });

        //登录
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username =et_username.getText().toString();
                String passsword =et_password.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(passsword)){
                    Toast.makeText(loginActivity.this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
                }else {
                    String name =mSharedPreference.getString("username", null);
                    String pwd =mSharedPreference.getString("password", null);

                    if(username.equals(name) && passsword.equals(pwd)){
                        //登陆成功
                        Intent intent =new Intent(loginActivity.this,MainActivity.class);
                        startActivity(intent);
                    }else{
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
}