/*
 * This file is part of Brewday.
 *
 * Brewday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brewday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Brewday.  If not, see <https://www.gnu.org/licenses/>.
 */

package mclachlan.brewday.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableRowSorter;
import mclachlan.brewday.db.Database;
import mclachlan.brewday.ingredients.Misc;
import mclachlan.brewday.process.ProcessStep;
import mclachlan.brewday.recipe.IngredientAddition;
import mclachlan.brewday.recipe.MiscAddition;
import mclachlan.brewday.recipe.Recipe;

/**
 *
 */
public class MiscAdditionDialog extends JDialog implements ActionListener, KeyListener
{
	private JTextField searchBox;
	private JTable table;
	private MiscsTableModel tableModel;
	private JSpinner weight, time;
	private JButton ok, cancel;
	private JComboBox<ProcessStep> usage;

	private IngredientAddition result;
	private ProcessStep stepResult;
	private TableRowSorter rowSorter;

	/*-------------------------------------------------------------------------*/
	public MiscAdditionDialog(Frame owner, String title, Recipe recipe)
	{
		super(owner, title, true);

		this.setLayout(new BorderLayout());

		JPanel content = new JPanel(new BorderLayout());

		Map<String, Misc> dbMiscs = Database.getInstance().getMiscs();
		List<Misc> miscs = new ArrayList<Misc>(dbMiscs.values());
		miscs.sort(Comparator.comparing(Misc::getName));

		tableModel = new MiscsTableModel(miscs);
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setPreferredScrollableViewportSize(new Dimension(700, 200));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		rowSorter = (TableRowSorter)table.getRowSorter();

		JLabel searchLabel = new JLabel(SwingUi.searchIcon);
		searchBox = new JTextField(30);
		searchLabel.setLabelFor(searchBox);
		searchBox.addKeyListener(this);

		JPanel top = new JPanel();
		top.add(searchLabel);
		top.add(searchBox);

		JLabel weightLabel = new JLabel("Weight (g):", JLabel.TRAILING);
		weight = new JSpinner(new SpinnerNumberModel(0D, 0D, 999D,0.01));
		weightLabel.setLabelFor(weight);

		JPanel bottom = new JPanel();
		bottom.add(weightLabel);
		bottom.add(weight);

		if (recipe != null)
		{
			JLabel usageLabel = new JLabel("Usage:");
			List<ProcessStep> possibleUsages = recipe.getStepsForIngredient(IngredientAddition.Type.MISC);
			usage = new JComboBox<>(new Vector<>(possibleUsages));
			usageLabel.setLabelFor(usage);

			JLabel timeLabel = new JLabel("Time:");
			time = new JSpinner(new SpinnerNumberModel(0D, 0D, 999D, 1D));
			timeLabel.setLabelFor(time);

			bottom.add(usageLabel);
			bottom.add(usage);
			bottom.add(timeLabel);
			bottom.add(time);
		}

		content.add(top, BorderLayout.NORTH);
		content.add(new JScrollPane(table), BorderLayout.CENTER);
		content.add(bottom, BorderLayout.SOUTH);

		ok = new JButton("OK");
		ok.addActionListener(this);

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.add(content, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			int selectedRow = table.getSelectedRow();
			if (selectedRow > -1)
			{
				selectedRow = table.getRowSorter().convertRowIndexToModel(selectedRow);
				Misc y = tableModel.getData().get(selectedRow);
				result = new MiscAddition(y, (Double)weight.getValue(), getTime());
				stepResult = usage==null ? null : (ProcessStep)usage.getSelectedItem();
				setVisible(false);
			}
		}
		else if (e.getSource() == cancel)
		{
			result = null;
			stepResult = null;
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public IngredientAddition getResult()
	{
		return result;
	}

	public ProcessStep getStepResult()
	{
		return stepResult;
	}

	public double getTime()
	{
		return time == null ? 0 : (Double)time.getValue();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void keyTyped(KeyEvent e)
	{
		// "(?i)" makes it case insensitive
		rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchBox.getText()));
	}

	@Override
	public void keyPressed(KeyEvent e)
	{

	}

	@Override
	public void keyReleased(KeyEvent e)
	{

	}
}
