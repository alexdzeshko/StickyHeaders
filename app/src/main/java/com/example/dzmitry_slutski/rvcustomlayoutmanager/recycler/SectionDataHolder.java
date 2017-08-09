package com.example.dzmitry_slutski.rvcustomlayoutmanager.recycler;

import java.util.ArrayList;
import java.util.List;

public class SectionDataHolder<T> {

    public int code;
    public int sectionPosition;
    boolean isExpanded;
    List<T> data = new ArrayList<>();
    List<Integer> itemsPositions = new ArrayList<>();

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public List<T> getData() {
        return data;
    }

    public boolean isLastItem(T item) {
        return !getData().isEmpty() && getData().get(getData().size() - 1).equals(item);
    }

    @Override
    public String toString() {
        return code + " items [" + data.size() + "] isExp: " + isExpanded;
    }

}
