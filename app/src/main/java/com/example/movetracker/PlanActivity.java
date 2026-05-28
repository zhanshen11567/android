package com.example.movetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.movetracker.database.AppDatabase;
import com.example.movetracker.database.entity.SportPlan;
import java.util.ArrayList;
import java.util.List;

public class PlanActivity extends AppCompatActivity {
    private AppDatabase db;
    private RecyclerView recyclerView;
    private List<SportPlan> planList = new ArrayList<>();
    private PlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewPlans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlanAdapter(planList);
        recyclerView.setAdapter(adapter);

        loadPlans();
    }

    private void loadPlans() {
        new Thread(() -> {
            List<SportPlan> dbPlans = db.sportPlanDao().getAllPlans();
            if (dbPlans.isEmpty()) {
                // 插入默认计划
                db.sportPlanDao().insertPlan(new SportPlan("新手3公里", 3000, false));
                db.sportPlanDao().insertPlan(new SportPlan("进阶5公里", 5000, false));
                db.sportPlanDao().insertPlan(new SportPlan("燃脂8公里", 8000, false));
                dbPlans = db.sportPlanDao().getAllPlans();
            }
            planList.clear();
            planList.addAll(dbPlans);
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {
        private List<SportPlan> plans;

        PlanAdapter(List<SportPlan> plans) { this.plans = plans; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SportPlan plan = plans.get(position);
            holder.textView.setText(plan.getPlanName() + " (" + plan.getTargetDistance() + "米)");
            holder.itemView.setOnClickListener(v -> {
                new Thread(() -> {
                    // 取消之前激活的计划
                    SportPlan active = db.sportPlanDao().getActivePlan();
                    if (active != null) {
                        active.setActive(false);
                        db.sportPlanDao().updatePlan(active);
                    }
                    plan.setActive(true);
                    db.sportPlanDao().updatePlan(plan);
                    runOnUiThread(() -> {
                        Toast.makeText(PlanActivity.this, "已选择: " + plan.getPlanName(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            });
        }

        @Override
        public int getItemCount() { return plans.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}