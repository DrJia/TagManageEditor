package com.jiabin.playlistmgrtest.helper;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TagItemDecoration extends RecyclerView.ItemDecoration {

    int mSpace;
    int leftPadding;
    int rightPadding;

    public TagItemDecoration(int space) {
        super();
        mSpace = space;
        leftPadding = 45;
        rightPadding = 45;

    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        view.setTag(position);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            int spanCount =  gridLayoutManager.getSpanCount();
            int spanIndex = spanSizeLookup.getSpanIndex(position, spanCount);
            int spanSize = spanSizeLookup.getSpanSize(position);
            if(spanSize == 1){
                //my other
//                outRect.right = mSpace;
//                outRect.left = mSpace;
//                outRect.top = mSpace;

                outRect.top = mSpace;
                if (spanIndex == spanCount) {
                    //占满
                    outRect.left = mSpace;
                    outRect.right = mSpace;
                } else {
                    outRect.left = (int) (((float) (spanCount - spanIndex)) / spanCount * mSpace);
                    outRect.right = (int) (((float) mSpace * (spanCount + 1) / spanCount) - outRect.left);
                }
            }else {
                //header
                outRect.bottom = -mSpace;
            }
        }
    }
}
