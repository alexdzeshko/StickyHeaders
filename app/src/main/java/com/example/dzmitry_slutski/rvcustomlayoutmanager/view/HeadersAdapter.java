package com.example.dzmitry_slutski.rvcustomlayoutmanager.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.R;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.recycler.MapRecyclerAdapter;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.recycler.SectionDataHolder;


public class HeadersAdapter extends MapRecyclerAdapter<Item, EpisodeViewHolder, EmptyViewHolder> {

    @Override
    public int sectionCode(Item item) {
        return 0;
    }

    @Override
    protected int itemViewType(Item item) {
        return 0;
    }

    @Override
    protected int sectionViewType() {
        return 1;
    }

    @Override
    protected EpisodeViewHolder itemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new EpisodeViewHolder(inflater.inflate(R.layout.rv_episode_item, parent, false));
    }

    @Override
    protected EmptyViewHolder sectionViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new EmptyViewHolder(new View(parent.getContext()));
    }

    @Override
    protected void onBindItemViewHolder(EpisodeViewHolder holder, Item item) {

    }

    @Override
    protected void onBindSectionViewHolder(EmptyViewHolder holder, SectionDataHolder<Item> sectionData) {

    }
}
