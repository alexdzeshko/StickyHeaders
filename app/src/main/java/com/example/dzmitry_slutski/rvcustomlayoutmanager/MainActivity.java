package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new CustomLayoutManager(this));
        recycler.setAdapter(new RecyclerAdapter(this, initList()));
    }

    private List<IModel> initList() {
        Resources resources = getResources();

        final List<IModel> models = new ArrayList<>();
        for (int i = 0; i < 82; i++) {
            models.add(new Model(resources.getIdentifier("ic_" + i, "drawable", getPackageName()), "Title #" + i, "sub title: " + i));
        }
        return models;
    }

    private class Model implements IModel {

        String title;
        String subTitle;
        int imageResId;

        Model(final int imgId, final String pTitle, final String pSubTitle) {
            title = pTitle;
            subTitle = pSubTitle;
            imageResId = imgId;
        }

        @Override
        public int imageId() {
            return imageResId;
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public String subTitle() {
            return subTitle;
        }

        @Override
        public String rootId() {
            return null;
        }
    }
}
