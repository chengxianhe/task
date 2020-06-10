package com.cxh.task.dispatcher;

import android.content.Context;
import android.os.Looper;


import com.cxh.task.runnable.TaskRunnable;
import com.cxh.task.task.Task;
import com.cxh.task.util.LogUtil;
import com.cxh.task.util.ProcessUtils;
import com.cxh.task.util.TaskSortUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskDispatcher {
    private static volatile TaskDispatcher taskDispatcher;
    //所有任务需要等待的时间
    private final int WAITING_TIME = 10000;
    private Context context;
    //是否在主进程
    private boolean isInMainProgress;
    //存放每个Task  （key= Class < ? extends Task>）
    private HashMap<Class<? extends Task>, Task> taskHashMap;
    //每个Task的孩子 （key= Class < ? extends Task>）
    private HashMap<Class<? extends Task>, List<Class<? extends Task>>> taskChildHashMap;
    //通过Add添加进来的所有任务
    private List<Task> startTaskList;
    //拓扑排序后的所有任务
    private List<Task> sortTaskList;

    //需要等待的任务总数，用于阻塞
    private CountDownLatch countDownLatch;
    //需要等待的任务总数，用于CountDownLatch
    private AtomicInteger needWaitCount;
    //所有的任务总数
    private long startTime, finishTime;

    public static TaskDispatcher getInstance() {
        if (taskDispatcher == null) {
            synchronized (TaskDispatcher.class) {
                if (taskDispatcher == null) {
                    taskDispatcher = new TaskDispatcher();
                }
            }
        }
        return taskDispatcher;
    }

    private TaskDispatcher() {
        taskHashMap = new HashMap<>();
        taskChildHashMap = new HashMap<>();
        startTaskList = new ArrayList<>();
        needWaitCount = new AtomicInteger();
    }

    public TaskDispatcher setContext(Context context) {
        this.context = context;
        isInMainProgress = ProcessUtils.isMainProcess(this.context);
        return this;
    }

    public TaskDispatcher addTask(Task task) {
        startTime = System.currentTimeMillis();
        if (task == null) {
            throw new RuntimeException("this task is null");
        }
        startTaskList.add(task);
        if (ifNeedWait(task)) {
            needWaitCount.getAndIncrement();
        }
        return this;
    }

    public TaskDispatcher start() {
        if (context == null) {
            throw new RuntimeException("this context is null play call setContext()");
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("Must be in the main thread ");
        }

        if (!isInMainProgress) {
            LogUtil.d("当前进程非主进程");
            return this;
        }
        //拓扑排序，拿到排好序之后的任务队列
        sortTaskList = TaskSortUtil.getSortResult(startTaskList, taskHashMap, taskChildHashMap);
        printSortTask();
        countDownLatch = new CountDownLatch(needWaitCount.get());
        senAppStartTask();
        return this;
    }

    //输出排好序的Task
    private void printSortTask() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("当前所有任务排好的顺序为：");
        String className = "";
        for (int i = 0; i < sortTaskList.size(); i++) {
            className = sortTaskList.get(i).getClass().getName();
            className = className.replace(sortTaskList.get(i).getClass().getPackage().getName() + ".", "");
            if (i == 0) {
                stringBuffer.append(className);
            } else {
                stringBuffer.append("---＞" + className);

            }
        }
        LogUtil.d(stringBuffer.toString());
    }

    //发送任务
    private void senAppStartTask() {
        //先发送非主线程的任务
        for (Task task : sortTaskList) {
            if (!task.isRunOnMainThread()) {
                task.runOnExecutor().execute(new TaskRunnable(task, this));
            }
        }
        //先发送主线程的任务
        for (Task appStartTask : sortTaskList) {
            if (appStartTask.isRunOnMainThread()) {
                new TaskRunnable(appStartTask, this).run();
            }
        }
    }

    //通知Children一个前置任务已完成
    public void satNotifyChildren(Task appStartTask) {
        List<Class<? extends Task>> arrayList = taskChildHashMap.get(appStartTask.getClass());
        if (arrayList != null && arrayList.size() > 0) {
            for (Class<? extends Task> aClass : arrayList) {
                taskHashMap.get(aClass).Notify();
            }
        }
    }

    //标记已经完成的Task
    public void markAppStartTaskFinish(Task appStartTask) {
        LogUtil.d("任务完成了：" + appStartTask.getClass().getName());
        if (ifNeedWait(appStartTask)) {
            countDownLatch.countDown();
            needWaitCount.getAndDecrement();
        }
    }

    //是否需要等待，主线程的任务本来就是阻塞的，所以不用管
    private boolean ifNeedWait(Task task) {
        return !task.isRunOnMainThread() && task.needWait();
    }

    //等待，阻塞主线程
    public void await() {
        try {
            if (countDownLatch == null) {
                throw new RuntimeException("Before the call to await (), you must first call start ()");
            }
            countDownLatch.await(WAITING_TIME, TimeUnit.MILLISECONDS);
            finishTime = System.currentTimeMillis() - startTime;
            LogUtil.d("启动耗时：" + finishTime);
        } catch (InterruptedException e) {
        }
    }
}
