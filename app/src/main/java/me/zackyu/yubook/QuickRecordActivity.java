package me.zackyu.yubook;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.zackyu.yubook.constant.DBConstant;
import me.zackyu.yubook.db.iDBHelper;

public class QuickRecordActivity extends AppCompatActivity {

    // 视图组件
    private Button btnBack;
    private Button btnIncome;
    private Button btnExpense;
    private EditText etAmount;
    private EditText etCategory;
    private Spinner spinnerAccount;
    private EditText etNote;
    private Button btnSave;

    // 分类按钮
    private Button categoryFood;
    private Button categoryShopping;
    private Button categoryTransport;
    private Button categoryEntertainment;
    private Button categorySalary;
    private Button categoryOther;

    // 数据
    private boolean isIncome = true;
    private iDBHelper iDBHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final Date crtTime = new Date();

    // 线程池
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_record);

        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        initDBHelper();
        setupListeners();
        updateCategoryButtons();
    }

    private void initViews() {
        btnBack = findViewById(R.id.button_quick_back);
        btnIncome = findViewById(R.id.btn_quick_income);
        btnExpense = findViewById(R.id.btn_quick_expense);
        etAmount = findViewById(R.id.et_quick_amount);
        etCategory = findViewById(R.id.et_quick_category);
        spinnerAccount = findViewById(R.id.spinner_account);
        etNote = findViewById(R.id.et_quick_note);
        btnSave = findViewById(R.id.btn_quick_save);

        categoryFood = findViewById(R.id.category_food);
        categoryShopping = findViewById(R.id.category_shopping);
        categoryTransport = findViewById(R.id.category_transport);
        categoryEntertainment = findViewById(R.id.category_entertainment);
        categorySalary = findViewById(R.id.category_salary);
        categoryOther = findViewById(R.id.category_other);

        // 设置金额输入框默认样式
        etAmount.requestFocus();
    }

    private void initDBHelper() {
        try {
            iDBHelper = new iDBHelper(this, DBConstant.NAME, null, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "数据库初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnIncome.setOnClickListener(v -> {
            isIncome = true;
            updateTypeButtons();
            updateCategoryButtons();
            etCategory.setText("");
            etCategory.setHint("请输入收入来源（如：工资、兼职）");
        });

        btnExpense.setOnClickListener(v -> {
            isIncome = false;
            updateTypeButtons();
            updateCategoryButtons();
            etCategory.setText("");
            etCategory.setHint("请输入支出分类（如：餐饮、购物）");
        });

        // 分类按钮点击
        categoryFood.setOnClickListener(v -> {
            if (isIncome) setCategory("工资");
            else setCategory("餐饮");
        });

        categoryShopping.setOnClickListener(v -> {
            if (isIncome) setCategory("兼职");
            else setCategory("购物");
        });

        categoryTransport.setOnClickListener(v -> {
            if (isIncome) setCategory("奖金");
            else setCategory("交通");
        });

        categoryEntertainment.setOnClickListener(v -> {
            if (isIncome) setCategory("理财");
            else setCategory("娱乐");
        });

        categorySalary.setOnClickListener(v -> {
            if (isIncome) setCategory("红包");
            else setCategory("医疗");
        });

        categoryOther.setOnClickListener(v -> {
            if (isIncome) setCategory("其他收入");
            else setCategory("其他支出");
        });

        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void updateTypeButtons() {
        if (isIncome) {
            btnIncome.setBackgroundResource(R.drawable.btn_income_selected);
            btnExpense.setBackgroundResource(R.drawable.btn_expense);
        } else {
            btnIncome.setBackgroundResource(R.drawable.btn_income);
            btnExpense.setBackgroundResource(R.drawable.btn_expense_selected);
        }
    }

    private void updateCategoryButtons() {
        if (isIncome) {
            categoryFood.setText("💰 工资");
            categoryShopping.setText("💼 兼职");
            categoryTransport.setText("🎁 奖金");
            categoryEntertainment.setText("📈 理财");
            categorySalary.setText("🧧 红包");
            categoryOther.setText("📦 其他");
            etCategory.setHint("请输入收入来源（如：工资、兼职）");
        } else {
            categoryFood.setText("🍜 餐饮");
            categoryShopping.setText("🛒 购物");
            categoryTransport.setText("🚗 交通");
            categoryEntertainment.setText("🎬 娱乐");
            categorySalary.setText("🏥 医疗");
            categoryOther.setText("📦 其他");
            etCategory.setHint("请输入支出分类（如：餐饮、购物）");
        }
    }

    private void setCategory(String category) {
        etCategory.setText(category);
    }

    private void saveRecord() {
        // 禁用保存按钮，防止重复点击
        btnSave.setEnabled(false);
        btnSave.setText("保存中...");

        // 获取金额
        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("💾 一键保存");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的金额格式", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("💾 一键保存");
            etAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "金额必须大于0", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("💾 一键保存");
            etAmount.requestFocus();
            return;
        }

        // 获取分类
        String category = etCategory.getText().toString().trim();
        if (TextUtils.isEmpty(category)) {
            String hint = isIncome ? "请输入收入来源" : "请输入支出分类";
            Toast.makeText(this, hint, Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("💾 一键保存");
            etCategory.requestFocus();
            return;
        }

        // 获取账户
        String account = spinnerAccount.getSelectedItem().toString();

        // 获取备注
        String note = etNote.getText().toString().trim();

        // 计算最终金额
        double finalAmount = isIncome ? amount : -amount;

        // 准备保存的数据
        String source;
        String type;

        if (isIncome) {
            source = category;
            type = "收入";
        } else {
            source = account;
            type = category;
        }

        if (!TextUtils.isEmpty(note)) {
            type = type + "（" + note + "）";
        }

        final String finalSource = source;
        final String finalType = type;
        final String finalAccount = account;
        final double finalFinalAmount = finalAmount;

        // 在后台线程执行数据库操作
        executorService.execute(() -> {
            boolean success = saveToDatabase(finalSource, finalType, finalAccount, finalFinalAmount);

            mainHandler.post(() -> {
                btnSave.setEnabled(true);
                btnSave.setText("💾 一键保存");

                if (success) {
                    String typeText = isIncome ? "收入" : "支出";
                    Toast.makeText(QuickRecordActivity.this, typeText + "记录保存成功 ✓", Toast.LENGTH_LONG).show();

                    // 跳转到主页
                    Intent intent = new Intent(QuickRecordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(QuickRecordActivity.this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean saveToDatabase(String source, String type, String account, double amount) {
        SQLiteDatabase db = null;
        try {
            if (iDBHelper == null) {
                iDBHelper = new iDBHelper(this, DBConstant.NAME, null, 1);
            }
            db = iDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConstant.SOURCE, source);
            values.put(DBConstant.TYPE, type);
            values.put(DBConstant.ACCOUNT, account);
            values.put(DBConstant.AMOUNT, amount);
            values.put(DBConstant.CRTTIME, dateFormat.format(crtTime));

            long result = db.insert(DBConstant.TNAME, null, values);
            return result != -1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}