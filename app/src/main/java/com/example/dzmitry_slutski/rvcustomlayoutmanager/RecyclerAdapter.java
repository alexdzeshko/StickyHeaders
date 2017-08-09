package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.data.ItemsGenerator;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.view.BaseViewHolder;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.view.EpisodeViewHolder;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.view.Item;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.view.SeasonViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerAdapter
 * Version info
 * 30-06-17
 * Created by Dzmitry_Slutski.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private final List<Item> mModels = new ArrayList<>();
    private final LayoutInflater mInflater;

    RecyclerAdapter(final Context pContext, final Iterable<IRoot> pModels) {
        mInflater = LayoutInflater.from(pContext);

        mModels.addAll(new ItemsGenerator(pContext).fillModelList(pModels));
    }

    @Override
    public BaseViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == CustomLayoutManager.TYPE_ITEM) {
            return new EpisodeViewHolder(mInflater.inflate(R.layout.rv_episode_item, parent, false));
        } else if (viewType == CustomLayoutManager.TYPE_HEADER) {
            return new SeasonViewHolder(mInflater.inflate(R.layout.rv_season_item, parent, false));
        } else {
            throw new RuntimeException("Unknown view type!");
        }
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        holder.draw(mModels.get(position).item);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return mModels.get(position).type;
    }

    public void removeItem(int index) {
        mModels.remove(index);
        notifyItemRemoved(index);
    }

}
