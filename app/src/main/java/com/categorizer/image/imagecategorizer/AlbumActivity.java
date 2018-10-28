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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {
    GridView galleryGridView;
    static ArrayList<HashMap<String, String>> imageList = new ArrayList<>();
    static ArrayList<String> imageList_Selected = new ArrayList<String>();
    static String album_name = "";
    LoadAlbumImages loadAlbumTask;
    String tag_item=null;
    static int lastTaggedIndex=0;
    static int select_counter=0;
    static SingleAlbumAdapter adapter;
    static SingleAlbumAdapter_noCheck adapter_noCheck;
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
    static FloatingActionMenu optionsMenu;
    FloatingActionButton share, removeTag,assignTag,deleteUntagged,selectMultiple;
    static int showMultiple_options=0;
    static int checked_item=0;
    static int deleted=0;
    static int select_all=0;
    static int tagsareAssigned=0;
    static int mintaggedIndex=0;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
        optionsMenu= (FloatingActionMenu) findViewById(R.id.floating_menu);
        share= (FloatingActionButton) findViewById(R.id.share);
        removeTag= (FloatingActionButton) findViewById(R.id.remove_tag);
        assignTag= (FloatingActionButton) findViewById(R.id.assign_tag);
        deleteUntagged=(FloatingActionButton)findViewById(R.id.delete_untagged);
        selectMultiple=(FloatingActionButton)findViewById(R.id.multiple_select);
        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();

        if(deleted==1){
            deleted=0;
            finish();
            startActivity(getIntent());
        }


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageList_Selected.size()>0) {
                    ArrayList<Uri> files = new ArrayList<Uri>();
                    for (int k = 0; k < imageList_Selected.size(); k++) {
                        File file = new File(imageList_Selected.get(k));
                        Uri uri = Uri.fromFile(file);
                        files.add(uri);
                    }

                    Intent i = new Intent(android.content.Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image From IC");
                    //To be coded
                    i.putExtra(Intent.EXTRA_STREAM, files);
                    //i.putExtra(Intent.ACTION_ATTACH_DATA, Uri.fromFile(new File(path)));
                    startActivity(Intent.createChooser(i, "Share via"));
                }
                else{
                    Snackbar.make(view, "Please Select At least One Image", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        if(showMultiple_options==0 && checked_item==0 ){
            AlbumActivity.imageList_Selected.clear();
        }
        assignTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(imageList_Selected.size()==0){
                    Snackbar.make(view, "Please Select At least One Image", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else
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

                    alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        ImageMetaData imd=new ImageMetaData();
                        String getData = null;
                        String setData;
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setNegativeButton("Assign Tag", new DialogInterface.OnClickListener() {
                        ImageMetaData imd = new ImageMetaData();
                        String getData = null;
                        String setData;

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if((tag_item == null))
                            {
                                Snackbar.make(view, "Either Tag or Description is Empty", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                            else{
                            System.out.println("Total Size of array list to assign Tag " + imageList_Selected.size());
                            for (int i = 0; i < imageList_Selected.size(); i++) {

                                try {

                                    if (imageList_Selected.get(i).endsWith(".png")) {
                                        //Toast.makeText(GalleryPreview.this,"This is PNG Image",Toast.LENGTH_SHORT).show();
                                        File dest = new File(imageList_Selected.get(i));
                                        FileInputStream fis;
                                        fis = new FileInputStream(dest);
                                        Bitmap img = BitmapFactory.decodeStream(fis);

                                        String filename = imageList_Selected.get(i).substring(0, imageList_Selected.get(i).lastIndexOf("."));
                                        String filename_jpg = filename + ".jpg";
                                        System.out.println("Filename is = " + filename_jpg);
                                        OutputStream out = new FileOutputStream(filename_jpg);

                                        if (img != null) {
                                            img.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                            //img.recycle();
                                        }
                                        dest.delete();
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList_Selected.get(i)))));
                                        imageList.get(i).put(Function.KEY_PATH, filename_jpg);

                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                                        setData = imd.setMetaData(imageList.get(i).get(Function.KEY_PATH), getApplicationContext(), tag_item, description_text.getText().toString());
                                        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filename_jpg))));


                                    } else {
                                        boolean flag=isExternalStorageWritable();
                                        System.out.println("Assigned flag" + flag);
                                        setData = imd.setMetaData(imageList_Selected.get(i), getApplicationContext(), tag_item, description_text.getText().toString());

                                    }


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //Toast.makeText(getApplicationContext(), setData, Toast.LENGTH_LONG).show();
                                DatabaseHelper dbhelper = new DatabaseHelper(AlbumActivity.this, "LAST_TAGGED");

                                //Boolean c = dbhelper.SAVE_LAST_TAGGED(album_name, lastTaggedIndex);
                                Cursor cur = dbhelper.getLASTTAGGED(album_name);
                                ArrayList<String> row = new ArrayList<>();
                                while (cur.moveToNext()) {
                                    row.add(cur.getString(0));
                                }
                                if (cur.getCount() > 0) {
                                    if (Integer.parseInt(row.get(0)) > lastTaggedIndex) {
                                        lastTaggedIndex = Integer.parseInt(row.get(0));
                                        System.out.println("Index Greater Assigned to " + lastTaggedIndex);
                                    } else {
                                        dbhelper.SAVE_LAST_TAGGED(album_name, lastTaggedIndex);
                                    }
                                } else
                                    dbhelper.SAVE_LAST_TAGGED(album_name, lastTaggedIndex);
                                    cur.close();
                                System.out.println("Assigned Index" + lastTaggedIndex);
                                //Delete untagged before this index
                            }
                            showMultiple_options = 0;
                            select_all = 0;

                            //tagsareAssigned=1;
                            AlbumActivity.adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
                            galleryGridView.setAdapter(AlbumActivity.adapter);
                            imageList_Selected.clear();
                            finish();
                            startActivity(getIntent());
                        }
                    }
                    });
                    alert.show();
                }

            }
        });

        removeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(imageList_Selected.size()>0)
                {

                final LinearLayout layout = new LinearLayout(AlbumActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 0, 50, 50);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setLayoutParams(lp);
                ///////////////////////////////////////////////////////////////
                alert = new AlertDialog.Builder(AlbumActivity.this);
                alert.setTitle("Are you sure you want to remove Tag ?");
                alert.setView(layout);
                alert.setMessage("Tag once removed cannot be fetched again");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            for (int j = 0; j < imageList_Selected.size(); j++) {
                                ExifInterface exifInterface = new ExifInterface(imageList_Selected.get(j));
                                exifInterface.setAttribute(ExifInterface.TAG_MAKE, null);
                                exifInterface.setAttribute("UserComment", null);
                                exifInterface.saveAttributes();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(GalleryPreview.this,exifInterface.getAttribute(ExifInterface.TAG_MAKE),Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "Tag Removed Successfully", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        DatabaseHelper dbhelper = new DatabaseHelper(AlbumActivity.this, "LAST_TAGGED");
                        Cursor cur = dbhelper.getLASTTAGGED(album_name);
                        ArrayList<String> row = new ArrayList<>();
                        while (cur.moveToNext()) {
                            row.add(cur.getString(0));
                        }
                        cur.close();
                        String tagged=null;
                        if(cur.getCount()>0) {
                            tagged = imageList.get(Integer.parseInt(row.get(0))).get(Function.KEY_PATH);
                            if (Integer.parseInt(row.get(0)) <= lastTaggedIndex && imageList_Selected.contains(tagged))
                                dbhelper.SAVE_LAST_TAGGED(album_name, 0);
                            else
                                lastTaggedIndex = Integer.parseInt(row.get(0));
                        }
                        else
                            dbhelper.SAVE_LAST_TAGGED(album_name, 0);

                        Cursor cur2 = dbhelper.getLASTTAGGED(album_name);
                        ArrayList<String> row2 = new ArrayList<>();
                        while (cur2.moveToNext()) {
                            row2.add(cur2.getString(0));
                        }
                        cur2.close();
                        lastTaggedIndex = Integer.parseInt(row2.get(0));
                        System.out.println("Removed Index" + lastTaggedIndex);

                        dialogInterface.dismiss();
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.show();


            }
            else{
                    Snackbar.make(view, "Please Select At least One Image", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });


        deleteUntagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout layout = new LinearLayout(AlbumActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 0, 50, 50);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(500, 500);
                layout.setLayoutParams(lp);
                ///////////////////////////////////////////////////////////////

                alert = new AlertDialog.Builder(AlbumActivity.this);
                alert.setTitle("Are you sure you want to Delete UnTagged Images ?");

                DatabaseHelper dbhelper = new DatabaseHelper(AlbumActivity.this, "LAST_TAGGED");
                Cursor cur = dbhelper.getLASTTAGGED(album_name);
                ArrayList<String> row = new ArrayList<>();
                while (cur.moveToNext()) {
                    row.add(cur.getString(0));
                }
                if(cur.getCount()>0){
                    lastTaggedIndex = Integer.parseInt(row.get(0));
                }
                else
                    lastTaggedIndex=0;

                if(lastTaggedIndex==0) {
                    alert.setMessage("No Last Tagged Image Found");
                }
                else {
                    alert.setMessage("Here is the Last Tagged Image");
                    ImageView im=new ImageView(AlbumActivity.this);
                    im.setImageURI(Uri.fromFile(new File(imageList.get(lastTaggedIndex).get(Function.KEY_PATH))));
                    im.setLayoutParams(lp);
                    layout.addView(im);
                }
                cur.close();
                alert.setView(layout);
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        DatabaseHelper dbhelper = new DatabaseHelper(AlbumActivity.this, "LAST_TAGGED");
                        Cursor cur = dbhelper.getLASTTAGGED(album_name);
                        ArrayList<String> row = new ArrayList<>();
                        while (cur.moveToNext()) {
                            row.add(cur.getString(0));
                        }
                        if(cur.getCount()>0){
                            lastTaggedIndex = Integer.parseInt(row.get(0));
                        }
                        else
                            lastTaggedIndex=0;
                        cur.close();
///////////////////////////////Get Tags from database//////////
                        DatabaseHelper dbhelpert=new DatabaseHelper(AlbumActivity.this,"IMAGE_TAGS");
                        Cursor ct= dbhelpert.getData();
                        ArrayList<String> tagList = new ArrayList<>();
                        while(ct.moveToNext()){
                            tagList.add(ct.getString(0));
                        }
                        ct.close();

                        System.out.println("Initial Last Index is set to " + lastTaggedIndex);
                        int k=0;
                        for (int i = 0; i < lastTaggedIndex; i++) {
                            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageList.get(i).get(Function.KEY_PATH).toString()))));
                            try {
                                ExifInterface exifInterface = new ExifInterface(imageList.get(i).get(Function.KEY_PATH).toString());
                                String tag = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                                System.out.println("Tag name is " + tag);
                                System.out.println("Row size is " + tagList.size());
                                if (!tagList.contains(tag)) {
                                    File f = new File(imageList.get(i).get(Function.KEY_PATH));
                                    f.delete();
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
                                    imageList.get(i).remove(Function.KEY_PATH);

                                    k++;
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        DatabaseHelper dbhelper1 = new DatabaseHelper(AlbumActivity.this, "LAST_TAGGED");
                        Boolean c = dbhelper1.SAVE_LAST_TAGGED(album_name, lastTaggedIndex-k);
                        Cursor cur1 = dbhelper1.getLASTTAGGED(album_name);
                        ArrayList<String> row1 = new ArrayList<>();
                        while (cur1.moveToNext()) {
                            row1.add(cur1.getString(0));
                        }
                        cur1.close();
                        lastTaggedIndex = Integer.parseInt(row1.get(0));
                        System.out.println("Deleted Untagged Index" + lastTaggedIndex);
                        //galleryGridView.setAdapter(adapter);

                        AlbumActivity.adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
                        galleryGridView.setAdapter(AlbumActivity.adapter);
                        //lastTaggedIndex = 0;
                        System.out.println("Last Index is " + lastTaggedIndex);
                        showMultiple_options=0;
                        deleted=1;
                        finish();
                        startActivity(getIntent());
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });
                alert.show();



            }
        });

        selectMultiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_all=1;
                if(showMultiple_options!=1) {
                    lastTaggedIndex=0;
                    showMultiple_options = 1;
                    //select_all=1;
                    finish();
                    startActivity(getIntent());
                }
                else{
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        MainActivity.refresh=1;
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

                if(path.contains("/emulated/0"))
                imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            if(showMultiple_options!=0) {
                showMultiple_options=1;
                adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
                galleryGridView.setAdapter(adapter);
                imageList_Selected.clear();
            }
            else{
                adapter_noCheck = new SingleAlbumAdapter_noCheck(AlbumActivity.this, imageList);
                galleryGridView.setAdapter(adapter_noCheck);
                int index = galleryGridView.getFirstVisiblePosition();
                galleryGridView.setSelection(index);
            }

            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);
                    intent.putExtra("path", imageList.get(+position).get(Function.KEY_PATH) + ","+position );
                    startActivity(intent);
                }
            });

            galleryGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, long l)
                {
                    showMultiple_options=1;
                    checked_item=i;
                    //lastTaggedIndex=i;
                    select_all=0;
                    finish();
                    startActivity(getIntent());
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

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if(showMultiple_options==1){
            showMultiple_options=0;
            checked_item=0;
            finish();
            startActivity(getIntent());
        }
        else if(optionsMenu.isOpened()){
            optionsMenu.close(true);
        }
        else{
            DatabaseHelper dbhelper1 = new DatabaseHelper(AlbumActivity.this, "LAST_TAGGED");
            dbhelper1.TRUNC_TABLE();

            //imageList_Selected.clear();
            super.onBackPressed();
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
    private ArrayList<Integer> checked_item=new ArrayList<>();
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

    public View getView(final int position, View convertView, ViewGroup parent) {

        System.out.println("Position = "+ position);
        SingleAlbumViewHolder holder = null;

            holder = new SingleAlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.single_album_row, parent, false);
            holder.checkBox=(CheckBox) convertView.findViewById(R.id.checkBox);
            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            convertView.setTag(holder);


        holder.checkBox.setId(position);
        holder.galleryImage.setId(position);
        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {

            Glide.with(activity).load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);

            holder.galleryImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.startActivity(new Intent(activity,GalleryPreview.class).putExtra("path",data.get(position).get(Function.KEY_PATH)+","+position));
                }
            });
