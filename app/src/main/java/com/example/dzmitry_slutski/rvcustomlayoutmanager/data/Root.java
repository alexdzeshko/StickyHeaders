package com.example.dzmitry_slutski.rvcustomlayoutmanager.data;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.IModel;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.IRoot;

import java.util.List;
import java.util.Random;

public class Root implements IRoot {

    private Random mRandom = new Random();

    String title;
    String subTitle;
    int imageResId;
    List<IModel> mModels;
    int color;

    Root(final int imgId, final String pTitle, final String pSubTitle, final List<IModel> pModels) {
        title = pTitle;
        subTitle = pSubTitle;
        imageResId = imgId;
        mModels = pModels;

        color = mRandom.nextInt();
    }

    @Override
    public String id() {
        return title;
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
    public List<IModel> getModels() {
        return mModels;
    }

    @Override
    public int getColor() {
        return color;
    }
}
