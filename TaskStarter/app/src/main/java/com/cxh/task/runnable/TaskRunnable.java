package com.cxh.task.runnable;

import android.os.Process;

import com.cxh.task.dispatcher.TaskDispatcher;
import com.cxh.task.task.Task;


public class TaskRunnable implements Runnable {
    private Task appStartTask;
    private TaskDispatcher taskDispatcher;

    public TaskRunnable(Task appStartTask, TaskDispatcher appStartTaskDispatcher) {
        this.appStartTask = appStartTask;
        this.taskDispatcher = appStartTaskDispatcher;
    }

    @Override
    public void run() {
        Process.setThreadPriority(appStartTask.priority());
        appStartTask.waitToNotify();
        appStartTask.run();
        taskDispatcher.satNotifyChildren(appStartTask);
        taskDispatcher.markAppStartTaskFinish(appStartTask);
    }
}
