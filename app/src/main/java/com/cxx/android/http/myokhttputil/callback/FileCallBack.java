package com.cxx.android.http.myokhttputil.callback;


import com.cxx.android.http.myokhttputil.util.HttpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by zhy on 15/12/15.
 *
 * 需要注意的是，文件的总长度不是总能返回回来的，如果total值为0，那么回掉本身是不能用的
 */
public abstract class FileCallBack implements okhttp3.Callback {
    /**
     * 目标文件存储的文件夹路径
     */
    private String destFileDir;
    /**
     * 目标文件存储的文件名
     */
    private String destFileName;

    public abstract void inProgress(float progress, long total, boolean completed);

    public abstract void onFailed(Exception e);

    public FileCallBack(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        onFailed(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        saveFile(response);
    }

    public File saveFile(Response response) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            long sum = 0;

            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                HttpUtils.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        inProgress(finalSum * 1.0f / total, total, false);
                    }
                });
            }
            fos.flush();
            HttpUtils.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    inProgress(1, total, true);
                }
            });
            return file;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                onFailed(e);
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                onFailed(e);
            }
        }
    }

}
