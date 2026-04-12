package me.zackyu.yubook;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.zackyu.yubook.constant.DBConstant;
import me.zackyu.yubook.db.Record;
import me.zackyu.yubook.db.iDBHelper;

public class RecordsActivity extends AppCompatActivity {

    private TextView textTitle;
    private ListView recordListView;

    private RecordAdapter recordAdapter;
    private iDBHelper iDBHelper;
    private List<Record> records;
    private List<Record> recordsIncome;
    private List<Record> recordsPay;

    private Button buttonRecordsAll;
    private Button buttonRecordsIncome;
    private Button buttonRecordsPay;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        setTitle("记录");

        initViews();
        initDBHelper();
        initData();
        setListeners();
    }

    private void initViews() {
        buttonRecordsAll = findViewById(R.id.button_records_all);
        buttonRecordsIncome = findViewById(R.id.button_records_income);
        buttonRecordsPay = findViewById(R.id.button_records_pay);
        textTitle = findViewById(R.id.text_title);
        recordListView = findViewById(R.id.record_list);
    }

    private void initDBHelper() {
        iDBHelper = new iDBHelper(RecordsActivity.this, DBConstant.NAME, null, 1);
    }

    private void initData() {
        records = new ArrayList<>();
        recordsIncome = new ArrayList<>();
        recordsPay = new ArrayList<>();
        getAllRecords();
        recordAdapter = new RecordAdapter(this, R.layout.record_item, records);
        recordListView.setAdapter(recordAdapter);



    }

    private void setListeners() {
        buttonRecordsAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textTitle.setText("所有");
                getAllRecords();
                recordAdapter = new RecordAdapter(RecordsActivity.this, R.layout.record_item, records);
                recordListView.setAdapter(recordAdapter);
            }
        });

        buttonRecordsIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textTitle.setText("收入");
                getIncomeRecords();
                recordAdapter = new RecordAdapter(RecordsActivity.this, R.layout.record_item, recordsIncome);
                recordListView.setAdapter(recordAdapter);
            }
        });

        buttonRecordsPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textTitle.setText("支出");
                getPayRecords();
                recordAdapter = new RecordAdapter(RecordsActivity.this, R.layout.record_item, recordsPay);
                recordListView.setAdapter(recordAdapter);
            }
        });
    }

    public void getAllRecords() {
        records.clear();
        SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.query(DBConstant.TNAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Record record = new Record();
                int id = cursor.getInt(0);
                String source = cursor.getString(1);
                String type = cursor.getString(2);
                String account = cursor.getString(3);
                double amount = cursor.getDouble(4);
                String date = cursor.getString(5);
                try {
                    Date crtTime = simpleDateFormat.parse(date);
                    record.setSource(source);
                    record.setType(type);
                    record.setAccount(account);
                    record.setAmount(amount);
                    record.setId(id);
                    record.setCrttime(crtTime);
                    records.add(record);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

    public void getIncomeRecords() {
        recordsIncome.clear();
        String sqlIncome = "select  * from record where amount > 0";
        SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();
        Cursor cursorIncome = sqLiteDatabase.rawQuery(sqlIncome, null);
        if (cursorIncome.moveToFirst()) {
            while (!cursorIncome.isAfterLast()) {
                Record record = new Record();
                int id = cursorIncome.getInt(0);
                String source = cursorIncome.getString(1);
                String type = cursorIncome.getString(2);
                String account = cursorIncome.getString(3);
                double amount = cursorIncome.getDouble(4);
                String date = cursorIncome.getString(5);
                try {
                    Date crtTime = simpleDateFormat.parse(date);
                    record.setSource(source);
                    record.setType(type);
                    record.setAccount(account);
                    record.setAmount(amount);
                    record.setId(id);
                    record.setCrttime(crtTime);
                    recordsIncome.add(record);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cursorIncome.moveToNext();
            }
        }
        cursorIncome.close();
    }

    public void getPayRecords() {
        recordsPay.clear();
        SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();
        String sqlPay = "select * from record where amount < 0";
        Cursor cursorPay = sqLiteDatabase.rawQuery(sqlPay, null);
        if (cursorPay.moveToFirst()) {
            while (!cursorPay.isAfterLast()) {
                Record record = new Record();
                int id = cursorPay.getInt(0);
                String source = cursorPay.getString(1);
                String type = cursorPay.getString(2);
                String account = cursorPay.getString(3);
                double amount = cursorPay.getDouble(4);
                String date = cursorPay.getString(5);
                try {
                    Date crtTime = simpleDateFormat.parse(date);
                    record.setSource(source);
                    record.setType(type);
                    record.setAccount(account);
                    record.setAmount(amount);
                    record.setId(id);
                    record.setCrttime(crtTime);
                    recordsPay.add(record);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cursorPay.moveToNext();
            }
        }
        cursorPay.close();
    }
}