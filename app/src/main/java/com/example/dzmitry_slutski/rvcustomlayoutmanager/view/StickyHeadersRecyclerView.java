package com.example.dzmitry_slutski.rvcustomlayoutmanager.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.R;


public class StickyHeadersRecyclerView extends FrameLayout {

    RecyclerView recyclerView;
    RecyclerView.RecycledViewPool headerViewsPool;

    public StickyHeadersRecyclerView(@NonNull final Context context) {
        this(context, null);
    }

    public StickyHeadersRecyclerView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickyHeadersRecyclerView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        inflate(context, R.layout.view_frame_plus_recycler, this);

        headerViewsPool = new RecyclerView.RecycledViewPool();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }
}
