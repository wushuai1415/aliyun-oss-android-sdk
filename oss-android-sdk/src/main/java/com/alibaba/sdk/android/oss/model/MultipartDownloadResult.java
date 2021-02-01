package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;

public class MultipartDownloadResult extends OSSResult {

    private ObjectMetadata metadata;

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}
