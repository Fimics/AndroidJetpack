package com.hnradio.common.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FilePicker
{

    private Activity mActivity;
    private Fragment mSupportFragment;
    private int mRequestCode;
    private String[] mFileTypes;
    private String mStartPath;

    /**
     * 绑定Activity
     *
     * @param activity
     * @return
     */
    public FilePicker withActivity(Activity activity) {
        this.mActivity = activity;
        return this;
    }

    public FilePicker withFragment(Fragment frag) {
        this.mSupportFragment = frag;
        return this;
    }

    /**
     * 请求码
     *
     * @param requestCode
     * @return
     */
    public FilePicker withRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    public FilePicker withFileFilter(String[] arrs) {
        this.mFileTypes = arrs;
        return this;
    }

    /**
     * 设置初始显示路径
     *
     * @param path
     * @return
     */
    public FilePicker withStartPath(String path) {
        this.mStartPath = path;
        return this;
    }

    public void start() {
        if (mActivity == null && mSupportFragment == null) {
            throw new RuntimeException("You must pass Activity or Fragment by withActivity or withFragment or withSupportFragment method");
        }
        Intent intent = initIntent();
        Bundle bundle = getBundle();
        intent.putExtras(bundle);

        if (mActivity != null) {
            mActivity.startActivityForResult(intent, mRequestCode);
        } else {
            mSupportFragment.startActivityForResult(intent, mRequestCode);
        }
    }

    private Intent initIntent() {
        Intent intent;
        if (mActivity != null) {
            intent = new Intent(mActivity, FilePickerActivity.class);
        } else {
            intent = new Intent(mSupportFragment.getActivity(), FilePickerActivity.class);
        }
        return intent;
    }

    @NonNull
    private Bundle getBundle() {
        ConfigParam paramEntity = new ConfigParam();
        paramEntity.fileTypes = mFileTypes;
        paramEntity.mutilyMode = false;
        paramEntity.path = mStartPath;
        Bundle bundle = new Bundle();
        bundle.putParcelable("param", paramEntity);
        return bundle;
    }
}
