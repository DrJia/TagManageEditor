package com.jiabin.picsortmgrtest;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class PicDragHelperCallback extends ItemTouchHelper.Callback {

    private PicMgrAdapter mAdapter;
    private View delArea;
    private DragListener mDragListener;
    private boolean mIsInside = false;
    private int delPos = -1;
    private RecyclerView.ViewHolder tempHolder;
    //private int mActionState = ItemTouchHelper.ACTION_STATE_IDLE;

    public PicDragHelperCallback(@NonNull PicMgrAdapter adapter, View delArea) {
        mAdapter = adapter;
        this.delArea = delArea;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags;
        if (viewHolder instanceof PicMgrAdapter.PicAddViewHolder) {
            return 0;
        }
        dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        if (target instanceof PicMgrAdapter.PicAddViewHolder) {
            return false;
        }
        ArrayList<Pic> list = mAdapter.getList();
        if (list == null || list.size() < 2) {
            return false;
        }
        int from = viewHolder.getAdapterPosition();
        int endPosition = target.getAdapterPosition();
        Collections.swap(list, from, endPosition);
        mAdapter.notifyItemMoved(from, endPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

//    @Override
//    public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
//        return true;
//    }


    @Override
    public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        Log.d("jiabin","getAnimationDuration");
        if(mIsInside){
            return 0;
        }
        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // 不在闲置状态
        Log.d("jiabin","onSelectedChanged:" + actionState);
        //mActionState = actionState;
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setScaleX(1.2f);
            viewHolder.itemView.setScaleY(1.2f);
            if (mDragListener != null) {
                mDragListener.onDragStart();
            }
            delPos = viewHolder.getAdapterPosition();
            tempHolder = viewHolder;
            Log.d("jiabin","delPos:" + delPos);
        }else{
            if (mDragListener != null) {
                mDragListener.onDragFinish();
            }
            if(mIsInside && delPos >= 0 && tempHolder != null){
                tempHolder.itemView.setVisibility(View.INVISIBLE);
                mAdapter.removeItemFromDrag(delPos);
            }
            delPos = -1;
            tempHolder = null;
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //Log.d("jiabin","dX:" + dX + " | dY:" + dY);
//        int[] location = new int[2] ;
//        delArea.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
        int delAreaY = delArea.getTop();
        int itemHeight = viewHolder.itemView.getHeight();
        //Log.d("jiabin", "delAreaY:" + delAreaY + " | itemHeight:" + itemHeight + " | dY:" + dY);

        boolean isInside = false;
        if(dY + itemHeight > delAreaY){
            isInside = true;
        }else {
            isInside = false;
        }
        if(isInside != mIsInside){
            if(mDragListener != null){
                mDragListener.onDragAreaChange(isInside);
            }
        }
        mIsInside = isInside;
//        if(isInside && mActionState == ItemTouchHelper.ACTION_STATE_IDLE){
//            viewHolder.itemView.setVisibility(View.INVISIBLE);
//        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setScaleX(1.0f);
        viewHolder.itemView.setScaleY(1.0f);
//        if (mDragListener != null) {
//            mDragListener.onDragFinish();
//        }
//        if(mIsInside && delPos >= 0){
//            mAdapter.removeItem(delPos);
//        }
//        delPos = -1;
        //mAdapter.notifyDataSetChanged();
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // 支持长按拖拽功能
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // 不支持滑动功能
        return false;
    }

    public interface DragListener {
        void onDragStart();

        void onDragFinish();

        void onDragAreaChange(boolean isInside);
    }

    public void setDragListener(DragListener listener) {
        mDragListener = listener;
    }
}
