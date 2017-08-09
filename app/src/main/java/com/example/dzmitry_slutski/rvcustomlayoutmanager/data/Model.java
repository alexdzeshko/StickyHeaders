package com.example.dzmitry_slutski.rvcustomlayoutmanager.data;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.IModel;

import java.util.Random;

public class Model implements IModel {

    private final Random mRandom = new Random();
    String title;
    String subTitle;
    int imageResId;
    int color;

    Model(final int imgId, final String pTitle, final String pSubTitle) {
        title = pTitle;
        subTitle = pSubTitle;
        imageResId = imgId;
        color = mRandom.nextInt();
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

    @Override
    public int getColor() {
        return color;
    }
}
