package com.androidtim.funnycats;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    public static final int REQUEST_INTERNET_PERMISSION = 10;
    public static final String API_KEY = "86d2645c709de96a4e5e891d549b7ca9";
    public static final String SHARED_SECRET = "fcb38516f319fd5f";
    public static final String SEARCH_TEXT = "funny cats";

    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private View firstProgressBar;
    private List<String> urls = new ArrayList<>();
    private int page = 1;

    private SearchParameters searchParameters = new SearchParameters();
    private Flickr flickr = new Flickr(API_KEY, SHARED_SECRET, new REST());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(MainActivity.this);
        viewPager.setAdapter(pagerAdapter);
        firstProgressBar = findViewById(R.id.firstProgressBar);
        ImageView share = (ImageView) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urls.isEmpty()) return;

                String url = urls.get(viewPager.getCurrentItem());
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(sharingIntent, getResources().getText(R.string.send_to)));
            }
        });

        searchParameters.setSort(SearchParameters.RELEVANCE);
        searchParameters.setText(SEARCH_TEXT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUrls();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_INTERNET_PERMISSION
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadUrls();
        } else {
            Snackbar.make(viewPager, R.string.permissions_denied, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ask_permissons, v -> askPermissions())
                    .setActionTextColor(Color.GREEN)
                    .show();
        }
    }

    private void loadUrls() {
        if (viewPager == null || pagerAdapter == null) {
            return;
        }
        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            new FillUrlsTask().execute();
        } else {
            askPermissions();
        }
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
    }

    @Override
    public void onPageSelected(int position) {
        if (position > urls.size() - 10) {
            loadUrls();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private class FillUrlsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                PhotoList<Photo> photoList = flickr.getPhotosInterface().search(searchParameters, 200, page++);
                if (page >= photoList.getPages()) {
                    page = 1;
                }
                Collections.shuffle(photoList);

                int count = 0;
                for (Photo photo : photoList) {
                    if (++count > 20) break;

                    String url = getString(R.string.url_format,
                            photo.getFarm(), photo.getServer(), photo.getId(), photo.getSecret());
                    urls.add(url);
                }
                return true;
            } catch (FlickrException e) {
                e.printStackTrace();
                Snackbar.make(viewPager, R.string.connection_failed, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.connect, v -> startActivity(new Intent(Settings.ACTION_SETTINGS)))
                        .setActionTextColor(Color.GREEN)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success && firstProgressBar != null) {
                firstProgressBar.setVisibility(View.GONE);
            }
            if (pagerAdapter != null) {
                pagerAdapter.setUrls(new ArrayList<>(urls));
                pagerAdapter.notifyDataSetChanged();
            }
        }
    }

}
