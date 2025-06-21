/*
 * Copyright (c) 2017-2020 Peter G. Horvath, All Rights Reserved.
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

 
package com.github.blausql.ui.components;

import com.github.blausql.TerminalUI;
import com.github.blausql.ui.LegacyWindowSupport;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Inspired by com.googlecode.lanterna.gui.dialog.WaitingDialog,
 * with the majority of code re-written and bugs fixes
 */
public final class WaitDialog extends LegacyWindowSupport {

    private final Thread spinAnimationThread = new Thread(new SpinAnimationTask());

    private final Label spinLabel;

    //private static final String[] SPIN_STEPS = new String[]{"-", "\\", "|", "-"};
    private static final String[] SPIN_STEPS = new String[] {
            "[#     ]",
            "[##    ]",
            "[###   ]",
            "[####  ]",
            "[ #### ]",
            "[  ####]",
            "[   ###]",
            "[    ##]",
            "[     #]" };

    private final AtomicInteger currentSpinnerStepIndex = new AtomicInteger(0);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public WaitDialog(String title, String text) {
        this(title, text, null);
    }

    public WaitDialog(String title, String text, final Runnable onCancel) {
        super(title);
        spinLabel = new Label(SPIN_STEPS[currentSpinnerStepIndex.get()]);
        final Panel panel = new Panel(new LinearLayout());
        panel.addComponent(new Label(text));
        panel.addComponent(spinLabel);

        if (onCancel != null) {
            panel.addComponent(new Label("   "));

            panel.addComponent(new ActionButton("Cancel",
                    new Runnable() {
                        @Override
                        public void run() {
                            WaitDialog.this.close();
                            onCancel.run();
                        }
                    }));
        }

        addComponent(panel);
    }

    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);


        if (visible) {
            if (isClosed.get()) {
                // if by any chance, onVisible is triggered after close,
                // ensure that the window will actually be closed
                close();
            } else {
                spinAnimationThread.start();
            }
        }

    }

    @Override
    public void close() {
        isClosed.set(true);

        if (spinAnimationThread.isAlive()) {
            spinAnimationThread.interrupt();
        }

        TerminalUI.runInEventThread(new Runnable() {
            public void run() {
                WaitDialog.super.close();
            }
        });
    }

    private void stepSpinner() {


        if (currentSpinnerStepIndex.incrementAndGet() >= SPIN_STEPS.length) {
            currentSpinnerStepIndex.set(0);
        }

        String spinnerText = SPIN_STEPS[currentSpinnerStepIndex.get()];

        spinLabel.setText(spinnerText);
    }

    private final class SpinAnimationTask implements Runnable {

        private static final int SPIN_DELAY = 200;

        @Override
        public void run() {
            try {

                while (!Thread.currentThread().isInterrupted()) {

                    TerminalUI.runInEventThread(new Runnable() {
                        public void run() {
                            stepSpinner();
                        }
                    });

                    Thread.sleep(SPIN_DELAY);

                }
            } catch (InterruptedException e) {
                // stop task on Thread interrupt and
                // restore the interrupted status
                Thread.currentThread().interrupt();

            }
        }
    }

}
