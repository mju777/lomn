package me.zackyu.yubook;

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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.zackyu.yubook.constant.DBConstant;
import me.zackyu.yubook.db.iDBHelper;
import me.zackyu.yubook.util.NeutralDialogFragment;

public class NewPayActivity extends AppCompatActivity {

    private Button buttonNewPayRecord;
    private Button buttonNewPayBack;
    private Spinner newPayType;
    private Spinner newPaySource;
    private EditText newPayAccount;
    private EditText newPayAmount;

    private String type;
    private String account;
    private Double amount;
    private String source;
    private String crtTime;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date newPayCrtTime = new Date();

    private iDBHelper iDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pay_record);

        initViews();
        setListeners();
    }

    private void initViews() {
        buttonNewPayRecord = findViewById(R.id.button_new_pay_record);
        buttonNewPayBack = findViewById(R.id.button_new_pay_back);
        newPaySource = findViewById(R.id.new_pay_sources);
        newPayType = findViewById(R.id.new_pay_type);
        newPayAccount = findViewById(R.id.new_pay_account);
        newPayAmount = findViewById(R.id.new_pay_amount);
    }

    private void setListeners() {
        newPaySource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = getResources().getStringArray(R.array.pay_from)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        newPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                source = getResources().getStringArray(R.array.pay_to)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonNewPayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = newPayAccount.getText().toString();
                String strAmount = newPayAmount.getText().toString();

                if (TextUtils.isEmpty(strAmount)) {
                    amount = 0d;
                } else {
                    amount = Double.parseDouble(strAmount);
                    String strBigAmount = BigDecimal.ZERO.subtract(new BigDecimal(amount))
                            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    amount = Double.parseDouble(strBigAmount);
                }

                crtTime = simpleDateFormat.format(newPayCrtTime);

                iDBHelper = new iDBHelper(NewPayActivity.this, DBConstant.NAME, null, 1);
                SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBConstant.SOURCE, source);
                contentValues.put(DBConstant.TYPE, type);
                contentValues.put(DBConstant.ACCOUNT, account);
                contentValues.put(DBConstant.AMOUNT, amount);
                contentValues.put(DBConstant.CRTTIME, crtTime);

                if (TextUtils.isEmpty(account)) {
                    showDialog("提示", "请输入账号！");
                } else if (amount >= 0) {
                    showDialog("提示", "请检查输入的金额！");
                } else {
                    sqLiteDatabase.insert(DBConstant.TNAME, null, contentValues);
                    Intent intent = new Intent(NewPayActivity.this, MainActivity.class);
                    startActivity(intent);
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "记录成功，返回主页", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            }
        });

        buttonNewPayBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showDialog(String title, String message) {
        NeutralDialogFragment neutralDialogFragment = new NeutralDialogFragment();
        neutralDialogFragment.show(title, message, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 可以根据需要添加点击确定后的操作
            }
        }, getFragmentManager());
    }
}