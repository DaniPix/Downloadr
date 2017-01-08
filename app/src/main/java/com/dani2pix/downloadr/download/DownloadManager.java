package com.dani2pix.downloadr.download;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Domnica on 1/7/2017.
 */

public final class DownloadManager {

    private static final int CORE_POOL_SIZE = 3; // 3 parallel downloads by default
    private static final int MAXIMUM_POOL_SIZE = 6; // 6 maximum parallel downloads possible
    private static final int KEEP_ALIVE_TIME = 10; // 10 seconds

    private static DownloadManager instance;

    private Map<Object, Download> downloads; // map the downloads with a unique identifier for each download
    private List<DownloadManagerListener> listeners; // listener for each download to update the UI
    private ExecutorService executorService; // service to run the downloads queue
    private LinkedBlockingQueue<Runnable> workQueue; // downloads sitting idle

    private OkHttpClient client;
    private Handler handler;

    private DownloadManager() {
        downloads = new ConcurrentHashMap<>();
        listeners = new ArrayList<>();
        workQueue = new LinkedBlockingQueue<>();
        executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue);
        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }

        return instance;
    }

    public void addListener(DownloadManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DownloadManagerListener listener) {
        listeners.remove(listener);
    }

    public Download getDownload(Object id) {
        return downloads.get(id);
    }

    public Download startDownload(final Object id, final String source, final String destination) {
        Download download = downloads.get(id);
        if (download == null) {
            download = new Download();
            downloads.put(id, download);

            final Download currentDownload = download;
            final Runnable command = new Runnable() {
                @Override
                public void run() {
                    if (currentDownload.isCancelled()) {
                        return;
                    }

                    File file = new File(destination);
                    try {
                        doDownload(file, currentDownload, source, id);
                    } catch (IOException e) {
                        if (file.exists() && file.delete()) {
                            downloads.remove(id);
                        }

                        Log.e(getClass().getName(), e.getMessage(), e);
                        notifyDownloadStatus(id, DownloadManagerListener.CompletionStatus.FAILURE);
                    }
                }
            };
            executorService.execute(command);

            currentDownload.setCancelHandler(new Runnable() {
                @Override
                public void run() {
                    if (workQueue.remove(command)) {
                        downloads.remove(id);
                    }
                }
            });
        }
        return download;
    }

    private void doDownload(File file, Download download, String source, Object id) throws IOException {
        Request request = new Request.Builder().url(source).build();
        Call callRequest = client.newCall(request);
        Response callResponse = callRequest.execute();

        if (callResponse.isSuccessful()) {
            ResponseBody body = callResponse.body();
            BufferedSource bufferedSource = body.source();
            BufferedSink sink = Okio.buffer(Okio.sink(file));

            long startTime = System.currentTimeMillis();
            long contentLength = body.contentLength();
            double total = 0;
            int timeCount = 1;
            long read;

            while ((read = bufferedSource.read(sink.buffer(), 2048)) != -1) {
                if (download.isCancelled()) {
                    cancelDownload(file, id, sink, bufferedSource);
                    return;
                }

                total += read;
                download.setProgress((int) ((total / contentLength) * 100));
                sink.flush();

                long currentTime = System.currentTimeMillis() - startTime;
                if (currentTime > 1000 * timeCount) {
                    notifyDownloadProgress(download);
                    timeCount++;
                }
            }

            sink.flush();
            sink.close();
            bufferedSource.close();

            notifyDownloadStatus(id, DownloadManagerListener.CompletionStatus.SUCCESS);
            downloads.remove(id);
        }
    }

    private void cancelDownload(File file, Object id, BufferedSink sink, BufferedSource source) throws IOException {
        sink.close();
        source.close();
        if (file.exists() && file.delete()) {
            downloads.remove(id);
        }
        notifyDownloadStatus(id, DownloadManagerListener.CompletionStatus.CANCELLED);
    }

    private void notifyDownloadProgress(final Download download) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                download.postDownloadProgressNotification();
            }
        });
    }

    private void notifyDownloadStatus(final Object id, final DownloadManagerListener.CompletionStatus status) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownloadManagerListener listener : listeners) {
                    listener.onDownloadStatusUpdated(id, status);
                }
            }
        });
    }
}
