package com.example.movetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.movetracker.database.AppDatabase;
import com.example.movetracker.database.entity.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etNickname, etPassword, etConfirmPassword;
    private Button btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = AppDatabase.getInstance(this);

        etUsername = findViewById(R.id.etRegUsername);
        etNickname = findViewById(R.id.etRegNickname);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                User existing = db.userDao().getUserByUsername(username);
                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "用户名已存在", Toast.LENGTH_SHORT).show());
                    return;
                }
                db.userDao().insertUser(new User(username, password, nickname));
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }
}