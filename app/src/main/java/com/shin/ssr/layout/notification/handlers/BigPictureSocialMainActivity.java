/*
Copyright 2016 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.shin.ssr.layout.notification.handlers;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.fit.samples.stepcounter.GeofenceTransitionsJobIntentService;
import com.google.android.gms.fit.samples.stepcounter.R;
import com.shin.ssr.layout.notification.PushNotification;
import com.shin.ssr.layout.tab.HttpUtil;
import com.shin.ssr.vo.ProductVO;
import com.shin.ssr.vo.StepVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.shin.ssr.layout.tab.FitTab.SERVER_URL;

/**
 * Template class meant to include functionality for your Social App. (This project's main focus
 * is on Notification Styles.)
 */
public class BigPictureSocialMainActivity extends Activity implements Runnable{


    ArrayList<Bitmap> bitmap = new ArrayList<>();
    ImageView img1,img2,img3,img4;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ImageView[] productImg = {img1,img2,img3,img4};
            for(int z =0; z<bitmap.size(); z++) {
                Log.d("geo",bitmap.get(z).toString());
            }

            // 서버에서 받아온 이미지를 핸들러를 경유해 이미지뷰에 비트맵 리소스 연결
            for(int i =0; i< bitmap.size(); i++) {
                Log.d("geo",productImg[i].toString());
                Log.d("geo", "inside for loop for image?");
                Bitmap bitmap1 = bitmap.get(i);
                productImg[i].setImageBitmap(bitmap1);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_product);

        img1 = findViewById(R.id.recommendedProduct1);
        img2 = findViewById(R.id.recommendedProduct2);
        img3 = findViewById(R.id.recommendedProduct3);
        img4 = findViewById(R.id.recommendedProduct4);


        // Cancel Notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(PushNotification.NOTIFICATION_ID);
        Thread th = new Thread(BigPictureSocialMainActivity.this);

        th.start();



    }

    @Override
    public void run() {
        HttpUtil hu = new HttpUtil(BigPictureSocialMainActivity.this);
        String[] params = {SERVER_URL+"/product.do", "dummy1:"+1, "dummy2:"+ 1} ;

        ArrayList<ProductVO> productArry = new ArrayList<>();
        hu.execute(params);
        String result;
        URL url = null;

        try {
            result = hu.get();
            JSONArray object = null;
            Log.d("geo","result from spring" + result);

            try {
                object =  new JSONArray(result);

                for(int i =0; i < object.length(); i++) {
                    JSONObject obj = (JSONObject)object.get(i);
                    android.util.Log.d("geo",obj.getString("item_price"));
                    android.util.Log.d("geo",obj.getString("item_name"));
                    android.util.Log.d("geo",obj.getString("item_img_path"));
                    productArry.add(new ProductVO(obj.optString("item_name"),obj.optString("item_price"),obj.optString("item_weight"),obj.optString("item_img_path")));
                }
                HttpURLConnection conn=null;
                InputStream is=null;
                for(int i=0; i < productArry.size(); i++ ) {
                    url = new URL(SERVER_URL + "resources/img" + productArry.get(i).getItem_img_path());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    Log.d("geo",url.toString());

                    is = conn.getInputStream();
                    // 스트림에서 받은 데이터를 비트맵 변환
                    // 인터넷에서 이미지 가져올 때는 Bitmap을 사용해야함
                    bitmap.add(BitmapFactory.decodeStream(is));

                    Log.d("geo", "inside thread" +bitmap.get(i).toString());


                }

                // 핸들러에게 화면 갱신을 요청한다.
                handler.sendEmptyMessage(0);
                // 연결 종료
                is.close();
                conn.disconnect();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}