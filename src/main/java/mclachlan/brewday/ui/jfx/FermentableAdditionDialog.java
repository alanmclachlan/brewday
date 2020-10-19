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
 * along with Brewday.  If not, see https://www.gnu.org/licenses.
 */

package mclachlan.brewday.ui.jfx;

import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import mclachlan.brewday.Settings;
import mclachlan.brewday.StringUtils;
import mclachlan.brewday.db.Database;
import mclachlan.brewday.ingredients.Fermentable;
import mclachlan.brewday.math.Quantity;
import mclachlan.brewday.math.TimeUnit;
import mclachlan.brewday.math.WeightUnit;
import mclachlan.brewday.process.ProcessStep;
import mclachlan.brewday.recipe.FermentableAddition;
import mclachlan.brewday.recipe.IngredientAddition;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 */
class FermentableAdditionDialog extends IngredientAdditionDialog<FermentableAddition, Fermentable>
{
	private QuantityEditWidget<WeightUnit> weight;
	private QuantityEditWidget<TimeUnit> time;

	/*-------------------------------------------------------------------------*/
	public FermentableAdditionDialog(ProcessStep step, FermentableAddition addition)
	{
		super(JfxUi.fermentableIcon, "common.add.fermentable", step);

		if (addition != null)
		{
			weight.refresh(addition.getQuantity());
			time.refresh(addition.getTime());
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	protected IngredientAddition.Type getIngredientType()
	{
		return IngredientAddition.Type.FERMENTABLES;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	protected void addUiStuffs(MigPane pane)
	{
		Settings settings = Database.getInstance().getSettings();

		Quantity.Unit weightUnit = settings.getUnitForStepAndIngredient(Quantity.Type.WEIGHT, getStep().getType(), IngredientAddition.Type.FERMENTABLES);
		Quantity.Unit timeUnit = settings.getUnitForStepAndIngredient(Quantity.Type.TIME, getStep().getType(), IngredientAddition.Type.FERMENTABLES);

		weight = new QuantityEditWidget<>(weightUnit);
		pane.add(new Label(StringUtils.getUiString("recipe.amount")));
		pane.add(weight, "wrap");

		time = new QuantityEditWidget<>(timeUnit);
		pane.add(new Label(StringUtils.getUiString("recipe.time")));
		pane.add(time, "wrap");
	}

	/*-------------------------------------------------------------------------*/
	protected FermentableAddition createIngredientAddition(
		Fermentable selectedItem)
	{
		return new FermentableAddition(
			selectedItem, weight.getQuantity(), weight.getUnit(), time.getQuantity());
	}

	/*-------------------------------------------------------------------------*/
	protected Map<String, Fermentable> getReferenceIngredients()
	{
		return Database.getInstance().getFermentables();
	}

	/*-------------------------------------------------------------------------*/
	protected TableColumn<Fermentable, String>[] getColumns()
	{
		return new TableColumn[]
			{
				getPropertyValueTableColumn("fermentable.name", "name"),
				getPropertyValueTableColumn("fermentable.type", "type"),
				getPropertyValueTableColumn("fermentable.origin", "origin"),
				getQuantityPropertyValueCol("fermentable.yield", Fermentable::getYield, Quantity.Unit.PERCENTAGE_DISPLAY),
				getQuantityPropertyValueCol("fermentable.colour", Fermentable::getColour, Quantity.Unit.SRM),
			};
	}
}