package study.luofeng.com.multitask;

/**
 * FileInfo
 * Created by weixi on 2016/9/17.
 */
public class FileInfo {

    private String displayName;
    private String downloadUrl;
    private String fileName;
    private long fileSize;
    private long downloadLength;

    public FileInfo(String displayName, String downloadUrl) {
        this.displayName = displayName;
        this.downloadUrl = downloadUrl;
    }

    public FileInfo() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
