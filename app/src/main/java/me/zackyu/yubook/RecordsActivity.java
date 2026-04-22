package me.zackyu.yubook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.zackyu.yubook.constant.DBConstant;
import me.zackyu.yubook.db.Record;
import me.zackyu.yubook.db.iDBHelper;

public class RecordsActivity extends AppCompatActivity {

    private static final String TAG = "RecordsActivity";

    // 视图组件
    private TextView textTitle;
    private TextView tvTotalCount;
    private ListView recordListView;
    private View emptyView;

    // 按钮
    private Button buttonRecordsAll;
    private Button buttonRecordsIncome;
    private Button buttonRecordsPay;
    private Button buttonRecordsBack;

    // 数据
    private RecordAdapter recordAdapter;
    private iDBHelper iDBHelper;
    private List<Record> allRecords;
    private List<Record> incomeRecords;
    private List<Record> expenseRecords;
    private List<Record> currentRecords;

    // 当前筛选类型
    private int currentFilter = 0; // 0:全部, 1:收入, 2:支出

    // 日期格式化
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        initViews();
        initDBHelper();
        initData();
        setupListeners();

        // 默认加载全部数据
        loadAllRecords();
    }

    private void initViews() {
        buttonRecordsAll = findViewById(R.id.button_records_all);
        buttonRecordsIncome = findViewById(R.id.button_records_income);
        buttonRecordsPay = findViewById(R.id.button_records_pay);
        buttonRecordsBack = findViewById(R.id.button_records_back);
        textTitle = findViewById(R.id.text_title);
        tvTotalCount = findViewById(R.id.tv_total_count);
        recordListView = findViewById(R.id.record_list);
        emptyView = findViewById(R.id.empty_view);
    }

    private void initDBHelper() {
        iDBHelper = new iDBHelper(RecordsActivity.this, DBConstant.NAME, null, 1);
    }

    private void initData() {
        allRecords = new ArrayList<>();
        incomeRecords = new ArrayList<>();
        expenseRecords = new ArrayList<>();
        currentRecords = allRecords;
    }

    private void setupListeners() {
        // 全部按钮
        buttonRecordsAll.setOnClickListener(v -> {
            currentFilter = 0;
            textTitle.setText("全部");
            updateButtonStyles();
            loadAllRecords();
        });

        // 收入按钮
        buttonRecordsIncome.setOnClickListener(v -> {
            currentFilter = 1;
            textTitle.setText("收入");
            updateButtonStyles();
            loadIncomeRecords();
        });

        // 支出按钮
        buttonRecordsPay.setOnClickListener(v -> {
            currentFilter = 2;
            textTitle.setText("支出");
            updateButtonStyles();
            loadExpenseRecords();
        });

        // 返回按钮
        buttonRecordsBack.setOnClickListener(v -> finish());

        // 列表点击事件
        recordListView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentRecords != null && position < currentRecords.size()) {
                Record record = currentRecords.get(position);
                showRecordDetail(record);
            }
        });
    }

    private void updateButtonStyles() {
        // 重置所有按钮样式
        buttonRecordsAll.setBackgroundResource(R.drawable.btn_filter_secondary);
        buttonRecordsIncome.setBackgroundResource(R.drawable.btn_filter_secondary);
        buttonRecordsPay.setBackgroundResource(R.drawable.btn_filter_secondary);

        // 设置当前选中按钮样式
        switch (currentFilter) {
            case 0:
                buttonRecordsAll.setBackgroundResource(R.drawable.btn_filter_all);
                break;
            case 1:
                buttonRecordsIncome.setBackgroundResource(R.drawable.btn_filter_income);
                break;
            case 2:
                buttonRecordsPay.setBackgroundResource(R.drawable.btn_filter_expense);
                break;
        }
    }

    private void loadAllRecords() {
        allRecords.clear();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = iDBHelper.getReadableDatabase();
            cursor = db.query(DBConstant.TNAME, null, null, null, null, null, "id DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Record record = parseRecordFromCursor(cursor);
                    if (record != null) {
                        allRecords.add(record);
                    }
                } while (cursor.moveToNext());
            }

            currentRecords = allRecords;
            updateAdapter();

        } catch (Exception e) {
            Log.e(TAG, "loadAllRecords error", e);
            Toast.makeText(this, "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    private void loadIncomeRecords() {
        incomeRecords.clear();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = iDBHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + DBConstant.TNAME + " WHERE amount > 0 ORDER BY id DESC";
            cursor = db.rawQuery(sql, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Record record = parseRecordFromCursor(cursor);
                    if (record != null) {
                        incomeRecords.add(record);
                    }
                } while (cursor.moveToNext());
            }

            currentRecords = incomeRecords;
            updateAdapter();

        } catch (Exception e) {
            Log.e(TAG, "loadIncomeRecords error", e);
            Toast.makeText(this, "加载收入失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    private void loadExpenseRecords() {
        expenseRecords.clear();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = iDBHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + DBConstant.TNAME + " WHERE amount < 0 ORDER BY id DESC";
            cursor = db.rawQuery(sql, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Record record = parseRecordFromCursor(cursor);
                    if (record != null) {
                        expenseRecords.add(record);
                    }
                } while (cursor.moveToNext());
            }

            currentRecords = expenseRecords;
            updateAdapter();

        } catch (Exception e) {
            Log.e(TAG, "loadExpenseRecords error", e);
            Toast.makeText(this, "加载支出失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    private Record parseRecordFromCursor(Cursor cursor) {
        try {
            Record record = new Record();
            int id = cursor.getInt(0);
            String source = cursor.getString(1);
            String type = cursor.getString(2);
            String account = cursor.getString(3);
            double amount = cursor.getDouble(4);
            String dateStr = cursor.getString(5);

            Date crtTime = dateFormat.parse(dateStr);

            record.setId(id);
            record.setSource(source);
            record.setType(type);
            record.setAccount(account);
            record.setAmount(amount);
            record.setCrttime(crtTime);

            return record;
        } catch (ParseException e) {
            Log.e(TAG, "parseRecordFromCursor error", e);
            return null;
        }
    }

    private void updateAdapter() {
        runOnUiThread(() -> {
            if (recordAdapter == null) {
                recordAdapter = new RecordAdapter(RecordsActivity.this, R.layout.record_item, currentRecords);
                recordListView.setAdapter(recordAdapter);
            } else {
                recordAdapter.updateData(currentRecords);
            }

            // 更新总条数
            int count = currentRecords != null ? currentRecords.size() : 0;
            tvTotalCount.setText(String.valueOf(count));

            // 显示/隐藏空视图
            if (count == 0) {
                emptyView.setVisibility(View.VISIBLE);
                recordListView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recordListView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showRecordDetail(Record record) {
        if (record == null) return;

        String type = record.getAmount() > 0 ? "收入" : "支出";
        int typeColor = record.getAmount() > 0 ? 0xFF4CAF50 : 0xFFFF6B6B;
        String amountText = String.format(Locale.getDefault(), "¥ %.2f", Math.abs(record.getAmount()));

        // 创建自定义对话框视图
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_record_detail, null);

        // 获取控件
        TextView tvType = dialogView.findViewById(R.id.tv_detail_type);
        TextView tvTypeValue = dialogView.findViewById(R.id.tv_detail_type_value);
        TextView tvSource = dialogView.findViewById(R.id.tv_detail_source);
        TextView tvSourceValue = dialogView.findViewById(R.id.tv_detail_source_value);
        TextView tvCategory = dialogView.findViewById(R.id.tv_detail_category);
        TextView tvCategoryValue = dialogView.findViewById(R.id.tv_detail_category_value);
        TextView tvAccount = dialogView.findViewById(R.id.tv_detail_account);
        TextView tvAccountValue = dialogView.findViewById(R.id.tv_detail_account_value);
        TextView tvAmount = dialogView.findViewById(R.id.tv_detail_amount);
        TextView tvAmountValue = dialogView.findViewById(R.id.tv_detail_amount_value);
        TextView tvTime = dialogView.findViewById(R.id.tv_detail_time);
        TextView tvTimeValue = dialogView.findViewById(R.id.tv_detail_time_value);
        View goldLine = dialogView.findViewById(R.id.gold_line_detail);

        // 设置数据
        tvTypeValue.setText(type);
        tvTypeValue.setTextColor(typeColor);

        tvSourceValue.setText(record.getSource() != null ? record.getSource() : "未知");
        tvCategoryValue.setText(record.getType() != null ? record.getType() : "未知");
        tvAccountValue.setText(record.getAccount() != null ? record.getAccount() : "未知");
        tvAmountValue.setText(amountText);
        tvAmountValue.setTextColor(typeColor);
        tvTimeValue.setText(record.getCrttime() != null ? dateFormat.format(record.getCrttime()) : "未知");

        // 应用主题颜色
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String themeColor = prefs.getString("theme_color", "gold");
        int goldColor = getThemeColor(themeColor);
        if (goldLine != null) {
            goldLine.setBackgroundColor(goldColor);
        }

        // 创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.GlassDialogTheme)
                .setView(dialogView)
                .setPositiveButton("关闭", null)
                .create();

        dialog.show();

        // 设置按钮样式
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_glass_ultraclear));
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            positiveButton.setTextSize(14);
            positiveButton.setPadding(40, 12, 40, 12);
        }
    }

    private int getThemeColor(String themeColor) {
        switch (themeColor) {
            case "gold": return 0xFFD4AF37;
            case "blue": return 0xFF007AFF;
            case "green": return 0xFF34C759;
            case "purple": return 0xFFAF52DE;
            default: return 0xFFD4AF37;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 刷新当前列表
        switch (currentFilter) {
            case 0:
                loadAllRecords();
                break;
            case 1:
                loadIncomeRecords();
                break;
            case 2:
                loadExpenseRecords();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iDBHelper != null) {
            iDBHelper.close();
        }
    }
}