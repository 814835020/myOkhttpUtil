package com.cxx.android.http.myokhttputil.upload;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class AsyncTaskForUpLoadFiles extends AsyncTask<Object, Object, Object> {

    private static final int CONNECT_TIME = 10 * 1000;//connect timeout
    private static final int READ_TIME = 10 * 1000;// read timeout

    private static byte[] end_data; // 请求结束标志
    private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
    private static final String PREFIX = "--";// 起始标示
    private static final String LINEND = "\r\n"; // 换行
    private static final String CHARSET = "UTF-8";// 字符集
    private static final String MULTIPART_FROM_DATA = "multipart/form-data";// 表单提交 标示

    private String actionUrl;
    private Map<String, String> params;
    private Map<String, File> files;
    private String fileParamName;
    private UploadFinishedListener mListener;

    private long mTotalSize;//上传数据总大小
    private long mSendSize;//已发送数据大小

    public AsyncTaskForUpLoadFiles(String actionUrl,
                                   Map<String, String> params, Map<String, File> files,
                                   String fileParamName) {

        this.actionUrl = actionUrl;
        this.files = files;
        this.fileParamName = fileParamName;
        this.params = params;
        end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();

    }

    @Override
    protected String doInBackground(Object... paramss) {
        return post(actionUrl, params, files);
    }

    @Override
    protected void onPostExecute(Object result) {
        // 处理返回结果,在主线程中执行
        if (mListener == null)
            return;
        int errorCode = 0;
        if (result.equals("1")) {
            errorCode = 1;
        } else if (result.equals("2")) {
            errorCode = 2;
        } else {
            errorCode = 0;
        }
        mListener.onUploadFinished(errorCode, (String) result);
    }

    /**
     * 附件上传
     *
     * @param actionUrl 附件上传目标地址url
     * @param params    表单内容 包括文本内容
     * @param files     附件集合 可以上传多个图片
     * @return 成功与否
     */
    public String post(String actionUrl, Map<String, String> params,
                       Map<String, File> files) {
        try {
            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(CONNECT_TIME);
            conn.setReadTimeout(READ_TIME);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", CHARSET);
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                    + ";boundary=" + BOUNDARY);

            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"" + LINEND);
                sb.append("Content-Type: text/plain; charset=" + CHARSET
                        + LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sb.append(LINEND);
                sb.append(entry.getValue());
                sb.append(LINEND);
            }

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            BufferedOutputStream bos = new BufferedOutputStream(dos);
            bos.write(sb.toString().getBytes());

            // 发送文件数据
            if (files != null && files.size() > 0) {

                for (Map.Entry<String, File> file : files.entrySet()) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\""
                            + fileParamName + "\"; filename=\"" + file.getKey()
                            + "\"" + LINEND);
                    sb1.append("Content-Type: multipart/form-data; charset="
                            + CHARSET + LINEND);
                    sb1.append(LINEND);
                    bos.write(sb1.toString().getBytes("utf-8"));

                    InputStream is = new FileInputStream(file.getValue());
                    byte[] buffer = new byte[10 * 1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                        mSendSize += len;
                    }
                    is.close();
                    bos.write(LINEND.getBytes());
                }
            }
            bos.write(end_data);
            bos.flush();

            // 得到响应码
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                String json = convertStreamToStringUTF8(in);
                System.out.println(json);
                bos.close();
                dos.close();
                conn.disconnect();
                return json;
            } else {
                System.out.println("服务器异常，：" + conn.getResponseCode()
                        + conn.getResponseCode());
                return "1";
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO异常");
            return "2";
        }
    }

    // 流转字符串 UTF-8
    private static String convertStreamToStringUTF8(InputStream is)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = null;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is,
                    "utf-8"));

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        is.close();
        return sb.toString();
    }

    public void addUpLoadFinishedListener(UploadFinishedListener l) {
        mListener = l;
    }

    public void removeUpLoadFinishedListener() {
        mListener = null;
    }

    public interface UploadFinishedListener {
        // 0 操作成功， 1 服务器异常， 2 本地IO异常
        public void onUploadFinished(int errorCode, String resultJson);
    }

}
