package com.categorizer.image.imagecategorizer;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Myadd extends AppCompatActivity {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Test ads are being shown. "
            + "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";


    private Button skip,cancel;
    private ImageView ad_show;
    static int loadAd=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myadd);

        getSupportActionBar().hide();

        ad_show=(ImageView)findViewById(R.id.adshow);
        //skip=(Button)findViewById(R.id.skip);
        cancel=(Button)findViewById(R.id.cancel);

        ad_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loadAd==0) {
                    loadAd=1;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://redchillicrackers.com/"));
                    startActivity(browserIntent);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(Myadd.this,MainActivity.class));
            }
        });

//        skip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//                startActivity(new Intent(Myadd.this,MainActivity.class));
//            }
//        });

    }

    @Override
    protected void onResume() {
        if(loadAd==1){
            finish();
            startActivity(new Intent(Myadd.this,MainActivity.class));
        }
        super.onResume();
    }
}
