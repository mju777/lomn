package me.zackyu.yubook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import me.zackyu.yubook.constant.DBConstant;

public class iDBHelper extends SQLiteOpenHelper {

    private final Context mContext;

    public static final String CREATE_ACCOUNT_DETAIL_TABLE =
            "create table "+ DBConstant.TNAME +" (" +
                    "id integer primary key autoincrement, " +
                    "type text, " +
                    "source text, " +
                    "account text, " +
                    "amount real, " +
                    "crttime NUMERIC)";

    public static final String CREATE_ACCOUNT_NAME_TABLE =
            "create table AccountName (" +
                    "id integer primary key autoincrement, " +
                    "aname text)";

    public iDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(CREATE_ACCOUNT_DETAIL_TABLE);
            sqLiteDatabase.execSQL(CREATE_ACCOUNT_NAME_TABLE);
            Toast.makeText(mContext, "create table！", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("DB_CREATE_ERROR", "创建表时出错: " + e.getMessage());
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 您可以根据需要在这里添加数据库升级的逻辑
    }
}