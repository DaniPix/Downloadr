package com.dani2pix.downloadr.download;

/**
 * Created by Domnica on 1/8/2017.
 */

public interface DownloadManagerListener {

    enum CompletionStatus {
        SUCCESS, FAILURE, CANCELLED
    }

    void onDownloadStatusUpdated(Object id, CompletionStatus status);

}
