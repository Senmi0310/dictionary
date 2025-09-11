package com.example.dictionary;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class friendActivity extends AppCompatActivity {
    private ListView friendsListView;
    private EditText searchEditText;
    private ImageButton addFriendButton;
    private FriendsDatabaseHelper dbHelper;
    private FriendsAdapter adapter;
    private List<Friend> friendsList;
    private CircleImageView profileImage;

    // 预设的四个好友信息
    private static final String[] PRESET_FRIENDS_NAMES = {"zhangsan", "lisi", "wangwu", "zhaoliu"};
    private static final String[] PRESET_FRIENDS_PHONES = {"13800138000", "13900139000", "13700137000", "13600136000"};
    private static final String[] PRESET_FRIENDS_EMAILS = {
            "zhangsan@example.com",
            "lisi@example.com",
            "wangwu@example.com",
            "zhaoliu@example.com"
    };

    // 当前用户信息（假设）
    private static final String CURRENT_USER_NAME = "我自己";
    private static final String CURRENT_USER_PHONE = "12345678900";
    private static final String CURRENT_USER_EMAIL = "me@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // 初始化数据库帮助类
        dbHelper = new FriendsDatabaseHelper(this);

        // 初始化视图
        friendsListView = findViewById(R.id.friends_list);
        searchEditText = findViewById(R.id.search_edit_text);
        addFriendButton = findViewById(R.id.add_friend_button);
        profileImage = findViewById(R.id.profile_image); // 添加头像视图

        // 设置头像点击事件，点击返回主页面
        profileImage.setOnClickListener(v -> {
            finish();
        });

        // 加载好友列表
        loadFriends();

        // 设置搜索功能
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 设置添加好友按钮点击事件
        addFriendButton.setOnClickListener(v -> {
            showAddFriendDialog();
        });

        // 设置返回按钮点击事件
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 加载当前用户头像
        loadCurrentUserAvatar();
    }

    private void loadCurrentUserAvatar() {
        // 从MainActivity获取当前用户头像
        Bitmap avatar = MainActivity.getCurrentUserAvatar();
        if (avatar != null) {
            profileImage.setImageBitmap(avatar);
        } else {
            // 如果MainActivity尚未加载头像，则直接加载
            try {
                android.content.res.Resources res = getResources();
                Bitmap defaultAvatar = BitmapFactory.decodeResource(res, R.mipmap.login_picture);
                profileImage.setImageBitmap(defaultAvatar);
                MainActivity.setCurrentUserAvatar(defaultAvatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFriends() {
        friendsList = dbHelper.getAllFriends();
        adapter = new FriendsAdapter(this, friendsList, dbHelper);
        adapter.setOnDataChangeListener(() -> {
            // 当数据发生变化时重新加载列表
            friendsList = dbHelper.getAllFriends();
            adapter = new FriendsAdapter(friendActivity.this, friendsList, dbHelper);
            friendsListView.setAdapter(adapter);
        });
        friendsListView.setAdapter(adapter);
    }

    private void searchFriends(String query) {
        if (query.isEmpty()) {
            loadFriends();
        } else {
            friendsList = dbHelper.searchFriends(query);
            adapter = new FriendsAdapter(this, friendsList, dbHelper);
            friendsListView.setAdapter(adapter);
        }
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加好友");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_friend, null);
        final EditText searchInput = view.findViewById(R.id.search_input);

        // 隐藏不需要的字段
        view.findViewById(R.id.name_layout).setVisibility(View.GONE);
        view.findViewById(R.id.phone_layout).setVisibility(View.GONE);
        view.findViewById(R.id.email_layout).setVisibility(View.GONE);

        // 添加搜索提示
        searchInput.setHint("请输入用户名、电话或邮箱");

        builder.setView(view);

        // 设置按钮
        builder.setPositiveButton("搜索", (dialog, which) -> {
            // 这里留空，我们将在对话框显示后重写点击事件
        });

        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // 显示后自动弹出输入法
        searchInput.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // 重写确定按钮的点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String input = searchInput.getText().toString().trim();
            Log.d("AddFriend", "Search input: " + input);

            if (input.isEmpty()) {
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查是否输入的是当前用户信息
            if (input.equals(CURRENT_USER_NAME) || input.equals(CURRENT_USER_PHONE) || input.equals(CURRENT_USER_EMAIL)) {
                Toast.makeText(this, "不能添加自己为好友", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查是否匹配预设好友
            Friend foundFriend = findPresetFriend(input);
            Log.d("AddFriend", "Found friend: " + (foundFriend != null ? foundFriend.getName() : "null"));

            if (foundFriend != null) {
                // 检查是否已经是好友
                if (isFriendAlreadyAdded(foundFriend)) {
                    Toast.makeText(this, "该用户已经是您的好友", Toast.LENGTH_SHORT).show();
                } else {
                    // 添加好友
                    dbHelper.addFriend(foundFriend);
                    loadFriends(); // 重新加载列表
                    Toast.makeText(this, "成功添加好友: " + foundFriend.getName(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(this, "不存在此用户", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 在预设好友中查找匹配的用户
    private Friend findPresetFriend(String input) {
        for (int i = 0; i < PRESET_FRIENDS_NAMES.length; i++) {
            if (input.equals(PRESET_FRIENDS_NAMES[i]) ||
                    input.equals(PRESET_FRIENDS_PHONES[i]) ||
                    input.equals(PRESET_FRIENDS_EMAILS[i])) {

                return new Friend(
                        PRESET_FRIENDS_NAMES[i],
                        PRESET_FRIENDS_PHONES[i],
                        PRESET_FRIENDS_EMAILS[i],
                        "default_avatar"
                );
            }
        }
        return null;
    }

    // 检查用户是否已经是好友
    private boolean isFriendAlreadyAdded(Friend friend) {
        for (Friend f : friendsList) {
            if (f.getName().equals(friend.getName()) ||
                    f.getPhone().equals(friend.getPhone()) ||
                    f.getEmail().equals(friend.getEmail())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}