package com.example.tripkey;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripkey.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.shape.MapPoints;
import com.kakao.vectormap.shape.Polygon;
import com.kakao.vectormap.shape.PolygonOptions;
import com.kakao.vectormap.shape.PolygonStyle;
import com.kakao.vectormap.shape.ShapeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private KakaoMap kakaoMap;
    // 방문한 시군구 이름 리스트 (예시)
    private Set<String> visited = new HashSet<>();
    private int startZoomLevel = 15;
    private LatLng startPosition = LatLng.from(37.566826, 126.9786567);

    private FirebaseFirestore db;
    private String userId;
    private List<RegionPolygon> regionPolygons = new ArrayList<>();
    private List<String> currentRegionList = new ArrayList<>();
    private List<String> pendingRegionList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        db = FirebaseFirestore.getInstance();


        mapView = findViewById(R.id.map_view);
        mapView.start(lifeCycleCallback, readyCallback);
    }

    // MapReadyCallback 을 통해 지도가 정상적으로 시작된 후에 수신할 수 있다.
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            MapActivity.this.kakaoMap = kakaoMap;

            // 지도 클릭 리스너 등록
            kakaoMap.setOnMapClickListener((map, latLng, screenpoint, poi_) -> {
                handleMapClick(latLng);
            });

            kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(36.002005, 127.68621)));
            kakaoMap.moveCamera(CameraUpdateFactory.zoomTo(7));

            Toast.makeText(getApplicationContext(), "Map Start!", Toast.LENGTH_SHORT).show();
            loadAndDrawGeoJson(); // GeoJSON 로드 및 폴리곤 추가
            loadVisitedRegions(); // 저장된 지역 불러오기

            Log.i("k3f", "startPosition: "
                    + kakaoMap.getCameraPosition().getPosition().toString());
            Log.i("k3f", "startZoomLevel: "
                    + kakaoMap.getZoomLevel());
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return startPosition;
        }

        @NonNull
        @Override
        public int getZoomLevel() {
            return startZoomLevel;
        }
    };

    // MapLifeCycleCallback 을 통해 지도의 LifeCycle 관련 이벤트를 수신할 수 있다.
    private MapLifeCycleCallback lifeCycleCallback = new MapLifeCycleCallback() {

        @Override
        public void onMapResumed() {
            super.onMapResumed();
        }

        @Override
        public void onMapPaused() {
            super.onMapPaused();
        }

        @Override
        public void onMapDestroy() {
            Toast.makeText(getApplicationContext(), "onMapDestroy",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMapError(Exception error) {
            Toast.makeText(getApplicationContext(), error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    };

    private void loadAndDrawGeoJson() {
        new Thread(() -> {
            try {
                InputStream is = getAssets().open("sig.json");
                String json = new Scanner(is).useDelimiter("\\A").next();
                JSONObject geoJson = new JSONObject(json);
                JSONArray features = geoJson.getJSONArray("features");

                runOnUiThread(() -> {
                    for (int i = 0; i < features.length(); i++) {
                        try {
                            JSONObject feature = features.getJSONObject(i);
                            JSONObject geometry = feature.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            String regionName = feature.getJSONObject("properties")
                                    .getString("SIG_KOR_NM");

                            drawPolygon(coordinates, regionName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // 폴리곤 다 그린 뒤 region 색칠
                    if (pendingRegionList != null) {
                        updateMapWithRegion(pendingRegionList);
                        pendingRegionList = null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawPolygon(JSONArray coordinates, String regionName) {
        try {
            List<LatLng> points = new ArrayList<>();
            JSONArray linearRing = coordinates.getJSONArray(0); // 첫 번째 외곽선

            for (int i = 0; i < linearRing.length(); i++) {
                JSONArray coord = linearRing.getJSONArray(i);
                double lon = coord.getDouble(0);
                double lat = coord.getDouble(1);
                points.add(LatLng.from(lat, lon)); // 위도, 경도 변환
            }

            // 폴리곤 스타일 설정
            PolygonOptions options = PolygonOptions.from(
                    MapPoints.fromLatLng(points),
                    PolygonStyle.from(
                    Color.parseColor("#00FFFFFF"),
                    2, // strokeWidth (픽셀 단위)
                    Color.RED
                    )
            );

            // 지도에 추가
            kakaoMap.getShapeManager().getLayer().addPolygon(options);

            Polygon polygon = kakaoMap.getShapeManager().getLayer().addPolygon(options);
            regionPolygons.add(new RegionPolygon(regionName, points, polygon));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isPointInPolygon(LatLng point, List<LatLng> polygon) {
        int intersectCount = 0;
        for (int i = 0; i < polygon.size(); i++) {
            LatLng p1 = polygon.get(i);
            LatLng p2 = polygon.get((i+1) % polygon.size());

            if (rayCastIntersect(point, p1, p2)) {
                intersectCount++;
            }
        }
        return (intersectCount % 2) == 1;
    }

    private boolean rayCastIntersect(LatLng point, LatLng p1, LatLng p2) {
        double lat = point.getLatitude();
        double lng = point.getLongitude();
        double lat1 = p1.getLatitude();
        double lng1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lng2 = p2.getLongitude();

        if (Math.max(lat1, lat2) < lat || Math.min(lat1, lat2) > lat) {
            return false;
        }

        double t = (lat - lat1) / (lat2 - lat1);
        double intersectLng = lng1 + t * (lng2 - lng1);
        return lng <= intersectLng;
    }

    // 방문 지역 저장 함수
    private void saveVisitedRegion(String regionName) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("region", FieldValue.arrayUnion(regionName))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "지역 저장 성공"))
                .addOnFailureListener(e -> Log.e(TAG, "저장 실패", e));
    }

    // region 필드에서 방문 지역 불러오기
    private void loadVisitedRegions() {
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) return;

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> regions = (List<String>) documentSnapshot.get("region");
                        if (regions != null) {
                            currentRegionList.clear();
                            currentRegionList.addAll(regions); // 항상 최신 region 배열로 동기화
                            // regionPolygons가 준비됐으면 바로 색칠, 아니면 pending에 저장
                            if (regionPolygons.size() > 0) {
                                updateMapWithRegion(regions);
                            } else {
                                pendingRegionList = new ArrayList<>(regions);
                            }
                        }
                    } else {
                        createUserDocument(); // 사용자 문서가 없을 때 생성
                    }
                });
    }
    // region 필드가 없으면 새 user 문서 생성
    private void createUserDocument() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("region", new ArrayList<String>());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "새 사용자 문서 생성"));
    }
    // 체크리스트 기반 맵 업데이트
    private void updateMapWithRegion(List<String> regionList) {
        for (RegionPolygon region : regionPolygons) {
            boolean isSelected = regionList.contains(region.name);
            updatePolygonStyle(region, isSelected);
        }
    }

    // 지도 클릭 시
    private void handleMapClick(LatLng clickPoint) {
        for (RegionPolygon region : regionPolygons) {
            if (isPointInPolygon(clickPoint, region.points)) {
                // region.name이 region 배열에 이미 있는지 확인
                boolean isSelected = currentRegionList.contains(region.name);
                if (isSelected) {
                    updatePolygonStyle(region, false); // 색 해제
                    removeVisitedRegion(region.name);  // region 배열에서 삭제
                } else {
                    updatePolygonStyle(region, true);  // 색칠
                    saveVisitedRegion(region.name);    // region 배열에 추가
                }
                break;
            }
        }
    }
    private void updatePolygonStyle(RegionPolygon region, boolean isSelected) {
        // 기존 폴리곤 제거
        if (region.polygon != null) {
            region.polygon.remove();
        }
        // 새로운 스타일로 PolygonOptions 생성
        int fillColor = isSelected ? Color.parseColor("#8000FF00") : Color.TRANSPARENT;
        int strokeColor = isSelected ? Color.RED : Color.RED;

        PolygonOptions options = PolygonOptions.from(
                MapPoints.fromLatLng(region.points),
                PolygonStyle.from(fillColor, 2, strokeColor)
        );

        // 지도에 새로 추가하고 polygon 객체 갱신
        region.polygon = kakaoMap.getShapeManager().getLayer().addPolygon(options);
    }

    private void removeVisitedRegion(String regionName) {
        db.collection("users").document(userId)
                .update("region", FieldValue.arrayRemove(regionName))
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "지역 삭제 성공"))
                .addOnFailureListener(e -> Log.e("Firebase", "지역 삭제 실패", e));
    }

    // RegionPolygon 클래스
    public static class RegionPolygon {
        public String name;
        public List<LatLng> points;
        public Polygon polygon;

        public RegionPolygon(String name, List<LatLng> points, Polygon polygon) {
            this.name = name;
            this.points = points;
            this.polygon = polygon;
        }
    }
}


