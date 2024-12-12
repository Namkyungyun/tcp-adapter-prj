package com.example.tcpadapter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ClassUtils {
    // ??? thread 추가시 사용?
    private static Logger log = LoggerFactory.getLogger(ClassUtils.class);

    public static final void startThread(Runnable runnable) {
        ThreadFactory ThFact = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable)
            {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(false);
                return thread;
            }
        };
        Thread job = ThFact.newThread(runnable);
        job.start();
    }
}
