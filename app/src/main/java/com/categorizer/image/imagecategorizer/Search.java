package com.categorizer.image.imagecategorizer;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import static com.categorizer.image.imagecategorizer.TaggedImagesInitializer.all_ImageList_toDisplay;
import static com.categorizer.image.imagecategorizer.TaggedImagesInitializer.all_ImageList;
import static com.categorizer.image.imagecategorizer.TaggedImagesInitializer.exifDataList;

public class Search extends AppCompatActivity {

    static EditText search_text;
    GridView imagesGridView;
    Button search;
    Intent intent=null;
    private ProgressBar pgsBar;
    static SingleAlbumAdapter_noCheck adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().hide();
        intent = getIntent();
        search=(Button)findViewById(R.id.button);
        search_text = (EditText) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                search.setEnabled(false);
                SearchImage si = new SearchImage();
                si.execute();
                search.setEnabled(true);
            }
        });
        MainActivity.refresh=1;
    }

    class SearchImage extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            pgsBar = (ProgressBar) findViewById(R.id.pBar);
            pgsBar.setVisibility(View.VISIBLE);
            imagesGridView = (GridView) findViewById(R.id.searchedImages);

        }
        @Override
        protected String doInBackground(String... strings) {

            String xml = "";
            try {
                TaggedImagesInitializer.doit=1;
                TaggedImagesInitializer.initialize_List(Search.this);
            } catch (Exception e) {

            }

            int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
            Resources resources = getApplicationContext().getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = iDisplayWidth / (metrics.densityDpi / 160f);
            if (dp < 360) {
                dp = (dp - 17) / 2;
                float px = Function.convertDpToPixel(dp, getApplicationContext());
                imagesGridView.setColumnWidth(Math.round(px));
            }
            //////////////Search for tag or desc
            if(search_text.getText().toString().isEmpty()){
                //Snackbar.make(view, "Search Field is Empty", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            else {
                all_ImageList_toDisplay.clear();
                for (int i = 0; i < exifDataList.size(); i++) {
                        ExifInterface exifInterface = exifDataList.get(i);
                        String tag = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                        String desc = exifInterface.getAttribute("UserComment");
                        if (tag != null)
                            if (desc != null) {
                                if (tag.equalsIgnoreCase(search_text.getText().toString()) || Pattern.compile(Pattern.quote(search_text.getText().toString()), Pattern.CASE_INSENSITIVE).matcher(desc).find()) {
                                    all_ImageList_toDisplay.add(all_ImageList.get(i));
                                }
                            } else {
                                if (tag.equalsIgnoreCase(search_text.getText().toString())) {
                                    all_ImageList_toDisplay.add(all_ImageList.get(i));
                                }
                            }
                }
                adapter = new SingleAlbumAdapter_noCheck(Search.this, all_ImageList_toDisplay);
            }
            return xml;
    }

        @Override
        protected void onPostExecute(String xml) {
            pgsBar.setVisibility(View.GONE);
            imagesGridView.setAdapter(adapter);
            imagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    Intent intent = new Intent(Search.this, GalleryPreview.class);
                    intent.putExtra("path", all_ImageList_toDisplay.get(+position).get(Function.KEY_PATH)+","+position);
                    startActivity(intent);
                }
            });
        }


    }


}
