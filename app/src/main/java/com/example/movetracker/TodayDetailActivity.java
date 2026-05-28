package com.example.movetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movetracker.database.AppDatabase;
import com.example.movetracker.database.entity.SportRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvSummary, tvSummaryCalories, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_detail);

        tvSummary = findViewById(R.id.tvSummary);
        tvSummaryCalories = findViewById(R.id.tvSummaryCalories);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerViewRecords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<SportRecord> records = db.sportRecordDao().getTodayRecords(userId, today);
            double totalDist = db.sportRecordDao().getTodayTotalDistance(userId, today);
            long totalDuration = db.sportRecordDao().getTodayTotalDuration(userId, today);

            runOnUiThread(() -> {
                if (records == null || records.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvSummary.setText("今日暂无运动记录");
                    tvSummaryCalories.setText("");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    int recordsCount = records.size();
                    int min = (int) (totalDuration / 60);
                    double totalCal = 0;
                    for (SportRecord r : records) {
                        totalCal += r.getCalories();
                    }
                    tvSummary.setText(String.format("共 %d 次运动 | %.0f 米 | %d 分钟",
                            recordsCount, totalDist, min));
                    tvSummaryCalories.setText(String.format("共 %.0f 千卡", totalCal));
                    recyclerView.setAdapter(new RecordAdapter(records));
                }
            });
        }).start();
    }

    private static class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

        private final List<SportRecord> records;

        RecordAdapter(List<SportRecord> records) {
            this.records = records;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sport_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SportRecord record = records.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(record.getStartTime())));
            holder.tvDistance.setText(String.format("%.0f 米", record.getDistance()));
            int dur = (int) (record.getDuration() / 60);
            holder.tvDuration.setText(dur + " 分钟");
            holder.tvCalories.setText(String.format("%.0f 千卡", record.getCalories()));
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime, tvDistance, tvDuration, tvCalories;

            ViewHolder(View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tvRecordTime);
                tvDistance = itemView.findViewById(R.id.tvRecordDistance);
                tvDuration = itemView.findViewById(R.id.tvRecordDuration);
                tvCalories = itemView.findViewById(R.id.tvRecordCalories);
            }
        }
    }
}
