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
import mclachlan.brewday.ingredients.Misc;
import mclachlan.brewday.math.Quantity;
import mclachlan.brewday.math.TimeUnit;
import mclachlan.brewday.process.ProcessStep;
import mclachlan.brewday.recipe.IngredientAddition;
import mclachlan.brewday.recipe.MiscAddition;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 */
class MiscAdditionDialog extends IngredientAdditionDialog<MiscAddition, Misc>
{
	private QuantitySelectAndEditWidget quantity;
	private QuantityEditWidget<TimeUnit> time;

	/*-------------------------------------------------------------------------*/
	public MiscAdditionDialog(ProcessStep step, MiscAddition addition)
	{
		super(JfxUi.miscIcon, "common.add.misc", step);

		if (addition != null)
		{
			quantity.refresh(addition.getQuantity(), addition.getUnit());
			time.refresh(addition.getTime());
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	protected IngredientAddition.Type getIngredientType()
	{
		return IngredientAddition.Type.MISC;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	protected void addUiStuffs(MigPane pane)
	{
		Settings settings = Database.getInstance().getSettings();

		IngredientAddition.Type ingType = IngredientAddition.Type.MISC;
		Quantity.Unit unit = settings.getUnitForStepAndIngredient(Quantity.Type.WEIGHT, getStep().getType(), ingType);
		Quantity.Unit timeUnit = settings.getUnitForStepAndIngredient(Quantity.Type.TIME, getStep().getType(), ingType);

		quantity = new QuantitySelectAndEditWidget(unit);
		pane.add(new Label(StringUtils.getUiString("recipe.amount")));
		pane.add(quantity, "wrap");

		time = new QuantityEditWidget<>(timeUnit);
		pane.add(new Label(StringUtils.getUiString("recipe.time")));
		pane.add(time, "wrap");
	}

	/*-------------------------------------------------------------------------*/
	protected MiscAddition createIngredientAddition(
		Misc selectedItem)
	{
		return new MiscAddition(selectedItem, quantity.getQuantity(), quantity.getUnit(), time.getQuantity());
	}

	/*-------------------------------------------------------------------------*/
	protected Map<String, Misc> getReferenceIngredients()
	{
		return Database.getInstance().getMiscs();
	}

	/*-------------------------------------------------------------------------*/
	protected TableColumn<Misc, String>[] getColumns()
	{
		return new TableColumn[]
			{
				getPropertyValueTableColumn("misc.name", "name"),
				getPropertyValueTableColumn("misc.type", "type"),
				getPropertyValueTableColumn("misc.use", "use"),
				getPropertyValueTableColumn("misc.usage.recommendation", "usageRecommendation")
			};
	}
}