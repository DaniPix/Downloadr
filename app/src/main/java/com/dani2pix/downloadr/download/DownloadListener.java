package com.dani2pix.downloadr.download;

/**
 * Created by Domnica on 1/7/2017.
 */

public interface DownloadListener {
    void onProgressChanged(int newProgress);

    void onDownloadCancelled();

    void onDownloadFailed();

    void onDownloadSuccess();

}
