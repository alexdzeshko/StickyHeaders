package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomLayoutManager
 * Version info
 * 30-06-17
 * Created by Dzmitry_Slutski.
 */

public class CustomLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "CustomLayoutManager";

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_HEADER = 1;

    private final RecyclerView.OnFlingListener mOnFlingListener = new FlingListener();
    private final RecyclerView.OnScrollListener mOnScrollListener = new ScrollListener();
    private RecyclerView mAttachedRecyclerView;

    private int mFirstVisiblePosition = RecyclerView.NO_POSITION;
    private int mFirstVisibleItemTop;
    private final SparseArray<View> mViewCache = new SparseArray<>();
    private RecyclerView.Adapter mAdapter;

    private final int mItemHorizontalShift;
    private int mCurrentHeaderHeight;
    private int mCurrentItemListHeight;
    private int mItemBottom;
    private int mHeaderBottom;
    private final List<View> mItemsIndexes = new ArrayList<>();
    private boolean isNotFirstSeasonFill;


    public CustomLayoutManager(final int pEpisodeHorizontalShift) {
        mItemHorizontalShift = pEpisodeHorizontalShift;
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler pRecycler, final RecyclerView.State pState) {
        final int itemCount = getItemCount();
        //We have nothing to show for an empty data set but clear any existing views
        if (itemCount == 0) {
            removeAndRecycleAllViews(pRecycler);

            return;
        }

        if (getChildCount() == 0 && pState.isPreLayout()) {
            //Nothing to do during prelayout when empty
            return;
        }

        int initialPosition = 0;
        if (getChildCount() > 0) {
            final View firstChild = getChildAt(0);
            initialPosition = getPosition(firstChild);
        }

        detachAndScrapAttachedViews(pRecycler);

        int itemViewTop = 0;

        int headerViewTop = 0;

        int headerWidth = mItemHorizontalShift;// TODO: 8/9/17 find and measure header first

        for (int i = initialPosition; i < itemCount; i++) {
            final View currentView = getViewForPosition(pRecycler, i);

            addView(currentView);

            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {

                //measure and layout header

                if (headerViewTop < itemViewTop) {
                    headerViewTop = itemViewTop;
                } else if (itemViewTop < headerViewTop) {
                    itemViewTop = headerViewTop;
                }

                measureChild(currentView, 0, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);

                headerWidth = measuredWidth;

                layoutDecorated(currentView, 0, headerViewTop, measuredWidth, headerViewTop + usedHeightForView);
                /*Log.d(TAG, "onLayoutChildren() = " +
                        "   L: [" + 0 +
                        "], T: [" + headerViewTop +
                        "], R: [" + (mItemHorizontalShift + measuredWidth) +
                        "], B: [" + (headerViewTop + usedHeightForView) + "] childCount: " + getChildCount());*/
                headerViewTop += usedHeightForView;

            } else {

                //measure and layout item view

                measureChild(currentView, headerWidth, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);

                layoutDecorated(currentView, headerWidth, itemViewTop, headerWidth + measuredWidth, itemViewTop + usedHeightForView);
                /*Log.d(TAG, "onLayoutChildren() = " +
                        "   L: [" + mItemHorizontalShift +
                        "], T: [" + itemViewTop +
                        "], R: [" + (mItemHorizontalShift + measuredWidth) +
                        "], B: [" + (itemViewTop + usedHeightForView) + "] childCount: " + getChildCount());*/
                itemViewTop += usedHeightForView;
            }
            if (itemViewTop > getHeight()) {
                break;
            }
        }

        // TODO: 8/9/17 relayout header if necessary
    }

    @Override
    public void onAdapterChanged(final RecyclerView.Adapter oldAdapter, final RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);

        mAdapter = newAdapter;
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
        final int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }

        final int delta = calculatePossibleScrollVertically(dy);

        Log.d(TAG, "scrollVerticallyBy() called with: dy = [" + dy + "], childCount = [" + getChildCount() + "], delta = [" + delta + "]");
        offsetChildrenVertical(-delta);
        if (dy >= 0) {
            // Contents are scrolling up
            fillFromUpToDown(recycler);
        } else {
            fillFromDownToUp(recycler);
        }
        return delta;
    }

    private void fillFromDownToUp(final RecyclerView.Recycler pRecycler) {

        Log.d(TAG, "fillFromDownToUp() called with");
        fillViewCacheAndDetachFromRecyclerView(pRecycler);
        mItemsIndexes.clear();

        int seasonBottomIndex = RecyclerView.NO_POSITION;
        int episodeBottomIndex = RecyclerView.NO_POSITION;
        mItemBottom = 0;
        mHeaderBottom = 0;

        final int recyclerViewHeight = getHeight();
        for (int i = 0; i < mViewCache.size(); i++) {
            final View child = mViewCache.valueAt(i);
            final int adapterPos = getPosition(child);

            final int decoratedBottom = getDecoratedBottom(child);
            final int decoratedTop = getDecoratedTop(child);

            final boolean isInBoundOfRecyclerView = decoratedTop <= recyclerViewHeight;
            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {
                if (decoratedBottom > mHeaderBottom && isInBoundOfRecyclerView) {
                    seasonBottomIndex = adapterPos;
                    mHeaderBottom = decoratedBottom;
                }
            } else {
                if (decoratedBottom > mItemBottom && isInBoundOfRecyclerView) {
                    episodeBottomIndex = adapterPos;
                    mItemBottom = decoratedBottom;
                }
            }
        }

        Log.d(TAG, "fillFromDownToUp() indexes: sInd: " + seasonBottomIndex +
                " sB: " + mHeaderBottom +
                " eI : " + episodeBottomIndex +
                " eB: " + mItemBottom);
        //TODO probably TopIndexes could be RecyclerView.NO_POSITION should process that issue here

        mCurrentItemListHeight = 0;
        mCurrentHeaderHeight = 0;
        isNotFirstSeasonFill = false;
        int episodeViewBottom = mItemBottom;
        for (int i = episodeBottomIndex; i >= 0; i--) {
            final View currentView = getViewForPosition(pRecycler, i);
            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {
                fillSeasonAndEpisodes(currentView);
                episodeViewBottom = mItemBottom;
            } else {
                /*if (mItemBottom == -1) {
                    mItemBottom = episodeViewBottom;
                }*/
                measureChild(currentView, mItemHorizontalShift, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);
                episodeViewBottom -= usedHeightForView;
                mCurrentItemListHeight += usedHeightForView;
                mItemsIndexes.add(currentView);
            }
            if (episodeViewBottom < 0) {
                Log.d(TAG, "fillFromDownToUp() called with: episodeViewBottom < 0!");
                fillSeasonAndEpisodes(findSeasonView(i, pRecycler));
                break;
            }
        }
        Log.d(TAG, "fillFromDownToUp() called with: FINISHED");
        recycleViewsFromCache(pRecycler);
    }

    @Nullable
    private View findSeasonView(final int findFromIndex, final RecyclerView.Recycler pRecycler) {
        for (int i = findFromIndex; i >= 0; i--) {
            final View currentView = getViewForPosition(pRecycler, i);
            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {
                return currentView;
            }
        }

        return null;
    }

    private void fillSeasonAndEpisodes(final View seasonView) {
        Log.d(TAG, "fillSeasonAndEpisodes() called with: episode count: " + mItemsIndexes.size());
        measureChild(seasonView, 0, 0);
        final int seasonViewHeight = getDecoratedMeasuredHeight(seasonView);
        final int seasonViewWidth = getDecoratedMeasuredWidth(seasonView);

//        mHeaderBottom, mItemBottom
        if (mCurrentItemListHeight < seasonViewHeight && isNotFirstSeasonFill && (mItemBottom - mCurrentItemListHeight > 0)) {
            mItemBottom = mItemBottom - (seasonViewHeight - mCurrentItemListHeight);
        }
        isNotFirstSeasonFill = true;
        int lineEpisodeBottom = mItemBottom;
        for (final View episodeView : mItemsIndexes) {
            addView(episodeView);
            int top = mItemBottom - getDecoratedMeasuredHeight(episodeView);
            layoutDecorated(episodeView, mItemHorizontalShift,
                    top,
                    mItemHorizontalShift + getDecoratedMeasuredWidth(episodeView),
                    mItemBottom);
            Log.d(TAG, "fillSeasonAndEpisodes() Ep: [" + top + "]");
            mItemBottom = top;
        }
        addView(seasonView);

        int seasonTop = mItemBottom;
        if (seasonTop < 0) {
            seasonTop = 0;
        }
        if (lineEpisodeBottom - seasonViewHeight < 0) {
            seasonTop = lineEpisodeBottom - seasonViewHeight;
        }

        layoutDecorated(seasonView, 0,
                seasonTop,
                seasonViewWidth,
                seasonTop + seasonViewHeight);
        Log.d(TAG, "fillSeasonAndEpisodes() sTop: [" + mItemBottom + "]");

//        mHeaderBottom = -1;
//        mItemBottom = -1;
        mCurrentItemListHeight = 0;
        mCurrentHeaderHeight = 0;
        mItemsIndexes.clear();
    }

    @NonNull
    private View getViewForPosition(final RecyclerView.Recycler pRecycler, final int pPosition) {
        final View view = getViewAndRemoveFromCache(pPosition);
        if (view == null) {
            return pRecycler.getViewForPosition(pPosition);
        }
        return view;
    }

    private View getViewAndRemoveFromCache(final int pPosition) {
        final View view = mViewCache.get(pPosition, null);
        if (view != null) {
            mViewCache.delete(pPosition);
        }

        return view;
    }

    private void fillFromUpToDown(final RecyclerView.Recycler pRecycler) {
        fillViewCacheAndDetachFromRecyclerView(pRecycler);

        int headerTopIndex = 0;//RecyclerView.NO_POSITION;
        int itemTopIndex = 0;//RecyclerView.NO_POSITION;
        int itemTop = 0;
        int headerTop = 0;

        boolean isTopVisibleItemFound = false;
        boolean isTopVisibleHeaderFound = false;

        for (int i = 0; i < mViewCache.size(); i++) {
            final View child = mViewCache.valueAt(i);
//            final int pos = getPosition(child);
//            mViewCache.put(pos, child);

            final int decoratedTop = getDecoratedTop(child);
            final int decoratedBottom = getDecoratedBottom(child);

            final int adapterPos = mViewCache.keyAt(i);
            if (decoratedTop < 0 && decoratedBottom >= 0) {

                if (!isTopVisibleHeaderFound) {
                    headerTopIndex = adapterPos;
                    itemTop = decoratedTop;
                    isTopVisibleHeaderFound = true;

                } else if (!isTopVisibleItemFound) {
                    itemTopIndex = adapterPos;
                    headerTop = decoratedTop;
                    isTopVisibleItemFound = true;
                }
            }
        }

        //TODO probably TopIndexes could be RecyclerView.NO_POSITION should process that issue here
        View previousSeason = null;
        int episodeViewTop = itemTop;
        int firstEpisodeInSeasonTop = itemTop;
        int episodeLineHeight = 0;

        for (int i = headerTopIndex; i < getItemCount(); i++) {
            final View currentView = getViewForPosition(pRecycler, i);

            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {

                //

                if (previousSeason != null) {
                    final int seasonViewBottom = layoutHeader(previousSeason, firstEpisodeInSeasonTop, episodeLineHeight);
                    if (seasonViewBottom > episodeViewTop) {
                        Log.d(TAG, "fillFromUpToDown() season = [" + seasonViewBottom + "]");
                        episodeViewTop = seasonViewBottom;
                    }
                }
                episodeLineHeight = 0;
                previousSeason = currentView;
                firstEpisodeInSeasonTop = episodeViewTop;

            } else {

                //measure and layout item

                addView(currentView);
                measureChild(currentView, mItemHorizontalShift, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);
                layoutDecorated(currentView, mItemHorizontalShift, episodeViewTop,
                        mItemHorizontalShift + measuredWidth, episodeViewTop + usedHeightForView);

                episodeViewTop += usedHeightForView;
                episodeLineHeight += usedHeightForView;

            }
            if (episodeViewTop > getHeight()) {
                break;
            }
        }
        layoutHeader(previousSeason, firstEpisodeInSeasonTop, episodeLineHeight);
        //todo add season if not added yet
        recycleViewsFromCache(pRecycler);
    }

    private int layoutHeader(final View pSeasonView, final int pFirstEpisodeInSeasonTop, final int pEpisodeLineHeight) {
        int seasonViewTop = pFirstEpisodeInSeasonTop;
        addView(pSeasonView);
        measureChild(pSeasonView, 0, 0);
        final int usedHeightForView = getDecoratedMeasuredHeight(pSeasonView);
        final int measuredWidth = getDecoratedMeasuredWidth(pSeasonView);

        if (pFirstEpisodeInSeasonTop < 0 && pEpisodeLineHeight + pFirstEpisodeInSeasonTop >= usedHeightForView) {
//            seasonViewTop = 0;
            Log.d(TAG, "layoutHeader() seasonViewTop = 0 should be!");
        }

        layoutDecorated(pSeasonView, 0, seasonViewTop, measuredWidth, seasonViewTop + usedHeightForView);

        return seasonViewTop + usedHeightForView;
    }

    private void fillViewCacheAndDetachFromRecyclerView(final RecyclerView.Recycler pRecycler) {
        mViewCache.clear();
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            final int pos = getPosition(child);
            mViewCache.put(pos, child);
        }
        Log.d(TAG, "fillViewCacheAndDetachFromRecyclerView() called with: mViewCache size = [" + mViewCache.size() + "]");
        detachAndScrapAttachedViews(pRecycler);
    }

    private void recycleViewsFromCache(final RecyclerView.Recycler recycler) {
        for (int i = 0; i < mViewCache.size(); i++) {
            recycler.recycleView(mViewCache.valueAt(i));
        }
        mViewCache.clear();
    }

    private int calculatePossibleScrollVertically(final int dy) {
        /*final int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }

        final View topItemView = findFirstItemView();
        final View lastView = findLastViewOfAnyType();

        final int lastViewBottom = getDecoratedBottom(lastView);
        final int firstItemViewTop = getDecoratedTop(topItemView);

        final int viewSpan = lastViewBottom - firstItemViewTop;
        final int recyclerViewHeight = getHeight();

        if (viewSpan < recyclerViewHeight) {
            return 0;
        }

        int delta = 0;
        if (dy < 0) {
            //scroll up
            delta = Math.max(firstItemViewTop, dy);
        } else if (dy > 0) {
            //scroll down
            delta = Math.min(lastViewBottom - recyclerViewHeight, dy);
        }
        Log.d(TAG, "calculatePossibleScrollVertically() returned: " + delta);
        return delta;*/
        return dy;
    }

    @Override
    public void onAttachedToWindow(final RecyclerView view) {
        super.onAttachedToWindow(view);

        mAttachedRecyclerView = view;
        mAttachedRecyclerView.addOnScrollListener(mOnScrollListener);
//        mAttachedRecyclerView.setOnFlingListener(mOnFlingListener);
    }

    private View findLastViewOfAnyType() {
        View lastView = null;
        int maxBottomPos = -1;
        final int viewChildCount = getChildCount();
        for (int i = 0; i < viewChildCount; i++) {
            final View childAt = getChildAt(i);
            final int decoratedBottom = getDecoratedBottom(childAt);

            if (decoratedBottom > maxBottomPos) {
                maxBottomPos = decoratedBottom;
                lastView = childAt;
            }
        }

        return lastView;
    }

    private View findFirstItemView() {
        View topView = null;
        int minTop = getHeight();
        final int viewChildCount = getChildCount();
        for (int i = 0; i < viewChildCount; i++) {
            final View childAt = getChildAt(i);

            if (mAdapter.getItemViewType(getPosition(childAt)) == TYPE_ITEM) {
                final int decoratedTop = getDecoratedTop(childAt);

                if (decoratedTop < minTop) {
                    minTop = decoratedTop;
                    topView = childAt;
                }
            }
        }

        return topView;
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
