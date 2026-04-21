package me.zackyu.yubook;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class IncomeSourceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_source);
        Toast.makeText(this, "收入来源管理功能开发中", Toast.LENGTH_SHORT).show();
    }
}