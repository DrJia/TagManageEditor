package com.jiabin.playlistmgrtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.jiabin.playlistmgrtest.entry.Tag;
import com.jiabin.playlistmgrtest.entry.TagsEntry;
import com.jiabin.playlistmgrtest.helper.ItemDragHelperCallback;
import com.jiabin.playlistmgrtest.helper.TagItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecy = (RecyclerView) findViewById(R.id.recy);
        init();
    }

    private void init() {
        final List<Tag> items = new ArrayList<>();
        Tag rec = new Tag();
        rec.id = 0;
        rec.name = "推荐";
        rec.isResident = true;
        Tag jp = new Tag();
        jp.id = 1;
        jp.name = "精品";
        jp.isResident = true;
        items.add(rec);
        items.add(jp);
        for (int i = 0; i < 3; i++) {
            Tag tag = new Tag();
            tag.name = "语种" + i;
            tag.id = 1000 + i;
            tag.viewType = 1;
            items.add(tag);
        }


        TagsEntry lagTagsEntry = new TagsEntry();
        final List<Tag> lagItems = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Tag tag = new Tag();
            tag.name = "语种" + i;
            tag.id = 1000 + i;
            tag.viewType = 1;
            lagItems.add(tag);
        }
        lagTagsEntry.tags = lagItems;
        lagTagsEntry.category = "语种";
        lagTagsEntry.viewType = 1;

        TagsEntry styTagsEntry = new TagsEntry();
        final List<Tag> styItems = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            Tag tag = new Tag();
            tag.name = "风格风格" + i;
            tag.id = 2000 + i;
            tag.viewType = 2;
            styItems.add(tag);
        }
        styTagsEntry.tags = styItems;
        styTagsEntry.category = "风格";
        styTagsEntry.viewType = 2;

        TagsEntry sceTagsEntry = new TagsEntry();
        final List<Tag> sceItems = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Tag tag = new Tag();
            tag.name = "场景场景场景" + i;
            tag.id = 3000 + i;
            tag.viewType = 3;
            sceItems.add(tag);
        }
        sceTagsEntry.tags = sceItems;
        sceTagsEntry.category = "场景";
        sceTagsEntry.viewType = 3;

        List<TagsEntry> entryList = new ArrayList<>();
        entryList.add(lagTagsEntry);
        entryList.add(styTagsEntry);
        entryList.add(sceTagsEntry);

        GridLayoutManager manager = new GridLayoutManager(this, 4);
        mRecy.setLayoutManager(manager);
        mRecy.addItemDecoration(new TagItemDecoration(30));
        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        final ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecy);

        final TagsMgrAdapter adapter = new TagsMgrAdapter(this, helper,4, 30 , items, entryList);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                return viewType == TagsMgrAdapter.TYPE_MY || viewType == TagsMgrAdapter.TYPE_OTHER ? 1 : 4;
            }
        });
        mRecy.setAdapter(adapter);

//        adapter.setOnMyChannelItemClickListener(new ChannelAdapter.OnMyChannelItemClickListener() {
//            @Override
//            public void onItemClick(View v, int position) {
//                Toast.makeText(ChannelActivity.this, items.get(position).getName(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
