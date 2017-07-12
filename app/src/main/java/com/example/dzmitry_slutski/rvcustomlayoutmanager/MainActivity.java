package com.example.dzmitry_slutski.rvcustomlayoutmanager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random mRandom;
    private Resources mResources;
    private int[] indexes = new int[]{4, 2, 5, 3, 1};
    private EditText mEditText;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRandom = new Random();
        final RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new CustomLayoutManager(getResources().getDimensionPixelSize(R.dimen.episode_shift)));
        final RecyclerAdapter adapter = new RecyclerAdapter(this, initList());
        recycler.setAdapter(adapter);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycler.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(final Rect outRect, final View view, final RecyclerView parent, final RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                outRect.bottom = 48;
            }
        });

        mEditText = (EditText) findViewById(R.id.number);
        findViewById(R.id.removeButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                int indexToRemove = Integer.parseInt(mEditText.getText().toString());
                adapter.removeItem(indexToRemove);
            }
        });

        findViewById(R.id.requestLayout).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                recycler.requestLayout();
            }
        });

        findViewById(R.id.scrollDown).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                recycler.scrollBy(0, -10);
            }
        });
    }

    private List<IRoot> initList() {
        mResources = getResources();
        int index = 0;
        final List<IRoot> roots = new ArrayList<>();
        for (int i = 0; i < 5; i++) {

            String seasonTitle = "Season #" + i + " index: " + (index++);
            final List<IModel> models = new ArrayList<>();
            for (int episodeNumber = 0; episodeNumber < indexes[i]; episodeNumber++) {
                models.add(new Model(getDrawableResIdByNumber(episodeNumber), "S" + i + ", Ep #" + episodeNumber + " index: " + index, "sub title: " + episodeNumber));
                index++;
            }

            roots.add(new Root(getDrawableResIdByNumber(i), seasonTitle + " ep_c: " + models.size(), "Sub title of season #" + i, models));
        }

        return roots;
    }

    private int getDrawableResIdByNumber(final int pEpisodeNumber) {
        return mResources.getIdentifier("ic_" + pEpisodeNumber, "drawable", getPackageName());
    }

    private class Model implements IModel {

        String title;
        String subTitle;
        int imageResId;
        int color;

        Model(final int imgId, final String pTitle, final String pSubTitle) {
            title = pTitle;
            subTitle = pSubTitle;
            imageResId = imgId;
            color = mRandom.nextInt();
        }

        @Override
        public int imageId() {
            return imageResId;
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public String subTitle() {
            return subTitle;
        }

        @Override
        public String rootId() {
            return null;
        }

        @Override
        public int getColor() {
            return color;
        }
    }

    private class Root implements IRoot {

        String title;
        String subTitle;
        int imageResId;
        List<IModel> mModels;
        int color;

        Root(final int imgId, final String pTitle, final String pSubTitle, final List<IModel> pModels) {
            title = pTitle;
            subTitle = pSubTitle;
            imageResId = imgId;
            mModels = pModels;

            color = mRandom.nextInt();
        }

        @Override
        public String id() {
            return title;
        }

        @Override
        public int imageId() {
            return imageResId;
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public String subTitle() {
            return subTitle;
        }

        @Override
        public List<IModel> getModels() {
            return mModels;
        }

        @Override
        public int getColor() {
            return color;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mEditText.post(new Runnable() {

            @Override
            public void run() {
                hideKeyboard(mEditText);
            }
        });
    }

    public void hideKeyboard(final View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final View currentFocus = getCurrentFocus();
        if (currentFocus != null && currentFocus.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
