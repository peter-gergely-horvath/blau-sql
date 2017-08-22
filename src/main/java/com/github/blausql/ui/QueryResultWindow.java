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


package com.github.blausql.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Table;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

class QueryResultWindow extends Window {

    @SuppressWarnings("FieldCanBeLocal")
    private final WindowAdapter closeOnEscOrEnterWindowListener = new WindowAdapter() {

        @Override
        public void onUnhandledKeyboardInteraction(Window window, Key key) {

            if (Kind.Escape.equals(key.getKind())
                    || Kind.Enter.equals(key.getKind())) {

                QueryResultWindow.this.close();
            }
        }
    };

    //CHECKSTYLE.OFF: AvoidInlineConditionals
    public QueryResultWindow(List<Map<String, Object>> queryResult) {
        super("Query result (press Enter/ESC to close)");

        addWindowListener(closeOnEscOrEnterWindowListener);

        if (queryResult.size() == 0) {
            addComponent(new Label("(query yielded no results)"));
        } else {

            Map<String, Object> firstRow = queryResult.get(0);

            final ArrayList<String> columnLabels = new ArrayList<>(
                    firstRow.keySet());
            final int numberOfColumns = columnLabels.size();

            Table table = new Table(numberOfColumns);

            Component[] components = new Component[numberOfColumns];

            for (int i = 0; i < numberOfColumns; i++) {
                components[i] = new Label(columnLabels.get(i));
            }
            table.addRow(components);

            for (Map<String, Object> row : queryResult) {

                for (int i = 0; i < numberOfColumns; i++) {
                    final String currentColumnLabel = columnLabels.get(i);
                    final Object valueForCurrentColumn = row.get(currentColumnLabel);

                    final Label labelForCurrentColumnValue = (valueForCurrentColumn != null)
                            ? new Label(valueForCurrentColumn.toString()) : new Label("null", true);

                    components[i] = labelForCurrentColumnValue;
                }

                table.addRow(components);
            }

            addComponent(table);

        }

    }
    //CHECKSTYLE.ON

}
