package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerAdapter
 * Version info
 * 30-06-17
 * Created by Dzmitry_Slutski.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.BaseViewHolder> {

    private final List<Item> mModels = new ArrayList<>();
    private final LayoutInflater mInflater;

    RecyclerAdapter(final Context pContext, final Iterable<IRoot> pModels) {
        mInflater = LayoutInflater.from(pContext);

        fillModelList(pModels);
    }

    private void fillModelList(final Iterable<IRoot> pModels) {
        for (final IRoot season : pModels) {
            Item item = new Item();
            item.type = CustomLayoutManager.TYPE_SEASON;
            item.item = season;
            mModels.add(item);

            final List<IModel> models = season.getModels();
            for (final IModel episode : models) {
                item = new Item();
                item.type = CustomLayoutManager.TYPE_EPISOD;
                item.item = episode;

                mModels.add(item);
            }
        }
    }

    @Override
    public RecyclerAdapter.BaseViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == CustomLayoutManager.TYPE_EPISOD) {
            return new EpisodeViewHolder(mInflater.inflate(R.layout.rv_episode_item, parent, false));
        } else if (viewType == CustomLayoutManager.TYPE_SEASON) {
            return new SeasonViewHolder(mInflater.inflate(R.layout.rv_season_item, parent, false));
        } else {
            throw new RuntimeException("Unknown view type!");
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerAdapter.BaseViewHolder holder, final int position) {
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

    abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

        BaseViewHolder(final View itemView) {
            super(itemView);
        }

        protected abstract void draw(T t);
    }

    public void removeItem(int index) {
        mModels.remove(index);
        notifyItemRemoved(index);
    }

    class SeasonViewHolder extends BaseViewHolder<IRoot> {

        ImageView mImage;
        TextView mTitle;
        TextView mSubTitle;

        SeasonViewHolder(final View itemView) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.season_image);
            mTitle = (TextView) itemView.findViewById(R.id.season_title);
            mSubTitle = (TextView) itemView.findViewById(R.id.season_sub_title);
        }

        @Override
        protected void draw(final IRoot pIRoot) {
            mImage.setImageResource(pIRoot.imageId());
            mTitle.setText(pIRoot.title());
            mSubTitle.setText(pIRoot.subTitle());

            itemView.setBackgroundColor(pIRoot.getColor());
        }
    }

    class EpisodeViewHolder extends BaseViewHolder<IModel> {

        ImageView mImage;
        TextView mTitle;
        TextView mSubTitle;

        EpisodeViewHolder(final View itemView) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.episode_image);
            mTitle = (TextView) itemView.findViewById(R.id.episode_title);
            mSubTitle = (TextView) itemView.findViewById(R.id.episode_sub_title);
        }

        @Override
        protected void draw(final IModel pIModel) {
            mImage.setImageResource(pIModel.imageId());
            mTitle.setText(pIModel.title());
            mSubTitle.setText(pIModel.subTitle());

            itemView.setBackgroundColor(pIModel.getColor());
        }
    }

    private class Item {

        Object item;
        int type;
    }
}
