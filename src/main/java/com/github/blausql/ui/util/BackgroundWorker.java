/*
 * Copyright (c) 2017-2025 Peter G. Horvath, All Rights Reserved.
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

import com.github.blausql.ui.components.ApplicationWindow;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.gui2.TextGUIThread;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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

    private final ReentrantLock lock = new ReentrantLock();

    private final TextGUIThread textGUIThread;

    protected BackgroundWorker(ApplicationWindow parent) {
        Objects.requireNonNull(parent, "Argument parent cannot be null");

        TextGUI textGUI = parent.getApplicationTextGUI();
        Objects.requireNonNull(textGUI, "textGUI");

        TextGUIThread guiThread = textGUI.getGUIThread();
        Objects.requireNonNull(guiThread, "guiThread");

        this.textGUIThread = guiThread;
    }

    public final void start() {

        try {
            boolean couldLock = lock.tryLock(1, TimeUnit.MINUTES);
            if (!couldLock) {
                throw new IllegalStateException("Could not acquire lock");
            }

            try {
                if (future != null && future.isCancelled()) {
                    throw new IllegalStateException("Cancelled already");
                }

                this.future = EXECUTOR_SERVICE.submit(new RunBackgroundTask());

            } finally {
                lock.unlock();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public final void cancel() {

        try {
            boolean couldLock = lock.tryLock(1, TimeUnit.MINUTES);
            if (!couldLock) {
                throw new IllegalStateException("Could not acquire lock");
            }

            try {
                if (future == null) {
                    throw new IllegalStateException(this + " is not started yet");
                }

                future.cancel(true);

            } finally {
                lock.unlock();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    protected abstract R doBackgroundTask() throws Exception;

    protected abstract void onBackgroundTaskInterrupted(InterruptedException interruptedException);

    protected abstract void onBackgroundTaskFailed(Throwable t);

    protected abstract void onBackgroundTaskCompleted(R result);

    private final class RunBackgroundTask implements Runnable {

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

            } catch (InterruptedException interruptedException) {

                dispatchInterrupted(interruptedException);

                Thread.currentThread().interrupt();

                return;

            } catch (Throwable t) {

                dispatchFailure(t);

                return;
            }

            dispatchCompleted(result);

        }

        private void dispatchInterrupted(final InterruptedException interruptedException) {
            runInEventThread(() -> onBackgroundTaskInterrupted(interruptedException));
        }

        private void dispatchFailure(final Throwable t) {
            runInEventThread(() -> onBackgroundTaskFailed(t));
        }

        private void dispatchCompleted(final R result) {
            runInEventThread(() -> onBackgroundTaskCompleted(result));
        }

        private void runInEventThread(Runnable runnable) {
            textGUIThread.invokeLater(runnable);
        }
    }
}
