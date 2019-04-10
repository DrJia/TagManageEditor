package com.jiabin.picsortmgrtest;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiabin.playlistmgrtest.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PicMgrAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Pic> mList;
    private LayoutInflater mInflater;
    private Context mContext;

    private static final int MAX_COUNT = 9;

    private int maxCount = MAX_COUNT;

    private PicClickListener mPicClickListener;

    public static final int TYPE_PIC = 101;
    public static final int TYPE_PIC_ADD = 102;

    private int mItemHeight;
    private int mItemWidth;
    private float mProportion = 1.0f;

    public PicMgrAdapter(@NonNull Context context, int itemHeight) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemHeight = itemHeight;
        mItemWidth = (int) (itemHeight * mProportion);
    }

    /**
     * 设置item比例
     *
     * @param proportion 高度为1，宽度为高度的proportion倍
     */
    public void setProportion(float proportion) {
        mProportion = proportion;
        mItemWidth = (int) (mItemHeight * mProportion);
        notifyDataSetChanged();
    }

    public void setMaxCount(@IntRange(from = 1, to = Integer.MAX_VALUE) int maxCount) {
        this.maxCount = maxCount;
    }

    public void setList(ArrayList<Pic> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public ArrayList<Pic> getList() {
        return mList;
    }

    public void removeItem(int pos) {
        if (mList == null) {
            return;
        }
        if (pos < 0 || pos > mList.size()) {
            return;
        }
        mList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void removeItemFromDrag(int pos) {
        if (mList == null) {
            return;
        }
        if (pos < 0 || pos > mList.size()) {
            return;
        }
        mList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount() - pos, "payload");
    }

    public void addItem(Pic pic) {
        if (mList == null) {
            return;
        }
        mList.add(pic);
        //notifyItemInserted(getItemCount() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mList == null) {
            return TYPE_PIC_ADD;
        }
        int count = mList.size();
        if (count >= maxCount) {
            return TYPE_PIC;
        } else {
            if (position == count) {//-1+1
                return TYPE_PIC_ADD;
            } else {
                return TYPE_PIC;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 1;
        }
        int count = mList.size();
        if (count >= maxCount) {
            count = maxCount;
        } else {
            count = count + 1;
        }
        return count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PIC) {
            View view = mInflater.inflate(R.layout.item_pic, parent, false);
            PicViewHolder picViewHolder = new PicViewHolder(view);
            return picViewHolder;
        } else if (viewType == TYPE_PIC_ADD) {
            View view = mInflater.inflate(R.layout.item_pic_add, parent, false);
            PicAddViewHolder picAddViewHolder = new PicAddViewHolder(view);
            return picAddViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            int viewType = holder.getItemViewType();
            if (viewType == TYPE_PIC) {
                PicViewHolder picHolder = (PicViewHolder) holder;
                picHolder.itemView.setVisibility(View.VISIBLE);
            } else if (viewType == TYPE_PIC_ADD) {
                onBindViewHolder(holder, position);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == TYPE_PIC) {
            Pic pic = mList.get(position);
            PicViewHolder picHolder = (PicViewHolder) holder;
            picHolder.itemView.getLayoutParams().height = mItemHeight;
            picHolder.itemView.getLayoutParams().width = mItemWidth;
            picHolder.itemView.setVisibility(View.VISIBLE);
            picHolder.pic.setImageResource(R.mipmap.f1);
            picHolder.txt.setText("" + pic.id);
        } else if (viewType == TYPE_PIC_ADD) {
            PicAddViewHolder picAddHolder = (PicAddViewHolder) holder;
            picAddHolder.itemView.getLayoutParams().height = mItemHeight;
            picAddHolder.itemView.getLayoutParams().width = mItemWidth;
//            if(getItemCount() >= maxCount + 1){
//                picAddHolder.itemView.setVisibility(View.GONE);
//            }else {
//                picAddHolder.itemView.setVisibility(View.VISIBLE);
//            }
        }

    }


    class PicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView edit;
        private ImageView pic;
        private TextView txt;

        public PicViewHolder(View itemView) {
            super(itemView);
            edit = (ImageView) itemView.findViewById(R.id.img_edit);
            pic = (ImageView) itemView.findViewById(R.id.img_pic);
            txt = (TextView) itemView.findViewById(R.id.txt);
            itemView.setOnClickListener(this);
            edit.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (v == itemView) {
                if (mPicClickListener != null) {
                    mPicClickListener.onPicClick(v, pos);
                }
            } else if (v == edit) {
                removeItem(pos);
            }
        }

    }

    class PicAddViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txt;

        public PicAddViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mPicClickListener != null) {
                mPicClickListener.onAddClick(v);
            }
        }
    }

    public interface PicClickListener {
        void onPicClick(View view, int pos);

        void onAddClick(View view);
    }

    public void setPicClickListener(PicClickListener listener) {
        mPicClickListener = listener;
    }
}
