package me.zackyu.yubook;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AccountManageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);
        Toast.makeText(this, "账户管理功能开发中", Toast.LENGTH_SHORT).show();
    }
}