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

package mclachlan.brewday.recipe;

import mclachlan.brewday.StringUtils;
import mclachlan.brewday.ingredients.Misc;
import mclachlan.brewday.math.Quantity;
import mclachlan.brewday.math.TimeUnit;
import mclachlan.brewday.math.WeightUnit;

/**
 *
 */
public class MiscAddition extends IngredientAddition
{
	private Misc misc;
	private Quantity quantity;

	public MiscAddition()
	{
	}

	public MiscAddition(Misc misc, Quantity quantity, TimeUnit time)
	{
		this.misc = misc;
		setQuantity(quantity);
		setTime(time);
	}

	public Misc getMisc()
	{
		return misc;
	}

	public void setMisc(Misc misc)
	{
		this.misc = misc;
	}

	@Override
	public String getName()
	{
		return misc.getName();
	}

	@Override
	public void setName(String newName)
	{
		// not possible
	}

	@Override
	public Type getType()
	{
		return Type.MISC;
	}

	public Quantity getQuantity()
	{
		return quantity;
	}

	public void setQuantity(Quantity q)
	{
		this.quantity = q;
	}

	@Override
	public IngredientAddition clone()
	{
		return new MiscAddition(
			this.misc,
			new WeightUnit(this.quantity.get()),
			this.getTime());
	}

	@Override
	public String toString()
	{
		return StringUtils.getUiString("misc.addition.toString",
			getName(),
			getQuantity().get(Quantity.Unit.GRAMS),
			getTime().get(Quantity.Unit.MINUTES));
	}
}
