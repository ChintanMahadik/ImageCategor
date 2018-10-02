package com.categorizer.image.imagecategorizer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_PERMISSION_KEY = 1;
    LoadAlbum loadAlbumTask;
    GridView galleryGridView;
    NavigationView navigationView;
    static  ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    static int refresh =0;
    static String sfolderpaths;
    ProgressBar pb;
    static Spinner s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb=(ProgressBar)findViewById(R.id.progressBar) ;
        //startActivity(new Intent(MainActivity.this,Myadd.class));


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Gallery");
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.Open,R.string.Close);
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view );
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 int id=item.getItemId();
                 if(id==R.id.create_tag){
                     CreateTag_Popup createTag_popup=new CreateTag_Popup();
                     createTag_popup.DisplayPopup(MainActivity.this);
                 }
                if(id==R.id.delete_tag){
                    DeleteTag_Popup deleteTag_popup=new DeleteTag_Popup();
                    deleteTag_popup.DisplayPopup(MainActivity.this);
                }
                 if(id == R.id.tags_album){

                     System.out.println("Album size is "+albumList.size());
                     startActivity(new Intent(MainActivity.this,ShowTags.class).putExtra("album_list", sfolderpaths));
                 }
                 if(id==R.id.search){
                     String s="";
                     for(int i=0;i<albumList.size();i++){
                         s+=albumList.get(i).get(Function.KEY_PATH)+",";
                     }
                     startActivity(new Intent(MainActivity.this,Search.class).putExtra("album_list", s));
                 }
                 if(id==R.id.about){
                     Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://redchillicrackers.com/"));
                     startActivity(browserIntent);
                 }
                if(id==R.id.logo){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=chintan%20mahadik&hl=en"));
                    startActivity(browserIntent);

                }
                if(id==R.id.donate){
                    Toast.makeText(MainActivity.this,"This will redirect to Payment Page",Toast.LENGTH_SHORT).show();

                }
                if(id==R.id.share){
                    Intent i = new Intent(android.content.Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image Categorizer");
                    i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/search?q=chintan%20mahadik&hl=en");
                    startActivity(Intent.createChooser(i, "Share via"));

                }

                 return true;
            }
        });


         galleryGridView = (GridView) findViewById(R.id.galleryGridView);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if(dp < 360)
        {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    class LoadAlbum extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            albumList.clear();
        }

        protected String doInBackground(String... args) {

            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;


            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Function.getCount(getApplicationContext(), album);

                albumList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
            }
            cursor.close();
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending

            return xml;

        }

        @Override
        protected void onPostExecute(String xml) {

            AlbumAdapter adapter = new AlbumAdapter(MainActivity.this, albumList);
            galleryGridView.setAdapter(adapter);

            Toast.makeText(MainActivity.this,albumList.get(0).get(Function.KEY_PATH).toString(),Toast.LENGTH_SHORT);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    DatabaseHelper dbhelper1 = new DatabaseHelper(MainActivity.this, "LAST_TAGGED");
                    dbhelper1.TRUNC_TABLE();
                    Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                    intent.putExtra("name", albumList.get(+position).get(Function.KEY_ALBUM));
                    startActivity(intent);
                }
            });

            sfolderpaths="";
            for(int i=0;i<albumList.size();i++){
                sfolderpaths+=albumList.get(i).get(Function.KEY_PATH)+",";
            }
            System.out.println("All Paths are = "+sfolderpaths);
            TaggedImagesInitializer ti=new TaggedImagesInitializer();
            try {
                ti.initialize(MainActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            pb.setVisibility(View.GONE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
//                    loadAlbumTask = new LoadAlbum();
//                    loadAlbumTask.execute();
                } else
                {

                }
            }
        }

    }


    @Override
    public void onBackPressed() {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
            else {
                super.onBackPressed();
            }

    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(this, PERMISSIONS)){
            startActivity(new Intent(MainActivity.this,StartScreen.class));
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }
        else if (refresh!=0){
            refresh=0;
            pb.setVisibility(View.VISIBLE);
            if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
            finish();
            startActivity(getIntent());
        }
        else
        {
            pb.setVisibility(View.VISIBLE);
            loadAlbumTask = new LoadAlbum();
            loadAlbumTask.execute();


        }
    }

}

class AlbumAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;
    public AlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
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
        AlbumViewHolder holder = null;
        if (convertView == null) {
            holder = new AlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.album_row, parent, false);

            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            holder.gallery_count = (TextView) convertView.findViewById(R.id.gallery_count);
            holder.gallery_title = (TextView) convertView.findViewById(R.id.gallery_title);

            convertView.setTag(holder);
        } else {
            holder = (AlbumViewHolder) convertView.getTag();
        }
        holder.galleryImage.setId(position);
        holder.gallery_count.setId(position);
        holder.gallery_title.setId(position);

        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {
            holder.gallery_title.setText(song.get(Function.KEY_ALBUM));
            holder.gallery_count.setText(song.get(Function.KEY_COUNT));

            Glide.with(activity)
                    .load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);


        } catch (Exception e) {}
        return convertView;
    }
}


class AlbumViewHolder {
    ImageView galleryImage;
    TextView gallery_count, gallery_title;
}
