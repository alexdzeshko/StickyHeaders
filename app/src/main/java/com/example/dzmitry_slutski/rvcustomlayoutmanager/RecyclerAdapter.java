package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RecyclerAdapter
 * Version info
 * 30-06-17
 * Created by Dzmitry_Slutski.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder> {

    private final List<IModel> mModels = new ArrayList<>();
    private final LayoutInflater mInflater;

    RecyclerAdapter(final Context pContext, final Collection<IModel> pModels) {
        mInflater = LayoutInflater.from(pContext);
        mModels.addAll(pModels);
    }

    @Override
    public RecyclerAdapter.CustomViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new CustomViewHolder(mInflater.inflate(R.layout.rv_simple_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerAdapter.CustomViewHolder holder, final int position) {
        final IModel model = mModels.get(position);
        holder.mImage.setImageResource(model.imageId());
        holder.mTitle.setText(model.title());
        holder.mSubTitle.setText(model.subTitle());
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView mTitle;
        TextView mSubTitle;

        CustomViewHolder(final View itemView) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.image);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mSubTitle = (TextView) itemView.findViewById(R.id.sub_title);
        }
    }
}
