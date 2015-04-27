package com.example.android.networkconnect;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of AsyncTask, to fetch the data in the background away from the UI thread.
 */
public class RequestTask extends AsyncTask<String, Integer, String> {

    public static final String TAG = "RequestTask";
    private static final int CONNECT_TIMEOUT = 15000 /* milliseconds */;
    private static final int BUFFER_SIZE = 32;
    private boolean isPost = false;
    private RequestTaskListener mListener;

    public RequestTask(RequestTaskListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Set POST method.
     *
     * @param isPost true if post.
     */
    public void setPost(boolean isPost) {
        this.isPost = isPost;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String result = "";
            if (!updateProgress(25))
                return result;
            HttpURLConnection conn = isPost ? doPost(strings[0], strings[1]) : doGet(strings[0]);
            if (!updateProgress(50))
                return result;
            // Success HTTP Status Code
            if (conn.getResponseCode() / 100 != 2)
                return result;
            if (!updateProgress(75))
                return result;
            result = getStringFromStream(conn.getInputStream());
            if (!updateProgress(100))
                return result;
            return result;
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onTaskStart();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mListener.onTaskFinished(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mListener.onTaskCancelled();
    }

    // === Network Connection

    /**
     * Given a string representation of a URL, sets up a connection for GET request.
     *
     * @param urlString A string representation of a URL.
     * @return A HttpURLConnection.
     * @throws java.io.IOException
     */
    private HttpURLConnection doGet(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn;
    }

    /**
     * Given a string representation of a URL, sets up a connection for POST request.
     *
     * @param urlString A string representation of a URL.
     * @param postData  Post data.
     * @return A HttpURLConnection.
     * @throws java.io.IOException
     */
    private HttpURLConnection doPost(String urlString, String postData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(conn.getOutputStream());
            dataOutputStream.writeBytes(postData);
        } finally {
            if (dataOutputStream != null)
                dataOutputStream.close();
        }
        return conn;
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param inputStream InputStream.
     * @return String from inputStream.
     * @throws java.io.IOException
     */
    private String getStringFromStream(InputStream inputStream) throws IOException {
        StringBuilder sb = null;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(inputStream, "UTF-8");
            sb = new StringBuilder(BUFFER_SIZE);
            char[] buffer = new char[BUFFER_SIZE];
            int length;
            while ((length = isr.read(buffer, 0, BUFFER_SIZE)) != -1) {
                sb.append(buffer, 0, length);
                if (isCancelled())
                    return null;
            }
            inputStream.close();
        } finally {
            if (isr != null)
                isr.close();
        }
        return sb.toString();
    }


    // === Progress

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mListener.onTaskUpdate(values[0]);
    }

    /**
     * Update progress.
     *
     * @param progress Task progress estimated by an integer.
     * @return false if cancelled.
     */
    private boolean updateProgress(int progress) {
        if (isCancelled())
            return false;
        publishProgress(progress);
        return true;
    }
}
