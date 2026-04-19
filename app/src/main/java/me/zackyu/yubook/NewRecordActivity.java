package me.zackyu.yubook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.zackyu.yubook.db.iDBHelper;

public class NewRecordActivity extends AppCompatActivity {

    private iDBHelper iDBHelper;
    private Button buttonIncome;
    private Button buttonPay;
    private Button buttonShortcut;
    private Button buttonSoundRecord;
    private Button buttonBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);

        initViews();
        initDBHelper();
        setupListeners();
    }

    private void initViews() {
        buttonIncome = findViewById(R.id.button_income);
        buttonPay = findViewById(R.id.button_pay);
        buttonShortcut = findViewById(R.id.button_shortcut);
        buttonSoundRecord = findViewById(R.id.button_sound_record);
        buttonBack = findViewById(R.id.button_new_record_return);
    }

    private void initDBHelper() {
        try {
            iDBHelper = new iDBHelper(this, "MyAccount.db", null, 1);
            iDBHelper.getWritableDatabase();
        } catch (Exception e) {
            Toast.makeText(NewRecordActivity.this, "数据库初始化失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        // 收入按钮
        buttonIncome.setOnClickListener(v -> {
            Intent intent = new Intent(NewRecordActivity.this, NewIncomeActivity.class);
            startActivity(intent);
        });

        // 支出按钮
        buttonPay.setOnClickListener(v -> {
            Intent intent = new Intent(NewRecordActivity.this, NewPayActivity.class);
            startActivity(intent);
        });

        // 快捷记账
        buttonShortcut.setOnClickListener(v -> {
            Toast.makeText(NewRecordActivity.this, "快捷记账功能开发中...", Toast.LENGTH_SHORT).show();
            // TODO: 实现快捷记账
            Intent intent = new Intent(NewRecordActivity.this, QuickRecordActivity.class);
            startActivity(intent);
        });

        // 语音记账
        buttonSoundRecord.setOnClickListener(v -> {
            Toast.makeText(NewRecordActivity.this, "语音记账功能开发中...", Toast.LENGTH_SHORT).show();
            // TODO: 实现语音记账
        });

        // 返回按钮
        buttonBack.setOnClickListener(v -> finish());
    }
}