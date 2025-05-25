package com.example.tripkey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class AccomodationSearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ListView resultListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<String> placeList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    private static final String KAKAO_API_KEY = "42d61720c6096d7a9ec5e7c8d0950740";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accomodation_search);

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
                    searchPlace(s.toString());
                } else {
                    resultList.clear();
                    placeList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 리스트에서 선택 시 결과 반환
        resultListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = placeList.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_accomodation", selected);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void searchPlace(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encodedQuery + "&category_group_code=AD5";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "KakaoAK " + KAKAO_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resultList.clear();
                    placeList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        try {
                            JSONObject json = new JSONObject(res);
                            JSONArray documents = json.getJSONArray("documents");
                            for (int i = 0; i < documents.length(); i++) {
                                JSONObject doc = documents.getJSONObject(i);
                                String placeName = doc.optString("place_name"); // 장소명
                                String roadAddress = doc.optString("road_address_name"); // 도로명 주소
                                String address = doc.optString("address_name"); // 지번 주소
                                String phone = doc.optString("phone"); // 전화번호

                                String display = placeName;
                                if (!roadAddress.isEmpty()) display += "\n" + roadAddress;
                                else if (!address.isEmpty()) display += "\n" + address;
                                //if (!phone.isEmpty()) display += "\n" + phone;

                                if (!resultList.contains(display)) {
                                    resultList.add(display);
                                    placeList.add(placeName);
                                }
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(AccomodationSearchActivity.this, "파싱 오류", Toast.LENGTH_SHORT).show());
                        }
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(AccomodationSearchActivity.this, "검색 실패", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "검색 오류", Toast.LENGTH_SHORT).show();
        }
    }
}
