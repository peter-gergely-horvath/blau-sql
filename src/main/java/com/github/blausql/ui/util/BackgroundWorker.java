package com.github.blausql.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BackgroundWorker<R> {

	private static final ExecutorService EXECUTOR_SERVICE = 
			Executors.newSingleThreadExecutor();

	public void start() {

		EXECUTOR_SERVICE.submit(new Runnable() {

			public void run() {
				final R result;
				try {
					result = BackgroundWorker.this.doBackgroundTask();
				} catch (Throwable t) {
					dispatchFailure(t);

					return;
				}
				dispatchCompleted(result);

			}

			private void dispatchFailure(final Throwable t) {
				LanternaUtilities.invokeLater(new Runnable() {
					public void run() {
						onBackgroundTaskFailed(t);
					}
				});
			}
			
			private void dispatchCompleted(final R result) {
				LanternaUtilities.invokeLater(new Runnable() {
					public void run() {
						onBackgroundTaskCompleted(result);
					}
				});
			}
		});

	}

	protected abstract R doBackgroundTask();

	protected abstract void onBackgroundTaskFailed(Throwable t);

	protected abstract void onBackgroundTaskCompleted(R result);

}
