package com.jiabin.playlistmgrtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import java.util.ArrayList;
import java.util.List;

public class TagsMgrAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    private static final long ANIM_TIME = 250L;

    // 我的 标题部分
    public static final int TYPE_MY_TAG_HEADER = 1;
    // 我的
    public static final int TYPE_MY = 2;
    // 其他 标题部分
    public static final int TYPE_OTHER_TAG_HEADER = 3;
    // 其他
    public static final int TYPE_OTHER = 4;

    private ItemTouchHelper mItemTouchHelper;
    private List<Tag> mMyTags;
    private List<TagsEntry> mAllTagsEntries;
    int myTagsSize;
    //HashMap<Integer,Integer> otherHeaderPosMap = new HashMap<>();
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


    public TagsMgrAdapter(Context context, @NonNull ItemTouchHelper helper, int spanCount, int space, int margin, @NonNull List<Tag> myTags, @NonNull List<TagsEntry> allTagsEntries) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemTouchHelper = helper;
        mScreenWitdh = DimensionUtils.getScreenWidth(context);
        mSpace = space;
        mMargin = margin;
        mSpanCount = spanCount;
        mItemWidth = (mScreenWitdh - mSpace * (spanCount + 1) - mMargin * 2) / spanCount;
        refreshList(myTags, allTagsEntries);
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
        //totalSize = 0;
        totalSize = mMyTags.size() + 1;
        for (TagsEntry entry : mAllTagsEntries) {
            totalSize += entry.tags.size() + 1;
        }
        checkDisable();
        notifyDataSetChanged();
    }

    //private boolean isRemoving = false;

    public void removeMyTag(int position) {
        int startPosition = position - 1;
        if (startPosition > mMyTags.size() - 1) {
            return;
        }
        //Tag item = mMyTags.get(startPosition);
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
        //totalSize = 0;
        totalSize = mMyTags.size() + 1;
        for (TagsEntry entry : mAllTagsEntries) {
            totalSize += entry.tags.size() + 1;
        }
        notifyItemRemoved(position);

    }

    public void addMyTag(RecyclerView recyclerView, Tag item) {
        //
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
        //totalSize = 0;
        totalSize = mMyTags.size() + 1;
        for (TagsEntry entry : mAllTagsEntries) {
            totalSize += entry.tags.size() + 1;
        }
        notifyItemInserted(myTagsSize);

        GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int pos = gridLayoutManager.findLastVisibleItemPosition();
        //Log.d("jiabin","pos:" + pos + " totalSize:" + totalSize);
        if (pos < totalSize - 2) {
            //当没到底的时候insert需要notifychanged，因为会导致滚到下一页的时候item显示不正确，但是不能使用notifyDataSetChanged，否则会使动画消失
            notifyItemRangeChanged(pos, totalSize - pos - 2);
        }
        //notifyDataSetChanged();
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
            return TYPE_MY_TAG_HEADER;
        } else if (position > 0 && position < myTagsSize + 1) {
            return TYPE_MY;
        } else {
            if (otherHeaderPosList.size() > 0) {
                if (otherHeaderPosList.contains(position)) {
                    return TYPE_OTHER_TAG_HEADER;
                } else {
                    return TYPE_OTHER;
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
            case TYPE_MY_TAG_HEADER:
                view = mInflater.inflate(R.layout.item_my_tag_header, parent, false);
                final MyTagHeaderViewHolder myHeaderholder = new MyTagHeaderViewHolder(view);
                myHeaderholder.tvBtnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isEditMode) {
                            startEditMode((RecyclerView) parent);
                            myHeaderholder.tvBtnEdit.setText("完成");
                            myHeaderholder.tips.setText("拖动可排序");
                        } else {
                            cancelEditMode((RecyclerView) parent);
                            myHeaderholder.tvBtnEdit.setText("编辑");
                            myHeaderholder.tips.setText("长按可编辑");
                        }
                    }
                });
                return myHeaderholder;
            case TYPE_MY:
                view = mInflater.inflate(R.layout.item_my, parent, false);
                final MyViewHolder myHolder = new MyViewHolder(view);
                myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsAniming) {
                            return;
                        }
                        int position = myHolder.getAdapterPosition();
                        int startPosition = position - 1;
                        if (startPosition > mMyTags.size() - 1 || startPosition < 0) {
                            return;
                        }
                        Tag myTag = mMyTags.get(startPosition);
                        if (myTag == null) {
                            return;
                        }

                        if (isEditMode) {
                            RecyclerView recyclerView = ((RecyclerView) parent);
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
                            //int pos = manager.findLastVisibleItemPosition();

                            otherIndex = otherIndex % mSpanCount;
                            int targetX = getXoffset(otherIndex);
                            if (recyclerView.indexOfChild(targetView) >= 0) {
                                int targetY = targetView.getTop();
                                int lastMyPosition = myTagsSize - 1;
                                if (lastMyPosition % mSpanCount == 0) {
                                    //我的里面最后一个在最后一个第一行
                                    targetY = targetY - targetView.getHeight() - mSpace;
                                }
                                startAnimationMy(recyclerView, currentView, targetX, targetY);

                            } else {
                                startAnimationMy(recyclerView, currentView, targetX, recyclerView.getBottom() + 100);
                            }
                            notifyItemChanged(otherPos);
                            removeMyTag(position);
                            //moveMyToOther(myHolder);
                        } else {
                            //do nothing
                            Toast.makeText(mContext.getApplicationContext(), "" + myTag.name, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        if (!isEditMode) {
                            RecyclerView recyclerView = ((RecyclerView) parent);
                            startEditMode(recyclerView);

                            // header 按钮文字 改成 "完成"
                            View view = recyclerView.getChildAt(0);
                            if (view == recyclerView.getLayoutManager().findViewByPosition(0)) {
                                TextView tvBtnEdit = (TextView) view.findViewById(R.id.tv_btn_edit);
                                tvBtnEdit.setText("完成");
                                TextView tips = (TextView) view.findViewById(R.id.tips);
                                tips.setText("拖动可排序");
                            }
                        }

                        mItemTouchHelper.startDrag(myHolder);
                        return true;
                    }
                });
                return myHolder;

            case TYPE_OTHER_TAG_HEADER:
                view = mInflater.inflate(R.layout.item_other_tag_header, parent, false);
                OtherTagHeaderViewHolder otherHeaderViewHolder = new OtherTagHeaderViewHolder(view);
                //otherHeaderViewHolder.tv.setText("");
                return otherHeaderViewHolder;
            case TYPE_OTHER:
                view = mInflater.inflate(R.layout.item_other, parent, false);
                final OtherViewHolder otherHolder = new OtherViewHolder(view);
                otherHolder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //
                        if (mIsAniming) {
                            return;
                        }

                        RecyclerView recyclerView = ((RecyclerView) parent);
                        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                        int position = otherHolder.getAdapterPosition();
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