//Very important
            if(AlbumActivity.select_all==1){

                    holder.checkBox.setChecked(true);
                    AlbumActivity.lastTaggedIndex=AlbumActivity.imageList.size()-1;

                for(int i=0;i<=AlbumActivity.lastTaggedIndex;i++){
                    if(!AlbumActivity.imageList_Selected.contains(AlbumActivity.imageList.get(i).get(Function.KEY_PATH)))
                        AlbumActivity.imageList_Selected.add(AlbumActivity.imageList.get(i).get(Function.KEY_PATH));
                }


                System.out.println("Last Auto selected index is "+AlbumActivity.lastTaggedIndex);
                //AlbumActivity.select_all=0;
            }
            if(checked_item.contains(position)){

                holder.checkBox.setChecked(true);
            }
            System.out.println(AlbumActivity.imageList_Selected);

            final SingleAlbumViewHolder finalHolder = holder;
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(finalHolder.checkBox.isChecked()){
                        checked_item.add(position);
                        finalHolder.checkBox.setChecked(true);
                        AlbumActivity.imageList_Selected.add(data.get(position).get(Function.KEY_PATH));

                        if(position>AlbumActivity.lastTaggedIndex)
                            AlbumActivity.lastTaggedIndex=position;

                            System.out.println("Last Index = " +AlbumActivity.lastTaggedIndex);

                    }
                    else{
                        finalHolder.checkBox.setChecked(false);
                        if(AlbumActivity.imageList_Selected.contains(data.get(position).get(Function.KEY_PATH)))
                        {
                            AlbumActivity.imageList_Selected.remove(data.get(position).get(Function.KEY_PATH));
                        }

                    }
                    System.out.println(AlbumActivity.imageList_Selected);
                }
            });

        } catch (Exception e) {}
        return convertView;
    }


}


class SingleAlbumViewHolder {
    CheckBox checkBox;
    ImageView galleryImage;
}


class SingleAlbumAdapter_noCheck extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;
    public SingleAlbumAdapter_noCheck(Activity a, ArrayList < HashMap < String, String >> d) {
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
        SingleAlbumViewHolder_noCheck holder = null;
        if (convertView == null) {
            holder = new SingleAlbumViewHolder_noCheck();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.tagged_album_row, parent, false);
            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            convertView.setTag(holder);
        } else {
            holder = (SingleAlbumViewHolder_noCheck) convertView.getTag();
        }
        holder.galleryImage.setId(position);
        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {

            Glide.with(activity).load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);

        } catch (Exception e) {}
        return convertView;
    }


}

class SingleAlbumViewHolder_noCheck {
    ImageView galleryImage;

}
