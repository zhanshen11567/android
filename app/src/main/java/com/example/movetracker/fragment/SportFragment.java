package com.example.movetracker.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.movetracker.PlanActivity;
import com.example.movetracker.R;
import com.example.movetracker.SportTrackingActivity;
import com.example.movetracker.TargetActivity;
import com.example.movetracker.TodayDetailActivity;
import com.example.movetracker.database.AppDatabase;
import com.example.movetracker.database.entity.SportPlan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SportFragment extends Fragment {
    private TextView tvTargetDistance, tvTodayDistance, tvTodayDuration, tvTodayCalories, tvPlanName;
    private CardView cardTarget, cardToday, cardPlan;
    private Button btnStartSport;
    private AppDatabase db;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport, container, false);
        db = AppDatabase.getInstance(requireContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", 0);
        userId = prefs.getInt("user_id", -1);

        tvTargetDistance = view.findViewById(R.id.tvTargetDistance);
        tvTodayDistance = view.findViewById(R.id.tvTodayDistance);
        tvTodayDuration = view.findViewById(R.id.tvTodayDuration);
        tvTodayCalories = view.findViewById(R.id.tvTodayCalories);
        tvPlanName = view.findViewById(R.id.tvPlanName);
        cardTarget = view.findViewById(R.id.cardTarget);
        cardToday = view.findViewById(R.id.cardToday);
        cardPlan = view.findViewById(R.id.cardPlan);
        btnStartSport = view.findViewById(R.id.btnStartSport);

        loadData();

        cardTarget.setOnClickListener(v -> startActivity(new Intent(getActivity(), TargetActivity.class)));
        cardToday.setOnClickListener(v -> startActivity(new Intent(getActivity(), TodayDetailActivity.class)));
        cardPlan.setOnClickListener(v -> startActivity(new Intent(getActivity(), PlanActivity.class)));
        btnStartSport.setOnClickListener(v -> {
            // 检查定位权限
            if (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            } else {
                startActivity(new Intent(getActivity(), SportTrackingActivity.class));
            }
        });
        return view;
    }

    private void loadData() {
        new Thread(() -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            SportPlan activePlan = db.sportPlanDao().getActivePlan();
            double todayDist = db.sportRecordDao().getTodayTotalDistance(userId, today);
            long duration = db.sportRecordDao().getTodayTotalDuration(userId, today);
            double todayCal = db.sportRecordDao().getTodayTotalCalories(userId, today);

            requireActivity().runOnUiThread(() -> {
                if (activePlan != null) {
                    tvTargetDistance.setText(String.format("%.0f 米", activePlan.getTargetDistance()));
                    tvPlanName.setText(activePlan.getPlanName());
                } else {
                    tvTargetDistance.setText("3000 米 (默认)");
                    tvPlanName.setText("未选择计划");
                }
                tvTodayDistance.setText(String.format("%.0f 米", todayDist));
                int min = (int) (duration / 60);
                tvTodayDuration.setText(String.format("时长: %d分钟", min));
                tvTodayCalories.setText(String.format("%.0f 千卡", todayCal));
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // 从计划页面返回时刷新
    }
}