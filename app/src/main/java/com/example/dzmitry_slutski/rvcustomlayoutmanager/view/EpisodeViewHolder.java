package com.example.dzmitry_slutski.rvcustomlayoutmanager.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dzmitry_slutski.rvcustomlayoutmanager.IModel;
import com.example.dzmitry_slutski.rvcustomlayoutmanager.R;

public class EpisodeViewHolder extends BaseViewHolder<IModel> {

    ImageView mImage;
    TextView mTitle;
    TextView mSubTitle;

    public EpisodeViewHolder(final View itemView) {
        super(itemView);

        mImage = (ImageView) itemView.findViewById(R.id.episode_image);
        mTitle = (TextView) itemView.findViewById(R.id.episode_title);
        mSubTitle = (TextView) itemView.findViewById(R.id.episode_sub_title);
    }

    @Override
    public void draw(final IModel pIModel) {
        mImage.setImageResource(pIModel.imageId());
        mTitle.setText(pIModel.title());
        mSubTitle.setText(pIModel.subTitle());

        itemView.setBackgroundColor(pIModel.getColor());
    }
}
