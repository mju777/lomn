package me.zackyu.yubook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import me.zackyu.yubook.db.iDBHelper;

public class MainActivity extends AppCompatActivity {
    private Button buttonNewRecord;
    private Button buttonRecords;
    private TextView textIncome;
    private TextView textPay;
    private TextView textTotal;
    private TextView textviewAboutMe;
    private iDBHelper iDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
        initDatabase();

        String html = "<u>about</u>";
        textviewAboutMe.setText(Html.fromHtml(html));
        showData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showData();
    }

    private void initViews() {
        buttonNewRecord = findViewById(R.id.button_new_record);
        buttonRecords = findViewById(R.id.button_records);
        textviewAboutMe = findViewById(R.id.textview_about_me);
        textIncome = findViewById(R.id.text_income);
        textPay = findViewById(R.id.text_pay);
        textTotal = findViewById(R.id.text_total);
    }

    private void setListeners() {
        textviewAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutMeActivity.class);
                startActivity(intent);
            }
        });

        buttonNewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewRecordActivity.class);
                startActivity(intent);
            }
        });

        buttonRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDatabase() {
        iDBHelper = new iDBHelper(MainActivity.this, "MyAccount.db", null, 1);
    }

    private void showData() {
        SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();
        String sqlIncome = "select sum(amount) from record where amount > 0";
        String sqlPay = "select sum(amount) from record where amount < 0";

        Cursor cursorIncome = sqLiteDatabase.rawQuery(sqlIncome, null);
        Cursor cursorPay = sqLiteDatabase.rawQuery(sqlPay, null);
        Double income = 0.0;

        if (cursorIncome.moveToFirst()) {
            income = cursorIncome.getDouble(0);
            textIncome.setText(new BigDecimal(income)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toString());
        }

        Double pay = 0.0;
        if (cursorPay.moveToFirst()) {
            pay = cursorPay.getDouble(0);
            textPay.setText(BigDecimal.ZERO.subtract(new BigDecimal(pay))
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toString());
        }

        double total = 0.0;
        if (income!= null && pay!= null) {
            total = income + pay;
        }
        textTotal.setText(new BigDecimal(total)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .toString());
    }
}