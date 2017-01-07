package com.dani2pix.downloadr.download;

/**
 * Created by Domnica on 1/7/2017.
 */

public interface SingleDownloadListener {

    void onFileStatusUpdated(Object id, DownloadCompletionStatus status);

}
