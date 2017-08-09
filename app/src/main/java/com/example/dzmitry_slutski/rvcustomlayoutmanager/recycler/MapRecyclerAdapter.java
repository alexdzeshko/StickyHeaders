package com.example.dzmitry_slutski.rvcustomlayoutmanager.recycler;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MapRecyclerAdapter<T, IVH extends RecyclerView.ViewHolder, SVH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Map<Integer, SectionDataHolder<T>> dataSet = new LinkedHashMap<>();
    private final List<Integer> viewTypes = new ArrayList<>();
    private final SparseIntArray positionToSectionCodeMap = new SparseIntArray();
    private final SparseArray<T> positionToItemMap = new SparseArray<>();
    private final SparseArray<RecyclerView.ViewHolder> headers = new SparseArray<>();
    private final SparseArray<RecyclerView.ViewHolder> footers = new SparseArray<>();
    private boolean isSectionsCollapsible = true;
    private Procedure<SectionDataHolder<T>> onSectionExpandListener;

    private final List<T> items = new ArrayList<>();

    public SparseArray<RecyclerView.ViewHolder> getHeaders() {
        return headers;
    }

    public boolean isSectionsCollapsible() {
        return isSectionsCollapsible;
    }

    public void setSectionsCollapsible(boolean sectionsCollapsible) {
        if (isSectionsCollapsible != sectionsCollapsible) {
            isSectionsCollapsible = sectionsCollapsible;
            updateInternalStructures();
            notifyDataSetChanged();
        }
    }

    public void addHeaderView(@NonNull RecyclerView.ViewHolder viewHolder) {
        headers.put(viewHolder.hashCode(), viewHolder);
        updateInternalStructures();
        notifyDataSetChanged();
    }

    public void addFooterView(@NonNull RecyclerView.ViewHolder viewHolder) {
        footers.put(viewHolder.hashCode(), viewHolder);
        updateInternalStructures();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (headers.indexOfKey(viewType) >= 0) {
            return headers.get(viewType);
        } else if (footers.indexOfKey(viewType) >= 0) {
            return footers.get(viewType);
        } else if (viewType == sectionViewType()) {
            final SVH vh = sectionViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
            if (isSectionsCollapsible) {
//                vh.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        onSectionClick(vh);
//                    }
//                });
            }
            return vh;
        } else {
            return itemViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < headers.size()) {
            onBindHeaderViewHolder(holder);
        } else if (positionToSectionCodeMap.indexOfKey(position) >= 0) {
            onBindSectionViewHolder((SVH) holder, getSectionDataHolder(position));
        } else if (positionToItemMap.indexOfKey(position) >= 0) {
            onBindItemViewHolder((IVH) holder, positionToItemMap.get(position));
        } else {
            onBindFooterViewHolder(holder);
        }
    }

    private void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
        //no-op
    }

    private void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {
        //no-op
    }

    private SectionDataHolder<T> getSectionDataHolder(int position) {
        return dataSet.get(positionToSectionCodeMap.get(position));
    }

    protected SectionDataHolder<T> getSectionDataHolderByCode(int sectionCode) {
        return dataSet.get(sectionCode);
    }

    private void onSectionClick(SVH viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        Integer code = positionToSectionCodeMap.get(adapterPosition);
        SectionDataHolder<T> sectionData = dataSet.get(code);
        if (sectionData.isExpanded) {
            //collapse
            sectionData.isExpanded = false;
            updateInternalStructures();
            notifyItemRangeRemoved(adapterPosition + 1, sectionData.data.size());

        } else {
            //expand
            sectionData.isExpanded = true;
            updateInternalStructures();
            notifyItemRangeInserted(adapterPosition + 1, sectionData.data.size());
        }
        onSectionClicked(viewHolder, sectionData);
        if (onSectionExpandListener != null) {
            onSectionExpandListener.apply(sectionData);
        }
    }

    public List<T> getItems() {
        return items;
    }

    protected void onSectionClicked(SVH viewHolder, SectionDataHolder<T> sectionData) {
        //no-op
    }

    @Override
    public int getItemCount() {
        return headers.size() + dataSet.size() + countItems() + footers.size();
    }

    private int countItems() {
        int count = 0;
        for (Map.Entry<Integer, SectionDataHolder<T>> entry : dataSet.entrySet()) {
            SectionDataHolder<T> sectionData = entry.getValue();
            if (!isSectionsCollapsible || sectionData.isExpanded) {
                count += sectionData.data.size();
            }
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return viewTypes.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (positionToSectionCodeMap.indexOfKey(position) >= 0) {
            return Integer.valueOf(positionToSectionCodeMap.get(position)).hashCode();
        } else {
            return super.getItemId(position);
        }
    }

    public void setItems(List<? extends T> items) {
        this.items.clear();
        this.items.addAll(items);
        calculateInternalStructures();
    }

    private void calculateInternalStructures() {
        dataSet.clear();
        viewTypes.clear();
        positionToSectionCodeMap.clear();
        positionToItemMap.clear();
        for (T item : items) {
            int sectionCode = sectionCode(item);
            SectionDataHolder<T> sectionData = dataSet.get(sectionCode);
            if (sectionData == null) {
                sectionData = new SectionDataHolder<>();
                sectionData.code = sectionCode;
                dataSet.put(sectionCode, sectionData);
            }
            sectionData.data.add(item);
        }
        updateInternalStructures();
        notifyDataSetChanged();
    }

    private void updateInternalStructures() {
        viewTypes.clear();
        positionToSectionCodeMap.clear();
        positionToItemMap.clear();
        int position = 0;
        for (int i = 0; i < headers.size(); i++) {
            Integer headerViewType = headers.keyAt(i);
            viewTypes.add(position, headerViewType);
            position++;
        }
        for (Map.Entry<Integer, SectionDataHolder<T>> entry : dataSet.entrySet()) {
            Integer code = entry.getKey();
            viewTypes.add(position, sectionViewType());
            positionToSectionCodeMap.put(position, code);
            SectionDataHolder<T> sectionData = dataSet.get(code);
            sectionData.sectionPosition = position;
            position++;
            if (!isSectionsCollapsible || sectionData.isExpanded) {
                for (T t : sectionData.data) {
                    viewTypes.add(position, itemViewType(t));
                    positionToItemMap.put(position, t);
                    sectionData.itemsPositions.add(position);
                    position++;
                }
            }
        }
        for (int i = 0; i < footers.size(); i++) {
            Integer headerViewType = footers.keyAt(i);
            viewTypes.add(position, headerViewType);
            position++;
        }
    }

    public void notifyItemChanged(T item) {
        int valueIndex = positionToItemMap.indexOfValue(item);
        if (valueIndex != -1) {
            notifyItemChanged(positionToItemMap.keyAt(valueIndex));
        }
    }

    public void notifySectionChanged(T item) {
        int valueIndex = positionToSectionCodeMap.indexOfValue(sectionCode(item));
        if (valueIndex != -1) {
            notifyItemChanged(positionToSectionCodeMap.keyAt(valueIndex));
        }
    }

    public void setOnExpandListener(Procedure<SectionDataHolder<T>> onSectionExpandListener) {
        this.onSectionExpandListener = onSectionExpandListener;
    }

    public List<SectionDataHolder<T>> getExpandedSections() {
        ArrayList<SectionDataHolder<T>> list = new ArrayList<>(dataSet.size());
        for (Map.Entry<Integer, SectionDataHolder<T>> entry : dataSet.entrySet()) {
            SectionDataHolder<T> section = entry.getValue();
            if (section.isExpanded()) {
                list.add(section);
            }
        }
        return list;
    }

    protected void sortSection(int sectionCode, Comparator<T> comparator, Procedure<List<T>> afterSortProcedure) {
        SectionDataHolder<T> sectionData = dataSet.get(sectionCode);
        List<T> data = sectionData.getData();
        Collections.sort(data, comparator);
        if (afterSortProcedure != null) {
            afterSortProcedure.apply(data);
        }
        updateInternalStructures();
        notifyDataSetChanged();
    }

    public void clear() {
        viewTypes.clear();
        positionToSectionCodeMap.clear();
        positionToItemMap.clear();
        headers.clear();
        footers.clear();
        items.clear();
        notifyDataSetChanged();
    }

    public void sortItems(Comparator<T> comparator) {
        Collections.sort(items, comparator);
        calculateInternalStructures();
    }

    public void firstSectionExpanded(boolean isExpand) {
        if (!dataSet.isEmpty()) {
            dataSet.get(0).setExpanded(isExpand);
            updateInternalStructures();
        }
    }

    public void allSectionsExpanded(boolean isExpand) {
        for (int i = 0; i < dataSet.size(); i++) {
            dataSet.get(i).setExpanded(isExpand);
        }
        updateInternalStructures();
    }

    public abstract int sectionCode(T item);

    protected abstract int itemViewType(T item);

    protected abstract int sectionViewType();

    protected abstract IVH itemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    protected abstract SVH sectionViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    protected abstract void onBindItemViewHolder(IVH holder, T item);

    protected abstract void onBindSectionViewHolder(SVH holder, SectionDataHolder<T> sectionData);

}
