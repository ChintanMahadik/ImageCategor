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

public class Search extends AppCompatActivity {

    static EditText search_text;
    static ArrayList<HashMap<String, String>> all_ImageList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> all_ImageList_toDisplay = new ArrayList<HashMap<String, String>>();
    GridView imagesGridView;
    Button search;
    Intent intent=null;
    //private ProgressBar pgsBar;
    static SingleAlbumAdapter_noCheck adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().hide();
        intent = getIntent();
        //pgsBar = (ProgressBar) findViewById(R.id.pBar);
        search=(Button)findViewById(R.id.button);
        search_text = (EditText) findViewById(R.id.search);
        SearchImage si = new SearchImage();
        //si.execute();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(search_text.getText().toString().isEmpty()){
                    Snackbar.make(view, "Search Field is Empty", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else {
                    all_ImageList_toDisplay.clear();
                    for (int i = 0; i < all_ImageList.size(); i++) {
                        try {

                            ExifInterface exifInterface = new ExifInterface(all_ImageList.get(i).get(Function.KEY_PATH).toString());
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

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter = new SingleAlbumAdapter_noCheck(Search.this, all_ImageList_toDisplay);

                    imagesGridView.setAdapter(adapter);
                    imagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                final int position, long id) {
                            Intent intent = new Intent(Search.this, GalleryPreview.class);
                            intent.putExtra("path", all_ImageList_toDisplay.get(+position).get(Function.KEY_PATH)+","+position);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
        MainActivity.refresh=1;
    }

    class SearchImage extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pgsBar.setVisibility(View.VISIBLE);
            all_ImageList.clear();
            all_ImageList_toDisplay.clear();
        }

        @Override
        protected String doInBackground(String... strings) {

            String xml = "";
            String s = intent.getStringExtra("album_list");
            String album_paths[] = s.split(",");
            String album_paths_folder[] = new String[album_paths.length];
            for (int i = 0; i < album_paths.length; i++) {
                album_paths_folder[i] = album_paths[i].substring(0, album_paths[i].lastIndexOf("/"));
            }

            imagesGridView = (GridView) findViewById(R.id.searchedImages);
            int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
            Resources resources = getApplicationContext().getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = iDisplayWidth / (metrics.densityDpi / 160f);

            if (dp < 360) {
                dp = (dp - 17) / 2;
                float px = Function.convertDpToPixel(dp, getApplicationContext());
                imagesGridView.setColumnWidth(Math.round(px));
            }


                for (int i = 0; i < album_paths_folder.length; i++) {

                    String path = null;
                    String album = null;
                    String timestamp = null;
                    String album_name = album_paths_folder[i].substring(album_paths_folder[i].lastIndexOf('/') + 1, album_paths_folder[i].length());
                    System.out.println(album_name);

                    Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

                    String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};


                    Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
                    Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
                    Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
                    while (cursor.moveToNext()) {

                        path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                        all_ImageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
                    }
                    cursor.close();
                    Collections.sort(all_ImageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
                }



            return xml;

    }


        @Override
        protected void onPostExecute(String xml) {
            //pgsBar.setVisibility(View.GONE);

        }


    }


}
