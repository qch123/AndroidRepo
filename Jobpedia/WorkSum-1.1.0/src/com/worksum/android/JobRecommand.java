package com.worksum.android;

import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Created by chao on 16/3/7.
 */
public class JobRecommand extends TitlebarFragment{
    @Override
    public int getLayoutId() {
        return R.layout.job_recommand;
    }

    @Override
    protected void setupView(ViewGroup vg, Bundle savedInstanceState) {
        super.setupView(vg, savedInstanceState);
        setTitle(R.string.inbox_title_recommand);
        setActionLeftDrawable(R.drawable.common_nav_arrow);
    }
}
