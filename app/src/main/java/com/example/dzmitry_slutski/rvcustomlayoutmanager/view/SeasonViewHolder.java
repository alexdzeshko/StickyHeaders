package com.example.dzmitry_slutski.rvcustomlayoutmanager.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.IRoot;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.R;

public class SeasonViewHolder extends BaseViewHolder<IRoot> {

    ImageView mImage;
    TextView mTitle;
    TextView mSubTitle;

    public SeasonViewHolder(final View itemView) {
        super(itemView);

        mImage = (ImageView) itemView.findViewById(R.id.season_image);
        mTitle = (TextView) itemView.findViewById(R.id.season_title);
        mSubTitle = (TextView) itemView.findViewById(R.id.season_sub_title);
    }

    @Override
    public void draw(final IRoot pIRoot) {
        mImage.setImageResource(pIRoot.imageId());
        mTitle.setText(pIRoot.title());
        mSubTitle.setText(pIRoot.subTitle());

        itemView.setBackgroundColor(pIRoot.getColor());
    }
}
