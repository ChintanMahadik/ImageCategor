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

public class CreateTag_Popup extends  AppCompatActivity{

    private static int flag=0;

    public boolean DisplayPopup(final Context context) {


        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 0, 50, 50);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(lp);

        ///////////////////////////////////////////////////////////////
        alert.setTitle("Create Tag");
        final EditText tag = new EditText(context);
        tag.setLayoutParams(lp);

        TextView tag_lable = new TextView(context);
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

                DatabaseHelper dbhelper=new DatabaseHelper(context,"IMAGE_TAGS");
                boolean insertData = dbhelper.addData(tag.getText().toString());

                Toast.makeText(context, ""+insertData, Toast.LENGTH_SHORT).show();
                CreateTag_Popup.flag=0;

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                CreateTag_Popup.flag=1;

            }
        });

        alert.show();
        if(CreateTag_Popup.flag==0)
            return true;
        else
            return false;

    }
}
