package com.categorizer.image.imagecategorizer;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.categorizer.image.imagecategorizer.MainActivity.albumList;

public class ShowTags extends AppCompatActivity {
    ArrayList<String> all_images_paths=new ArrayList<>();
    //it contains all images path data
    ArrayList<HashMap<String, String>> all_ImageList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> all_ImageList_toDisplay = new ArrayList<HashMap<String, String>>();
    Intent intent=null;
    GridView tagsGridView;
    private ProgressBar pgsBar;
    String tagname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tags);
        intent = getIntent();
        DisplayTagAlbums displayTagAlbums=new DisplayTagAlbums();
        pgsBar = (ProgressBar) findViewById(R.id.pBar);
        displayTagAlbums.execute();
    }

    class DisplayTagAlbums extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            all_ImageList.clear();
            all_ImageList_toDisplay.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            pgsBar.setVisibility(View.VISIBLE);
            String xml = "";
            String s = intent.getStringExtra("album_list");
            String album_paths[]=s.split(",");
            String album_paths_folder[]=new String[album_paths.length];
            for(int i=0;i<album_paths.length;i++){
                album_paths_folder[i]=album_paths[i].substring(0,album_paths[i].lastIndexOf("/"));
            }
            tagsGridView = (GridView) findViewById(R.id.showTags);
            int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
            Resources resources = getApplicationContext().getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = iDisplayWidth / (metrics.densityDpi / 160f);

            if(dp < 360)
            {
                dp = (dp - 17) / 2;
                float px = Function.convertDpToPixel(dp, getApplicationContext());
                tagsGridView.setColumnWidth(Math.round(px));
            }

////////////Getting the List of images into ArrayList with all attributes i.e path,timestamp, count etc///////////////
            for(int i=0;i<album_paths_folder.length;i++) {

                String path = null;
                String album = null;
                String timestamp = null;
                String countPhoto = null;
                String album_name = album_paths_folder[i].substring(album_paths_folder[i].lastIndexOf('/')+1,album_paths_folder[i].length());
                System.out.println(album_name);

                Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

                String[] projection = {MediaStore.MediaColumns.DATA,MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};


                Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
                Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
                Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
                while (cursor.moveToNext()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    all_ImageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
                    }
                cursor.close();
                Collections.sort(all_ImageList, new MapComparator(Function.KEY_TIMESTAMP, "asc")); // Arranging photo album by timestamp decending
            }


////////////////////Calling Database list of tags//////////////////////////////
            DatabaseHelper dbhelper=new DatabaseHelper(ShowTags.this,"IMAGE_TAGS");
            Cursor c= dbhelper.getData();
            ArrayList<String> tagList = new ArrayList<>();
            while(c.moveToNext()){
                tagList.add(c.getString(0));
            }
            System.out.println("Tag List is "+tagList);
            ExifInterface exifInterface = null;

    /////////////////////////////Getting the matched images from tagList and displaying it into Tag Folders///////////////////
            for(int i=0; i<tagList.size();i++){
                String path = null;
                String album = null;
                String timestamp = null;
                int countPhoto = 0;
                
                for(int j=0;j<all_ImageList.size();j++){
                    try {
                        exifInterface=new ExifInterface(all_ImageList.get(j).get(Function.KEY_PATH).toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String tagname=exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                    if(tagList.get(i).equals(tagname)){
                        path=all_ImageList.get(j).get(Function.KEY_PATH).toString();
                        album=tagname;
                        timestamp=all_ImageList.get(j).get(Function.KEY_TIMESTAMP);
                        countPhoto++;
                    }


                }
                if(countPhoto>0){
                    all_ImageList_toDisplay.add(Function.mappingInbox(album,path , timestamp, Function.converToTime(timestamp), Integer.toString(countPhoto)));
                   // break;
                }
            }

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            pgsBar.setVisibility(View.GONE);
            AlbumAdapter adapter = new AlbumAdapter(ShowTags.this, all_ImageList_toDisplay);
            tagsGridView.setAdapter(adapter);

            tagsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    String s="";
                    for(int i=0;i<all_ImageList.size();i++){
                        s+=all_ImageList.get(i).get(Function.KEY_PATH).toString()+",";
                    }
                    s+=all_ImageList_toDisplay.get(position).get(Function.KEY_ALBUM)+",";
                    s+=all_ImageList_toDisplay.get(position).get(Function.KEY_TIMESTAMP)+",";
                    Intent intent = new Intent(ShowTags.this, TagAlbumActivity.class);
                    intent.putExtra("album_list", s);
                    startActivity(intent);
                }
            });
        }


    }
}
