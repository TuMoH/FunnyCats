package timurkasoft.ru.funnycats;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {

    private List<String> urls;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    @Override
    public Fragment getItem(int position) {
        String url = urls != null ? urls.get(position) : null;
        return ImageFragment.newInstance(url != null ? url : "");
    }

    @Override
    public int getCount() {
        return urls != null ? urls.size() : 0;
    }

}
