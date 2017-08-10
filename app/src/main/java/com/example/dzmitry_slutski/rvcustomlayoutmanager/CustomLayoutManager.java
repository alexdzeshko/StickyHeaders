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
    private final List<View> mItemsIndexes = new ArrayList<>();
    private boolean isNotFirstSectionFill;


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
            fillFromTopToBottom(recycler);
        } else {
            fillFromBottomToTop(recycler);
        }
        return delta;
    }

    private void fillFromBottomToTop(final RecyclerView.Recycler pRecycler) {

        Log.d(TAG, "fillFromBottomToTop() called with");

        fillViewCacheAndDetachFromRecyclerView(pRecycler);
        mItemsIndexes.clear();

        int headerBottomIndex = RecyclerView.NO_POSITION;
        int itemBottomIndex = RecyclerView.NO_POSITION;
        mItemBottom = 0;
        int headerBottom = 0;

        final int recyclerViewHeight = getHeight();

        for (int i = 0; i < mViewCache.size(); i++) {

            final View child = mViewCache.valueAt(i);
            final int adapterPosition = getPosition(child);

            final int decoratedBottom = getDecoratedBottom(child);
            final int decoratedTop = getDecoratedTop(child);

            final boolean isInBoundOfRecyclerView = decoratedTop <= recyclerViewHeight;

            if (isInBoundOfRecyclerView) {
                if (mAdapter.getItemViewType(adapterPosition) == TYPE_HEADER) {
                    if (decoratedBottom > headerBottom) {
                        headerBottomIndex = adapterPosition;
                        headerBottom = decoratedBottom;
                    }
                } else {
                    if (decoratedBottom > mItemBottom) {
                        itemBottomIndex = adapterPosition;
                        mItemBottom = decoratedBottom;
                    }
                }
            } else {
                break;
            }
        }

        Log.d(TAG, "fillFromBottomToTop() indexes: sInd: " + headerBottomIndex +
                " sB: " + headerBottom +
                " eI : " + itemBottomIndex +
                " eB: " + mItemBottom);
        //TODO probably TopIndexes could be RecyclerView.NO_POSITION should process that issue here

        mCurrentItemListHeight = 0;
        mCurrentHeaderHeight = 0;
        isNotFirstSectionFill = false;
        int itemViewBottom = mItemBottom;

        for (int i = itemBottomIndex; i >= 0; i--) {

            final View currentView = getViewForPosition(pRecycler, i);
            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {

                fillHeaderAndItems(currentView);
//                itemViewBottom = mItemBottom;
            } else {

                measureChild(currentView, mItemHorizontalShift, 0);

                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
//                final int measuredWidth = getDecoratedMeasuredWidth(currentView);

                itemViewBottom -= usedHeightForView;
                mCurrentItemListHeight += usedHeightForView;
                mItemsIndexes.add(currentView);
            }
            if (itemViewBottom < 0) {
                Log.d(TAG, "fillFromBottomToTop() called with: itemViewBottom < 0!");
                fillHeaderAndItems(findHeaderView(i, pRecycler));
                break;
            }
        }
        Log.d(TAG, "fillFromBottomToTop() called with: FINISHED");
        recycleViewsFromCache(pRecycler);
    }

    private void fillFromTopToBottom(final RecyclerView.Recycler pRecycler) {

        fillViewCacheAndDetachFromRecyclerView(pRecycler);

        int topHeaderAdapterPosition = RecyclerView.NO_POSITION;
        int itemTopIndex = RecyclerView.NO_POSITION;
        int itemTop = 0;
        int headerTop = 0;

        for (int i = 0; i < mViewCache.size(); i++) {

            final View child = mViewCache.valueAt(i);
            final int adapterPosition = mViewCache.keyAt(i);

            final int decoratedTop = getDecoratedTop(child);
            final int decoratedBottom = getDecoratedBottom(child);

            final boolean isInBoundOfRecyclerView = decoratedBottom >= 0;

            if (isInBoundOfRecyclerView) {

                if (mAdapter.getItemViewType(adapterPosition) == TYPE_HEADER) {
                    if (topHeaderAdapterPosition == RecyclerView.NO_POSITION) {
                        topHeaderAdapterPosition = adapterPosition;
                        itemTop = decoratedTop;
                        // TODO: 8/10/17 header top coordinate should be alligned with first item in section
                        // or pushed up by header below
                        // belowHeaderTop
                    }
                } else {
                    if (itemTopIndex == RecyclerView.NO_POSITION) {
                        itemTopIndex = adapterPosition;
                        headerTop = decoratedTop;
                    }
                }
            }
        }

        View previousHeader = null; // TODO: 8/10/17 find previous header

        int firstItemInSectionTop = itemTop;
        int itemsSectionHeight = 0;

        for (int i = topHeaderAdapterPosition; i < getItemCount(); i++) {
            final View currentView = getViewForPosition(pRecycler, i);

            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {

                //layout header

                if (previousHeader != null) {
                    final int headerViewBottom = layoutHeaderReturnHeaderBottom(previousHeader, firstItemInSectionTop, itemsSectionHeight);
                    if (headerViewBottom > itemTop) {
                        itemTop = headerViewBottom;
                    }
                }
                itemsSectionHeight = 0;
                previousHeader = currentView;
                firstItemInSectionTop = itemTop;

            } else {

                //measure and layout item

                addView(currentView);
                measureChild(currentView, mItemHorizontalShift, 0);
                final int usedHeightForView = getDecoratedMeasuredHeight(currentView);
                final int measuredWidth = getDecoratedMeasuredWidth(currentView);
                layoutDecorated(currentView, mItemHorizontalShift, itemTop,
                        mItemHorizontalShift + measuredWidth, itemTop + usedHeightForView);

                itemTop += usedHeightForView;
                itemsSectionHeight += usedHeightForView;

            }
            if (itemTop > getHeight()) {
                break;
            }
        }
        layoutHeaderReturnHeaderBottom(previousHeader, firstItemInSectionTop, itemsSectionHeight);
        //todo add season if not added yet
        recycleViewsFromCache(pRecycler);
    }

    private void fillHeaderAndItems(final View headerView) {
        Log.d(TAG, "fillHeaderAndItems() called with: episode count: " + mItemsIndexes.size());
        measureChild(headerView, 0, 0);
        final int headerViewHeight = getDecoratedMeasuredHeight(headerView);
        final int headerViewWidth = getDecoratedMeasuredWidth(headerView);

//        mHeaderBottom, mItemBottom
        if (mCurrentItemListHeight < headerViewHeight && isNotFirstSectionFill && (mItemBottom - mCurrentItemListHeight > 0)) {
            mItemBottom = mItemBottom - (headerViewHeight - mCurrentItemListHeight);
        }
        isNotFirstSectionFill = true;
        int lineItemBottom = mItemBottom;
        for (final View itemView : mItemsIndexes) {
            addView(itemView);
            int top = mItemBottom - getDecoratedMeasuredHeight(itemView);
            layoutDecorated(itemView, mItemHorizontalShift,
                    top,
                    mItemHorizontalShift + getDecoratedMeasuredWidth(itemView),
                    mItemBottom);
            Log.d(TAG, "fillHeaderAndItems() Ep: [" + top + "]");
            mItemBottom = top;
        }
        addView(headerView);

        int headerTop = mItemBottom;
        if (headerTop < 0) {
            headerTop = 0;
        }
        if (lineItemBottom - headerViewHeight < 0) {
            headerTop = lineItemBottom - headerViewHeight;
        }

        layoutDecorated(headerView, 0,
                headerTop,
                headerViewWidth,
                headerTop + headerViewHeight);
        Log.d(TAG, "fillHeaderAndItems() sTop: [" + mItemBottom + "]");

//        mHeaderBottom = -1;
//        mItemBottom = -1;
        mCurrentItemListHeight = 0;
        mCurrentHeaderHeight = 0;
        mItemsIndexes.clear();
    }

    @Nullable
    private View findHeaderView(final int findFromIndex, final RecyclerView.Recycler pRecycler) {
        for (int i = findFromIndex; i >= 0; i--) {
            final View currentView = getViewForPosition(pRecycler, i);
            if (mAdapter.getItemViewType(i) == TYPE_HEADER) {
                return currentView;
            }
        }

        return null;
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

    private int layoutHeaderReturnHeaderBottom(final View pHeaderView, final int pFirstItemInSectionTop, final int pItemLineHeight) {

        int headerViewTop = pFirstItemInSectionTop;

        addView(pHeaderView);

        measureChild(pHeaderView, 0, 0);

        final int usedHeightForView = getDecoratedMeasuredHeight(pHeaderView);
        final int measuredWidth = getDecoratedMeasuredWidth(pHeaderView);

        if (pFirstItemInSectionTop < 0 && pItemLineHeight + pFirstItemInSectionTop >= usedHeightForView) {
//            headerViewTop = 0;
            Log.d(TAG, "layoutHeaderReturnHeaderBottom() headerViewTop = 0 should be!");
        }

        layoutDecorated(pHeaderView, 0, headerViewTop, measuredWidth, headerViewTop + usedHeightForView);

        return headerViewTop + usedHeightForView;
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
        // TODO: 8/10/17 return correct value to stop overscrolling
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

    private View findFirstVisibleItem() {
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child != null && (getDecoratedTop(child) >= 0)) {
                return child;
            }
        }

        return null;
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
    public void onDetachedFromWindow(final RecyclerView view, final RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);

        if (mAttachedRecyclerView != null) {
            mAttachedRecyclerView.removeOnScrollListener(mOnScrollListener);
            mAttachedRecyclerView.setOnFlingListener(null);
            mAttachedRecyclerView = null;
        }
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
