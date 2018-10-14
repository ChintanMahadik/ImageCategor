package com.categorizer.image.imagecategorizer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.categorizer.image.imagecategorizer.MainActivity.albumList;
import static com.categorizer.image.imagecategorizer.TaggedImagesInitializer.all_ImageList_toDisplay;
import static com.categorizer.image.imagecategorizer.TaggedImagesInitializer.all_ImageList;

import com.bumptech.glide.Glide;
import com.categorizer.image.imagecategorizer.TaggedImagesInitializer;

public class ShowTags extends AppCompatActivity {

    GridView tagsGridView;
    private ProgressBar pgsBar;
    String tagname;
    static int refresh=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tags);
        tagsGridView = (GridView) findViewById(R.id.showTags);

        LoadAllImages li=new LoadAllImages();
        li.execute();
        MainActivity.refresh=1;
    }

    class LoadAllImages extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            pgsBar=(ProgressBar)findViewById(R.id.pBar);
            pgsBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                TaggedImagesInitializer.initialize_tags(ShowTags.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            AlbumAdapter_Tags adapter = new AlbumAdapter_Tags(ShowTags.this, all_ImageList_toDisplay);
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
                    tagsGridView.setEnabled(false);
                }
            });


            pgsBar.setVisibility(View.GONE);
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


class AlbumAdapter_Tags extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;
    public AlbumAdapter_Tags(Activity a, ArrayList < HashMap < String, String >> d) {
        activity = a;
        data = d;
    }
    public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumViewHolder_Tags holder = null;
        if (convertView == null) {
            holder = new AlbumViewHolder_Tags();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.album_row, parent, false);

            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            holder.gallery_count = (TextView) convertView.findViewById(R.id.gallery_count);
            holder.gallery_title = (TextView) convertView.findViewById(R.id.gallery_title);

            convertView.setTag(holder);
        } else {
            holder = (AlbumViewHolder_Tags) convertView.getTag();
        }
        holder.galleryImage.setId(position);
        holder.galleryImage.setImageResource(R.drawable.categorize);
       // holder.gallery_count.setId(position);
        holder.gallery_title.setId(position);

        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {
            holder.gallery_title.setText(song.get(Function.KEY_ALBUM));
            holder.gallery_count.setText("Click to view Album");

            Glide.with(activity)
                    .load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);


        } catch (Exception e) {}
        return convertView;
    }
}

class AlbumViewHolder_Tags {
    ImageView galleryImage;
    TextView  gallery_count, gallery_title;
}
