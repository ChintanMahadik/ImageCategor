package com.categorizer.image.imagecategorizer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DeleteTag_Popup extends  AppCompatActivity{

    private static int flag=0;
    static String tag_item;
    static Spinner sItems;
    static Context c;
    public boolean DisplayPopup(final Context context) {

        c = context;
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50,0,50,50);
        LinearLayout.LayoutParams lp; lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(lp);

        sItems=setDropDown(lp);
        ///////////////////////////////////////////////////////////////
        alert.setTitle("Select Tag to Delete");
        layout.addView(sItems);
        //Set the layout to the alert
        alert.setView(layout);
        alert.setMessage("Please select your tag name");

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseHelper dbhelper=new DatabaseHelper(context,"IMAGE_TAGS");
                boolean deletedTag = dbhelper.deleteTag(tag_item);
                //Toast.makeText(context, ""+deletedTag, Toast.LENGTH_SHORT).show();
                DeleteTag_Popup.flag=0;

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                DeleteTag_Popup.flag=1;

            }
        });

        alert.show();
        if(DeleteTag_Popup.flag==0)
            return true;
        else
            return false;

    }

    public Spinner setDropDown(LinearLayout.LayoutParams lp){
        DatabaseHelper dbhelper=new DatabaseHelper(DeleteTag_Popup.c,"IMAGE_TAGS");
        Cursor c= dbhelper.getData();
        ArrayList<String> tagList = new ArrayList<>();
        while(c.moveToNext()){
            tagList.add(c.getString(0));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeleteTag_Popup.c, android.R.layout.simple_spinner_item, tagList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = new Spinner(DeleteTag_Popup.c);
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
