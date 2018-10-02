package com.categorizer.image.imagecategorizer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;


public class TagAlbumActivity extends AppCompatActivity {
    EditText search_text;
    ArrayList<HashMap<String, String>> all_ImageList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> all_Tag_ImageList_toDisplay = new ArrayList<HashMap<String, String>>();
    GridView imagesGridView;
    Button search;
    Intent intent=null;
    private ProgressBar pgsBar;
    SearchImage searchImage;
    String album_name;
    static int refresh=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_album);
//        /getSupportActionBar().hide();
        intent = getIntent();
        imagesGridView=(GridView)findViewById(R.id.displayTagImages) ;
        searchImage = new SearchImage();
        searchImage.execute();

        ShowTags.refresh=1;
    }

    class SearchImage extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pgsBar.setVisibility(View.VISIBLE);
            all_ImageList.clear();
            all_Tag_ImageList_toDisplay.clear();
        }

        @Override
        protected String doInBackground(String... strings) {

            String xml = "";
            String s = intent.getStringExtra("album_list");

            String paths[]=s.split(",");
            int len=paths.length;
            String title=paths[len-2].toString();
            System.out.println("Title is "+title);
            setTitle(title);
            System.out.println(paths[len-1]);
            ExifInterface exifInterface=null;
            String tag_name=null;
            for(int i=0;i<len-2;i++){
                try {
                    exifInterface =new ExifInterface(paths[i]);
                    tag_name=exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                    if(tag_name!=null)
                    if(tag_name.equals(title)){
                        all_Tag_ImageList_toDisplay.add(Function.mappingInbox(title, paths[i].toString(), paths[len-1], Function.converToTime(paths[len-1]), null));
                        Collections.sort(all_Tag_ImageList_toDisplay, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
           // pgsBar.setVisibility(View.GONE);
            SingleAlbumAdapter_noCheck adapter = new SingleAlbumAdapter_noCheck(TagAlbumActivity.this, all_Tag_ImageList_toDisplay);
            imagesGridView.setAdapter(adapter);

            imagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    Intent intent = new Intent(TagAlbumActivity.this, GalleryPreview.class);
                    intent.putExtra("path", all_Tag_ImageList_toDisplay.get(+position).get(Function.KEY_PATH)+","+position);
                    startActivity(intent);
                }
            });
        }


    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        if(refresh!=0) {
            refresh=0;
            finish();
            startActivity(getIntent());
        }
    }
}



