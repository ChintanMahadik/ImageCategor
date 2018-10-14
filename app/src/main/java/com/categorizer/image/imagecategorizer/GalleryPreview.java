package com.categorizer.image.imagecategorizer;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by SHAJIB on 25/12/2015.
 */
public class GalleryPreview extends AppCompatActivity {

    ImageView GalleryPreviewImg;
    static String path;
    FloatingActionMenu optionsMenu;
    FloatingActionButton share, viewTag, removeTag,assignTag;
    AlertDialog.Builder alert;
    ExifInterface exifInterface;
    static LinearLayout layout;
    static LinearLayout.LayoutParams lp;
    Spinner sItems;
    String tag_item=null;
    LinearLayout.LayoutParams params;
    static EditText description_text;
    static TextView add_newTag;
    static TextView tag_lable;
    static TextView description;
    Drawable d;
    static Button createTag;
    String album_name;
    String extras[];
    int image_index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gallery_preview);
        Intent intent = getIntent();
        getSupportActionBar().hide();
        extras=intent.getStringExtra("path").split(",");
        System.out.println("Gallery intent "+extras[0]);
        path = extras[0];
        image_index=Integer.parseInt(extras[1]);
        album_name=path.substring(0,path.lastIndexOf("/"));
        album_name=album_name.substring(album_name.lastIndexOf("/")+1,album_name.length());
       // Toast.makeText(GalleryPreview.this,album_name,Toast.LENGTH_SHORT).show();
        GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);
        Glide.with(GalleryPreview.this)
                .load(new File(path)) // Uri of the picture
                .into(GalleryPreviewImg);

        optionsMenu= (FloatingActionMenu) findViewById(R.id.floating_menu);
        share= (FloatingActionButton) findViewById(R.id.share);
        viewTag= (FloatingActionButton) findViewById(R.id.viewTag);
        removeTag= (FloatingActionButton) findViewById(R.id.removeTag);
        assignTag= (FloatingActionButton) findViewById(R.id.assign_tag);

        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Image From IC");
                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                //i.putExtra(Intent.ACTION_ATTACH_DATA, Uri.fromFile(new File(path)));
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });

        assignTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                {
                    alert = new AlertDialog.Builder(GalleryPreview.this);
                    layout = new LinearLayout(GalleryPreview.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(50,0,50,50);
                    lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    layout.setLayoutParams(lp);

                    //////////////////////Drop down////////////////////////////////
                    sItems=setDropDown(lp);

                    ///////////////////////////////////////////////////////////////
                    alert.setTitle("Options");
                    description_text = new EditText(GalleryPreview.this);
                    description_text.setLayoutParams(lp);

                    add_newTag= new TextView(GalleryPreview.this);
                    add_newTag.setText("Add New Tag");

                    tag_lable=new TextView(GalleryPreview.this);
                    tag_lable.setText("Select Tag");
                    tag_lable.setPadding(0,30,0,0);

                    description= new TextView(GalleryPreview.this);
                    description.setText("Describe Image");
                    description.setPadding(0,30,0,0);

                    d = getResources().getDrawable(R.drawable.add_button);
                    createTag=new Button(GalleryPreview.this);
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
                        ImageMetaData imd=new ImageMetaData();
                        String getData = null;
                        String setData;
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {

                                if((tag_item==null))
                                {
                                    Snackbar.make(view, "Either Tag or Description is Empty", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                }
                                else {


                                    if (path.endsWith(".png")) {
                                        //Toast.makeText(GalleryPreview.this,"This is PNG Image",Toast.LENGTH_SHORT).show();
                                        File location = new File(path.substring(0, path.lastIndexOf(".")));
                                        String fileName = path.substring(path.lastIndexOf("."));
                                        File dest = new File(path);
                                        FileInputStream fis;
                                        fis = new FileInputStream(dest);
                                        Bitmap img = BitmapFactory.decodeStream(fis);

                                        String filename = path.substring(0, path.lastIndexOf("."));
                                        String filename_jpg = filename + ".jpg";
                                        System.out.println("Filename is = " + filename_jpg);
                                        OutputStream out = new FileOutputStream(filename_jpg);

                                        if (img != null) {
                                            img.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                            //img.recycle();
                                        }
                                        dest.delete();
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));

                                        path = filename_jpg;
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                                        Glide.with(GalleryPreview.this)
                                                .load(new File(path)) // Uri of the picture
                                                .into(GalleryPreviewImg);
                                        exifInterface = new ExifInterface(path);
                                        exifInterface.setAttribute(ExifInterface.TAG_MAKE, tag_item);
                                        exifInterface.setAttribute("UserComment", description_text.getText().toString());
                                        exifInterface.saveAttributes();

                                        finish();
                                        startActivity(getIntent().putExtra("path", path + ";" + image_index));


                                    } else {
                                        exifInterface.setAttribute(ExifInterface.TAG_MAKE, tag_item);
                                        exifInterface.setAttribute("UserComment", description_text.getText().toString());
                                        exifInterface.saveAttributes();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getApplicationContext(), setData, Toast.LENGTH_LONG).show();
                            album_name=path.substring(0,path.lastIndexOf("/"));
                            album_name=album_name.substring(album_name.lastIndexOf("/")+1,album_name.length());

                            DatabaseHelper dbhelper=new DatabaseHelper(GalleryPreview.this,"LAST_TAGGED");
                            Boolean c= dbhelper.SAVE_LAST_TAGGED(album_name,image_index);
                            Cursor cur=dbhelper.getLASTTAGGED(album_name);
                            ArrayList<String> row = new ArrayList<>();
                            while(cur.moveToNext()){
                                row.add(cur.getString(0));
                            }
                            cur.close();
                            AlbumActivity.lastTaggedIndex=Integer.parseInt(row.get(0));
                        }
                    });
                    alert.show();
                }
            }
        });

        viewTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout layout = new LinearLayout(GalleryPreview.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50,0,50,50);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(lp);
                ///////////////////////////////////////////////////////////////
                alert = new AlertDialog.Builder(GalleryPreview.this);
                alert.setTitle("*Info");

                TextView tag_label=new TextView(GalleryPreview.this);
                tag_label.setText("Tag:");
                tag_label.setPadding(0,30,0,0);
                tag_label.setTypeface(tag_label.getTypeface(), Typeface.BOLD);

                TextView tag=new TextView(GalleryPreview.this);
                tag.setText(exifInterface.getAttribute(ExifInterface.TAG_MAKE));

                TextView description= new TextView(GalleryPreview.this);
                description.setText("Image Description:");
                description.setPadding(0,30,0,0);
                description.setTypeface(description.getTypeface(), Typeface.BOLD);

                TextView description_text = new TextView(GalleryPreview.this);
                description_text.setText(exifInterface.getAttribute("UserComment"));


                layout.addView(tag_label);
                layout.addView(tag);
                layout.addView(description);
                layout.addView(description_text);

                alert.setView(layout);
                alert.setMessage("Tag and Description Detail");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.show();
            }
        });


        removeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


                LinearLayout layout = new LinearLayout(GalleryPreview.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50,0,50,50);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setLayoutParams(lp);
                ///////////////////////////////////////////////////////////////
                alert = new AlertDialog.Builder(GalleryPreview.this);
                alert.setTitle("Are you sure you want to remove Tag ?");
                alert.setView(layout);
                alert.setMessage("Tag once removed cannot be fetched again");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            exifInterface.setAttribute(ExifInterface.TAG_MAKE,null);
                            exifInterface.setAttribute("UserComment",null);
                            exifInterface.saveAttributes();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(GalleryPreview.this,exifInterface.getAttribute(ExifInterface.TAG_MAKE),Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "Tag Removed Successfully", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

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
        });


        AlbumActivity.refresh=1;
        TagAlbumActivity.refresh=1;
    }
