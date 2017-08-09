package com.example.dzmitry_slutski.rvcustomlayoutmanager.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    BaseViewHolder(final View itemView) {
        super(itemView);
    }

    public abstract void draw(T t);
}
