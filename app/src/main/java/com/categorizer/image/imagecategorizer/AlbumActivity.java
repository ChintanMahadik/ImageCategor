package com.categorizer.image.imagecategorizer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AlbumActivity extends AppCompatActivity {
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    String album_name = "";
    LoadAlbumImages loadAlbumTask;
    String tag_item=null;
    static int lastTaggedIndex=0;
    static SingleAlbumAdapter adapter;
    static Spinner sItems;
    static LinearLayout.LayoutParams lp;
    static AlertDialog.Builder alert;
    static LinearLayout layout;
    static EditText description_text;
    static TextView add_newTag;
    static TextView tag_lable;
    static TextView description;
    Drawable d;
    static Button createTag;
    LinearLayout.LayoutParams params;
    static int refresh=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();
        album_name = intent.getStringExtra("name");
        setTitle(album_name);

        System.out.println("Last index "+lastTaggedIndex);
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


        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();


    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.album_option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.select_all:
                //Toast.makeText(AlbumActivity.this,"Select All",Toast.LENGTH_SHORT).show();
                break;
            case R.id.create_tag:
                CreateTag_Popup createTag_popup=new CreateTag_Popup();
                createTag_popup.DisplayPopup(AlbumActivity.this);
                break;

            case R.id.remove_tag:
                //Toast.makeText(AlbumActivity.this,"Remove Tag",Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                //Toast.makeText(AlbumActivity.this,"Delete UnTagged",Toast.LENGTH_SHORT).show();
                for(int i=0;i<lastTaggedIndex;i++){
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                    try {
                        ExifInterface exifInterface =new ExifInterface(imageList.get(i).get(Function.KEY_PATH).toString());
                        String tag=exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                        System.out.println("Tag name is "+tag);
                        if(tag==null){
                            File f=new File(imageList.get(i).get(Function.KEY_PATH));
                            f.delete();
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                            imageList.get(i).remove(Function.KEY_PATH);
                            AlbumActivity.adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                //galleryGridView.setAdapter(adapter);

                galleryGridView.setAdapter(AlbumActivity.adapter);
                lastTaggedIndex=0;
                System.out.println("Last Index is "+lastTaggedIndex);
                finish();
                startActivity(getIntent());
                break;
            case R.id.copy:
               // Toast.makeText(AlbumActivity.this,"Copy",Toast.LENGTH_SHORT).show();
                break;
            case R.id.move:
                //Toast.makeText(AlbumActivity.this,"Move",Toast.LENGTH_SHORT).show();
                break;
            case R.id.cut:
                //Toast.makeText(AlbumActivity.this,"Cut",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return  true;
    }

    class LoadAlbumImages extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";
            String path = null;
            String album = null;
            String timestamp = null;
            Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };


            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \""+album_name+"\"", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \""+album_name+"\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});
            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
            galleryGridView.setAdapter(adapter);


            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);
                    intent.putExtra("path", imageList.get(+position).get(Function.KEY_PATH) + ";"+position );
                    startActivity(intent);
                }
            });

            galleryGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                ImageMetaData imd=new ImageMetaData();
                String getData= null;
                String setData=null;


                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, long l)
                {
                    alert = new AlertDialog.Builder(AlbumActivity.this);
                    layout = new LinearLayout(AlbumActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(50,0,50,50);
                    lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    layout.setLayoutParams(lp);

                    //////////////////////Drop down////////////////////////////////
                    sItems=setDropDown(lp);

                    ///////////////////////////////////////////////////////////////
                    alert.setTitle("Options");
                    description_text = new EditText(AlbumActivity.this);
                    description_text.setLayoutParams(lp);

                    add_newTag= new TextView(AlbumActivity.this);
                    add_newTag.setText("Add New Tag");

                    tag_lable=new TextView(AlbumActivity.this);
                    tag_lable.setText("Select Tag");
                    tag_lable.setPadding(0,30,0,0);

                    description= new TextView(AlbumActivity.this);
                    description.setText("Describe Image");
                    description.setPadding(0,30,0,0);

                    d = getResources().getDrawable(R.drawable.add_button);
                    createTag=new Button(AlbumActivity.this);
                    params =
                            new LinearLayout.LayoutParams(60,60);
                    createTag.setBackground(d);
                    createTag.setLayoutParams(params);
                    createTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            create_popup();


                        }
                    });

                    //Setting views to layout [ tags , description]
                    layout.addView(add_newTag);
                    layout.addView(createTag);
                    layout.addView(tag_lable);
                    layout.addView(sItems);
                    layout.addView(description);
                    layout.addView(description_text);

                    //Set the layout to the alert
                    alert.setView(layout);
                    alert.setMessage("Please Select an options");

                    alert.setPositiveButton("Remove Tag", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                if(imageList.get(i).get(Function.KEY_PATH).toString().endsWith(".png")){
                                    //Toast.makeText(AlbumActivity.this,"This is PNG Image",Toast.LENGTH_SHORT).show();
                                    File dest = new File(imageList.get(i).get(Function.KEY_PATH).toString());
                                    FileInputStream fis;
                                    fis = new FileInputStream(dest);
                                    Bitmap img = BitmapFactory.decodeStream(fis);

                                    String filename=imageList.get(i).get(Function.KEY_PATH).toString().substring(0,imageList.get(i).get(Function.KEY_PATH).toString().lastIndexOf("."));
                                    String filename_jpg=filename+".jpg";
                                    System.out.println("Filename is = "+filename_jpg);
                                    OutputStream out=new FileOutputStream(filename_jpg);

                                    if(img!=null) {
                                        img.compress(Bitmap.CompressFormat.JPEG, 50, out);
                                    }

                                    dest.delete();
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                                    imageList.get(i).put(Function.KEY_PATH,filename_jpg);
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                                    galleryGridView.setAdapter(AlbumActivity.adapter);

                                    //ExifInterface exifInterface=new ExifInterface(imageList.get(i).get(Function.KEY_PATH));
                                    String removed=imd.setMetaData(imageList.get(i).get(Function.KEY_PATH), getApplicationContext(), null, null);
                                    getData = imd.getMetaData(imageList.get(i).get(Function.KEY_PATH),getApplicationContext());
                                    finish();
                                    startActivity(getIntent());
                                }
                                else{
                                    //ExifInterface exifInterface=new ExifInterface(imageList.get(i).get(Function.KEY_PATH));
                                    String removed=imd.setMetaData(imageList.get(i).get(Function.KEY_PATH), getApplicationContext(), null, null);
                                    //getData = imd.getMetaData(imageList.get(i).get(Function.KEY_PATH),getApplicationContext());
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getApplicationContext(), "Tag Removed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert.setNegativeButton("Assign Tag", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                if(imageList.get(i).get(Function.KEY_PATH).toString().endsWith(".png")){
                                    //Toast.makeText(AlbumActivity.this,"This is PNG Image",Toast.LENGTH_SHORT).show();
                                    File location = new File(imageList.get(i).get(Function.KEY_PATH).toString().substring(0,imageList.get(i).get(Function.KEY_PATH).toString().lastIndexOf(".")));
                                    String fileName=imageList.get(i).get(Function.KEY_PATH).toString().substring(imageList.get(i).get(Function.KEY_PATH).toString().lastIndexOf("."));
                                    File dest = new File(imageList.get(i).get(Function.KEY_PATH));
                                    FileInputStream fis;
                                    fis = new FileInputStream(dest);
                                    Bitmap img = BitmapFactory.decodeStream(fis);

                                    String filename=imageList.get(i).get(Function.KEY_PATH).toString().substring(0,imageList.get(i).get(Function.KEY_PATH).toString().lastIndexOf("."));
                                    String filename_jpg=filename+".jpg";
                                    System.out.println("Filename is = "+filename_jpg);
                                    OutputStream out=new FileOutputStream(filename_jpg);

                                    if(img!=null) {
                                        img.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                        //img.recycle();
                                    }
                                    dest.delete();
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                                    imageList.get(i).put(Function.KEY_PATH,filename_jpg);
                                    AlbumActivity.adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                                    setData = imd.setMetaData(imageList.get(i).get(Function.KEY_PATH), getApplicationContext(), tag_item, description_text.getText().toString());
                                    galleryGridView.setAdapter(AlbumActivity.adapter);
                                    finish();
                                    startActivity(getIntent());
                                }
                                else {
                                    setData = imd.setMetaData(imageList.get(i).get(Function.KEY_PATH), getApplicationContext(), tag_item, description_text.getText().toString());

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getApplicationContext(), setData, Toast.LENGTH_LONG).show();
                            DatabaseHelper dbhelper=new DatabaseHelper(AlbumActivity.this,"LAST_TAGGED");
                            Boolean c= dbhelper.SAVE_LAST_TAGGED(album_name,i);
                            Cursor cur=dbhelper.getLASTTAGGED(album_name);
                            ArrayList<String> row = new ArrayList<>();
                            while(cur.moveToNext()){
                                row.add(cur.getString(0));
                            }
                            lastTaggedIndex=Integer.parseInt(row.get(0));
                            System.out.println("Last Index is set to "+ lastTaggedIndex);
                            //Delete untagged before this index
                        }
                    });

                    alert.show();

                    return true;

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
    private void create_popup() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(AlbumActivity.this);
        LinearLayout layout = new LinearLayout(AlbumActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 0, 50, 50);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(lp);

        ///////////////////////////////////////////////////////////////
        alert.setTitle("Create Tag");
        final EditText tag = new EditText(AlbumActivity.this);
        tag.setLayoutParams(lp);

        TextView tag_lable = new TextView(AlbumActivity.this);
        tag_lable.setText("Tag");

        //Setting views to layout [ tags , description]
        layout.addView(tag_lable);
        layout.addView(tag);
        //Set the layout to the alert
        alert.setView(layout);
        alert.setMessage("Please type your tag name");

        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseHelper dbhelper=new DatabaseHelper(AlbumActivity.this,"IMAGE_TAGS");
                boolean insertData = dbhelper.addData(tag.getText().toString());

                //Toast.makeText(AlbumActivity.this, ""+insertData, Toast.LENGTH_SHORT).show();

                Spinner sItems= setDropDown(AlbumActivity.lp);
                AlbumActivity.layout.removeAllViews();
                AlbumActivity.layout.addView(AlbumActivity.add_newTag);
                AlbumActivity.layout.addView(AlbumActivity.createTag);
                AlbumActivity.layout.addView(AlbumActivity.tag_lable);
                AlbumActivity.layout.addView(sItems);
                AlbumActivity.layout.addView(AlbumActivity.description);
                AlbumActivity.layout.addView(AlbumActivity.description_text);


            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        alert.show();
    }

    public Spinner setDropDown(LinearLayout.LayoutParams lp){
        DatabaseHelper dbhelper=new DatabaseHelper(AlbumActivity.this,"IMAGE_TAGS");
        Cursor c= dbhelper.getData();
        ArrayList<String> tagList = new ArrayList<>();
        while(c.moveToNext()){
            tagList.add(c.getString(0));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AlbumActivity.this, android.R.layout.simple_spinner_item, tagList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = new Spinner(AlbumActivity.this);
        sItems.setLayoutParams(lp);
        sItems.setAdapter(adapter);

        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                tag_item=item;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return sItems;
    }
}



class SingleAlbumAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;
    public SingleAlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
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
        SingleAlbumViewHolder holder = null;
        if (convertView == null) {
            holder = new SingleAlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.single_album_row, parent, false);

            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);

            convertView.setTag(holder);
        } else {
            holder = (SingleAlbumViewHolder) convertView.getTag();
        }
        holder.galleryImage.setId(position);

        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {

            Glide.with(activity)
                    .load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);


        } catch (Exception e) {}
        return convertView;
    }
}


class SingleAlbumViewHolder {
    ImageView galleryImage;
}
