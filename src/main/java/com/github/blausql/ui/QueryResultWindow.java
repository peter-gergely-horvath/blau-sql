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

public class QueryResultWindow extends Window {

	public QueryResultWindow(List<Map<String, Object>> queryResult) {
		super("Query result (press Enter/ESC to close)");

		addWindowListener(new WindowAdapter() {

			@Override
			public void onUnhandledKeyboardInteraction(Window window, Key key) {

				if (Kind.Escape.equals(key.getKind())
						|| Kind.Enter.equals(key.getKind())) {
					QueryResultWindow.this.closeResultWindow();
				}
			}

		});

		if (queryResult.size() == 0) {
			addComponent(new Label("(query yielded no results)"));
		} else {

			Map<String, Object> firstRow = queryResult.get(0);

			final ArrayList<String> columnLabels = new ArrayList<String>(
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
					final String currentColumLabel = columnLabels.get(i);
					final Object valueForCurrentColumn = row
							.get(currentColumLabel);

					final Label labelForCurrentColumnValue = (valueForCurrentColumn != null) ? 
									new Label(valueForCurrentColumn.toString()) : 
										new Label("null", true);

					components[i] = labelForCurrentColumnValue;
				}

				table.addRow(components);
			}

			addComponent(table);

		}

	}

	protected void closeResultWindow() {
		this.close();
	}

}
