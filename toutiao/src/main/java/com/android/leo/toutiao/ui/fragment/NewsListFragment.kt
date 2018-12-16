package com.android.leo.toutiao.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.agile.android.leo.exception.ApiException
import com.agile.android.leo.utils.ListUtils
import com.agile.android.leo.utils.LogUtils
import com.android.leo.base.showToast
import com.android.leo.base.ui.fragments.BaseFragment
import com.android.leo.toutiao.Constant
import com.android.leo.toutiao.R
import com.android.leo.toutiao.mvp.contract.NewsListContract
import com.android.leo.toutiao.mvp.model.NewsListModel
import com.android.leo.toutiao.mvp.model.entity.News
import com.android.leo.toutiao.mvp.presenter.NewsListPresenter
import com.android.leo.toutiao.ui.adapter.NewsListAdapter
import kotlinx.android.synthetic.main.fragment_news_list.*
import java.util.*

class NewsListFragment : BaseFragment<NewsListPresenter>(), NewsListContract.View {

    private val mNewsList = ArrayList<News>()
    private var mChannelCode: String? = null
    private var isVideoList: Boolean? = false
    /**
     * 是否是推荐频道
     */
    private var isRecommendChannel: Boolean = false
    protected var mNewsAdapter: NewsListAdapter? = null
    private val linearLayoutManager by lazy {
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun resultError(exception: ApiException) {
        showToast(exception.message!!)
    }

    override fun showLoading() {
        multipleStatusView?.showLoading()
    }

    override fun dismissLoading() {
        multipleStatusView?.showContent()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_news_list
    }

    override fun initPresenter() {
        mPresenter = NewsListPresenter(NewsListModel(), this)
    }

    override fun configViews() {
        LogUtils.w("NewsListFragment configViews")
        multipleStatusView = feedMultipleStatusView
        multipleStatusView?.showLoading()
        mNewsAdapter = NewsListAdapter(mChannelCode!!, ArrayList())
        mNewsAdapter?.setData(mNewsList)
        mRecyclerView.layoutManager = linearLayoutManager
        mRecyclerView.adapter = mNewsAdapter
    }

    override fun initData(savedInstanceState: Bundle?) {
        mChannelCode = arguments?.getString(Constant.CHANNEL_CODE)
        isVideoList = arguments?.getBoolean(Constant.IS_VIDEO_LIST, false)
        val channelCodes = resources.getStringArray(R.array.channel_code)
        isRecommendChannel = mChannelCode == channelCodes[0]//是否是推荐频道
    }

    override fun requestData() {
        mChannelCode?.let {
            mPresenter?.requestNewsList(it)
        }
    }

    /**
     * 处理置顶新闻和广告重复
     */
    private fun dealRepeat(newList: MutableList<News>) {
        if (isRecommendChannel && !ListUtils.isEmpty(mNewsList)) {
            //如果是推荐频道并且数据列表已经有数据,处理置顶新闻或广告重复的问题
            mNewsList.removeAt(0)//由于第一条新闻是重复的，移除原有的第一条
            //新闻列表通常第4个是广告,除了第一次有广告，再次获取的都移除广告
            if (newList.size >= 4) {
                val fourthNews = newList[3]
                //如果列表第4个和原有列表第4个新闻都是广告，并且id一致，移除
                if (fourthNews.tag.equals(Constant.ARTICLE_GENRE_AD)) {
                    newList.remove(fourthNews)
                }
            }
        }
    }

    override fun onGetNewsListSuccess(newList: ArrayList<News>, tipInfo: String) {
        //如果是第一次获取数据
        if (ListUtils.isEmpty(mNewsList)) {
            if (ListUtils.isEmpty(newList)) {
                //获取不到数据,显示空布局
                multipleStatusView?.showEmpty()
                return
            }
        }
        if (ListUtils.isEmpty(newList)) {
            //已经获取不到新闻了，处理出现获取不到新闻的情况
            showToast(getString(R.string.no_news_now))
            return
        }
        if (TextUtils.isEmpty(newList[0].title)) {
            //由于汽车、体育等频道第一条属于导航的内容，所以如果第一条没有标题，则移除
            newList.removeAt(0)
        }
        dealRepeat(newList)//处理新闻重复问题
        mNewsList.addAll(0, newList)
        mNewsAdapter?.addItemData(mNewsList)
    }

}