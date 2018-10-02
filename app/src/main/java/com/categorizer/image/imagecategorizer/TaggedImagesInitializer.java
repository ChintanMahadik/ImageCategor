package com.categorizer.image.imagecategorizer;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.GridView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TaggedImagesInitializer
{
    static ArrayList<HashMap<String, String>> all_ImageList = new ArrayList<HashMap<String, String>>();
    static ArrayList<HashMap<String, String>> all_ImageList_toDisplay = new ArrayList<HashMap<String, String>>();
    static ArrayList<String> exifDataList = new ArrayList<String>();
    public static void initialize(Context context) throws IOException {
        {
            all_ImageList.clear();
            all_ImageList_toDisplay.clear();
            exifDataList.clear();
            String xml = "";
            String s = MainActivity.sfolderpaths;
            String album_paths[]=s.split(",");
            String album_paths_folder[]=new String[album_paths.length];
            System.out.println("Inside Inilializer "+album_paths);


                String path = null;
                String album = null;
                String timestamp = null;
                String countPhoto = null;
                Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.MediaColumns.DATA,MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
                Cursor cursorExternal = context.getContentResolver().query(uriExternal, projection, "", null, null);
                Cursor cursorInternal = context.getContentResolver().query(uriInternal, projection, "", null, null);
                Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
                while (cursor.moveToNext()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    exifDataList.add(new ExifInterface(path).getAttribute(ExifInterface.TAG_MAKE));
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    all_ImageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
                }
                cursor.close();
                Collections.sort(all_ImageList, new MapComparator(Function.KEY_TIMESTAMP, "asc")); // Arranging photo album by timestamp decending


////////////////////Calling Database list of tags//////////////////////////////
            DatabaseHelper dbhelper=new DatabaseHelper(context,"IMAGE_TAGS");
            Cursor c= dbhelper.getData();
            ArrayList<String> tagList = new ArrayList<>();
            while(c.moveToNext()){
                tagList.add(c.getString(0));
            }
            System.out.println("Tag List is "+tagList);

            /////////////////////////////Getting the matched images from tagList and displaying it into Tag Folders///////////////////
            for(int i=0; i<tagList.size();i++){
                String path2 = null;
                String album2 = null;
                String timestamp2 = null;
                int countPhoto2 = 0;

                if(exifDataList.contains(tagList.get(i))){
                    int pIndex=exifDataList.lastIndexOf(tagList.get(i));
                    path2=all_ImageList.get(pIndex).get(Function.KEY_PATH).toString();
                    album2=tagList.get(i);
                    timestamp2=all_ImageList.get(i).get(Function.KEY_TIMESTAMP);
                    countPhoto2=Collections.frequency(exifDataList, tagList.get(i));
                    all_ImageList_toDisplay.add(Function.mappingInbox(album2,path2 , timestamp2, Function.converToTime(timestamp2), Integer.toString(countPhoto2)));
                }

            }
        }
    }
}
