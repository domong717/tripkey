package com.example.tripkey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationSearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ListView resultListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> resultList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    private static final String KAKAO_API_KEY = "42d61720c6096d7a9ec5e7c8d0950740";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        searchInput = findViewById(R.id.search_input);
        resultListView = findViewById(R.id.result_list);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultList);
        resultListView.setAdapter(adapter);
        runOnUiThread(() -> adapter.notifyDataSetChanged());

        // 검색어 입력 시 자동 검색
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) { // 2글자 이상일 때만 검색
                    searchAddress(s.toString());
                } else {
                    resultList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 리스트에서 선택 시 결과 반환
        resultListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = resultList.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_location", selected);
            resultIntent.putExtra("result_type", "location");
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void searchAddress(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encodedQuery;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "KakaoAK " + KAKAO_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resultList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        try {
                            JSONObject json = new JSONObject(res);
                            JSONArray documents = json.getJSONArray("documents");
                            for (int i = 0; i < documents.length(); i++) {
                                JSONObject doc = documents.getJSONObject(i);
                                JSONObject address = doc.optJSONObject("address");
                                // 시/구 단위 주소 추출
                                String region1 = address.optString("address_name"); // 전체 주소
                                String region2 = address.optString("region_2depth_name"); // 구/군
                                String region1depth = address.optString("region_1depth_name"); // 시/도
                                if (!region1.isEmpty()) {
                                    // 시/구만 추출해서 리스트에 추가
                                    String location = region1depth + " " + region2;
                                    if (!resultList.contains(location)) {
                                        resultList.add(location);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(LocationSearchActivity.this, "파싱 오류", Toast.LENGTH_SHORT).show());
                        }
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(LocationSearchActivity.this, "검색 실패", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "검색 오류", Toast.LENGTH_SHORT).show();
        }
    }
}
