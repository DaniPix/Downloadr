package com.dani2pix.downloadr.download;

import java.util.List;

/**
 * Created by Domnica on 1/7/2017.
 */

public class Download {

    private int progress;
    private boolean cancelled;
    private Runnable cancelHandler;
    private List<DownloadListener> downloadListenerList;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelHandler != null) {
            cancelHandler.run();
        }
    }

    public void setCancelHandler(Runnable cancelHandler) {
        this.cancelHandler = cancelHandler;
    }

    public void addListener(DownloadListener listener) {
        downloadListenerList.add(listener);
    }

    public void removeListener(DownloadListener listener) {
        downloadListenerList.remove(listener);
    }

    public void postDownloadProgressNotification() {
        for (DownloadListener listener : downloadListenerList) {
            listener.onProgressChanged(getProgress());
        }
    }
}
