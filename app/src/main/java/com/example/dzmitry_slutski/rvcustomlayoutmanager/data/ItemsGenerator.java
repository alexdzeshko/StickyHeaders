package com.example.dzmitry_slutski.rvcustomlayoutmanager.data;

import android.content.Context;
import android.content.res.Resources;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.CustomLayoutManager;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.IModel;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.IRoot;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.view.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemsGenerator {

    private final int[] indexes = new int[]{4, 2, 5, 3, 1, 5, 5, 4, 3, 1, 3, 2, 4, 5, 1};

    private final Context context;

    public ItemsGenerator(final Context context) {
        this.context = context;
    }

    public List<IRoot> initList() {
        final Resources resources = context.getResources();
        final List<IRoot> roots = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < 15; i++) {

            final String seasonTitle = "Season #" + i + " index: " + (index++);
            final List<IModel> models = new ArrayList<>();
            for (int episodeNumber = 0; episodeNumber < indexes[i]; episodeNumber++) {
                models.add(new Model(getDrawableResIdByNumber(resources, episodeNumber), "S" + i + ", Ep #" + episodeNumber + " of " + indexes[i] + " index: " + index, "sub title: " + episodeNumber));
                index++;
            }

            roots.add(new Root(getDrawableResIdByNumber(resources, i), seasonTitle + " ep_c: " + models.size(), "Sub title of season #" + i, models));
        }

        return roots;
    }

    private int getDrawableResIdByNumber(final Resources resources, final int pEpisodeNumber) {
        return resources.getIdentifier("ic_" + pEpisodeNumber, "drawable", context.getPackageName());
    }

    public List<Item> fillModelList(final Iterable<IRoot> pModels) {
        final List<Item> items = new ArrayList<>();
        for (final IRoot season : pModels) {
            Item item = new Item(season, CustomLayoutManager.TYPE_HEADER);
            items.add(item);

            final List<IModel> models = season.getModels();
            for (final IModel episode : models) {
                item = new Item(episode, CustomLayoutManager.TYPE_ITEM);

                items.add(item);
            }
        }
        return items;
    }
}
