package com.example.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

//    private Bitmap bitmap;
    ImageView imageView;
    TextView textview;
    Button button;
    Button button1;
    ProgressBar progressBar;
    String selectedImagePath;
    int a = 0;
    int b = 0;

    ArrayList<Bitmap> images;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textview = findViewById(R.id.textview);
        imageView = findViewById(R.id.image);
        button = findViewById(R.id.button);
//        button1 = findViewById(R.id.button1);
        progressBar = findViewById(R.id.progress);

         images = new ArrayList<>();
        progressBar.setVisibility(View.GONE);
        restore();
        checkInternet();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

//                progressBar.setVisibility(View.VISIBLE);
//                textview.setVisibility(View.GONE);
                imageView.setImageResource(android.R.color.transparent);
//                imageView.setImageResource(imageAdapter.mThumbIds[++position]);

                SharedPreferences sp = getSharedPreferences("AppSharedPref", MODE_PRIVATE);
                selectedImagePath = sp.getString("ImagePath", "");
                byte[] decodedString = Base64.decode(selectedImagePath, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                images.add(decodedByte);


                ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null) {
//                    Toast.makeText(getApplicationContext(), "check internet connection", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(false)
                            .setTitle(getResources().getString(R.string.app_name))
                            .setMessage("Check your internet connection.")
                            .setPositiveButton("retry", (dialog, which) -> checkInternet()).show();


                    progressBar.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);

//                    getImage();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    textview.setVisibility(View.GONE);

//                    new AlertDialog.Builder(MainActivity.this)
//                            .setCancelable(false)
//                            .setTitle(getResources().getString(R.string.app_name))
//                            .setMessage("Check your internet connection.")
//                            .setPositiveButton("retry", (dialog, which) -> checkInternet()).show();
                    getImage();
                }
            }
        });

//        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                if(images.size()>0){
//                    Bitmap bitmap = images.get(images.size() - 1);
//                    imageView.setImageBitmap(bitmap);
//                    images.remove(images.size() - 1);
//                }else{
//                    Toast.makeText(getApplicationContext(), "please switch images", Toast.LENGTH_SHORT).show();
//                }
//
//        }});
    }

    public void checkInternet() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            Toast.makeText(getApplicationContext(), "check internet connection", Toast.LENGTH_SHORT).show();
//            new AlertDialog.Builder(MainActivity.this).setCancelable(false)
//            .setTitle(getResources().getString(R.string.app_name))
//            .setMessage("Please Check your internet connection...")
//            .setPositiveButton("retry", (dialog, which) -> checkInternet()).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        a = 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (a == 1) {
            restore();
        }
    }

    public void restore() {

        @SuppressLint("WrongConstant") SharedPreferences sp = getSharedPreferences("AppSharedPref", MODE_PRIVATE);
        selectedImagePath = sp.getString("ImagePath", "");
        byte[] decodedString = Base64.decode(selectedImagePath, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        imageView.setImageBitmap(decodedByte);
    }


    public void getImage() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://picsum.photos/200", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("res101", new String(responseBody));

                b = 1;

                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(responseBody)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

//                                progressBar.setVisibility(View.VISIBLE);
                                imageView.setImageBitmap(resource);
                                save(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                restore();

                Toast.makeText(getApplicationContext(), "Check your internet connection. Please try after sometime later.", Toast.LENGTH_LONG).show();


                Log.d("res101", "error " + error);
//                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
//                getImage();
            }
        });
    }

    private void save(Bitmap resource) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resource.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        String encoded = Base64.encodeToString(b, Base64.DEFAULT);


        @SuppressLint("WrongConstant") SharedPreferences sp = getSharedPreferences("AppSharedPref", MODE_PRIVATE); // Open SharedPreferences with name AppSharedPref
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ImagePath", encoded); // Store selectedImagePath with key "ImagePath". This key will be then used to retrieve data.
        editor.commit();
    }


}