package com.example.android.networkconnect;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

/**
 * Download file helper.
 */
public class DownloadHelper {
    private DownloadManager downloadManager;
    private Request request;
    private long downloadId = -1;
    /**
     * Broadcast when the download has successfully completed.
     */
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                // if download successful
                if (getInt(downloadId, DownloadManager.COLUMN_STATUS) == DownloadManager.STATUS_SUCCESSFUL) {
                    mDownloadDoneCallback.onDownloadDone();
                }
            }
        }
    };
    private DownloadDoneCallback mDownloadDoneCallback;

    public DownloadHelper(Context context, String uri, DownloadDoneCallback downloadDoneCallback) throws Exception {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(uri);
        request = new Request(downloadUri);
        String filename = downloadUri.getLastPathSegment();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setTitle(filename);
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        mDownloadDoneCallback = downloadDoneCallback;

        // Set filter to only when download is complete and register broadcast receiver
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downloadReceiver, filter);
    }

    /**
     * Start downloading.
     */
    public void start() {
        if (request == null) {
            return;
        }
        //Enqueue a new download and same the referenceId
        downloadId = downloadManager.enqueue(request);
    }

    /**
     * Get int value of a column for the id.
     *
     * @param id         filter id.
     * @param columnName column name.
     * @return first value of the column for the id.
     */
    private int getInt(long id, String columnName) {
        int result = -1;
        Cursor cursor = null;
        try {
            Query query = new Query().setFilterById(id);
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(columnName));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * Callback when the download has successfully completed.
     */
    public interface DownloadDoneCallback {
        public void onDownloadDone();
    }
}