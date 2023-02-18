package com.hnradio.common.file;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gyf.immersionbar.ImmersionBar;
import com.hnradio.common.R;
import com.hnradio.common.adapter.AdapterHelper;
import com.hnradio.common.util.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FilePickerActivity extends AppCompatActivity implements View.OnClickListener
{

    TextView mTvPath;
    TextView mTvBack;
    RecyclerView mRecylerView;

    private String mPath;
    private String rootPath;

    private List<UploadFileInfoBean> mListFiles = new ArrayList<>();
    private PathAdapter mPathAdapter;

    private ConfigParam mParamEntity;
    private FileFilter mFilter;

    private View ivBack;
    private TextView titleTv;
    private ViewGroup toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lfile_picker);
        ImmersionBar.with(this).titleBar(toolbar)
                .statusBarColor(R.color.white)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true).init();

        findView();
        mParamEntity = getIntent().getParcelableExtra("param");
        mPath = mParamEntity.path;
        rootPath = mParamEntity.path;
        mTvPath.setText(mPath);


        mFilter = new FileFilter(mParamEntity.fileTypes);

        if(!TextUtils.isEmpty(mPath))
        {
            cdIn(mPath);
        }
    }

    private void findView() {
        ivBack = findViewById(R.id.backTv);
        titleTv = findViewById(R.id.titleTv);
        titleTv.setText("文件选择");
        toolbar = findViewById(R.id.titleTopLayout);
        mTvPath = findViewById(R.id.tv_path);
        mTvBack = findViewById(R.id.tv_back);
        mRecylerView = findViewById(R.id.recylerview);

        mTvBack.setOnClickListener(this);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configAdapter()
    {
        if(mPathAdapter == null)
        {
            mPathAdapter = new PathAdapter(mListFiles);
//            mRecylerView.setLayoutManager(new LinearLayoutManager(this));
            AdapterHelper.INSTANCE.configListAdapter(mRecylerView, mPathAdapter, RecyclerView.VERTICAL, 1, Color.TRANSPARENT, 0);
            mPathAdapter.setOnItemClickListener((adapter, view, position) ->
            {
                UploadFileInfoBean bean = mListFiles.get(position);
                if(bean.type == FILE_TYPE.TYPE_DIR)
                {
                    cdIn(bean.path);
                }else
                {
                    chooseDone(bean);
                }
            });
        }else
        {
            mPathAdapter.notifyDataSetChanged();
        }
    }

    private void cdIn(String path)
    {
        mPath = path;
        setShowPath(mPath);
        new Thread(){
            @Override
            public void run()
            {
                mListFiles.clear();
                List<File> files = FileUtils.INSTANCE.listDirFiles(mPath, mFilter);
                if(files != null && files.size() != 0)
                {
                    for(File f : files)
                    {
                        UploadFileInfoBean i = new UploadFileInfoBean();
                        String fileName = f.getName().toLowerCase();
                        if(f.isDirectory())
                        {
                            i.type = FILE_TYPE.TYPE_DIR;
                        }else if(fileName.endsWith(".jpg")
                        || fileName.endsWith(".jpeg"))
                        {
                            i.type = FILE_TYPE.TYPE_JPEG;
                        }else if(fileName.endsWith(".png"))
                        {
                            i.type = FILE_TYPE.TYPE_PNG;
                        }else if(fileName.endsWith(".doc")
                        || fileName.endsWith(".docx"))
                        {
                            i.type = FILE_TYPE.TYPE_DOC;
                        }else if(fileName.endsWith(".pdf"))
                        {
                            i.type = FILE_TYPE.TYPE_PDF;
                        }else{
                            i.type = FILE_TYPE.TYPE_FILE;
                        }
                        i.size = f.length();
                        i.path = f.getAbsolutePath();
                        i.name = f.getName();
                        mListFiles.add(i);
                    }
                }
                runOnUiThread(() -> {
                    mRecylerView.scrollToPosition(0);
                    configAdapter();
                });
            }
        }.start();
    }

    /**
     * 显示顶部地址
     *
     * @param path
     */
    private void setShowPath(String path)
    {
        mTvPath.setText(path);
    }

    /**
     * 完成提交
     */
    private void chooseDone(UploadFileInfoBean bean)
    {
        Intent intent = new Intent();
        intent.putExtra("item", bean.copy());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_back){
            String tempPath = new File(mPath).getParent();
            if (tempPath == null
                    || rootPath.equals(mPath))
            {
                return;
            }
            cdIn(tempPath);
        }
    }
}
