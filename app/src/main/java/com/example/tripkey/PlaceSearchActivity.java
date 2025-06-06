package com.example.tripkey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 장소명(키워드)로 장소를 검색하는 Activity
 * 카카오 키워드 장소 검색 API를 사용합니다.
 */
public class PlaceSearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ListView resultListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<PlaceResult> placeResultList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    private static final String KAKAO_API_KEY = "42d61720c6096d7a9ec5e7c8d0950740";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);

        searchInput = findViewById(R.id.search_input);
        resultListView = findViewById(R.id.result_list);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultList);
        resultListView.setAdapter(adapter);

        // 검색어 입력 시 자동 검색
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) { // 2글자 이상일 때만 검색
                    searchPlaceByKeyword(s.toString());
                } else {
                    resultList.clear();
                    placeResultList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 리스트에서 선택 시 결과 반환
        resultListView.setOnItemClickListener((parent, view, position, id) -> {
            PlaceResult selectedPlace = placeResultList.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("result_type", "must_visit");
            resultIntent.putExtra("selected_place_name", selectedPlace.placeName);
            resultIntent.putExtra("selected_address", selectedPlace.addressName);
            resultIntent.putExtra("latitude", selectedPlace.latitude);
            resultIntent.putExtra("longitude", selectedPlace.longitude);
            resultIntent.putExtra("place_name", selectedPlace.placeName);
            resultIntent.putExtra("category", selectedPlace.categoryName);

            Log.d("Place", "위도" + selectedPlace.latitude + "경도" + selectedPlace.longitude);
            resultIntent.putExtra("field_index", getIntent().getIntExtra("field_index", -1));
          
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * 카카오 키워드 장소 검색 API 호출
     */
    private void searchPlaceByKeyword(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encodedQuery;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "KakaoAK " + KAKAO_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resultList.clear();
                    placeResultList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        try {
                            JSONObject json = new JSONObject(res);
                            JSONArray documents = json.getJSONArray("documents");
                            for (int i = 0; i < documents.length(); i++) {
                                JSONObject doc = documents.getJSONObject(i);

                                // 카테고리 코드 추출
                                String categoryCode = doc.optString("category_group_code");
                                String categoryName = CATEGORY_MAP.getOrDefault(categoryCode, "기타");

                                String placeName = doc.optString("place_name");
                                String address = doc.optString("address_name");
                                String roadAddress = doc.optString("road_address_name");
                                String x = doc.optString("x");
                                String y = doc.optString("y");

                                String display = placeName + " (" + (roadAddress.isEmpty() ? address : roadAddress) + ")";
                                resultList.add(display);

                                double latitude = 0.0, longitude = 0.0;
                                try {
                                    longitude = Double.parseDouble(x);
                                    latitude = Double.parseDouble(y);
                                } catch (Exception ignore) {}

                                placeResultList.add(new PlaceResult(placeName, address, latitude, longitude, categoryName));
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(PlaceSearchActivity.this, "파싱 오류", Toast.LENGTH_SHORT).show());
                        }
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(PlaceSearchActivity.this, "검색 실패", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "검색 오류", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 장소 검색 결과 데이터 클래스
     */
    private static class PlaceResult {
        String placeName;
        String addressName;
        double latitude;
        double longitude;
        String categoryName;
        PlaceResult(String placeName, String addressName, double latitude, double longitude, String categoryName) {
            this.placeName = placeName;
            this.addressName = addressName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.categoryName = categoryName;
        }
    }

    private static final HashMap<String, String> CATEGORY_MAP = new HashMap<String, String>() {{
        put("MT1", "대형마트");
        put("CS2", "편의점");
        put("PS3", "어린이/유아");
        put("SC4", "학교");
        put("AC5", "학원");
        put("PK6", "주차장");
        put("OL7", "주유소");
        put("SW8", "지하철역");
        put("BK9", "은행");
        put("CT1", "문화시설");
        put("AG2", "중개업소");
        put("PO3", "공공기관");
        put("AT4", "관광명소");
        put("AD5", "숙박");
        put("FD6", "음식점");
        put("CE7", "카페");
        put("HP8", "병원");
        put("PM9", "약국");
    }};
}
