package com.example.dictionary;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class FriendsAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> friendsList;
    private FriendsDatabaseHelper dbHelper;
    private OnDataChangeListener dataChangeListener;

    public FriendsAdapter(Context context, List<Friend> friendsList, FriendsDatabaseHelper dbHelper) {
        this.context = context;
        this.friendsList = friendsList;
        this.dbHelper = dbHelper;
    }

    public void setOnDataChangeListener(OnDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    @Override
    public int getCount() {
        return friendsList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return friendsList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.friend_name);
            holder.phoneTextView = convertView.findViewById(R.id.friend_phone);
            holder.editButton = convertView.findViewById(R.id.edit_button);
            holder.deleteButton = convertView.findViewById(R.id.delete_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Friend friend = friendsList.get(position);

        holder.nameTextView.setText(friend.getName());
        holder.phoneTextView.setText(friend.getPhone());

        // 编辑按钮点击事件
        holder.editButton.setOnClickListener(v -> {
            showEditFriendDialog(friend, position);
        });

        // 删除按钮点击事件
        holder.deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog(friend, position);
        });

        return convertView;
    }

    // ViewHolder模式提高列表性能
    private static class ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        ImageButton editButton;
        ImageButton deleteButton;
    }

    private void showEditFriendDialog(Friend friend, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("编辑好友");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_friend, null);
        final EditText nameEditText = view.findViewById(R.id.name_edit_text);
        final EditText phoneEditText = view.findViewById(R.id.phone_edit_text);
        final EditText emailEditText = view.findViewById(R.id.email_edit_text);

        // 隐藏搜索框
        view.findViewById(R.id.search_input).setVisibility(View.GONE);

        // 显示编辑字段
        view.findViewById(R.id.name_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.phone_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.email_layout).setVisibility(View.VISIBLE);

        nameEditText.setText(friend.getName());
        phoneEditText.setText(friend.getPhone());
        emailEditText.setText(friend.getEmail());

        builder.setView(view);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            if (!name.isEmpty()) {
                friend.setName(name);
                friend.setPhone(phone);
                friend.setEmail(email);

                dbHelper.updateFriend(friend);
                notifyDataSetChanged();

                if (dataChangeListener != null) {
                    dataChangeListener.onDataChanged();
                }
            } else {
                Toast.makeText(context, "姓名不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void showDeleteConfirmationDialog(Friend friend, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除好友");
        builder.setMessage("确定要删除 " + friend.getName() + " 吗？");

        builder.setPositiveButton("删除", (dialog, which) -> {
            dbHelper.deleteFriend(friend.getId());
            friendsList.remove(position);
            notifyDataSetChanged();

            if (dataChangeListener != null) {
                dataChangeListener.onDataChanged();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    public interface OnDataChangeListener {
        void onDataChanged();
    }
}