package com.example.dictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class FriendsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "friends.db";
    private static final int DATABASE_VERSION = 2;

    // 表名和列名
    public static final String TABLE_FRIENDS = "friends";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_AVATAR = "avatar";



    // 创建表的SQL语句
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_FRIENDS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_PHONE + " TEXT, "
                    + COLUMN_EMAIL + " TEXT, "
                    + COLUMN_AVATAR + " TEXT);";

    public FriendsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        // 移除了自动插入示例数据的代码
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }

    // 添加好友
    public long addFriend(Friend friend) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, friend.getName());
        values.put(COLUMN_PHONE, friend.getPhone());
        values.put(COLUMN_EMAIL, friend.getEmail());
        values.put(COLUMN_AVATAR, friend.getAvatar());

        long id = db.insert(TABLE_FRIENDS, null, values);
        db.close();
        return id;
    }

    // 获取所有好友
    public List<Friend> getAllFriends() {
        List<Friend> friends = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS + " ORDER BY " + COLUMN_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Friend friend = new Friend();
                friend.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                friend.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                friend.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                friend.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                friend.setAvatar(cursor.getString(cursor.getColumnIndex(COLUMN_AVATAR)));

                friends.add(friend);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return friends;
    }

    // 搜索好友
    public List<Friend> searchFriends(String query) {
        List<Friend> friends = new ArrayList<>();
        String searchQuery = "SELECT * FROM " + TABLE_FRIENDS +
                " WHERE " + COLUMN_NAME + " LIKE ? OR " +
                COLUMN_PHONE + " LIKE ? OR " +
                COLUMN_EMAIL + " LIKE ? " +
                " ORDER BY " + COLUMN_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(searchQuery, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                Friend friend = new Friend();
                friend.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                friend.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                friend.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                friend.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                friend.setAvatar(cursor.getString(cursor.getColumnIndex(COLUMN_AVATAR)));

                friends.add(friend);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return friends;
    }

    // 更新好友信息
    public int updateFriend(Friend friend) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, friend.getName());
        values.put(COLUMN_PHONE, friend.getPhone());
        values.put(COLUMN_EMAIL, friend.getEmail());
        values.put(COLUMN_AVATAR, friend.getAvatar());

        int rowsAffected = db.update(TABLE_FRIENDS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(friend.getId())});
        db.close();
        return rowsAffected;
    }

    // 删除好友
    public void deleteFriend(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRIENDS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 检查好友是否已存在
    public boolean isFriendExists(String name, String phone, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FRIENDS +
                " WHERE " + COLUMN_NAME + " = ? OR " +
                COLUMN_PHONE + " = ? OR " +
                COLUMN_EMAIL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{name, phone, email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}