//
//
//    @Override
//    public void onBackPressed() {
//        finish();
//        startActivity(new Intent(GalleryPreview.this,AlbumActivity.class).putExtra("name",album_name));
//    }


    private void create_popup() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(GalleryPreview.this);
        LinearLayout layout = new LinearLayout(GalleryPreview.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 0, 50, 50);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(lp);

        ///////////////////////////////////////////////////////////////
        alert.setTitle("Create Tag");
        final EditText tag = new EditText(GalleryPreview.this);
        tag.setLayoutParams(lp);

        TextView tag_lable = new TextView(GalleryPreview.this);
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

                DatabaseHelper dbhelper=new DatabaseHelper(GalleryPreview.this,"IMAGE_TAGS");
                boolean insertData = dbhelper.addData(tag.getText().toString());

                Toast.makeText(GalleryPreview.this, ""+insertData, Toast.LENGTH_SHORT).show();

                Spinner sItems= setDropDown(GalleryPreview.lp);
                GalleryPreview.layout.removeAllViews();
                GalleryPreview.layout.addView(GalleryPreview.add_newTag);
                GalleryPreview.layout.addView(GalleryPreview.createTag);
                GalleryPreview.layout.addView(GalleryPreview.tag_lable);
                GalleryPreview.layout.addView(sItems);
                GalleryPreview.layout.addView(GalleryPreview.description);
                GalleryPreview.layout.addView(GalleryPreview.description_text);


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
        DatabaseHelper dbhelper=new DatabaseHelper(GalleryPreview.this,"IMAGE_TAGS");
        Cursor c= dbhelper.getData();
        ArrayList<String> tagList = new ArrayList<>();
        while(c.moveToNext()){
            tagList.add(c.getString(0));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(GalleryPreview.this, android.R.layout.simple_spinner_item, tagList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = new Spinner(GalleryPreview.this);
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
