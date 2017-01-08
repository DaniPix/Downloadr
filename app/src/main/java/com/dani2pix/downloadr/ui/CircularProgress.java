package com.dani2pix.downloadr.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.dani2pix.downloadr.download.DownloadListener;

/**
 * Created by Domnica on 1/8/2017.
 */

public class CircularProgress extends View implements DownloadListener {

    private int size;
    private int thickness;
    private int progress;

    private RectF circleBounds;
    private Paint fillPaint;
    private Paint emptyPaint;
    private Paint textPaint;
    private Paint signPaint;

    public CircularProgress(Context context, AttributeSet attributeSet){
        super(context, attributeSet);



    }

    @Override
    public void onProgressChanged(int newProgress) {

    }

    @Override
    public void onDownloadCancelled() {

    }

    @Override
    public void onDownloadFailed() {

    }

    @Override
    public void onDownloadSuccess() {

    }
}
