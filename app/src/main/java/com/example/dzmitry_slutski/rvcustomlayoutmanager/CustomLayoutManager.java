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

    public static final String TAG = "CustomLayoutManager";

    public static final int TYPE_EPISOD = 0;
    public static final int TYPE_SEASON = 1;

    private final RecyclerView.OnFlingListener mOnFlingListener = new FlingListener();
    private final RecyclerView.OnScrollListener mOnScrollListener = new ScrollListener();
    private RecyclerView mAttachedRecyclerView;

    private int mFirstVisiblePosition = RecyclerView.NO_POSITION;
    private int mFirstVisibleItemTop;
    private final SparseArray<View> mViewCache = new SparseArray<>();
    private RecyclerView.Adapter mAdapter;

    private final int mEpisodeHorizontalShift;
    private int mCurrentSeasonHeight;
    private int mCurrentEpisodeListHeight;
    private int mEpisodeBottom;
    private int mSeasonBottom;

    public CustomLayoutManager(final int pEpisodeHorizontalShift) {
        mEpisodeHorizontalShift = pEpisodeHorizontalShift;
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler pRecycler, final RecyclerView.State pState) {
        final int itemCount = getItemCount();
        //We have nothing to show for an empty data set but clear any existing views
        if (itemCount == 0) {
            detachAndScrapAttachedViews(pRecycler);

            return;
        }
        final int initialPos;
        if (getChildCount() == 0) {
            initialPos = 0;
        } else {
            final View firstChild = getChildAt(0);
            initialPos = getPosition(firstChild);
        }

        detachAndScrapAttachedViews(pRecycler);

        int episodeViewTop = 0;
        int seasonViewTop = 0;
        for (int i = initialPos; i < itemCount; i++) {
            final View currentView = getViewForPosition(pRecycler, i);

            addView(currentView);
            if (mAdapter.getItemViewType(i) == TYPE_SEASON) {
                if (seasonViewTop < episodeViewTop) {
                    seasonViewTop = episodeViewTop;
                }
                if (episodeViewTop < seasonViewTop) {
                    episodeViewTop = seasonViewTop;
                }
                measureChild(currentView, 0, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);

                layoutDecorated(currentView, 0, seasonViewTop, measuredWidth, seasonViewTop + usedHeightForView);
                /*Log.d(TAG, "onLayoutChildren() = " +
                        "   L: [" + 0 +
                        "], T: [" + seasonViewTop +
                        "], R: [" + (mEpisodeHorizontalShift + measuredWidth) +
                        "], B: [" + (seasonViewTop + usedHeightForView) + "] childCount: " + getChildCount());*/
                seasonViewTop += usedHeightForView;
            } else {
                measureChild(currentView, mEpisodeHorizontalShift, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);

                layoutDecorated(currentView, mEpisodeHorizontalShift, episodeViewTop, mEpisodeHorizontalShift + measuredWidth, episodeViewTop + usedHeightForView);
                /*Log.d(TAG, "onLayoutChildren() = " +
                        "   L: [" + mEpisodeHorizontalShift +
                        "], T: [" + episodeViewTop +
                        "], R: [" + (mEpisodeHorizontalShift + measuredWidth) +
                        "], B: [" + (episodeViewTop + usedHeightForView) + "] childCount: " + getChildCount());*/
                episodeViewTop += usedHeightForView;
            }
            if (episodeViewTop > getHeight()) {
                return;
            }
        }
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
            fillFromUpToDown(recycler);
        } else {
            fillFromDownToUp(recycler);
        }
        return delta;
    }

    private final List<View> mEpisodeIndexes = new ArrayList<>();
    private boolean isNotFirstSeasonFill;

    private void fillFromDownToUp(final RecyclerView.Recycler pRecycler) {

        Log.d(TAG, "fillFromDownToUp() called with");
        fillViewCacheAndDetachFromRecyclerView(pRecycler);
        mEpisodeIndexes.clear();

        int seasonBottomIndex = RecyclerView.NO_POSITION;
        int episodeBottomIndex = RecyclerView.NO_POSITION;
        mEpisodeBottom = 0;
        mSeasonBottom = 0;

        final int recyclerViewHeight = getHeight();
        for (int i = 0; i < mViewCache.size(); i++) {
            final View child = mViewCache.valueAt(i);
            final int adapterPos = getPosition(child);

            final int decoratedBottom = getDecoratedBottom(child);
            final int decoratedTop = getDecoratedTop(child);

            final boolean isInBoundOfRecyclerView = decoratedTop <= recyclerViewHeight;
            if (mAdapter.getItemViewType(i) == TYPE_SEASON) {
                if (decoratedBottom > mSeasonBottom && isInBoundOfRecyclerView) {
                    seasonBottomIndex = adapterPos;
                    mSeasonBottom = decoratedBottom;
                }
            } else {
                if (decoratedBottom > mEpisodeBottom && isInBoundOfRecyclerView) {
                    episodeBottomIndex = adapterPos;
                    mEpisodeBottom = decoratedBottom;
                }
            }
        }

        Log.d(TAG, "fillFromDownToUp() indexes: sInd: " + seasonBottomIndex +
                " sB: " + mSeasonBottom +
                " eI : " + episodeBottomIndex +
                " eB: " + mEpisodeBottom);
        //TODO probably TopIndexes could be RecyclerView.NO_POSITION should process that issue here

        mCurrentEpisodeListHeight = 0;
        mCurrentSeasonHeight = 0;
        isNotFirstSeasonFill = false;
        int episodeViewBottom = mEpisodeBottom;
        for (int i = episodeBottomIndex; i >= 0; i--) {
            final View currentView = getViewForPosition(pRecycler, i);
            if (mAdapter.getItemViewType(i) == TYPE_SEASON) {
                fillSeasonAndEpisodes(currentView);
                episodeViewBottom = mEpisodeBottom;
            } else {
                /*if (mEpisodeBottom == -1) {
                    mEpisodeBottom = episodeViewBottom;
                }*/
                measureChild(currentView, mEpisodeHorizontalShift, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);
                episodeViewBottom -= usedHeightForView;
                mCurrentEpisodeListHeight += usedHeightForView;
                mEpisodeIndexes.add(currentView);
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
            if (mAdapter.getItemViewType(i) == TYPE_SEASON) {
                return currentView;
            }
        }

        return null;
    }

    private void fillSeasonAndEpisodes(final View seasonView) {
        Log.d(TAG, "fillSeasonAndEpisodes() called with: episode count: " + mEpisodeIndexes.size());
        measureChild(seasonView, 0, 0);
        final int seasonViewHeight = getDecoratedMeasuredHeight(seasonView);
        final int seasonViewWidth = getDecoratedMeasuredWidth(seasonView);

//        mSeasonBottom, mEpisodeBottom
        if (mCurrentEpisodeListHeight < seasonViewHeight && isNotFirstSeasonFill && (mEpisodeBottom - mCurrentEpisodeListHeight > 0)) {
            mEpisodeBottom = mEpisodeBottom - (seasonViewHeight - mCurrentEpisodeListHeight);
        }
        isNotFirstSeasonFill = true;
        int lineEpisodeBottom = mEpisodeBottom;
        for (final View episodeView : mEpisodeIndexes) {
            addView(episodeView);
            int top = mEpisodeBottom - getDecoratedMeasuredHeight(episodeView);
            layoutDecorated(episodeView, mEpisodeHorizontalShift,
                    top,
                    mEpisodeHorizontalShift + getDecoratedMeasuredWidth(episodeView),
                    mEpisodeBottom);
            Log.d(TAG, "fillSeasonAndEpisodes() Ep: [" + top + "]");
            mEpisodeBottom = top;
        }
        addView(seasonView);

        int seasonTop = mEpisodeBottom;
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
        Log.d(TAG, "fillSeasonAndEpisodes() sTop: [" + mEpisodeBottom + "]");

//        mSeasonBottom = -1;
//        mEpisodeBottom = -1;
        mCurrentEpisodeListHeight = 0;
        mCurrentSeasonHeight = 0;
        mEpisodeIndexes.clear();
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
        int seasonTopIndex = RecyclerView.NO_POSITION;
        int episodeTopIndex = RecyclerView.NO_POSITION;
        int episodeTop = 0;
        int seasonTop = 0;

        boolean isTopVisibleEpisodeFound = false;
        boolean isTopVisibleSeasonFound = false;

        for (int i = 0; i < mViewCache.size(); i++) {
            final View child = mViewCache.valueAt(i);
            final int pos = getPosition(child);
            mViewCache.put(pos, child);

            final int decoratedBottom = getDecoratedBottom(child);
            final int decoratedTop = getDecoratedTop(child);

            final int adapterPos = mViewCache.keyAt(i);
            if (decoratedTop < 0 && decoratedBottom >= 0 && !isTopVisibleSeasonFound) {
                seasonTopIndex = adapterPos;
                episodeTop = decoratedTop;
                isTopVisibleSeasonFound = true;
            }

            if (decoratedTop < 0 && decoratedBottom >= 0 && !isTopVisibleEpisodeFound) {
                episodeTopIndex = adapterPos;
                seasonTop = decoratedTop;
                isTopVisibleEpisodeFound = true;
            }
        }

        //TODO probably TopIndexes could be RecyclerView.NO_POSITION should process that issue here
        View previousSeason = null;
        int episodeViewTop = episodeTop;
        int firstEpisodeInSeasonTop = episodeTop;
        int episodeLineHeight = 0;
        for (int i = seasonTopIndex; i < getItemCount(); i++) {
            final View currentView = getViewForPosition(pRecycler, i);

            if (mAdapter.getItemViewType(i) == TYPE_SEASON) {
                if (previousSeason != null) {
                    final int seasonViewBottom = layoutSeason(previousSeason, firstEpisodeInSeasonTop, episodeLineHeight);
                    if (seasonViewBottom > episodeViewTop) {
                        Log.d(TAG, "fillFromUpToDown() season = [" + seasonViewBottom + "]");
                        episodeViewTop = seasonViewBottom;
                    }
                }
                episodeLineHeight = 0;
                previousSeason = currentView;
                firstEpisodeInSeasonTop = episodeViewTop;
            } else {
                addView(currentView);
                measureChild(currentView, mEpisodeHorizontalShift, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);
                layoutDecorated(currentView, mEpisodeHorizontalShift, episodeViewTop,
                        mEpisodeHorizontalShift + measuredWidth, episodeViewTop + usedHeightForView);

                episodeViewTop += usedHeightForView;
                episodeLineHeight += usedHeightForView;
            }
            if (episodeViewTop > getHeight()) {
                break;
            }
        }
        layoutSeason(previousSeason, firstEpisodeInSeasonTop, episodeLineHeight);
        //todo add season if not added yet
        recycleViewsFromCache(pRecycler);
    }

    private int layoutSeason(final View pSeasonView, final int pFirstEpisodeInSeasonTop, final int pEpisodeLineHeight) {
        int seasonViewTop = pFirstEpisodeInSeasonTop;
        addView(pSeasonView);
        measureChild(pSeasonView, 0, 0);
        final int usedHeightForView = getDecoratedMeasuredHeight(pSeasonView);
        final int measuredWidth = getDecoratedMeasuredWidth(pSeasonView);

        if (pFirstEpisodeInSeasonTop < 0 && pEpisodeLineHeight + pFirstEpisodeInSeasonTop >= usedHeightForView) {
//            seasonViewTop = 0;
            Log.d(TAG, "layoutSeason() seasonViewTop = 0 should be!");
        }

        layoutDecorated(pSeasonView, 0, seasonViewTop, measuredWidth,
                seasonViewTop + usedHeightForView);

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
        final int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }

        final View topEpisodeView = findFirstEpisodeView();
        final View lastView = findLastViewOfAnyType();

        final int lastViewBottom = getDecoratedBottom(lastView);
        final int firstEpisodeViewTop = getDecoratedTop(topEpisodeView);
        final int viewSpan = lastViewBottom - firstEpisodeViewTop;
        final int recyclerViewHeight = getHeight();
        if (viewSpan < recyclerViewHeight) {
            return 0;
        }

        int delta = 0;
        if (dy < 0) {
            //scroll up
            delta = Math.max(firstEpisodeViewTop, dy);
        } else if (dy > 0) {
            //scroll down
            delta = Math.min(lastViewBottom - recyclerViewHeight, dy);
        }
        return delta;
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

    private View findFirstEpisodeView() {
        View topView = null;
        int minTop = getHeight();
        final int viewChildCount = getChildCount();
        for (int i = 0; i < viewChildCount; i++) {
            final View childAt = getChildAt(i);
            if (mAdapter.getItemViewType(getPosition(childAt)) == TYPE_EPISOD) {
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
