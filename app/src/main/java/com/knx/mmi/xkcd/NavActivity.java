package com.knx.mmi.xkcd;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private View comicView;
    private ImageView comicImage;
    private TextView titleView, captionView;
    private Integer comicId;
    private Document comicDoc;
    private Integer maxComic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        comicView = findViewById(R.id.comicLayout);
        comicImage = comicView.findViewById(R.id.image_comic);
        titleView = comicView.findViewById(R.id.textTitle);
        captionView = comicView.findViewById(R.id.textCaption);

        Drawable d = GetComic(null);
        if (d!=null){
            comicImage.setImageDrawable(d);
            maxComic = comicId;
        } else {
            Toast.makeText(getApplicationContext(),"Failed to load comic", Toast.LENGTH_LONG).show();
        }
    }

    private Drawable GetComic(String url){
        if (url==null){
            url = "https://xkcd.com";
        }

        final String webUrl = url;
        final Drawable[] draw = new Drawable[1];
        final String info[] = new String[2];

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                InternetPermissionCheck();
                Drawable d = null;
                Document cDoc;
                String ttl;
                String capt;

                try{
                    Document doc = Jsoup.connect(webUrl).get();

                    String tmpTitle = doc.title();
                    tmpTitle = tmpTitle.replace("xkcd: ", "");
                    Element comicImageInfo = doc.select("#comic img").first();
                    capt = comicImageInfo.attr("title");
                    String srcUrl = comicImageInfo.absUrl("src");

                    InputStream is = (InputStream) new URL(srcUrl).getContent();
                    d = Drawable.createFromStream(is, "src");
                    cDoc = doc;
                    ttl = tmpTitle;
                    comicDoc = cDoc;
                    comicId = GetComicId();

                   // Toast.makeText(getApplicationContext(),"Attempted to load " + srcUrl, Toast.LENGTH_LONG).show();
                } catch (IOException e){

                    Log.e("App", e.toString());
                    return;
                }

                draw[0] = d;

                info[0] = ttl;
                info[1] = capt;

            }
        });

        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        titleView.setText(info[0]+"   ("+comicId+")");
        captionView.setText(info[1]);


        return draw[0];
    }

    private void InternetPermissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED){

            int rCode = 0;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    rCode);
        } else {
            Log.i("App", "Has internet permission");
        }
    }

    public void PreviousImage(View v){

        Drawable d = GetComic("https://xkcd.com/"+(--comicId));
        if (d!=null){
            comicImage.setImageDrawable(d);
        } else {
            Toast.makeText(getApplicationContext(),"Failed to load comic", Toast.LENGTH_LONG).show();
        }
    }

    public void NextImage(View v){
        Drawable d = GetComic("https://xkcd.com/"+(++comicId));
        if (d!=null){
            comicImage.setImageDrawable(d);
        } else {
            Toast.makeText(getApplicationContext(),"Failed to load comic", Toast.LENGTH_LONG).show();
        }
    }

    public void FirstImage(View v){
        Drawable d = GetComic("https://xkcd.com/");
        if (d!=null){
            comicImage.setImageDrawable(d);
        } else {
            Toast.makeText(getApplicationContext(),"Failed to load comic", Toast.LENGTH_LONG).show();
        }
    }

    public void RandomImage(View v){
        Random r = new Random();

        Drawable d = GetComic("https://xkcd.com/"+r.nextInt(maxComic));
        if (d!=null){
            comicImage.setImageDrawable(d);
        } else {
            Toast.makeText(getApplicationContext(),"Failed to load comic", Toast.LENGTH_LONG).show();
        }
    }

    private Integer GetComicId(){
        Integer id = null;

        Elements list = comicDoc.select("li");

        for(Element e : list){
            String res = e.html();
            Element a = e.select("a").first();
            String rel = a.attr("rel");
            if (a.attr("rel").equals("prev")){
                String href = a.attr("href");
                if (href=="#"){
                    id = new Integer(1);
                } else {
                    id = Integer.parseInt(href.substring(1,href.length()-1))+1;
                }
                break;
            }
        }

        if (id == null){
            return comicId;
        }

        return id;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_archive) {
            // Handle the camera action
        } else if (id == R.id.nav_whatIf) {

        } else if (id == R.id.nav_blag) {

        } else if (id == R.id.nav_store) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_feed) {

        } else if (id == R.id.nav_email) {

        } else if (id == R.id.nav_favorites) {

        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
