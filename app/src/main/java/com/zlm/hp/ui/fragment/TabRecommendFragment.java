package com.zlm.hp.ui.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zlm.hp.R;
import com.zlm.hp.adapter.RecommendAdapter;
import com.zlm.hp.net.api.RankListHttpUtil;
import com.zlm.hp.net.entity.RankListResult;
import com.zlm.hp.net.model.HttpResult;

import java.util.ArrayList;
import java.util.Map;

import base.utils.ThreadUtil;
import base.utils.ToastUtil;

/**
 * @Description: tab推荐界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:42
 * @Throws:
 */
public class TabRecommendFragment extends BaseFragment {

    /**
     * 列表视图
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    /**
     * 是否已加载数据
     */
    private boolean isLoadData = false;
    //
    private RecommendAdapter mAdapter;
    private ArrayList<RankListResult> mDatas;
    private Runnable runnable;

    public TabRecommendFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_recommend;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

        //
        mSwipeRefreshLayout = mainView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));

        //
        mDatas = new ArrayList<RankListResult>();
        mAdapter = new RecommendAdapter(mActivity, mDatas);
        mRecyclerView.setAdapter(mAdapter);
        //
        showLoadingView();

        setRefreshListener(new RefreshListener() {
            @Override
            public void refresh() {
                showLoadingView();

                RankListHttpUtil.cancel();
                loadDataUtil(0);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RankListHttpUtil.cancel();
                mDatas.clear();
                loadDataUtil(0);
            }
        });
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isLoadData) {
            if (isRestoreInstance) {
                mDatas.clear();
            }
            loadDataUtil(0);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        if (isVisibleToUser && !isLoadData) {
            isLoadData = true;
            loadDataUtil(0);
        }
    }

    /**
     * 加载数据
     */
    private void loadDataUtil(final int sleepTime) {
        //
        runnable = new Runnable() {
            @Override
            public void run() {
                final HttpResult httpResult = RankListHttpUtil.rankList(mActivity);
                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (httpResult.getStatus() == HttpResult.STATUS_NONET) {
                            showNoNetView(R.string.current_network_not_available);
                        } else if (httpResult.getStatus() == HttpResult.STATUS_NOWIFI) {
                            showNoNetView(R.string.current_network_not_wifi_close_only_wifi_mode);
                        } else if (httpResult.getStatus() == HttpResult.STATUS_SUCCESS) {

                            //
                            Map<String, Object> returnResult = (Map<String, Object>) httpResult.getResult();

                            ArrayList<RankListResult> datas = (ArrayList<RankListResult>) returnResult.get("rows");
                            if (datas.size() == 0) {
                                mAdapter.setState(RecommendAdapter.NODATA);
                            } else {
                                for (int i = datas.size() - 1; i >= 0; i--) {
                                    mDatas.add(0, datas.get(i));
                                }
                                mAdapter.setState(RecommendAdapter.NOMOREDATA);
                            }

                            mSwipeRefreshLayout.setRefreshing(false);
                            mAdapter.notifyDataSetChanged();
                            showContentView();
                        } else {
                            final String errorMsg =  httpResult.getErrorMsg();
                            mSwipeRefreshLayout.setRefreshing(false);
                            showContentView();
                            ToastUtil.showTextToast(mActivity.getApplicationContext(),errorMsg);

                        }
                    }
                });
            }
        };
        ThreadUtil.runInThread(runnable);
    }

    @Override
    public void onDestroy() {
        if(runnable != null) {
            ThreadUtil.cancelThread(runnable);
            runnable = null;
        }
        super.onDestroy();
    }


    @Override
    protected int setTitleViewId() {
        return 0;
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }
}