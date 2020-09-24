/*
 * Copyright (c) 2017 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.blausql.ui.util;

import com.github.blausql.TerminalUI;
import com.googlecode.lanterna.gui.Action;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BackgroundWorker<R> {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(
            new ThreadFactory() {

        private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
        private final AtomicInteger workerThreadCounter = new AtomicInteger();


        @Override
        public Thread newThread(Runnable r) {

            final long workerNumber = workerThreadCounter.incrementAndGet();

            final Thread thread = defaultThreadFactory.newThread(r);
            thread.setName(String.format("BackgroundWorker-thread-%s", workerNumber));
            thread.setDaemon(true);

            return thread;
        }
    });
    private Future<?> future;

    private final Object lockObject = new Object();

    public final void start() {

        synchronized (lockObject) {
            if (future != null && future.isCancelled()) {
                throw new IllegalStateException("Cancelled already");
            }

            this.future = EXECUTOR_SERVICE.submit(new RunBackgroundTask());
        }
    }

    public final void cancel() {

        synchronized (lockObject) {
            if (future == null) {
                throw new IllegalStateException(this + " is not started yet");
            }

            future.cancel(true);
        }
    }

    protected abstract R doBackgroundTask() throws Exception;

    protected abstract void onBackgroundTaskFailed(Throwable t);

    protected abstract void onBackgroundTaskCompleted(R result);

    private class RunBackgroundTask implements Runnable {

        public void run() {
            final R result;
            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Interrupted before background task started");
                }

                result = BackgroundWorker.this.doBackgroundTask();

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Interrupted after background task finished");
                }

            } catch (InterruptedException t) {
                dispatchFailure(t);

                Thread.currentThread().interrupt();

                return;
            } catch (Throwable t) {
                dispatchFailure(t);

                return;
            }
            dispatchCompleted(result);

        }

        private void dispatchFailure(final Throwable t) {
            TerminalUI.runInEventThread(new Action() {
                public void doAction() {
                    onBackgroundTaskFailed(t);
                }
            });
        }

        private void dispatchCompleted(final R result) {
            TerminalUI.runInEventThread(new Action() {
                public void doAction() {
                    onBackgroundTaskCompleted(result);
                }
            });
        }
    }
}
