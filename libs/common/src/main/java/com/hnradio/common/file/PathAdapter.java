package com.hnradio.common.file;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hnradio.common.R;
import com.hnradio.common.util.FileUtils;

import java.util.List;


public class PathAdapter extends BaseQuickAdapter<UploadFileInfoBean, BaseViewHolder>
{

    public PathAdapter(@Nullable List<UploadFileInfoBean> data)
    {
        super(R.layout.lfile_listitem, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UploadFileInfoBean item)
    {
        switch (item.type)
        {
            case TYPE_JPEG:
            case TYPE_PNG:
                helper.setImageResource(R.id.iv_type, R.drawable.ic_lfile_img);
                break;
            case TYPE_DOC:
            case TYPE_PDF:
                helper.setImageResource(R.id.iv_type, R.drawable.ic_lfile_doc);
                break;
            case TYPE_DIR:
                helper.setImageResource(R.id.iv_type, R.drawable.ic_lfile_dir);
                break;
            case TYPE_FILE:
                helper.setImageResource(R.id.iv_type, R.drawable.ic_lfile_doc);
        }
        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_detail, "文件大小：" + FileUtils.INSTANCE.getFileSize(item.size));
    }
}