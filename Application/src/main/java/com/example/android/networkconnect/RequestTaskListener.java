package com.example.android.networkconnect;

/**
 * Listener for RequestTask.
 */
public interface RequestTaskListener {
    /**
     * At the beginning of the task.
     */
    public void onTaskStart();

    /**
     * Updating task status.
     *
     * @param value progress value in range [0, 100].
     */
    public void onTaskUpdate(int value);

    /**
     * At the end of the task.
     * @param result task result.
     */
    public void onTaskFinished(String result);

    /**
     * When the task is cancelled.
     */
    public void onTaskCancelled();
}
