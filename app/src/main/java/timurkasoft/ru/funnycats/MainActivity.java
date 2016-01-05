package timurkasoft.ru.funnycats;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    public static final int REQUEST_INTERNET_PERMISSION = 10;
    public static final String API_KEY = "c119f34d934c39dc5408b8a80a764fcf";
    public static final String SHARED_SECRET = "f8ed288ca25aa2ef";
    public static final String SEARCH_TEXT = "funny cats";

    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
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

        searchParameters.setSort(SearchParameters.RELEVANCE);
        searchParameters.setText(SEARCH_TEXT);

        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            loadUrls();
        } else {
            askPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_INTERNET_PERMISSION
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadUrls();
        } else {
            Snackbar.make(viewPager, R.string.permissions_denied, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ask_permissons, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            askPermissions();
                        }
                    })
                    .show();
        }
    }

    private void loadUrls() {
        if (viewPager == null || pagerAdapter == null) {
            return;
        }
        new FillUrlsTask().execute();
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

    private class FillUrlsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (pagerAdapter != null) {
                pagerAdapter.setUrls(new ArrayList<>(urls));
                pagerAdapter.notifyDataSetChanged();
            }
        }
    }

}
