package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * CustomLayoutManager
 * Version info
 * 30-06-17
 * Created by Dzmitry_Slutski.
 */

public class CustomLayoutManager extends RecyclerView.LayoutManager {

    private final RecyclerView.OnFlingListener mOnFlingListener = new FlingListener();
    private final RecyclerView.OnScrollListener mOnScrollListener = new ScrollListener();
    private RecyclerView mAttachedRecyclerView;

    public CustomLayoutManager(final Context context) {

    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams lp) {
        return super.generateLayoutParams(lp);
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(final int dy, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    @Override
    public void onAttachedToWindow(final RecyclerView view) {
        super.onAttachedToWindow(view);

        mAttachedRecyclerView = view;
        mAttachedRecyclerView.addOnScrollListener(mOnScrollListener);
        mAttachedRecyclerView.setOnFlingListener(mOnFlingListener);
    }

    @Override
    public void onDetachedFromWindow(final RecyclerView view, final RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);

        if (mAttachedRecyclerView != null) {
            mAttachedRecyclerView.removeOnScrollListener(mOnScrollListener);
            mAttachedRecyclerView.setOnFlingListener(null);
            mAttachedRecyclerView = null;
        }
    }

    private View findFirstVisibleItem() {
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child != null && (getDecoratedTop(child) >= 0)) {
                return child;
            }
        }

        return null;
    }

    private class FlingListener extends RecyclerView.OnFlingListener {

        @Override
        public boolean onFling(final int velocityX, final int velocityY) {
            final View firstView = findFirstVisibleItem();

            if (firstView == null || mAttachedRecyclerView == null) {
                return false;
            } else {
                if (velocityY > 0) {
                    mAttachedRecyclerView.smoothScrollBy(0, firstView.getBottom());
                } else {
                    mAttachedRecyclerView.smoothScrollBy(0, firstView.getTop());
                }

                return true;
            }
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {

    }
}
