package me.zackyu.yubook;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.zackyu.yubook.constant.DBConstant;
import me.zackyu.yubook.db.iDBHelper;

public class NewIncomeActivity extends AppCompatActivity {

    // 视图组件
    private Button buttonNewIncomeBack;
    private Button buttonNewIncomeRecord;
    private Button buttonNewIncomeReset;
    private Spinner newIncomeSource;
    private Spinner newIncomeType;
    private EditText newIncomeAccount;
    private EditText newIncomeAmount;

    // 数据
    private String incomeSource;  // 收入来源
    private String incomeCategory; // 收入分类
    private String account;
    private double amount;
    private final Date crtTime = new Date();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private iDBHelper iDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_income_record);

        initViews();
        setupListeners();
    }

    private void initViews() {
        buttonNewIncomeBack = findViewById(R.id.button_new_income_back);
        buttonNewIncomeRecord = findViewById(R.id.button_new_income_record);
        buttonNewIncomeReset = findViewById(R.id.button_new_income_reset);
        newIncomeSource = findViewById(R.id.new_income_source);
        newIncomeType = findViewById(R.id.new_income_type);
        newIncomeAccount = findViewById(R.id.new_income_account);
        newIncomeAmount = findViewById(R.id.new_income_amount);
    }

    private void setupListeners() {
        // 收入来源选择
        newIncomeSource.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                incomeSource = getResources().getStringArray(R.array.income_from)[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                incomeSource = getResources().getStringArray(R.array.income_from)[0];
            }
        });

        // 收入分类选择
        newIncomeType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                incomeCategory = getResources().getStringArray(R.array.income_to)[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                incomeCategory = getResources().getStringArray(R.array.income_to)[0];
            }
        });

        // 保存收入记录
        buttonNewIncomeRecord.setOnClickListener(v -> saveIncomeRecord());

        // 重置表单
        buttonNewIncomeReset.setOnClickListener(v -> resetForm());

        // 返回按钮
        buttonNewIncomeBack.setOnClickListener(v -> finish());
    }

    private void saveIncomeRecord() {
        // 获取输入值
        account = newIncomeAccount.getText().toString().trim();
        String amountStr = newIncomeAmount.getText().toString().trim();

        // 验证账户
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请输入账户名称", Toast.LENGTH_SHORT).show();
            newIncomeAccount.requestFocus();
            return;
        }

        // 验证金额
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            newIncomeAmount.requestFocus();
            return;
        }

        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的金额格式", Toast.LENGTH_SHORT).show();
            newIncomeAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "金额必须大于0", Toast.LENGTH_SHORT).show();
            newIncomeAmount.requestFocus();
            return;
        }

        // 保存到数据库
        try {
            iDBHelper = new iDBHelper(NewIncomeActivity.this, DBConstant.NAME, null, 1);
            SQLiteDatabase db = iDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConstant.SOURCE, incomeSource);
            values.put(DBConstant.TYPE, incomeCategory);
            values.put(DBConstant.ACCOUNT, account);
            values.put(DBConstant.AMOUNT, amount);
            values.put(DBConstant.CRTTIME, dateFormat.format(crtTime));

            long result = db.insert(DBConstant.TNAME, null, values);
            db.close();

            if (result != -1) {
                Toast.makeText(this, "收入记录保存成功 ✓", Toast.LENGTH_LONG).show();

                // 跳转到主页
                Intent intent = new Intent(NewIncomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "数据库错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetForm() {
        // 重置 Spinner 到第一项
        newIncomeSource.setSelection(0);
        newIncomeType.setSelection(0);

        // 清空输入框
        newIncomeAccount.setText("");
        newIncomeAmount.setText("");

        // 清空变量
        account = "";
        amount = 0;

        // 聚焦到账户输入框
        newIncomeAccount.requestFocus();

        Toast.makeText(this, "表单已重置", Toast.LENGTH_SHORT).show();
    }
}