package me.zackyu.yubook;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.zackyu.yubook.db.iDBHelper;

public class NewRecordActivity extends AppCompatActivity {

    private iDBHelper iDBHelper;
    private Button buttonIncome;
    private Button buttonPay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);
        setTitle("charge to an account");

        initViews();
        initDBHelper();
        setListeners();
    }

    private void initViews() {
        buttonIncome = findViewById(R.id.button_income);
        buttonPay = findViewById(R.id.button_pay);
    }

    private void initDBHelper() {
        iDBHelper = new iDBHelper(this, "MyAccount.db", null, 1);
    }

    private void setListeners() {
        buttonIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(iDBHelper.getDatabaseName())) {
                    Toast.makeText(NewRecordActivity.this, "数据库名称为空The database name is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                iDBHelper.getWritableDatabase();
                Toast.makeText(NewRecordActivity.this, "记账charge to an account: " + iDBHelper.getDatabaseName(), Toast.LENGTH_LONG);
                Intent intent = new Intent(NewRecordActivity.this, NewIncomeActivity.class);
                startActivity(intent);
            }
        });

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewRecordActivity.this, NewPayActivity.class);
                startActivity(intent);
            }
        });
    }
}