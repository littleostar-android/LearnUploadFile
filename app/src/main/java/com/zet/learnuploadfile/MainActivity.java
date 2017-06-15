package com.zet.learnuploadfile;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "MainActivity";

    private final Handler mHandler = new Handler();

    private Button mBtnPostFile;
    private TextView mTxtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        actionUI();
    }

    private void actionUI()
    {
        Toast.makeText(getApplicationContext(), "开始 上传", Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mBtnPostFile.performClick();
            }
        }, 1);
    }

    private void initView()
    {
        mBtnPostFile = (Button) findViewById(R.id.mBtnPostFile);

        mBtnPostFile.setOnClickListener(this);
        mTxtInfo = (TextView) findViewById(R.id.mTxtInfo);
        mTxtInfo.setOnClickListener(this);
    }

    /**
     * 6.0权限监听
     * 回调监听。
     */
    private PermissionListener permissionListener = new PermissionListener()
    {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions)
        {
            switch (requestCode)
            {
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions)
        {
            switch (requestCode)
            {
            }
        }
    };

    /**
     * 上传文件
     */
    private void postFile()
    {
        // 是否已经获取文件读写权限？
        boolean hasPermission = AndPermission.hasPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // 是
        if (hasPermission)
        {
            // 上传文件
            try
            {
                // 获取手机存储根目录
                String canonicalPath = Environment.getExternalStorageDirectory().getCanonicalPath();
                // 上传地址
                String url = "http://192.168.137.1:8080/okhttp_server/postFile";
                // 本地图片地址
                File file = new File(canonicalPath, "cc.jpg");

                // 打印文件长度
                long length = file.length();
                Log.e(TAG, "postFile: " + length);

                // 如果 文件存在 并且 长度大于0
                if (file.exists() && length > 0)
                {
                    Log.e(TAG, "postFile: " + file.getAbsolutePath());
                    //
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    // 参数存放文件
                    // 这里的 "file", 和服务器参数对应
                    params.put("file", file);
                    // 上传...
                    client.post(url, params, new AsyncHttpResponseHandler()
                    {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
                        {
                            Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_LONG).show();
                            mTxtInfo.setText("success");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
                        {
                            Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_LONG).show();
                            mTxtInfo.setText("fail");
                        }
                    });
                } else
                {
                    Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();
                    mTxtInfo.setText("file not exist");
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.mBtnPostFile:
                requestPermission();
                postFile();
                break;
        }
    }

    /**
     * 请求权限
     */
    private void requestPermission()
    {
        // 申请单个权限。
        AndPermission.with(this)
                .requestCode(100)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback(permissionListener)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框；
                // 这样避免用户勾选不再提示，导致以后无法申请权限。
                // 你也可以不设置。

                .rationale(new RationaleListener()
                {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale)
                    {
                        // 这里的对话框可以自定义，只要调用rationale.resume()就可以继续申请。
                        AndPermission.rationaleDialog(MainActivity.this, rationale).
                                show();
                    }
                })
                .start();
    }
}
