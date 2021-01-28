package com.alibaba.sdk.android;

import android.support.test.InstrumentationRegistry;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.MultipartDownloadResult;
import com.alibaba.sdk.android.oss.model.MultipartDownloadRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MultipartDownloadTest extends BaseTestCase {

    private static final String MULTIPART_DOWNLOAD_OBJECT_KEY = "multipartDownloadFile";
    private static final String DOWNLOAD_PATH = InstrumentationRegistry.getContext().getFilesDir().getAbsolutePath() + "/file10m";
    private static final String CHECKPOINT_PATH = InstrumentationRegistry.getContext().getFilesDir().getAbsolutePath();
    private String file10mPath = OSSTestConfig.EXTERNAL_FILE_DIR + "file10m";

    @Override
    void initTestData() throws Exception {
        PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, file10mPath);
        oss.putObject(putObjectRequest);
    }

    @Test
    public void testMultipartDownload() throws ClientException, ServiceException, IOException, NoSuchAlgorithmException {

        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setProgressListener(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                OSSLog.logDebug("mul_download_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testMultipartDownloadWithInvalidBucketName() {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest("mBucketName", MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        ClientException exception = callback.clientException;
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("The bucket name is invalid"));
    }

    @Test
    public void testMultipartDownloadWithInvalidObjectKey() {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, "//invalidObjectKey", DOWNLOAD_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        ClientException exception = callback.clientException;
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("The object key is invalid"));
    }

    @Test
    public void testMultipartDownloadWithNullObjectKey() {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, null, DOWNLOAD_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        ClientException exception = callback.clientException;
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("The object key is invalid"));
    }

    @Test
    public void testGetNotExistObject() throws Exception {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, "objectKey", DOWNLOAD_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        ServiceException exception = callback.serviceException;
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    public void testConcurrentMultipartDownload() throws Exception {
        for (int i = 0; i < 5; i++) {
            PutObjectRequest request = new PutObjectRequest(mBucketName, "multipartDownload" + i, file10mPath);
            oss.putObject(request);
        }
        final CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, "multipartDownload" + index, DOWNLOAD_PATH + index);
                        oss.asyncMultipartDownload(request, null).getResult();
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        assertTrue(false);
                        latch.countDown();
                    }
                }
            }).start();
        }
        latch.await();
    }

    @Test
    public void testMultipartDownloadWithCancel() throws InterruptedException {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);

        Thread.sleep(100);
        task.cancel();
        task.waitUntilFinished();
        ClientException exception = callback.clientException;
        assertTrue(exception.getMessage().contains("multipartDownload cancel"));
    }

    @Test
    public void testMultipartDownloadWithCheckpoint() throws InterruptedException {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        final int[] progress = {0};
        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        request.setProgressListener(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                progress[0] = (int) ((float)currentSize / totalSize);
            }
        });
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);

        Thread.sleep(100);
        task.cancel();

        request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        request.setProgressListener(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                int p = (int) ((float)currentSize / totalSize);
                assertTrue(p >= progress[0]);
            }
        });
        task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();
    }

    @Test
    public void testMultipartDownloadFile() throws InterruptedException, ClientException, ServiceException, IOException, NoSuchAlgorithmException {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);

        Thread.sleep(100);
        task.cancel();

        PutObjectRequest putRequest = new PutObjectRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, OSSTestConfig.EXTERNAL_FILE_DIR + "file1m");
        oss.putObject(putRequest);

        callback = new OSSTestConfig.TestMultipartDownloadCallback();
        task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testMultipartDownloadSmallFile() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testMultipartDownloadBigFile() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        OSSTestConfig.TestMultipartDownloadCallback callback = new OSSTestConfig.TestMultipartDownloadCallback();

        MultipartDownloadRequest request = new MultipartDownloadRequest(mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<MultipartDownloadResult> task = oss.asyncMultipartDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, MULTIPART_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }
}
