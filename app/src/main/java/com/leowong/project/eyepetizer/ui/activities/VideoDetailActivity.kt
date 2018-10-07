package com.leowong.project.eyepetizer.ui.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.agile.android.leo.exception.ApiException
import com.lasingwu.baselibrary.ImageLoader
import com.lasingwu.baselibrary.ImageLoaderOptions
import com.leowong.project.eyepetizer.R
import com.leowong.project.eyepetizer.base.BaseActivity
import com.leowong.project.eyepetizer.mvp.contract.VideoDetailContract
import com.leowong.project.eyepetizer.mvp.model.VideoDetailModel
import com.leowong.project.eyepetizer.mvp.model.entity.HomeBean
import com.leowong.project.eyepetizer.mvp.presenter.VideoDetailPresenter
import com.leowong.project.eyepetizer.ui.adapters.VideoDetailAdapter
import com.leowong.project.eyepetizer.ui.view.widgets.VideoDetailMediaControlView
import com.leowong.project.eyepetizer.utils.StatusBarUtils
import kotlinx.android.synthetic.main.activity_video_detail.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class VideoDetailActivity : BaseActivity<VideoDetailPresenter>(), VideoDetailContract.View {
    protected var videoDetailAdapter: VideoDetailAdapter? = null
    protected var videoDetailMediaControlView: VideoDetailMediaControlView? = null

    private val linearLayoutManager by lazy {
        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun setVideo(url: String) {
        ijkvideo.setVideoPath(url)
        ijkvideo.startPlay()
    }

    override fun setVideoInfo(itemInfo: HomeBean.Issue.Item) {
        videoDetailAdapter?.addItemData(itemInfo)
    }

    override fun setBackground(url: String) {
        val imageLoaderOptions = ImageLoaderOptions.Builder(mVideoBackground, url).isCrossFade(true).build()
        ImageLoader.showImage(imageLoaderOptions)
    }

    override fun setRecentRelatedVideo(itemList: ArrayList<HomeBean.Issue.Item>) {
    }

    override fun setErrorMsg(errorMsg: String) {
    }

    override fun resultError(exception: ApiException) {
    }

    override fun showLoading() {
        multipleStatusView?.showLoading()
    }

    override fun dismissLoading() {
        multipleStatusView?.showContent()
    }

    /**
     * Item 详细数据
     */
    private lateinit var itemData: HomeBean.Issue.Item

    companion object {
        val BUNDLE_VIDEO_DATA = "video_data"
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_video_detail
    }

    override fun initPresenter() {
        mPresenter = VideoDetailPresenter(VideoDetailModel(), this)
    }

    override fun onPause() {
        super.onPause()
        ijkvideo.onPause()
    }

    override fun onBackPressed() {
        ijkvideo?.let {
            if (it.isFullScreen) {
                it.toggleFullScreen()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        ijkvideo.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        ijkvideo.onDestory()
    }

    override fun configViews() {
        StatusBarUtils.with(this).init()
        initSlide()
        multipleStatusView = videoDetailMultipleStatusView
        mRecyclerView.layoutManager = linearLayoutManager
        videoDetailAdapter = VideoDetailAdapter(ArrayList())
        mRecyclerView.adapter = videoDetailAdapter
        initVideoControl()
    }

    fun initVideoControl() {
        videoDetailMediaControlView = VideoDetailMediaControlView(this)
        videoDetailMediaControlView?.setMediaControl(ijkvideo)
        val cover = itemData.data?.cover?.feed
        cover?.let {
            videoDetailMediaControlView?.setVideoCover(it)
        }
        videoDetailMediaControlView?.setVideoTitle(itemData.data!!.title)
        ijkvideo?.addMediaPlayerListener(videoDetailMediaControlView!!)
        ijkvideo?.attachMediaControl(videoDetailMediaControlView!!)
    }

    override fun initData(savedInstanceState: Bundle?) {
        itemData = intent.getSerializableExtra(BUNDLE_VIDEO_DATA) as HomeBean.Issue.Item
        // init player
        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.native_profileBegin("libijkplayer.so")
    }

    override fun requestData() {
        mPresenter?.loadVideoInfo(itemData)
    }
}