//                        GridLayoutManager gridLayoutManager = ((GridLayoutManager) manager);
//                        int spanCount = gridLayoutManager.getSpanCount();
                            // 如果targetView不在屏幕内,则为-1
                            // 如果在屏幕内,则添加一个位移动画
                            if (recyclerView.indexOfChild(preTargetView) >= 0) {
                                //int targetX = preTargetView.getLeft();
                                int targetY = preTargetView.getTop();
                                int targetPosition = myTagsSize - 1 + 1 + 1;
                                int itemHeight = preTargetView.getHeight();
                                // target 在最后一行第一个
                                if ((targetPosition - 1) % mSpanCount == 0) {
                                    targetY = targetY + itemHeight + mSpace;
                                }
                                startAnimationOther(recyclerView, currentView, targetX, targetY);
                            } else {
                                startAnimationOther(recyclerView, currentView, targetX, -currentView.getHeight() - 100);
                            }

                            if (otherTag.isDisable) {
                                return;
                            }
                            otherTag.isDisable = true;
                            notifyItemChanged(position);

                            addMyTag(recyclerView, otherTag);
                            //moveOtherToMy(otherHolder, recyclerView);
                        } else {
                            Toast.makeText(mContext, "" + otherTag.name, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                return otherHolder;
        }
        return null;
    }

    /**
     * 其他频道 移动到 我的频道 伴随延迟
     *
     * @param otherHolder
     */
//    private void moveOtherToMyWithDelay(final OtherViewHolder otherHolder, final RecyclerView recyclerView) {
//        final int position = otherHolder.getAdapterPosition();
//        final Tag otherTag = getOtherTag(position);
//        if (otherTag.isDisable) {
//            return;
//        }
//        otherTag.isDisable = true;
//        notifyItemChanged(position);
//        delayHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                addMyTag(recyclerView, otherTag);
//            }
//        }, ANIM_TIME);
//    }

    private Handler delayHandler = new Handler();

    /**
     * 开始增删动画 从其他到我的
     */
    private void startAnimationOther(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop());
        //currentView.setVisibility(View.INVISIBLE);
        currentView.setEnabled(false);
        mirrorView.startAnimation(animation);

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
                        mIsAniming = false;
                        viewGroup.removeView(mirrorView);
