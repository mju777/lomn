package me.zackyu.yubook;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.zackyu.yubook.constant.DBConstant;
import me.zackyu.yubook.db.iDBHelper;
import me.zackyu.yubook.util.NeutralDialogFragment;

public class NewIncomeActivity extends AppCompatActivity {

    private Button buttonNewIncomeBack;
    private Button buttonNewIncomeRecord;
    private Spinner newIncomeType;
    private EditText newIncomeAccount;
    private EditText newIncomeAmount;
    private Spinner newIncomeSource;

    private String type;
    private String account;
    private Double amount;
    private String source;
    private final Date crtTime = new Date();
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private iDBHelper iDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_income_record);

        initViews();
        setListeners();
    }

    private void initViews() {
        buttonNewIncomeBack = findViewById(R.id.button_new_income_back);
        buttonNewIncomeRecord = findViewById(R.id.button_new_income_record);
        newIncomeSource = findViewById(R.id.new_income_source);
        newIncomeType = findViewById(R.id.new_income_type);
        newIncomeAccount = findViewById(R.id.new_income_account);
        newIncomeAmount = findViewById(R.id.new_income_amount);
    }

    private void setListeners() {
        newIncomeSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = getResources().getStringArray(R.array.income_from)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        newIncomeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                source = getResources().getStringArray(R.array.income_to)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonNewIncomeRecord.setOnClickListener(_ -> {
            account = newIncomeAccount.getText().toString();
            String strAmount = newIncomeAmount.getText().toString();

            if (TextUtils.isEmpty(strAmount)) {
                amount = 0d;
            } else {
                amount = Double.parseDouble(strAmount);
            }

            iDBHelper = new iDBHelper(NewIncomeActivity.this, DBConstant.NAME, null, 1);
            SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstant.SOURCE, source);
            contentValues.put(DBConstant.TYPE, type);
            contentValues.put(DBConstant.ACCOUNT, account);
            contentValues.put(DBConstant.AMOUNT, amount);
            contentValues.put(DBConstant.CRTTIME, simpleDateFormat.format(crtTime));

            if (TextUtils.isEmpty(account)) {
                showDialog("Enter your account！");
            } else if (amount <= 0) {
                showDialog("Check the amount you entered！");
            } else {
                sqLiteDatabase.insert(DBConstant.TNAME, null, contentValues);
                Intent intent = new Intent(NewIncomeActivity.this, MainActivity.class);
                startActivity(intent);
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Record saved. Return to homepage", Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        });

        buttonNewIncomeBack.setOnClickListener(_ -> finish());
    }

    private void showDialog(String message) {
        NeutralDialogFragment neutralDialogFragment = NeutralDialogFragment.createNeutralDialogFragment();
        neutralDialogFragment.show("point out",
                message,
                "confirm",
                (_, _) -> {
                    // 可以根据需要添加点击确定后的操作
                },
                getFragmentManager());
    }
}