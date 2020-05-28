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

import java.util.*;
import mclachlan.brewday.ingredients.Fermentable;
import mclachlan.brewday.ingredients.Hop;
import mclachlan.brewday.ingredients.Yeast;
import mclachlan.brewday.process.Volume;
import mclachlan.brewday.recipe.*;

/**
 *
 */
public class Equations
{
	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the new temperature of the body of fluid after an addition of
	 * some amount at a different temperature.
	 *
	 * @return New temp of the combined fluid volume.
	 */
	public static TemperatureUnit calcNewFluidTemperature(
		VolumeUnit currentVolume,
		TemperatureUnit currentTemperature,
		VolumeUnit volumeAddition,
		TemperatureUnit tempAddition)
	{
		boolean estimated =
			currentVolume.isEstimated() || currentTemperature.isEstimated() ||
			volumeAddition.isEstimated() || tempAddition.isEstimated();

		return new TemperatureUnit(
			(
				(currentVolume.get(Quantity.Unit.MILLILITRES) *
					currentTemperature.get(Quantity.Unit.CELSIUS) *
					Const.SPECIFIC_HEAT_OF_WATER)
				+
				volumeAddition.get(Quantity.Unit.MILLILITRES) *
					tempAddition.get(Quantity.Unit.CELSIUS) *
					Const.SPECIFIC_HEAT_OF_WATER
			)
			/
			(
				currentVolume.get(Quantity.Unit.MILLILITRES) *
					Const.SPECIFIC_HEAT_OF_WATER
					+
					volumeAddition.get(Quantity.Unit.MILLILITRES) *
						Const.SPECIFIC_HEAT_OF_WATER
			),
			Quantity.Unit.CELSIUS,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the gravity change when a volume change occurs.
	 *
	 * @return New gravity of the output volume.
	 */
	public static DensityUnit calcGravityWithVolumeChange(
		VolumeUnit volumeIn,
		DensityUnit gravityIn,
		VolumeUnit volumeOut)
	{
		boolean estimated = volumeIn.isEstimated() || gravityIn.isEstimated() || volumeOut.isEstimated();

		return new DensityUnit(
			gravityIn.get() *
				volumeIn.get(Quantity.Unit.MILLILITRES) /
				volumeOut.get(Quantity.Unit.MILLILITRES),
			gravityIn.getUnit(),
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the gravity of the combined fluids.
	 * source: https://www.quora.com/How-do-I-find-the-specific-gravity-when-two-liquids-are-mixed
	 * @return New gravity of the output volume.
	 */
	public static DensityUnit calcCombinedGravity(
		VolumeUnit v1,
		DensityUnit d1,
		VolumeUnit v2,
		DensityUnit d2)
	{
		boolean estimated = v1.isEstimated() || d1.isEstimated() || v2.isEstimated() || d2.isEstimated();

		return new DensityUnit(
			(v1.get() + v2.get()) /
				(v1.get()/d1.get()
					+
					v2.get()/d2.get()),
			d1.getUnit(),
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the colour of the combined fluids.
	 * Source: I made this up
	 * @return New colour of the output volume.
	 */
	public static ColourUnit calcCombinedColour(
		VolumeUnit v1,
		ColourUnit c1,
		VolumeUnit v2,
		ColourUnit c2)
	{
		boolean estimated = v1.isEstimated() || c1.isEstimated() || v2.isEstimated() || c2.isEstimated();

		return new ColourUnit(
			(v1.get() + v2.get()) /
				(v1.get()/c1.get()
					+
					v2.get()/c2.get()),
			c1.getUnit(),
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the bitterness of the combined fluids.
	 * Source: I made this up
	 * @return New bitterness of the combined volume.
	 */
	public static BitternessUnit calcCombinedBitterness(
		VolumeUnit v1,
		BitternessUnit b1,
		VolumeUnit v2,
		BitternessUnit b2)
	{
		boolean estimated = v1.isEstimated() || b1.isEstimated() || v2.isEstimated() || b2.isEstimated();

		return new BitternessUnit(
			(v1.get() + v2.get()) /
				(v1.get()/b1.get()
					+
					v2.get()/b2.get()),
			b1.getUnit(),
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the volume decrease due to cooling.
	 *
	 * @return The new volume
	 */
	public static VolumeUnit calcCoolingShrinkage(
		VolumeUnit volumeIn,
		TemperatureUnit tempDecrease)
	{
		boolean estimated = volumeIn.isEstimated() || tempDecrease.isEstimated();

		return new VolumeUnit(
			volumeIn.get(Quantity.Unit.MILLILITRES) *
			(1 - (Const.COOLING_SHRINKAGE * tempDecrease.get(Quantity.Unit.CELSIUS))),
			Quantity.Unit.MILLILITRES,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the ABV change when a volume change occurs
	 *
	 * @return the new ABV
	 */
	public static PercentageUnit calcAbvWithVolumeChange(
		VolumeUnit volumeIn,
		PercentageUnit abvIn,
		VolumeUnit volumeOut)
	{
		boolean estimated = volumeIn.isEstimated() || abvIn.isEstimated() || volumeOut.isEstimated();
		double abvInD = abvIn==null ? 0 : abvIn.get();
		double volInD = volumeIn.get();
		double volOutD = volumeOut.get();
		return new PercentageUnit(abvInD * volInD / volOutD, estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the ABV change when a gravity change occurs.
	 * Source: http://www.brewunited.com/abv_calculator.php
	 *
	 * @return the new ABV, expressed within 0..1
	 */
	public static PercentageUnit calcAbvWithGravityChange(
		DensityUnit gravityIn,
		DensityUnit gravityOut)
	{
		double abv = (gravityIn.get(Quantity.Unit.SPECIFIC_GRAVITY) - gravityOut.get(Quantity.Unit.SPECIFIC_GRAVITY)) * Const.ABV_CONST;
		boolean estimated = gravityIn.isEstimated() || gravityOut.isEstimated();
		return new PercentageUnit(abv/100D, estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the volume of the a new mash
	 *
	 * @param grainWeight in g
	 * @param waterVolume in ml
	 * @return Volume in ml
	 */
	public static VolumeUnit calcMashVolume(
		WeightUnit grainWeight,
		VolumeUnit waterVolume)
	{
		VolumeUnit absorbedWater = calcAbsorbedWater(grainWeight);
		double waterDisplacement = grainWeight.get(Quantity.Unit.GRAMS) * Const.GRAIN_WATER_DISPLACEMENT;
		boolean estimated = grainWeight.isEstimated() || waterVolume.isEstimated();

		return new VolumeUnit(
			waterVolume.get(Quantity.Unit.MILLILITRES)
				- absorbedWater.get(Quantity.Unit.MILLILITRES)
				+ waterDisplacement + grainWeight.get(Quantity.Unit.GRAMS),
			Quantity.Unit.MILLILITRES,
			estimated);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param grainWeight in g
	 * @return volume of water absorbed in the grain, in ml
	 */
	public static VolumeUnit calcAbsorbedWater(WeightUnit grainWeight)
	{
		boolean estimated = grainWeight.isEstimated();

		return new VolumeUnit(
			grainWeight.get(Quantity.Unit.KILOGRAMS) * Const.GRAIN_WATER_ABSORPTION,
			Quantity.Unit.LITRES,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the max volume of wort that can be drained from a given mash
	 *
	 * @param grainWeight in g
	 * @param waterVolume in ml
	 * @return Volume in ml
	 */
	public static VolumeUnit calcWortVolume(
		WeightUnit grainWeight, VolumeUnit waterVolume)
	{
		VolumeUnit absorbedWater = calcAbsorbedWater(grainWeight);

		boolean estimated = absorbedWater.isEstimated() || grainWeight.isEstimated();
		return new VolumeUnit(
			waterVolume.get(Quantity.Unit.MILLILITRES)
				- absorbedWater.get(Quantity.Unit.MILLILITRES),
			Quantity.Unit.MILLILITRES,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates the SRM of the output wort using the Morey formula. Source:
	 * http://brewwiki.com/index.php/Estimating_Color
	 *
	 * @param waterVolume in ml
	 * @return wort colour in SRM
	 */
	public static ColourUnit calcColourSrmMoreyFormula(
		List<IngredientAddition> grainBill,
		VolumeUnit waterVolume)
	{
		if (grainBill.isEmpty())
		{
			return new ColourUnit(0D, Quantity.Unit.SRM, false);
		}

		// calc malt colour units
		double mcu = 0D;
		for (IngredientAddition item : grainBill)
		{
			FermentableAddition fa = (FermentableAddition)item;
			Fermentable f = fa.getFermentable();
			mcu += (f.getColour() * fa.getQuantity().get(Quantity.Unit.POUNDS));
		}

		mcu /= waterVolume.get(Quantity.Unit.US_GALLON);

		// apply Dan Morey's formula
		return new ColourUnit(1.499D * (Math.pow(mcu, 0.6859D)), Quantity.Unit.SRM, true);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Calculates the colour impact of a boil.
	 *
	 * @param colourIn
	 * @return
	 */
	public static ColourUnit calcColourAfterBoil(ColourUnit colourIn)
	{
		//
		// Brewday has an issue with colour calculations: existing formulae (eg
		// Morey) require the use of MCUs based on post-boil gravity.
		// (source: http://www.beersmith.com/forum/index.php?topic=5797.0)
		// But Brewday can't easily do that because the process steps are
		// decoupled and there isn't necessarily a 1:1 mapping from mash to boil.
		//
		// One option would be passing MCUs around as a metric in the volumes,
		// waiting to arrive at a post-boil volume. I doubt this would work
		// properly and haven't tried it yet.
		//
		// Instead I'm doing this: the typical homebrew process produces a post-boil
		// volume about 60% of the input water. Working out a table of SRM values
		// shows me that the SRM output is 42% higher when the MCU's are worked
		// out with 60% of the water volume.
		// So to model this in Brewday at boil time we increase the SRM by 42%.
		//
		// This is kinda wacky I admit. But to quote Palmer, there are "inherent
		// limits of any model for beer colour" so I guess it's best to be a bit
		// relaxed about this stuff.
		//

		double srmIn = colourIn.get(Quantity.Unit.SRM);
		return new ColourUnit(srmIn*1.42, Quantity.Unit.SRM);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param volumeIn in ml, assumed SRM of 0
	 * @param colourIn in SRM
	 * @param volumeOut in ml
	 * @return colour in SRM
	 */
	public static ColourUnit calcColourWithVolumeChange(
		VolumeUnit volumeIn,
		ColourUnit colourIn,
		VolumeUnit volumeOut)
	{
		boolean estimated = volumeIn.isEstimated() || colourIn.isEstimated() || volumeOut.isEstimated();

		return new ColourUnit(colourIn.get(Quantity.Unit.SRM) *
			volumeIn.get(Quantity.Unit.MILLILITRES) /
			volumeOut.get(Quantity.Unit.MILLILITRES),
			Quantity.Unit.SRM,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param volumeIn assumed IBU of 0
	 */
	public static BitternessUnit calcBitternessWithVolumeChange(
		VolumeUnit volumeIn,
		BitternessUnit bitternessIn,
		VolumeUnit volumeOut)
	{
		if (bitternessIn == null)
		{
			return new BitternessUnit(0);
		}

		boolean estimated = volumeIn.isEstimated() || bitternessIn.isEstimated() || volumeOut.isEstimated();

		return new BitternessUnit(
			bitternessIn.get(Quantity.Unit.IBU) *
				volumeIn.get(Quantity.Unit.MILLILITRES) /
				volumeOut.get(Quantity.Unit.MILLILITRES),
			Quantity.Unit.IBU,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param colour in SRM
	 * @return colour after fermentation, in SRM
	 */
	public static ColourUnit calcColourAfterFermentation(ColourUnit colour)
	{
		return new ColourUnit(
			colour.get(Quantity.Unit.SRM) * (1 - Const.COLOUR_LOSS_DURING_FERMENTATION),
			Quantity.Unit.SRM,
			colour.isEstimated());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Source: http://www.realbeer.com/hops/research.html
	 * @param steepDuration in minutes
	 * @param wortGravity in GU (average during the steep duration)
	 * @param wortVolume in l (average during the steep duration)
	 */
	public static BitternessUnit calcIbuTinseth(
		HopAddition hopAddition,
		TimeUnit steepDuration,
		DensityUnit wortGravity,
		VolumeUnit wortVolume,
		double equipmentHopUtilisation)
	{
		// adjust to sg
		double aveGrav = wortGravity.get(DensityUnit.Unit.SPECIFIC_GRAVITY);

		double bignessFactor = 1.65D * Math.pow(0.000125, aveGrav-1);
		double boilTimeFactor = (1D - Math.exp(-0.04 * steepDuration.get(Quantity.Unit.MINUTES))) / 4.15D;
		double decimalAAUtilisation = bignessFactor * boilTimeFactor;

		Hop h = hopAddition.getHop();
		double mgPerL = (h.getAlphaAcid() * hopAddition.getQuantity().get(Quantity.Unit.GRAMS) * 1000) /
			(wortVolume.get(Quantity.Unit.LITRES));

		boolean estimated = wortGravity.isEstimated() || wortVolume.isEstimated();

		return new BitternessUnit(
			(mgPerL * decimalAAUtilisation) * equipmentHopUtilisation,
			Quantity.Unit.IBU,
			estimated);
	}

	/*-------------------------------------------------------------------------*/
	public static BitternessUnit calcMashHopIbu(
		List<HopAddition> hopAdditions,
		DensityUnit wortDensity,
		VolumeUnit wortVolume,
		double equipmentHopUtilisation)
	{
		BitternessUnit bitternessOut = new BitternessUnit(0);
		for (IngredientAddition hopCharge : hopAdditions)
		{
			bitternessOut.add(
				Equations.calcIbuTinseth(
					(HopAddition)hopCharge,
					hopCharge.getTime(),
					wortDensity,
					wortVolume,
					equipmentHopUtilisation));
		}

		// mash hop bitterness adjustment is -80% (source: BeerSmith)
		return new BitternessUnit(bitternessOut.get()*0.2D);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Given grain and water, returns the resultant mash temp.
	 * Source: http://howtobrew.com/book/section-3/the-methods-of-mashing/calculations-for-boiling-water-additions
	 * (rearranged the terms)
	 * @return
	 *  mash temp in C
	 */
	public static TemperatureUnit calcMashTemp(
		WeightUnit totalGrainWeight,
		WaterAddition strikeWater,
		TemperatureUnit grainTemp)
	{
		// ratio water to grain
		double r = strikeWater.getVolume().get(Quantity.Unit.MILLILITRES) /
			totalGrainWeight.get(Quantity.Unit.GRAMS);

		TemperatureUnit tw = strikeWater.getTemperature();

		double c = Const.MASH_TEMP_THERMO_CONST;

		boolean estimated = totalGrainWeight.isEstimated() || grainTemp.isEstimated();

		return new TemperatureUnit(
			(c*grainTemp.get(Quantity.Unit.CELSIUS)
			+ r*tw.get(Quantity.Unit.CELSIUS))
				/ (c + r),
			Quantity.Unit.CELSIUS,
			estimated);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Source: http://braukaiser.com/wiki/index.php/Effects_of_mash_parameters_on_fermentability_and_efficiency_in_single_infusion_mashing
	 * @param mashTemp
	 * 	The average mash temperature
	 * @return
	 * 	The estimated attenuation limit of the wort produced
	 */
	public static PercentageUnit getWortAttenuationLimit(
		TemperatureUnit mashTemp)
	{
		// per Braukaiser:
		// for mash temp >= 67.5C we model a line A = 0.9 - 0.04*(T - 67.5)
		// for mash temp < 67.5 we model a line A = 0.9 - 0.015*(67.5-T)

		double result;
		double tempC = mashTemp.get(Quantity.Unit.CELSIUS);

		if (tempC >= 67.5)
		{
			result = 0.9 - 0.04*(tempC-67.5);
		}
		else
		{
			result = 0.9 - 0.015*(67.5-tempC);
		}

		return new PercentageUnit(result, true);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 *
	 * @return
	 * 	Estimated apparent attenuation, in %
	 */
	public static double calcEstimatedAttenuation(Volume inputWort, YeastAddition yeastAddition)
	{
		PercentageUnit wortAttenuationLimit = inputWort.getFermentability();
		if (wortAttenuationLimit == null)
		{
			// assume the peak
			wortAttenuationLimit = new PercentageUnit(0.9D);
		}

		Yeast yeast = yeastAddition.getYeast();
		double yeastAttenuation = yeast.getAttenuation();
		double wortAttenuation = wortAttenuationLimit.get();

		// Return an attenuation midway between the yeast average attenuation and
		// the wort attenuation limit.
		// I have no scientific basis for this piece of math, it just feel about
		// right from personal experience looking at the listed yeast attenuation
		// numbers in the db

		if (wortAttenuation < yeastAttenuation)
		{
			return wortAttenuation + (yeastAttenuation-wortAttenuation)/2;
		}
		else
		{
			return yeastAttenuation + (wortAttenuation-yeastAttenuation)/2;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Calculates mash gravity using the grain yield to derive degrees Plato
	 * <p>
	 * Source: http://braukaiser.com/wiki/index.php/Understanding_Efficiency
	 */
	public static DensityUnit calcMashExtractContentFromYield(
		List<IngredientAddition> grainBill,
		double mashEfficiency,
		WaterAddition mashWater)
	{
		WeightUnit totalGrainWeight = getTotalGrainWeight(grainBill);

		// mash water-to-grain ratio in l/kg
		double r =
			(mashWater.getVolume().get(Quantity.Unit.LITRES)) /
				totalGrainWeight.get(Quantity.Unit.KILOGRAMS);

		double result = 0D;

		for (IngredientAddition item : grainBill)
		{
			FermentableAddition fa = (FermentableAddition)item;
			double yield = fa.getFermentable().getYield();
			double proportion = fa.getQuantity().get(Quantity.Unit.GRAMS) /
				totalGrainWeight.get(Quantity.Unit.GRAMS);

			result += mashEfficiency * 100 * proportion * (yield / (r + yield));
		}

		return new DensityUnit(result, DensityUnit.Unit.PLATO, true);
	}

	/*-------------------------------------------------------------------------*/
	public static WeightUnit getTotalGrainWeight(List<IngredientAddition> grainBill)
	{
		double result = 0D;
		for (IngredientAddition item : grainBill)
		{
			result += item.getQuantity().get(Quantity.Unit.GRAMS);
		}
		return new WeightUnit(result, Quantity.Unit.GRAMS, false);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Calculates mash gravity using the extract points / ppg method to derive SG.
	 * <p>
	 * Source: https://byo.com/article/hitting-target-original-gravity-and-volume-advanced-homebrewing/
	 */
	public static DensityUnit calcMashExtractContentFromPppg(
		List<IngredientAddition> grainBill,
		double mashEfficiency,
		VolumeUnit volumeOut)
	{
		double extractPoints = 0D;
		for (IngredientAddition item : grainBill)
		{
			if (item instanceof FermentableAddition)
			{
				FermentableAddition g = (FermentableAddition)item;
				extractPoints += g.getQuantity().get(Quantity.Unit.POUNDS) * g.getFermentable().getExtractPotential();
			}
		}

		double actualExtract = extractPoints * mashEfficiency;

		return new DensityUnit(actualExtract / volumeOut.get(Quantity.Unit.US_GALLON));
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Calculates the gravity returned by steeping the given grains.
	 * Source: Beersmith
	 */
	public static DensityUnit calcSteepedGrainsGravity(
		List<IngredientAddition> grainBill,
		VolumeUnit volumeOut)
	{
		// treat a steep like a mash with 15% efficiency
		return calcMashExtractContentFromPppg(grainBill, 0.15D, volumeOut);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Calculates the gravity provided by just dissolving the given fermentable
	 * in the given volume of fluid.
	 * <p>
	 * Source: http://braukaiser.com/wiki/index.php/Troubleshooting_Brewhouse_Efficiency
	 * @return The additional gravity
	 */
	public static DensityUnit calcSolubleFermentableAdditionGravity(
		FermentableAddition fermentableAddition,
		VolumeUnit volume)
	{
		Fermentable.Type type = fermentableAddition.getFermentable().getType();
		if (type == Fermentable.Type.GRAIN || type == Fermentable.Type.ADJUNCT)
		{
			// these are not soluble
			return new DensityUnit(0);
		}

		double weightLb = fermentableAddition.getQuantity().get(Quantity.Unit.POUNDS);
		double volumeGal = volume.get(Quantity.Unit.US_GALLON);
		double pppg = fermentableAddition.getFermentable().getExtractPotential();

		double points = weightLb * pppg / volumeGal;

		return new DensityUnit(points, Quantity.Unit.GU, true);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Source: http://www.howtobrew.com/book/section-2/what-is-malted-grain/extraction-and-maximum-yield
	 * @param yield
	 * 	the grain yield in %
	 * @return
	 * 	the extract potential in ppg
	 */
	public static double calcExtractPotentialFromYield(double yield)
	{
		return 46 * yield;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Source: http://beersmith.com/blog/2010/09/07/apparent-and-real-attenuation-for-beer-brewers-part-1/
	 * @param start
	 * 	The starting gravity
	 * @param end
	 * 	The final gravity
	 * @return
	 * 	The % attenuation
	 */
	public static double calcAttenuation(DensityUnit start, DensityUnit end)
	{
		double sgStart = start.get(Quantity.Unit.SPECIFIC_GRAVITY);
		double sgEnd = end.get(Quantity.Unit.SPECIFIC_GRAVITY);

		return (sgStart - sgEnd) / (sgStart - 1D);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Source: http://braukaiser.com/wiki/index.php/Accurately_Calculating_Sugar_Additions_for_Carbonation
	 * See also: https://byo.com/article/master-the-action-carbonation/
	 *
	 * @param inputVolume
	 * 	The volume to be carbonated
	 * @param priming
	 * 	The nature and quantity of the substance used for priming
	 * @return
	 * 	The carbonation of the beer volume, in volumes CO2
	 */
	public static CarbonationUnit calcCarbonation(
		VolumeUnit inputVolume,
		FermentableAddition priming)
	{
		Fermentable fermentable = priming.getFermentable();

		if (fermentable.getType() == Fermentable.Type.GRAIN ||
			fermentable.getType() == Fermentable.Type.ADJUNCT)
		{
			// these are not fermentable without modification; zero carbonation
			return new CarbonationUnit(0);
		}

		WeightUnit weight = (WeightUnit)priming.getQuantity();

		// Each gram of fermentable extract is fermented into equal parts (by weight)
		// of alcohol and CO2 (this is not exactly true, but close enough for this calculation).

		double gramsPerLitre = 0.5D * fermentable.getYield() * weight.get(Quantity.Unit.GRAMS)
			/ inputVolume.get(Quantity.Unit.LITRES);

		boolean estimated = inputVolume.isEstimated();

		return new CarbonationUnit(gramsPerLitre, Quantity.Unit.GRAMS_PER_LITRE, estimated);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Source: http://braukaiser.com/documents/CO2_content_metric.pdf
	 *
	 * @param temp the temp of the solution
	 * @param pressure the pressure under which the solution is, in kPa
	 */
	public static CarbonationUnit calcEquilibriumCo2(
		TemperatureUnit temp,
		PressureUnit pressure)
	{
		double tBeer = temp.get(Quantity.Unit.KELVIN);
		double gramsPerLitre = (pressure.get(Quantity.Unit.BAR))
			* Math.pow(2.71828182845904, -10.73797 + (2617.25 / tBeer))
			* 10;

		boolean estimated = temp.isEstimated() || pressure.isEstimated();

		return new CarbonationUnit(gramsPerLitre, Quantity.Unit.GRAMS_PER_LITRE, estimated);
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Hop hop = new Hop();
		hop.setAlphaAcid(.2);
		HopAddition hopAdd = new HopAddition(hop, new WeightUnit(20),
			new TimeUnit(60, Quantity.Unit.MINUTES, false));

		for (double grav=1.01D; grav <1.08; grav = grav+.01)
		{
			BitternessUnit v = calcIbuTinseth(
				hopAdd,
				new TimeUnit(60, Quantity.Unit.MINUTES, false),
				new DensityUnit(grav, DensityUnit.Unit.SPECIFIC_GRAVITY),
				new VolumeUnit(20000),
				1.0D);

			System.out.println(grav+": "+v.get(Quantity.Unit.IBU));
		}
	}
}
