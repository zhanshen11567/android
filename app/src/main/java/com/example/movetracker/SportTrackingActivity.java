package com.example.movetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.example.movetracker.database.AppDatabase;
import com.example.movetracker.database.entity.SportPlan;
import com.example.movetracker.database.entity.SportRecord;
import com.example.movetracker.util.CalorieCalculator;
import com.example.movetracker.util.TrackDataSerializer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SportTrackingActivity extends AppCompatActivity implements AMapLocationListener {

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;

    private List<LatLng> trackPoints = new ArrayList<>();
    private Polyline trackPolyline;
    private double totalDistance = 0; // 米
    private LatLng lastPoint;

    private boolean isRunning = false;
    private long startTime = 0;
    private long pauseTime = 0;
    private long totalPauseDuration = 0;

    private TextView tvCurrentDistance, tvTargetDistance, tvDuration;
    private Button btnStartPause;

    private double targetDistance = 3000;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    private AppDatabase db;
    private int userId;
    private String todayDate;

    // 控制地图只在首次定位时自动移动
    private boolean isFirstLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_tracking);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState); // 必须调用生命周期

        // 初始化数据库和用户
        db = AppDatabase.getInstance(this);
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        tvCurrentDistance = findViewById(R.id.tvCurrentDistance);
        tvTargetDistance = findViewById(R.id.tvTargetDistance);
        tvDuration = findViewById(R.id.tvDuration);
        btnStartPause = findViewById(R.id.btnStartPause);

        initMap();
        loadTargetDistance();

        btnStartPause.setOnClickListener(v -> {
            if (!isRunning) {
                startSport();
            } else {
                pauseSport();
            }
        });
    }

    /**
     * 初始化地图：隐私合规、获取AMap、基础设置
     */
    private void initMap() {
        // 高德隐私合规（必须在获取地图前调用）
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);

        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setMyLocationEnabled(true); // 显示定位蓝点
        aMap.getUiSettings().setZoomControlsEnabled(true);
        // 初始缩放级别15，约500米范围
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
    }

    /**
     * 从数据库加载今日目标距离（若无激活计划则使用默认3000米）
     */
    private void loadTargetDistance() {
        new Thread(() -> {
            SportPlan plan = db.sportPlanDao().getActivePlan();
            if (plan != null) {
                targetDistance = plan.getTargetDistance();
            }
            runOnUiThread(() -> tvTargetDistance.setText("目标: " + targetDistance + "米"));
        }).start();
    }

    /**
     * 开始运动：检查权限、重置状态、启动定位和计时
     */
    private void startSport() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        isRunning = true;
        btnStartPause.setText("暂停运动");

        // 重置轨迹与状态
        trackPoints.clear();
        totalDistance = 0;
        lastPoint = null;
        if (trackPolyline != null) trackPolyline.remove();
        isFirstLocation = true;  // 允许首次定位时移动地图

        try {
            locationClient = new AMapLocationClient(this);
            locationOption = new AMapLocationClientOption();
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOption.setInterval(2000); // 2秒定位一次
            locationClient.setLocationOption(locationOption);
            locationClient.setLocationListener(this);
            locationClient.startLocation();
        } catch (Exception e) {
            Toast.makeText(this, "定位初始化失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            isRunning = false;
            btnStartPause.setText("开始运动");
            return;
        }

        startTime = System.currentTimeMillis();
        totalPauseDuration = 0;
        startTimer();
    }

    /**
     * 暂停运动：停止定位、停止计时、保存当前记录
     */
    private void pauseSport() {
        isRunning = false;
        btnStartPause.setText("继续运动");
        pauseTime = System.currentTimeMillis();
        if (locationClient != null) locationClient.stopLocation();
        stopTimer();

        long durationMs = pauseTime - startTime - totalPauseDuration;
        long durationSec = durationMs / 1000;
        String trackJson = TrackDataSerializer.toJson(trackPoints);
        double calories = CalorieCalculator.calculate(totalDistance, durationSec);
        // 保存当前运动记录（简化版）
        new Thread(() -> {
            SportRecord record = new SportRecord(userId, todayDate);
            record.setDistance(totalDistance);
            record.setDuration(durationSec);
            record.setCalories(calories);
            record.setStartTime(startTime);
            record.setEndTime(pauseTime);
            record.setTrackData(trackJson);
            db.sportRecordDao().insertRecord(record);
        }).start();
    }

    /**
     * 定位回调：计算距离、绘制轨迹、首次定位时移动地图
     */
    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null && location.getErrorCode() == 0) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng currentPoint = new LatLng(lat, lng);

            // 距离计算（过滤漂移>100m的点）
            if (lastPoint != null) {
                float[] results = new float[1];
                Location.distanceBetween(lastPoint.latitude, lastPoint.longitude,
                        currentPoint.latitude, currentPoint.longitude, results);
                if (results[0] < 100) {
                    totalDistance += results[0];
                }
            }

            lastPoint = currentPoint;
            trackPoints.add(currentPoint);

            // 更新UI
            runOnUiThread(() -> tvCurrentDistance.setText(String.format("%.2f米", totalDistance)));

            // 绘制轨迹
            drawTrack();

            // 只在首次定位（或暂停后重新开始）时自动移动地图并缩放
            if (isFirstLocation) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPoint, 18));
                isFirstLocation = false;
            }

            // 目标达成提醒
            if (totalDistance >= targetDistance) {
                runOnUiThread(() ->
                        Toast.makeText(this, "恭喜！已达成今日目标！", Toast.LENGTH_LONG).show());
            }
        }
    }

    /**
     * 绘制轨迹折线
     */
    private void drawTrack() {
        if (trackPoints.size() < 2) return;
        if (trackPolyline != null) trackPolyline.remove();
        trackPolyline = aMap.addPolyline(new PolylineOptions()
                .addAll(trackPoints)
                .width(15)
                .color(Color.parseColor("#FF5722")));
    }

    /**
     * 启动计时器（每秒更新时长显示）
     */
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime - totalPauseDuration;
                int sec = (int) (elapsed / 1000) % 60;
                int min = (int) (elapsed / (1000 * 60)) % 60;
                int hr = (int) (elapsed / (1000 * 60 * 60));
                tvDuration.setText(String.format("%02d:%02d:%02d", hr, min, sec));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    /**
     * 停止计时器
     */
    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    // ========== MapView 生命周期管理 ==========
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}