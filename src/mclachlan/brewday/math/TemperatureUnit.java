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

package mclachlan.brewday.math;

import mclachlan.brewday.BrewdayException;

/**
 *
 */
public class TemperatureUnit
{
	/**
	 * Temperature in C
	 */
	private double temperature;

	/**
	 * @param temperature
	 * 	in grams
	 */
	public TemperatureUnit(double temperature)
	{
		this.temperature = temperature;
	}

	/**
	 * @return
	 * 	temp in C
	 */
	public double get()
	{
		return temperature;
	}

	/**
	 * @param unit the unit to return a value in
	 * @return this temp in the given unit
	 */
	public double get(Unit unit)
	{
		switch (unit)
		{
			case CELSIUS:
				return this.temperature;
			case KELVIN:
				return this.temperature + 273.15D;
			case FAHRENHEIT:
				return this.temperature*9D/5D +32;
			default:
				throw new BrewdayException("Invalid: "+unit);
		}
	}

	/**
	 * @param c the temp in C
	 */
	public void set(double c)
	{
		this.temperature = c;
	}

	public void set(double amount, Unit unit)
	{
		switch (unit)
		{
			case CELSIUS:
				this.temperature = amount;
			case KELVIN:
				this.temperature = amount - 273.15D;
			case FAHRENHEIT:
				this.temperature = (amount -32) * 5D/9D;
			default:
				throw new BrewdayException("Invalid: "+unit);
		}
	}

	public static enum Unit
	{
		CELSIUS,
		KELVIN,
		FAHRENHEIT,
	}
}