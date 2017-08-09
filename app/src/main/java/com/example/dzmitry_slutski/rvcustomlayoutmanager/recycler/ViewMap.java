package com.example.dzmitry_slutski.rvcustomlayoutmanager.recycler;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;

public class ViewMap extends SparseArray<View> {

    private View parent;

    public ViewMap(View parent) {
        this.parent = parent;
    }

    public void put(@IdRes int id) {
        put(id, parent.findViewById(id));
    }

    public void put(@NonNull int... ids) {
        for (int id : ids) {
            put(id);
        }
    }

    public void click(@IdRes int id, View.OnClickListener onClickListener) {
        getView(id).setOnClickListener(onClickListener);
    }

    public void click(View.OnClickListener onClickListener) {
        parent.setOnClickListener(onClickListener);
    }

    public View getView(@IdRes int id) {
        View view = get(id);
        if (view == null) {
            view = parent.findViewById(id);
            put(id);
        }
        return view;
    }
}
