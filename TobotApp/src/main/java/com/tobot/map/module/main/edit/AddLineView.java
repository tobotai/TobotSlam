package com.tobot.map.module.main.edit;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.slam.data.LocationBean;

import java.util.List;

/**
 * 添加虚拟轨道
 *
 * @author houdeming
 * @date 2020/5/9
 */
public class AddLineView extends LinearLayout {
    private PointAdapter mPointAdapter;

    public AddLineView(Context context) {
        this(context, null);
    }

    public AddLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_line_add, this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_num);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(context, ItemSplitLineDecoration.VERTICAL, true));
        mPointAdapter = new PointAdapter(context, R.layout.recycler_item_num);
        recyclerView.setAdapter(mPointAdapter);
    }

    public void init(List<LocationBean> data, BaseRecyclerAdapter.OnItemClickListener<LocationBean> listener) {
        setVisibility(VISIBLE);
        if (mPointAdapter != null) {
            mPointAdapter.setOnItemClickListener(listener);
            mPointAdapter.setData(data);
        }
    }

    public void remove() {
        setVisibility(GONE);
    }
}
