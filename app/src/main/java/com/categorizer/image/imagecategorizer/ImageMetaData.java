package com.categorizer.image.imagecategorizer;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bumptech.glide.util.Util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ImageMetaData extends AppCompatActivity
{
    public String getMetaData(String imagePath, Context context) throws IOException {
        System.out.println("Image Path is "+imagePath);
        Uri uri=Uri.fromFile(new File(imagePath));
        InputStream in =new FileInputStream(imagePath);
        System.out.println("URI is "+uri);
        try {


            ExifInterface exifInterface = new ExifInterface(imagePath);
            String exif = "";

            exif += "\n Tag Name: " +
                    exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            exif += "\n Description: " +
                    exifInterface.getAttribute("UserComment");

            System.out.println("Exif is " + exif);
           // Toast.makeText(context, exif, Toast.LENGTH_LONG).show();

            return exif;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }


    public String setMetaData(String imagePath, Context context,String tag_name,String desc){
        System.out.println("Image Path is "+imagePath);
        try {

            ExifInterface exifInterface = new ExifInterface(imagePath);
            exifInterface.setAttribute(ExifInterface.TAG_MAKE,tag_name);
            exifInterface.setAttribute("UserComment",desc);
            exifInterface.saveAttributes();
            String exif=" Tag Name is set to "+exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            exif+="\nDescription is set to "+exifInterface.getAttribute("UserComment");
            exif+="\nfile source is set to "+exifInterface.getAttribute(ExifInterface.TAG_FILE_SOURCE);

            System.out.println(exif);
            return exif;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

}
