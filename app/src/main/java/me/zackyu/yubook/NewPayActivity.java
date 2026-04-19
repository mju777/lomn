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

public class NewPayActivity extends AppCompatActivity {

    // 视图组件
    private Button buttonNewPayRecord;
    private Button buttonNewPayBack;
    private Button buttonNewPayReset;
    private Spinner newPaySource;
    private Spinner newPayType;
    private EditText newPayAccount;
    private EditText newPayAmount;

    // 数据
    private String payCategory;     // 支出类型（如：餐饮、购物）
    private String payDestination;  // 支出去向（如：微信、支付宝）
    private String account;
    private double amount;
    private final Date crtTime = new Date();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private iDBHelper iDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pay_record);

        initViews();
        setupListeners();
    }

    private void initViews() {
        buttonNewPayRecord = findViewById(R.id.button_new_pay_record);
        buttonNewPayBack = findViewById(R.id.button_new_pay_back);
        buttonNewPayReset = findViewById(R.id.button_new_pay_reset);
        newPaySource = findViewById(R.id.new_pay_sources);
        newPayType = findViewById(R.id.new_pay_type);
        newPayAccount = findViewById(R.id.new_pay_account);
        newPayAmount = findViewById(R.id.new_pay_amount);
    }

    private void setupListeners() {
        // 支出类型选择（餐饮、购物等）
        newPaySource.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                payCategory = getResources().getStringArray(R.array.pay_from)[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                payCategory = getResources().getStringArray(R.array.pay_from)[0];
            }
        });

        // 支出去向选择（微信、支付宝等）
        newPayType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                payDestination = getResources().getStringArray(R.array.pay_to)[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                payDestination = getResources().getStringArray(R.array.pay_to)[0];
            }
        });

        // 保存支出记录
        buttonNewPayRecord.setOnClickListener(v -> saveExpenseRecord());

        // 重置表单
        buttonNewPayReset.setOnClickListener(v -> resetForm());

        // 返回按钮
        buttonNewPayBack.setOnClickListener(v -> finish());
    }

    private void saveExpenseRecord() {
        // 获取输入值
        account = newPayAccount.getText().toString().trim();
        String amountStr = newPayAmount.getText().toString().trim();

        // 验证账户
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请输入账户名称", Toast.LENGTH_SHORT).show();
            newPayAccount.requestFocus();
            return;
        }

        // 验证金额
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            newPayAmount.requestFocus();
            return;
        }

        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的金额格式", Toast.LENGTH_SHORT).show();
            newPayAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "金额必须大于0", Toast.LENGTH_SHORT).show();
            newPayAmount.requestFocus();
            return;
        }

        // 支出金额存储为负数
        double negativeAmount = -amount;

        // 保存到数据库
        try {
            iDBHelper = new iDBHelper(NewPayActivity.this, DBConstant.NAME, null, 1);
            SQLiteDatabase db = iDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConstant.SOURCE, payDestination);      // 去向（如：微信）
            values.put(DBConstant.TYPE, payCategory);           // 类型（如：餐饮）
            values.put(DBConstant.ACCOUNT, account);
            values.put(DBConstant.AMOUNT, negativeAmount);      // 存储为负数
            values.put(DBConstant.CRTTIME, dateFormat.format(crtTime));

            long result = db.insert(DBConstant.TNAME, null, values);
            db.close();

            if (result != -1) {
                Toast.makeText(this, "支出记录保存成功 ✓", Toast.LENGTH_LONG).show();

                // 跳转到主页
                Intent intent = new Intent(NewPayActivity.this, MainActivity.class);
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
        newPaySource.setSelection(0);
        newPayType.setSelection(0);

        // 清空输入框
        newPayAccount.setText("");
        newPayAmount.setText("");

        // 清空变量
        account = "";
        amount = 0;

        // 聚焦到账户输入框
        newPayAccount.requestFocus();

        Toast.makeText(this, "表单已重置", Toast.LENGTH_SHORT).show();
    }
}