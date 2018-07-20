package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

// the main purpose of this is to check for new version in the remote
// server.
public class DownloadFile {
    Context context;
    DownloadManager manager;
    LocalBcstReceiver receiver = new LocalBcstReceiver();
    ArrayList<Long> requestid = new ArrayList<>();
    Callback          callback;

    public interface Callback {
        boolean downloadComplete(DownloadFile d, long id, String url, File file);
        void downloadFailed(DownloadFile d, long id, String url);
    }
    public DownloadFile(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
        manager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    void cleanup()
    {
        context.unregisterReceiver(receiver);
    }

    long downloadFile(String url, String title, String description, int request_flags, @Nullable String mimetype) {
        //String url = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        if (mimetype==null) {
            request.setMimeType("text/plain");//application/x-compressed
        }
        request.setDescription(description);

        request.setAllowedNetworkTypes(request_flags);
        request.setTitle(title);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "download.txt");
        long id = manager.enqueue(request);
        this.requestid.add(id);
        Log.d("download", "downloadFile scheduling .. " + id );
        return id;
    }

    // classes ..
    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.d("download", "BroadcastReceiver::onReceive");
            //String action = intent.getAction();
            //if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
            {
                //Log.d("download complete2", "compleete ... ");
                handle_download_complete(context, intent);
            }
        }

        public void handle_download_complete(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            long rids[] = requestid.stream().mapToLong(l->l).toArray();
            query.setFilterById(rids);
            if (rids.length==0) {
                Log.d("download", "no pending requests" + intent.getAction());
                return;
            }
            Log.d("download", "hadle download complete" + intent.getAction());
            Cursor c = manager.query(query);

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int ID = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID));
                switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        File mFile = new File(Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).getPath());
                        boolean removeFile = true;
                        if (DownloadFile.this.callback != null) {
                            String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                            removeFile = DownloadFile.this.callback.downloadComplete(DownloadFile.this, ID, url, mFile);
                        }
                        if (removeFile) {
                            mFile.delete();
                        }
                        requestid.remove(new Long(ID));
                        break;
                    case DownloadManager.STATUS_FAILED:
                        if (DownloadFile.this.callback != null) {
                            String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                            DownloadFile.this.callback.downloadFailed(DownloadFile.this, ID, url);
                        }
                        requestid.remove(new Long(ID));
                        break;
                }
            } // for loop
        }
    } // Receiver


}