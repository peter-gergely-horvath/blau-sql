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


package com.github.blausql.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyType;


class QueryResultWindow extends BasicWindow {

    //CHECKSTYLE.OFF: AvoidInlineConditionals
    QueryResultWindow(List<Map<String, Object>> queryResult) {
        super("Query result (press Enter/ESC to close)");

        if (queryResult.isEmpty()) {
            setComponent(new Label("(query yielded no results)"));
        } else {

            Map<String, Object> firstRow = queryResult.get(0);

            final ArrayList<String> columnLabels = new ArrayList<>(
                    firstRow.keySet());
            final int numberOfColumns = columnLabels.size();

            Table<String> table = new Table<>(columnLabels.toArray(new String[0]));
            TableModel<String> tableModel = table.getTableModel();

            String[] rowValues = new String[numberOfColumns];

            for (Map<String, Object> row : queryResult) {

                for (int i = 0; i < numberOfColumns; i++) {
                    final String currentColumnLabel = columnLabels.get(i);
                    final Object valueForCurrentColumn = row.get(currentColumnLabel);

                    rowValues[i] = String.valueOf(valueForCurrentColumn);
                }

                tableModel.addRow(rowValues);
            }

            setComponent(table);

            addWindowListener(HotKeyWindowListener.builder()
                    .keyType(KeyType.Escape).invoke(this::close)
                    .build());

        }

    }
    //CHECKSTYLE.ON

}
