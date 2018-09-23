package com.categorizer.image.imagecategorizer;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StartScreen extends AppCompatActivity {

    private ViewPager sliderViewPager;
    private LinearLayout dotslayout;
    private SliderAdapter sliderAdapter;

    private TextView[] dots;
    private Button prev;
    private Button next;
    private int currPage;

    private static int lstviewedpage=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        getSupportActionBar().hide();
        sliderViewPager= (ViewPager) findViewById(R.id.viewPager);
        dotslayout= (LinearLayout) findViewById(R.id.dots);

        sliderAdapter = new SliderAdapter(this);
        sliderViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        sliderViewPager.addOnPageChangeListener(onPageChangeListener);

        prev = (Button) findViewById(R.id.prev);
        next = (Button)findViewById(R.id.next);


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lstviewedpage--;
                sliderViewPager.setCurrentItem(currPage-1);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lstviewedpage++;
                if(next.getText()=="Finish" && lstviewedpage==3){
                    finish();
                }
                sliderViewPager.setCurrentItem(currPage+1);


            }
        });
    }

    public void addDotsIndicator(int position){
        dots=new TextView[3];
        dotslayout.removeAllViews();
        for(int i=0;i<dots.length;i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#9679;")+"  ");
            dots[i].setTextColor(getResources().getColor(R.color.Grey));

            dotslayout.addView(dots[i]);
        }

        if(dots.length>0){
            dots[position].setTextColor(getResources().getColor(R.color.White));
        }
    }

    ViewPager.OnPageChangeListener onPageChangeListener=new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currPage=position;
            lstviewedpage=currPage;
            if(position == 0 ){
                next.setEnabled(true);
                prev.setEnabled(false);
                prev.setVisibility(View.INVISIBLE);
                next.setText("Next");
                prev.setText("");
            }
            else if(position == dots.length-1){
                next.setEnabled(true);
                prev.setEnabled(true);
                prev.setVisibility(View.VISIBLE);
                next.setText("Finish");
                prev.setText("Back");
            }
            else{
                next.setEnabled(true);
                prev.setEnabled(true);
                prev.setVisibility(View.VISIBLE);
                next.setText("Next");
                prev.setText("Back");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    @Override
    public void onBackPressed() {

    }

    public class SliderAdapter extends PagerAdapter{
        Context context;
        LayoutInflater layoutInflater;

        public SliderAdapter(Context context){
            this.context=context;
        }
        //Arrays
        public int[] slide_images={
                R.drawable.start_image_1,
                R.drawable.start_image_1,
                R.drawable.start_image_1
        };

        public String[] slide_heading={
                "Categorize",
                "Search",
                "Delete Unused"
        };

        public String[] slide_desc={
                "An image (from Latin: imago) is an artifact that depicts visual perception, for example, a photo or a two-dimensional picture, that has a similar appearance to some subject—usually a physical object or a person, thus providing a depiction of it. In context of image signal processing, an image is a distributed amplitude of color",
                "An image (from Latin: imago) is an artifact that depicts visual perception, for example, a photo or a two-dimensional picture, that has a similar appearance to some subject—usually a physical object or a person, thus providing a depiction of it. In context of image signal processing, an image is a distributed amplitude of color",
                "An image (from Latin: imago) is an artifact that depicts visual perception, for example, a photo or a two-dimensional picture, that has a similar appearance to some subject—usually a physical object or a person, thus providing a depiction of it. In context of image signal processing, an image is a distributed amplitude of color"
        };

        @Override
        public int getCount() {
            return slide_heading.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (RelativeLayout)object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View view =layoutInflater.inflate(R.layout.slide,container,false);
            ImageView image=view.findViewById(R.id.slide_image);
            TextView heading=view.findViewById(R.id.heading);
            TextView desc=view.findViewById(R.id.description);

            image.setImageResource(slide_images[position]);
            heading.setText(slide_heading[position]);
            desc.setText(slide_desc[position]);

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout)object);
        }
    }

}
