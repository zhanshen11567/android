package com.example.movetracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.movetracker.database.AppDatabase;
import com.example.movetracker.database.entity.SportPlan;

public class TargetActivity extends AppCompatActivity {
    private TextView tvInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
        tvInfo = findViewById(R.id.tvInfo);
        new Thread(() -> {
            SportPlan plan = AppDatabase.getInstance(this).sportPlanDao().getActivePlan();
            runOnUiThread(() -> {
                if (plan != null) {
                    tvInfo.setText("当前计划：" + plan.getPlanName() + "\n目标距离：" + plan.getTargetDistance() + "米");
                } else {
                    tvInfo.setText("未选择计划，默认目标 3000 米");
                }
            });
        }).start();
    }
}