//                        if (currentView.getVisibility() == View.INVISIBLE) {
//                            currentView.setVisibility(View.VISIBLE);
//                        }
                        if (!currentView.isEnabled()) {
                            currentView.setEnabled(true);
                        }
                    }
                }, 360);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 开始增删动画 从我的到其他
     */
    private void startAnimationMy(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        currentView.setEnabled(false);
        mirrorView.startAnimation(animation);

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
                        mIsAniming = false;
                        viewGroup.removeView(mirrorView);
                        if (currentView.getVisibility() == View.INVISIBLE) {
                            currentView.setVisibility(View.VISIBLE);
                        }
                        if (!currentView.isEnabled()) {
                            currentView.setEnabled(true);
                        }
                    }
                },360);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 添加需要移动的 镜像View
     */
    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
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
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
//        //super.onBindViewHolder(holder, position, payloads);
//        switch (holder.getItemViewType()) {
//            case TYPE_MY:
//                MyViewHolder myHolder = (MyViewHolder) holder;
//                if (isEditMode) {
//                    if (myHolder.isResident) {
//                        myHolder.imgEdit.setVisibility(View.GONE);
//                    } else {
//                        myHolder.imgEdit.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    myHolder.imgEdit.setVisibility(View.GONE);
//                }
//                break;
//            case TYPE_OTHER:
//                OtherViewHolder otherHolder = (OtherViewHolder) holder;
//                if (isEditMode) {
//                    otherHolder.plus.setVisibility(View.VISIBLE);
//                    if (otherHolder.isDisable) {
//                        otherHolder.plus.setText("$");
//                    } else {
//                        otherHolder.plus.setText("+");
//                    }
//                } else {
//                    otherHolder.plus.setVisibility(View.GONE);
//                }
//                break;
//        }
//    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_MY_TAG_HEADER:
                MyTagHeaderViewHolder myHeaderHolder = (MyTagHeaderViewHolder) holder;
                if (isEditMode) {
                    myHeaderHolder.tvBtnEdit.setText("完成");
                    myHeaderHolder.tips.setText("拖动可排序");
                } else {
                    myHeaderHolder.tvBtnEdit.setText("编辑");
                    myHeaderHolder.tips.setText("长按可编辑");
                }
                break;
            case TYPE_MY:
                MyViewHolder myHolder = (MyViewHolder) holder;
