package com.jiabin.playlistmgrtest.helper;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.jiabin.playlistmgrtest.TagsMgrAdapter;

public class ItemDragHelperCallback extends ItemTouchHelper.Callback {
    /**
     * 这个方法用于让RecyclerView拦截各个方向的滑动
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags;
        //针对特定标记不能拖拽
        if(viewHolder instanceof TagsMgrAdapter.MyViewHolder){
            if(((TagsMgrAdapter.MyViewHolder) viewHolder).isResident){
                return 0;
            }
        }
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager || manager instanceof StaggeredGridLayoutManager) {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        } else {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        }
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    /**
     * drag状态下，在canDropOver()返回true时，会调用该方法让我们拖动换位置的逻辑(需要自己处理变换位置的逻辑)
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // 不同Type之间不可移动
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        //针对特定标记不能拖拽
        if(target instanceof TagsMgrAdapter.MyViewHolder){
            if(((TagsMgrAdapter.MyViewHolder) target).isResident){
                return false;
            }
        }

        if (recyclerView.getAdapter() instanceof OnItemMoveListener) {
            OnItemMoveListener listener = ((OnItemMoveListener) recyclerView.getAdapter());
            listener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    /**
     * 针对swipe状态，swipe 到达滑动消失的距离回调函数,一般在这个函数里面处理删除item的逻辑
     * 确切的来讲是swipe item滑出屏幕动画结束的时候调用
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    /**
     * 针对swipe和drag状态，当swipe或者drag对应的ViewHolder改变的时候调用
     * 我们可以通过重写这个函数获取到swipe、drag开始和结束时机，viewHolder 不为空的时候是开始，空的时候是结束
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // 不在闲置状态
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof OnDragVHListener) {
                OnDragVHListener itemViewHolder = (OnDragVHListener) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 针对swipe和drag状态，当一个item view在swipe、drag状态结束的时候调用
     * drag状态：当手指释放的时候会调用
     * swipe状态：当item从RecyclerView中删除的时候调用，一般我们会在onSwiped()函数里面删除掉指定的item view
     */
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof OnDragVHListener) {
            OnDragVHListener itemViewHolder = (OnDragVHListener) viewHolder;
            itemViewHolder.onItemFinish();
        }
        super.clearView(recyclerView, viewHolder);
    }

    /**
     * 该方法返回true时，表示支持长按拖动，即长按ItemView后才可以拖动，我们遇到的场景一般也是这样的。默认是返回true。
     */
    @Override
    public boolean isLongPressDragEnabled() {
        // 不支持长按拖拽功能 手动控制
        return false;
    }

    /**
     * 针对swipe状态，是否允许swipe(滑动)操作
     * 该方法返回true时，表示如果用户触摸并左右滑动了View，那么可以执行滑动删除操作，即可以调用到onSwiped()方法。默认是返回true
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        // 不支持滑动功能
        return false;
    }
}
