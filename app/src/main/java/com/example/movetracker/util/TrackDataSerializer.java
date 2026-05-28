package com.example.movetracker.util;

import com.amap.api.maps2d.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TrackDataSerializer {

    public static String toJson(List<LatLng> points) {
        if (points == null || points.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < points.size(); i++) {
            LatLng p = points.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"lat\":").append(p.latitude)
              .append(",\"lng\":").append(p.longitude).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<LatLng> fromJson(String json) {
        List<LatLng> points = new ArrayList<>();
        if (json == null || json.isEmpty()) return points;
        // 简单解析 [{"lat":x,"lng":y},...] 格式
        int i = 0;
        while (i < json.length()) {
            int latStart = json.indexOf("\"lat\":", i);
            if (latStart == -1) break;
            latStart += 6;
            int latEnd = json.indexOf(",", latStart);
            if (latEnd == -1) latEnd = json.indexOf("}", latStart);
            double lat = Double.parseDouble(json.substring(latStart, latEnd));

            int lngStart = json.indexOf("\"lng\":", latEnd);
            if (lngStart == -1) break;
            lngStart += 6;
            int lngEnd = json.indexOf("}", lngStart);
            if (lngEnd == -1) lngEnd = json.length();
            double lng = Double.parseDouble(json.substring(lngStart, lngEnd));

            points.add(new LatLng(lat, lng));
            i = lngEnd + 1;
        }
        return points;
    }
}
