package com.cxh.task.util;


import com.cxh.task.task.Task;
import com.cxh.task.util.model.TaskSortModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

public class TaskSortUtil {
    /**
     * 拓扑排序
     * taskIntegerHashMap每个Task的入度（key= Class < ? extends Task>）
     * taskHashMap每个Task            （key= Class < ? extends Task>）
     * taskChildHashMap每个Task的孩子  （key= Class < ? extends Task>）
     * deque 入度为0的Task
     */
    public static List<Task> getSortResult(List<Task> startTaskList, HashMap<Class<? extends Task>, Task> taskHashMap, HashMap<Class<? extends Task>, List<Class<? extends Task>>> taskChildHashMap) {
        List<Task> sortTaskList = new ArrayList<>();
        HashMap<Class<? extends Task>, TaskSortModel> taskIntegerHashMap = new HashMap<>();
        Deque<Class<? extends Task>> deque = new ArrayDeque<>();
        for (Task task : startTaskList) {
            if (!taskIntegerHashMap.containsKey(task.getClass())) {
                taskHashMap.put(task.getClass(), task);
                taskIntegerHashMap.put(task.getClass(), new TaskSortModel(task.getDependsTaskList() == null ? 0 : task.getDependsTaskList().size()));
                taskChildHashMap.put(task.getClass(), new ArrayList<Class<? extends Task>>());
                //入度为0的队列
                if (taskIntegerHashMap.get(task.getClass()).getIn() == 0) {
                    deque.offer(task.getClass());
                }
            } else {
                throw new RuntimeException("任务重复了: " + task.getClass());
            }
        }
        //把孩子都加进去
        for (Task task : startTaskList) {
            if (task.getDependsTaskList() != null) {
                for (Class<? extends Task> aclass : task.getDependsTaskList()) {
                    taskChildHashMap.get(aclass).add(task.getClass());
                }
            }
        }
        //循环去除入度0的，再把孩子入度变成0的加进去
        while (!deque.isEmpty()) {
            Class<? extends Task> aclass = deque.poll();
            sortTaskList.add(taskHashMap.get(aclass));
            for (Class<? extends Task> classChild : taskChildHashMap.get(aclass)) {
                taskIntegerHashMap.get(classChild).setIn(taskIntegerHashMap.get(classChild).getIn() - 1);
                if (taskIntegerHashMap.get(classChild).getIn() == 0) {
                    deque.offer(classChild);
                }
            }
        }
        if (sortTaskList.size() != startTaskList.size()) {
            throw new RuntimeException("出现环了");
        }
        return sortTaskList;
    }
}
