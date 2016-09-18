package study.luofeng.com.multitask;

import okhttp3.Headers;

/**
 * Utils
 * Created by weixi on 2016/9/18.
 */
public class Utils {

    /**
     * 从响应头或者url得到文件名
     * @param url     url
     * @param headers 响应头
     * @return 文件名
     */
    public static String getFileName(String url, Headers headers) {
        String disposition = headers.get("content-disposition");
        if (disposition != null) {
//            "attachment;filename=filename"
            int indexFileName;
            if ((indexFileName = disposition.indexOf("filename")) != -1) {
                return disposition.substring(indexFileName + 9);
            }
        }
        int startIndex = url.lastIndexOf("/") + 1;
        return url.substring(startIndex);
    }
}
