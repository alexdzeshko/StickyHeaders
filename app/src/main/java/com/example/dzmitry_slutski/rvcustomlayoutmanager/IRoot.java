package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import java.util.List;

/**
 * IRoot
 * Version info
 * 30-06-17
 * Created by Dzmitry_Slutski.
 */

public interface IRoot {
    String id();
    int imageId();

    String title();
    String subTitle();

    List<IModel> getModels();

    int getColor();
}