//                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) myHolder.itemView.getLayoutParams();
//                lp.width = mItemWidth;
//                if ((position - 1) % mSpanCount == 0) {
//                    //第一列
//                    lp.setMargins(mSpace , 0,0,0);
//                }
                myHolder.textView.setText(mMyTags.get(position - 1).name);//-1是减掉头部
                myHolder.isResident = mMyTags.get(position - 1).isResident;
                if (isEditMode) {
                    if (myHolder.isResident) {
                        myHolder.imgEdit.setVisibility(View.GONE);
                        myHolder.itemView.setEnabled(false);
                    } else {
                        myHolder.imgEdit.setVisibility(View.VISIBLE);
                        myHolder.itemView.setEnabled(true);
                    }
                } else {
                    myHolder.imgEdit.setVisibility(View.GONE);
                    myHolder.itemView.setEnabled(true);
                }
                break;
            case TYPE_OTHER_TAG_HEADER:
                OtherTagHeaderViewHolder otherHeaderHolder = (OtherTagHeaderViewHolder) holder;
                int headerIndex = getOtherHeaderIndex(position);
                if (headerIndex != -1 && headerIndex < mAllTagsEntries.size()) {
                    otherHeaderHolder.tv.setText(mAllTagsEntries.get(headerIndex).category);
                }
                break;
            case TYPE_OTHER:
                OtherViewHolder otherHolder = (OtherViewHolder) holder;
                //otherHolder.itemView.getLayoutParams().width = mItemWidth;
                Tag otherTag = getOtherTag(position);
                if (otherTag == null) {
                    return;
                }
                otherHolder.textView.setText(otherTag.name);
                otherHolder.itemView.setEnabled(!otherTag.isDisable);
                otherHolder.isDisable = otherTag.isDisable;
                if (otherTag.isDisable) {
                    otherHolder.plus.setVisibility(View.VISIBLE);
                    otherHolder.plus.setText("✅");
                } else {
                    if (isEditMode) {
                        otherHolder.plus.setVisibility(View.VISIBLE);
                        otherHolder.plus.setText("+");
                    } else {
                        otherHolder.plus.setVisibility(View.GONE);
                    }
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
        Tag item = mMyTags.get(fromPosition - 1);
        mMyTags.remove(fromPosition - 1);
        mMyTags.add(toPosition - 1, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * 开启编辑模式
     *
     * @param parent
     */
    private void startEditMode(RecyclerView parent) {
        isEditMode = true;
        notifyDataSetChanged();
        //notifyItemRangeChanged(0,getItemCount(),isEditMode);

//        int visibleChildCount = parent.getChildCount();
//        for (int i = 0; i < visibleChildCount; i++) {
//            View view = parent.getChildAt(i);
//            RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
//            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
//            if (imgEdit != null && holder instanceof MyViewHolder) {
//
//                if (((MyViewHolder) holder).isResident) {
//                    imgEdit.setVisibility(View.GONE);
//                } else {
//                    imgEdit.setVisibility(View.VISIBLE);
//                }
//            }
//            TextView plus = (TextView)view.findViewById(R.id.plus);
//            if(plus != null && holder instanceof OtherViewHolder){
//                plus.setVisibility(View.VISIBLE);
//                if(((OtherViewHolder)holder).isDisable){
//                    plus.setText("$️");
//                }else {
//                    plus.setText("+");
//                }
//            }
//        }
    }

    /**
     * 完成编辑模式
     *
     * @param parent
     */
    private void cancelEditMode(RecyclerView parent) {
        isEditMode = false;
        notifyDataSetChanged();
        //notifyItemRangeChanged(0,getItemCount(),isEditMode);

//        int visibleChildCount = parent.getChildCount();
//        for (int i = 0; i < visibleChildCount; i++) {
//            View view = parent.getChildAt(i);
//            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
//            if (imgEdit != null) {
//                imgEdit.setVisibility(View.GONE);
//            }
//
//            TextView plus = (TextView)view.findViewById(R.id.plus);
//            if(plus != null){
//                plus.setVisibility(View.GONE);
//            }
//        }
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
                        //notifyItemChanged(otherPos);
                        return otherPos;
                    }
                }
            }
            otherPos += entry.tags.size() + 1;
        }
        return -1;
    }

    /**
     * 我的频道 移动到 其他频道
     *
     * @param myHolder
     */
//    private void moveMyToOther(MyViewHolder myHolder) {
//        int position = myHolder.getAdapterPosition();
//
//        Tag myTag = removeMyTag(position);
//        if (myTag == null) {
//            return;
//        }
//        int otherPos = myTagsSize + 1;
//        for (int i = 0; i < mAllTagsEntries.size(); i++) {
//            TagsEntry entry = mAllTagsEntries.get(i);
//            if (entry.viewType == myTag.viewType) {
//                for (int j = 0; j < entry.tags.size(); j++) {
//                    if (myTag.id == entry.tags.get(j).id) {
//                        otherPos += j + 1;
//                        entry.tags.get(j).isDisable = false;
//                        notifyItemChanged(otherPos);
//                        break;
//                    }
//                }
//            }
//            otherPos += entry.tags.size() + 1;
//        }
//
//    }

    /**
     * 其他频道 移动到 我的频道
     *
     * @param otherHolder
     */
//    private void moveOtherToMy(OtherViewHolder otherHolder, RecyclerView recyclerView) {
//        int position = otherHolder.getAdapterPosition();
//        Tag otherTag = getOtherTag(position);
//        if (otherTag.isDisable) {
//            return;
//        }
//        otherTag.isDisable = true;
//        notifyItemChanged(position);
//
//        addMyTag(recyclerView, otherTag);
//    }


    /**
     * 我的频道
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private TextView textView;
        private ImageView imgEdit;
        public boolean isResident = false;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
            imgEdit = (ImageView) itemView.findViewById(R.id.img_edit);
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
        }
    }

    /**
     * 其他频道
     */
    class OtherViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private TextView plus;
        public boolean isDisable = false;

        public OtherViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
            plus = (TextView) itemView.findViewById(R.id.plus);
        }
    }

    /**
     * 我的频道  标题部分
     */
    class MyTagHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBtnEdit;
        private TextView tips;

        public MyTagHeaderViewHolder(View itemView) {
            super(itemView);
            tvBtnEdit = (TextView) itemView.findViewById(R.id.tv_btn_edit);
            tips = (TextView) itemView.findViewById(R.id.tips);
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
}
