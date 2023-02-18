package com.hnradio.common.widget.bottomDialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hnradio.common.R
import com.hnradio.common.adapter.CommentItemAdapter
import com.hnradio.common.base.BaseActivity
import com.hnradio.common.base.BaseDialog
import com.hnradio.common.base.CommonDialog
import com.hnradio.common.http.IronFansLifeApiUtil
import com.hnradio.common.http.ProgramApiUtil
import com.hnradio.common.http.bean.CommentBean
import com.hnradio.common.http.bean.CommentListResBean
import com.hnradio.common.manager.UserManager
import com.hnradio.common.model.IronFansLifeModel
import com.hnradio.common.model.ProgramModel
import com.hnradio.common.router.RouterUtil
import com.hnradio.common.util.FormatUtil
import com.hnradio.common.util.TimeUtils
import com.hnradio.common.util.ToastUtils
import com.hnradio.common.widget.roundedimageview.RoundedImageView
import com.hnradio.common.widget.roundedimageview.TitleAvatarView
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import org.json.JSONObject


/**
 *  二级评论
 * created by qiaoyan on 2021/8/11
 */
class SubCommentDialog(private val mContext: Context, val type: Int, val data: CommentBean) :
    Dialog(mContext, R.style.CustomDialog) {

    private var pageIndex = -1
    private val pageSize = 10
    private lateinit var commentAdapter: CommentItemAdapter

    private var lifeModel: IronFansLifeModel? = null
    private var programModel: ProgramModel? = null
    private lateinit var lifeObserve: Observer<CommentListResBean>
    private lateinit var programObserve: Observer<CommentListResBean>

    private var srlLoadMore: SmartRefreshLayout? = null
    private var tvRelyNum: TextView? = null


    companion object {
        const val TYPE_IRON_FANS_LIFE = 0 //铁粉生活评论  小视频\用户图文
        const val TYPE_PROGRAM = 1 //节目评论  音频\视频\平台图文
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog_sub_comment)

        window?.setGravity(Gravity.BOTTOM)
        window?.setWindowAnimations(R.style.Comment_Dialog_Anim_Style)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        tvRelyNum = findViewById(R.id.tv_reply_num)
        val rivPortrait = findViewById<TitleAvatarView>(R.id.riv_portrait)
        val tvName = findViewById<TextView>(R.id.tv_name)
        val tvDate = findViewById<TextView>(R.id.tv_date)
        val tvComment = findViewById<TextView>(R.id.tv_comment)
        val rvComment = findViewById<RecyclerView>(R.id.rv_recycler)
        val tvCommentPost = findViewById<TextView>(R.id.tv_comment_post)
        srlLoadMore = findViewById(R.id.srl_refresh)

        //填充一级评论
        totalNum=data.replyNum
        tvRelyNum?.text = "回复(${data.replyNum})"

//        Glide.with(context).load(data.headImageUrl).into(rivPortrait)
        rivPortrait.setImages(data.headImageUrl, data.levelImageUrl)

        tvName.text = data.nickName
        tvDate.text = TimeUtils.millis2String(data.createTime)
        tvComment.text = data.text
        tvComment.movementMethod = ScrollingMovementMethod.getInstance()
        //初始化model
        when (type) {
            TYPE_IRON_FANS_LIFE -> {
                lifeModel =
                    ViewModelProvider(mContext as BaseActivity<*, *>).get(IronFansLifeModel::class.java)
            }
            TYPE_PROGRAM -> {
                programModel =
                    ViewModelProvider(mContext as BaseActivity<*, *>).get(ProgramModel::class.java)
            }
        }
        //评论加载更多
        srlLoadMore?.apply {
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context))
            setOnRefreshListener {
                commentAdapter.data.clear()
                pageIndex = 1
                requestCommentList()
            }
            setOnLoadMoreListener {
                requestCommentList()
            }
        }
        //初始化评论内容
        rvComment.apply {
            commentAdapter = CommentItemAdapter(ArrayList())
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            commentAdapter.setOnItemClickListener { _, view, position ->
                commentPop(commentAdapter.data[position], false)
            }
            commentAdapter.addChildClickViewIds(R.id.tv_del_comment, R.id.riv_portrait)
            commentAdapter.setOnItemChildClickListener { _, view, position ->
                if(view.id == R.id.tv_del_comment){
                    CommonDialog.Builder(context)
                        .setCaceledable(true)
                        .setTitle("提示")
                        .setContent("确定删除本条评论吗？")
                        .setPositiveBtn("确定", object :
                            BaseDialog.BaseDialogClickListener.OnActiconListener {
                            override fun onClick() {
                                delComment(commentAdapter.data[position].id)
                            }
                        })
                        .setNegativeBtn("取消", null)
                        .build().show()
                }else if(view.id == R.id.riv_portrait){
                    RouterUtil.gotoAnchorHomepage(commentAdapter.data[position].userId)
                }
            }
            commentAdapter.onChildDelListener = object : CommentItemAdapter.OnChildDelCommentListener{
                override fun onDel(data: CommentBean) {
                    CommonDialog.Builder(context)
                        .setCaceledable(true)
                        .setTitle("提示")
                        .setContent("确定删除本条评论吗？")
                        .setPositiveBtn("确定", object :
                            BaseDialog.BaseDialogClickListener.OnActiconListener {
                            override fun onClick() {
                                delComment(data.id)
                            }
                        })
                        .setNegativeBtn("取消", null)
                        .build().show()
                }
            }
        }
        //注册返回数据
        programObserve = Observer<CommentListResBean> { it -> updateCommentList(it!!) }
        programModel?.subCommentListData?.observe(mContext as BaseActivity<*, *>, programObserve)

        lifeObserve = Observer<CommentListResBean> { it -> updateCommentList(it!!) }
        lifeModel?.subCommentListData?.observe(mContext as BaseActivity<*, *>, lifeObserve)

        //进行评论
        tvCommentPost.setOnClickListener {
            commentPop(data, true)
        }
        programModel?.postCommentData?.observe(mContext as BaseActivity<*, *>) {
            commentPostDialog?.dismiss()
            ToastUtils.show(it)
            commentAdapter.data.clear()
            pageIndex = 1
            //请求评论信息
            requestCommentList()
        }
        lifeModel?.postCommentData?.observe(mContext as BaseActivity<*, *>) {
            commentPostDialog?.dismiss()
            ToastUtils.show(it)
            commentAdapter.data.clear()
            pageIndex = 1
            //请求评论信息
            requestCommentList()
        }
        //第一次请求
        pageIndex = 1
        requestCommentList()
    }


    private fun delComment(id : Int){
        val reqObjet = JSONObject()
        reqObjet.put("commentId", id)
        when (type) {
            TYPE_IRON_FANS_LIFE -> {
                IronFansLifeApiUtil.delCommentLife(id, {
                    ToastUtils.show("删除成功")
                    //第一次请求
                    pageIndex = 1
                    requestCommentList()
                }, {

                })
            }
            TYPE_PROGRAM -> {
                ProgramApiUtil.delCommentAlbum(id, {
                    ToastUtils.show("删除成功")
                    //第一次请求
                    pageIndex = 1
                    requestCommentList()
                }, {

                })
            }
        }
    }

    private var commentPostDialog : CommentPostDialog? = null
    private fun commentPop(parent : CommentBean, isSecondLevel : Boolean) {
        commentPostDialog = CommentPostDialog(mContext, object : CommentPostDialog.OnSendClickListener {
                override fun onSendClick(text: String) {
                    if (!TextUtils.isEmpty(text)) {
                        val reqObjet = JSONObject()
//                        parentId（一级评论时固定0，其他评论时都设定为一级评论ID）
//                        targetCommentId（一级评论时为0，其他评论时为回复目标评论的ID）
//                        targetUserId（一级、二级评论时为0，其他评论时为回复目标评论的用户ID）
                        reqObjet.put("parentId", data.id)
                        reqObjet.put("targetCommentId", parent.id)
                        reqObjet.put("targetUserId", if(isSecondLevel) 0 else parent.userId)
                        reqObjet.put("text", text)
                        reqObjet.put("userId", UserManager.getLoginUser()?.id)
//                        reqObjet.put("parentId", parent.id)
//                        reqObjet.put("text", text)
//                        reqObjet.put("userId", UserManager.getLoginUser()?.id)

                        when (type) {
                            TYPE_IRON_FANS_LIFE -> {
                                reqObjet.put("tfLifeId", parent.tfLifeId)
                                lifeModel?.postCommentLife(reqObjet.toString())
                            }
                            TYPE_PROGRAM -> {
                                reqObjet.put("albumDetailId", parent.albumDetailId)
                                programModel?.postComment(reqObjet.toString())
                            }
                        }
                    } else
                        ToastUtils.show("请输入评论")
                }
            })
        commentPostDialog?.show()
    }

    //当点击
    override fun cancel() {
        super.cancel()
        programModel?.subCommentListData?.removeObserver(programObserve)
        lifeModel?.subCommentListData?.removeObserver(lifeObserve)
    }

    /**
     * 请求专辑节目的评论
     */
    private fun requestCommentList() {
        val reqObjet = JSONObject()
        reqObjet.put("pageIndex", pageIndex)
        reqObjet.put("pageSize", pageSize)
        reqObjet.put("parentId", data.id)
        when (type) {
            TYPE_IRON_FANS_LIFE -> {
                reqObjet.put("tfLifeId", data.tfLifeId)
                lifeModel?.getSubCommentListLife(reqObjet.toString())
            }
            TYPE_PROGRAM -> {
                reqObjet.put("albumDetailId", data.albumDetailId)
                programModel?.getSubCommentList(reqObjet.toString())
            }
        }
    }

    var totalNum=0
    /**
     * 更新评论列表
     */
    private fun updateCommentList(it: CommentListResBean) {
        if(pageIndex > 0){//规避第二次进入dialog后  observer会自动执行一次的bug
            tvRelyNum?.text = "回复(${it.total})"
            totalNum=it.total
            //组装数据
            when (it.current) {
                1 -> commentAdapter.setList(it.records)
                else -> commentAdapter.addData(it.records)
            }
            //更新状态
            if (srlLoadMore!!.isRefreshing)
                srlLoadMore?.finishRefresh()
            if (srlLoadMore!!.isLoading)
                srlLoadMore?.finishLoadMore()
            if (pageIndex <= it.pages)
                pageIndex++
            else if (it.current > 1)
                ToastUtils.show("已加载全部")
        }
    }


}