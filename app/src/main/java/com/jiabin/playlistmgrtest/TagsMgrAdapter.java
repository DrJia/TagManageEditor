package com.jiabin.playlistmgrtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiabin.playlistmgrtest.entry.Tag;
import com.jiabin.playlistmgrtest.entry.TagsEntry;
import com.jiabin.playlistmgrtest.helper.OnDragVHListener;
import com.jiabin.playlistmgrtest.helper.OnItemMoveListener;
import com.jiabin.playlistmgrtest.helper.TagGridLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagsMgrAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    private static final long ANIM_TIME = 360L;

    private static final long ANIM_TIME_OTHER = 360L;

    private static final long ANIM_TIME_OTHER_FIRST = 500L;

    public static final class VIEW_TYPES {
        // 我的 标题部分
        public static final int TYPE_MY_TAG_HEADER = 1001;
        // 我的
        public static final int TYPE_MY = 1002;
        // 其他 标题部分
        public static final int TYPE_OTHER_TAG_HEADER = 1003;
        // 其他
        public static final int TYPE_OTHER = 1004;
    }

    private ItemTouchHelper mItemTouchHelper;
    private List<Tag> mMyTags;
    private List<TagsEntry> mAllTagsEntries;
    int myTagsSize;
    ArrayList<Integer> otherHeaderPosList = new ArrayList<>();

    private LayoutInflater mInflater;

    // 是否为 编辑 模式
    private boolean isEditMode;

    private int totalSize;

    private int mSpace;
    private int mMargin;
    private int mItemWidth;
    private int mScreenWitdh;
    private int mSpanCount;

    private boolean mIsAniming;

    private Context mContext;
    private PlayListTagItemClickListener mPlayListTagItemClickListener;
    private PlayListTagChangeListener mPlayListTagChangeListener;


    public TagsMgrAdapter(Context context, @NonNull ItemTouchHelper helper, int spanCount, int space, int margin) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemTouchHelper = helper;
        mScreenWitdh = DimensionUtils.getScreenWidth(context);
        mSpace = space;
        mMargin = margin;
        mSpanCount = spanCount;
        mItemWidth = (mScreenWitdh - mSpace * (spanCount + 1) - mMargin * 2) / spanCount;
    }

    private void checkDisable() {
        for (int i = 0; i < mAllTagsEntries.size(); i++) {
            List<Tag> list = mAllTagsEntries.get(i).tags;
            for (Tag tag : list) {
                for (Tag myTag : mMyTags) {
                    if (tag.id == myTag.id) {
                        tag.isDisable = true;
                    }
                }
            }
        }
    }

    private int getXoffset(int index) {
        if (index > mSpanCount - 1) {
            return -1;
        }
        int offset = mSpace + index * (mItemWidth + mSpace);
        return offset;
    }

    public List<Tag> getMyTags() {
        return mMyTags;
    }

    public void refreshList(@NonNull List<Tag> myTags, @NonNull List<TagsEntry> allTagsEntries) {
        mMyTags = myTags;
        mAllTagsEntries = allTagsEntries;
        myTagsSize = mMyTags.size();
        otherHeaderPosList.clear();

        if (!mAllTagsEntries.isEmpty()) {
            otherHeaderPosList.add(myTagsSize + 1);
        }
        for (int i = 0; i < mAllTagsEntries.size() - 1; i++) {
            int pos = otherHeaderPosList.get(i) + mAllTagsEntries.get(i).tags.size() + 1;
            otherHeaderPosList.add(pos);
        }
        totalSize = mMyTags.size() + 1;
        for (TagsEntry entry : mAllTagsEntries) {
            totalSize += entry.tags.size() + 1;
        }
        checkDisable();
        notifyDataSetChanged();
    }

    public void removeMyTag(int position) {
        int startPosition = position - 1;
        if (startPosition > mMyTags.size() - 1) {
            return;
        }
        mMyTags.remove(startPosition);
        myTagsSize = mMyTags.size();
        otherHeaderPosList.clear();
        if (!mAllTagsEntries.isEmpty()) {
            otherHeaderPosList.add(myTagsSize + 1);
        }
        for (int i = 0; i < mAllTagsEntries.size() - 1; i++) {
            int pos = otherHeaderPosList.get(i) + mAllTagsEntries.get(i).tags.size() + 1;
            otherHeaderPosList.add(pos);
        }
        totalSize = mMyTags.size() + 1;
        for (TagsEntry entry : mAllTagsEntries) {
            totalSize += entry.tags.size() + 1;
        }
        notifyItemRemoved(position);

    }

    public void addMyTag(RecyclerView recyclerView, Tag item) {
        for (Tag tag : mMyTags) {
            if (tag.id == item.id) {
                return;
            }
        }
        mMyTags.add(item);
        myTagsSize = mMyTags.size();
        otherHeaderPosList.clear();

        if (!mAllTagsEntries.isEmpty()) {
            otherHeaderPosList.add(myTagsSize + 1);
        }
        for (int i = 0; i < mAllTagsEntries.size() - 1; i++) {
            int pos = otherHeaderPosList.get(i) + mAllTagsEntries.get(i).tags.size() + 1;
            otherHeaderPosList.add(pos);
        }
        totalSize = mMyTags.size() + 1;
        for (TagsEntry entry : mAllTagsEntries) {
            totalSize += entry.tags.size() + 1;
        }
        notifyItemInserted(myTagsSize);

        GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int pos = gridLayoutManager.findLastVisibleItemPosition();
        Log.d("jiabin","pos:" + pos + " | totalSize:" + totalSize);
        if (pos < totalSize) {
            //当没到底的时候insert需要notifychanged，因为会导致滚到下一页的时候item显示不正确，但是不能使用notifyDataSetChanged，否则会使动画消失
            notifyItemRangeChanged(pos, totalSize - pos , "payload");
        }
    }

    /**
     * 查找头部
     *
     * @param position
     * @return
     */
    private int getOtherHeaderIndex(int position) {
        if (otherHeaderPosList.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < otherHeaderPosList.size(); i++) {
            if (otherHeaderPosList.get(i) == position) {
                return i;
            }
        }
        return -1;
    }

    private Tag getOtherTag(int position) {
        int index = position - (myTagsSize + 1);
        for (int i = 0; i < mAllTagsEntries.size(); i++) {
            if (index <= mAllTagsEntries.get(i).tags.size() + 1) {
                return mAllTagsEntries.get(i).tags.get(index - 1);
            } else {
                index = index - (mAllTagsEntries.get(i).tags.size() + 1);
                if (index < 0) {
                    return null;
                }
            }
        }
        return null;
    }

    private int getOtherTagIndex(int position) {
        int index = position - (myTagsSize + 1);
        for (int i = 0; i < mAllTagsEntries.size(); i++) {
            if (index <= mAllTagsEntries.get(i).tags.size() + 1) {
                return index - 1;
            } else {
                index = index - (mAllTagsEntries.get(i).tags.size() + 1);
                if (index < 0) {
                    return -1;
                }
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPES.TYPE_MY_TAG_HEADER;
        } else if (position > 0 && position < myTagsSize + 1) {
            return VIEW_TYPES.TYPE_MY;
        } else {
            if (otherHeaderPosList.size() > 0) {
                if (otherHeaderPosList.contains(position)) {
                    return VIEW_TYPES.TYPE_OTHER_TAG_HEADER;
                } else {
                    return VIEW_TYPES.TYPE_OTHER;
                }
            }
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case VIEW_TYPES.TYPE_MY_TAG_HEADER:
                view = mInflater.inflate(R.layout.item_my_tag_header, parent, false);
                return new MyTagHeaderViewHolder(view);
            case VIEW_TYPES.TYPE_MY:
                view = mInflater.inflate(R.layout.item_my, parent, false);
                return new MyViewHolder((RecyclerView) parent, view);

            case VIEW_TYPES.TYPE_OTHER_TAG_HEADER:
                view = mInflater.inflate(R.layout.item_other_tag_header, parent, false);
                return new OtherTagHeaderViewHolder(view);
            case VIEW_TYPES.TYPE_OTHER:
                view = mInflater.inflate(R.layout.item_other, parent, false);
                return new OtherViewHolder((RecyclerView) parent, view);
        }
        return null;
    }


    private Handler delayHandler = new Handler();

    /**
     * 开始增删动画 从其他到我的
     */
    private void startAnimationOther(final RecyclerView recyclerView, final View currentView, OtherViewHolder otherViewHolder, final Tag otherTag, float targetX, float targetY, long animTime) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        otherViewHolder.setIconDel();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);
        otherViewHolder.setIconPlus();

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop(), animTime);
        mirrorView.startAnimation(animation);
        final TagGridLayoutManager gridLayoutManager = (TagGridLayoutManager) recyclerView.getLayoutManager();
        gridLayoutManager.setScrollEnabled(false);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAniming = true;
                recyclerView.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                recyclerView.setEnabled(true);
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewGroup.removeView(mirrorView);
                        otherTag.isHide = false;
                        //notifyItemChanged(myTagsSize);
                        notifyDataSetChanged();
                        if (mPlayListTagChangeListener != null) {
                            mPlayListTagChangeListener.onMyTagAdd((ArrayList<Tag>) mMyTags, otherTag);
                        }
                        gridLayoutManager.setScrollEnabled(true);
                        mIsAniming = false;
                    }
                }, 0);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 开始增删动画 从我的到其他
     */
    private void startAnimationMy(final RecyclerView recyclerView, final View currentView, MyViewHolder myViewHolder, final Tag myTag, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        myViewHolder.setIconPlus();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);
        myViewHolder.setIconDel();

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop(), ANIM_TIME);
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animation);
        final TagGridLayoutManager gridLayoutManager = (TagGridLayoutManager) recyclerView.getLayoutManager();
        gridLayoutManager.setScrollEnabled(false);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAniming = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewGroup.removeView(mirrorView);
                        if (currentView.getVisibility() == View.INVISIBLE) {
                            currentView.setVisibility(View.VISIBLE);
                        }
                        if (mPlayListTagChangeListener != null) {
                            mPlayListTagChangeListener.onMyTagRemove((ArrayList<Tag>) mMyTags, myTag);
                        }
                        gridLayoutManager.setScrollEnabled(true);
                        mIsAniming = false;
                    }
                }, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        //PixelCopy.request
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(recyclerView.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        recyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);

        return mirrorView;
    }

    /**
     * 获取位移动画
     */
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY, long animTime) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        translateAnimation.setDuration(animTime);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPES.TYPE_MY_TAG_HEADER:
                MyTagHeaderViewHolder myHeaderHolder = (MyTagHeaderViewHolder) holder;
                if (isEditMode) {
                    myHeaderHolder.tvBtnEdit.setText("完成");
                    myHeaderHolder.tips.setText("（拖动可排序）");
                } else {
                    myHeaderHolder.tvBtnEdit.setText("编辑");
                    myHeaderHolder.tips.setText("（长按可编辑）");
                }
                break;
            case VIEW_TYPES.TYPE_MY:
                MyViewHolder myHolder = (MyViewHolder) holder;
                Tag myListTag = mMyTags.get(position - 1);
                myHolder.textView.setText(myListTag.name);//-1是减掉头部
                myHolder.isResident = myListTag.isResident;
                if (myListTag.isHide) {
                    myHolder.itemView.setVisibility(View.INVISIBLE);
                } else {
                    myHolder.itemView.setVisibility(View.VISIBLE);
                }
                if (isEditMode) {
                    if (myHolder.isResident) {
                        myHolder.hideIcon();
                        myHolder.itemView.setEnabled(false);
                    } else {
                        myHolder.setIconDel();
                        myHolder.itemView.setEnabled(true);
                    }
                } else {
                    myHolder.hideIcon();
                    myHolder.itemView.setEnabled(true);
                }
                break;
            case VIEW_TYPES.TYPE_OTHER_TAG_HEADER:
                OtherTagHeaderViewHolder otherHeaderHolder = (OtherTagHeaderViewHolder) holder;
                int headerIndex = getOtherHeaderIndex(position);
                if (headerIndex != -1 && headerIndex < mAllTagsEntries.size()) {
                    otherHeaderHolder.tv.setText(mAllTagsEntries.get(headerIndex).category);
                }
                break;
            case VIEW_TYPES.TYPE_OTHER:
                OtherViewHolder otherHolder = (OtherViewHolder) holder;
                Tag otherTag = getOtherTag(position);
                if (otherTag == null) {
                    return;
                }
                otherHolder.textView.setText(otherTag.name);
                otherHolder.itemView.setEnabled(!otherTag.isDisable);
                otherHolder.isDisable = otherTag.isDisable;
                if (isEditMode) {
                    otherHolder.setIconPlus();
                } else {
                    otherHolder.hideIcon();
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return totalSize;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
//        Tag item = mMyTags.get(fromPosition - 1);
//        mMyTags.remove(fromPosition - 1);
//        mMyTags.add(toPosition - 1, item);
        Collections.swap(mMyTags, fromPosition - 1, toPosition - 1);
        notifyItemMoved(fromPosition , toPosition);
    }

    /**
     * 开启编辑模式
     */
    private void startEditMode() {
        isEditMode = true;
        notifyDataSetChanged();
    }

    /**
     * 完成编辑模式
     */
    private void cancelEditMode() {
        isEditMode = false;
        notifyDataSetChanged();
    }

    private int getOtherPosition(Tag myTag) {
        int otherPos = myTagsSize + 1;
        for (int i = 0; i < mAllTagsEntries.size(); i++) {
            TagsEntry entry = mAllTagsEntries.get(i);
            if (entry.viewType == myTag.viewType) {
                for (int j = 0; j < entry.tags.size(); j++) {
                    if (myTag.id == entry.tags.get(j).id) {
                        otherPos += j + 1;
                        entry.tags.get(j).isDisable = false;
                        return otherPos;
                    }
                }
            }
            otherPos += entry.tags.size() + 1;
        }
        return -1;
    }


    /**
     * 我的频道
     */
    public class MyViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener, View.OnClickListener, View.OnLongClickListener {
        private TextView textView;
        private TextView del;
        public boolean isResident = false;
        private RecyclerView recyclerView;

        public MyViewHolder(RecyclerView parent, View itemView) {
            super(itemView);
            recyclerView = parent;
            textView = (TextView) itemView.findViewById(R.id.tv);
            del = (TextView) itemView.findViewById(R.id.icon);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setIconDel() {
            del.setVisibility(View.VISIBLE);
            del.setText(" - ");
        }

        public void setIconPlus() {
            del.setVisibility(View.VISIBLE);
            del.setText(" + ");
        }

        public void hideIcon() {
            del.setVisibility(View.GONE);
        }

        /**
         * item 被选中时
         */
        @Override
        public void onItemSelected() {
            itemView.setBackgroundResource(R.drawable.bg_channel_p);
        }

        /**
         * item 取消选中时
         */
        @Override
        public void onItemFinish() {
            itemView.setBackgroundResource(R.drawable.bg_channel);
            if (mPlayListTagChangeListener != null) {
                mPlayListTagChangeListener.onMyTagMove((ArrayList<Tag>) mMyTags);
            }
        }

        @Override
        public void onClick(View v) {
            if (mIsAniming) {
                return;
            }
            int position = getAdapterPosition();
            int startPosition = position - 1;
            if (startPosition > mMyTags.size() - 1 || startPosition < 0) {
                return;
            }
            Tag myTag = mMyTags.get(startPosition);
            if (myTag == null) {
                return;
            }

            if (isEditMode) {
                //check left item
//                int count = 0;
//                for (Tag tag : mMyTags) {
//                    if (!tag.isResident) {
//                        count++;
//                    }
//                }
//                if (count <= 3) {
//                    Toast.makeText(mContext, "不能再删除了哦", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                //check end
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();

                if (myTag.isResident) {
                    return;
                }

                int otherPos = getOtherPosition(myTag);
                if (otherPos < 0) {
                    return;
                }
                View currentView = manager.findViewByPosition(position);
                View targetView = manager.findViewByPosition(otherPos);
                int otherIndex = getOtherTagIndex(otherPos);
                if (otherIndex < 0) {
                    return;
                }

                otherIndex = otherIndex % mSpanCount;
                int targetX = getXoffset(otherIndex);
                if (recyclerView.indexOfChild(targetView) >= 0) {
                    int targetY = targetView.getTop();
                    int lastMyPosition = myTagsSize - 1;
                    if (lastMyPosition % mSpanCount == 0) {
                        //我的里面最后一个在最后一个第一行
                        targetY = targetY - targetView.getHeight() - mSpace;
                    }
                    startAnimationMy(recyclerView, currentView, this, myTag, targetX, targetY);

                } else {
                    startAnimationMy(recyclerView, currentView, this, myTag, targetX, recyclerView.getBottom() + 100);
                }
                notifyItemChanged(otherPos);
                removeMyTag(position);
            } else {
                if (mPlayListTagItemClickListener != null) {
                    mPlayListTagItemClickListener.onMyTagClick(myTag.name);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (!isEditMode) {
                startEditMode();

                // header 按钮文字 改成 "完成"
                View view = recyclerView.getChildAt(0);
                if (view == recyclerView.getLayoutManager().findViewByPosition(0)) {
                    TextView tvBtnEdit = (TextView) view.findViewById(R.id.tv_btn_edit);
                    tvBtnEdit.setText("完成");
                    TextView tips = (TextView) view.findViewById(R.id.tips);
                    tips.setText("（拖动可排序）");
                }
            }

            mItemTouchHelper.startDrag(this);
            return true;
        }
    }

    /**
     * 其他频道
     */
    class OtherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView textView;
        private TextView plus;
        public boolean isDisable = false;
        private RecyclerView recyclerView;

        public OtherViewHolder(RecyclerView parent, View itemView) {
            super(itemView);
            recyclerView = parent;
            textView = (TextView) itemView.findViewById(R.id.tv);
            plus = (TextView) itemView.findViewById(R.id.icon);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setIconPlus() {
            plus.setVisibility(View.VISIBLE);
            plus.setText(" + ");
        }

        public void setIconDel() {
            plus.setVisibility(View.VISIBLE);
            plus.setText(" - ");
        }

        public void hideIcon() {
            plus.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (mIsAniming) {
                return;
            }

            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            int position = getAdapterPosition();
            Tag otherTag = getOtherTag(position);
            if (otherTag == null) {
                return;
            }
            if (isEditMode) {
                // 如果RecyclerView滑动到底部,移动的目标位置的y轴 - height
                View currentView = manager.findViewByPosition(position);
                // 目标位置的前一个item  即当前MyChannel的最后一个
                View preTargetView = manager.findViewByPosition(myTagsSize);// -1 + 1

                int myIndex = (myTagsSize - 1 + 1) % mSpanCount;
                int targetX = getXoffset(myIndex);

                if (otherTag.isDisable) {
                    return;
                }

                // 如果targetView不在屏幕内,则为-1
                // 如果在屏幕内,则添加一个位移动画
                if (recyclerView.indexOfChild(preTargetView) >= 0) {
                    int targetY = preTargetView.getTop();
                    int targetPosition = myTagsSize - 1 + 1 + 1;
                    int itemHeight = preTargetView.getHeight();
                    // target 在最后一行第一个
                    long animTime = ANIM_TIME_OTHER;
                    if ((targetPosition - 1) % mSpanCount == 0) {
                        targetY = targetY + itemHeight + mSpace;
                        animTime = ANIM_TIME_OTHER_FIRST;
                    }
                    startAnimationOther(recyclerView, currentView, this, otherTag, targetX, targetY, animTime);
                } else {
                    startAnimationOther(recyclerView, currentView, this, otherTag, targetX, -currentView.getHeight() - 100, ANIM_TIME);
                }

                otherTag.isDisable = true;
                notifyItemChanged(position);

                addMyTag(recyclerView, otherTag);
            } else {
                if (mPlayListTagItemClickListener != null) {
                    mPlayListTagItemClickListener.onOtherTagClick(otherTag.name);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (!isEditMode) {
                startEditMode();

                // header 按钮文字 改成 "完成"
                View view = recyclerView.getChildAt(0);
                if (view == recyclerView.getLayoutManager().findViewByPosition(0)) {
                    TextView tvBtnEdit = (TextView) view.findViewById(R.id.tv_btn_edit);
                    tvBtnEdit.setText("完成");
                    TextView tips = (TextView) view.findViewById(R.id.tips);
                    tips.setText("（拖动可排序）");
                }
            }
            return true;
        }
    }

    /**
     * 我的频道  标题部分
     */
    class MyTagHeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvBtnEdit;
        private TextView tips;

        public MyTagHeaderViewHolder(View itemView) {
            super(itemView);
            tvBtnEdit = (TextView) itemView.findViewById(R.id.tv_btn_edit);
            tips = (TextView) itemView.findViewById(R.id.tips);
            tvBtnEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!isEditMode) {
                startEditMode();
                tvBtnEdit.setText("完成");
                tips.setText("（拖动可排序）");
            } else {
                cancelEditMode();
                tvBtnEdit.setText("编辑");
                tips.setText("（长按可编辑）");
            }
        }
    }

    /**
     * 其他频道  标题部分
     */
    class OtherTagHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;

        public OtherTagHeaderViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
        }
    }

    public interface PlayListTagItemClickListener {
        void onMyTagClick(String name);

        void onOtherTagClick(String name);
    }

    public void setPlayListTagItemClickListener(PlayListTagItemClickListener listener) {
        mPlayListTagItemClickListener = listener;
    }

    public interface PlayListTagChangeListener {
        void onMyTagAdd(ArrayList<Tag> myTags, Tag target);

        void onMyTagRemove(ArrayList<Tag> myTags, Tag target);

        void onMyTagMove(ArrayList<Tag> myTags);
    }

    public void setPlayListTagChangeListener(PlayListTagChangeListener listener) {
        mPlayListTagChangeListener = listener;
    }
}
