package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

import java.util.Map;

public class MultipartDownloadRequest extends OSSRequest {

    //  Object bucket's name
    private String bucketName;

    // Object Key
    private String objectKey;

    // Gets the range of the object to return (starting from 0 to the object length -1)
    private Range range;

    // progress callback run with not ui thread
    private OSSProgressCallback progressListener;

    //
    private String downloadToFilePath;

    private Boolean enableCheckPoint = false;
    private String checkPointFilePath;

    private long partSize = 256 * 1024;

    private Map<String, String> requestHeader;

    public MultipartDownloadRequest(String bucketName, String objectKey, String downloadToFilePath) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.downloadToFilePath = downloadToFilePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public OSSProgressCallback getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(OSSProgressCallback progressListener) {
        this.progressListener = progressListener;
    }

    public String getDownloadToFilePath() {
        return downloadToFilePath;
    }

    public void setDownloadToFilePath(String downloadToFilePath) {
        this.downloadToFilePath = downloadToFilePath;
    }

    public Boolean getEnableCheckPoint() {
        return enableCheckPoint;
    }

    public void setEnableCheckPoint(Boolean enableCheckPoint) {
        this.enableCheckPoint = enableCheckPoint;
    }

    public String getCheckPointFilePath() {
        return checkPointFilePath;
    }

    public void setCheckPointFilePath(String checkPointFilePath) {
        this.checkPointFilePath = checkPointFilePath;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public String getTempFilePath() {
        return downloadToFilePath + ".tmp";
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
    }
}
