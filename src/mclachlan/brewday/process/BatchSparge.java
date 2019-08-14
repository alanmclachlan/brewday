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

package mclachlan.brewday.process;

import java.util.*;
import mclachlan.brewday.StringUtils;
import mclachlan.brewday.equipment.EquipmentProfile;
import mclachlan.brewday.math.*;
import mclachlan.brewday.recipe.FermentableAddition;
import mclachlan.brewday.recipe.IngredientAddition;
import mclachlan.brewday.recipe.Recipe;
import mclachlan.brewday.recipe.WaterAddition;

/**
 *
 */
public class BatchSparge extends ProcessStep
{
	private String mashVolume;
	private String wortVolume;
	private String outputCombinedWortVolume;
	private String outputMashVolume;
	private String outputSpargeRunnings;

	/*-------------------------------------------------------------------------*/
	public BatchSparge()
	{
	}

	/*-------------------------------------------------------------------------*/
	public BatchSparge(
		String name,
		String description,
		String mashVolume,
		String wortVolume,
		String outputCombinedWortVolume,
		String outputSpargeRunnings,
		String outputMashVolume,
		List<IngredientAddition> ingredients)
	{
		super(name, description, Type.BATCH_SPARGE);
		this.mashVolume = mashVolume;
		this.wortVolume = wortVolume;
		this.outputCombinedWortVolume = outputCombinedWortVolume;
		this.outputSpargeRunnings = outputSpargeRunnings;
		this.outputMashVolume = outputMashVolume;
		setIngredients(ingredients);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Constructor that sets the fields appropriately for the given batch.
	 */
	public BatchSparge(Recipe recipe)
	{
		super(recipe.getUniqueStepName(Type.BATCH_SPARGE), StringUtils.getProcessString("batch.sparge.desc"), Type.BATCH_SPARGE);

		this.mashVolume = recipe.getVolumes().getVolumeByType(Volume.Type.MASH);
		this.wortVolume = recipe.getVolumes().getVolumeByType(Volume.Type.WORT);

		this.outputCombinedWortVolume = StringUtils.getProcessString("batch.sparge.combined.wort", getName());
		this.outputSpargeRunnings = StringUtils.getProcessString("batch.sparge.sparge.runnings", getName());
		this.outputMashVolume = StringUtils.getProcessString("batch.sparge.lautered.mash", getName());
	}

	/*-------------------------------------------------------------------------*/
	public BatchSparge(BatchSparge step)
	{
		super(step.getName(), step.getDescription(), Type.BATCH_SPARGE);

		this.mashVolume = step.mashVolume;
		this.wortVolume = step.wortVolume;

		this.outputMashVolume = step.outputMashVolume;
		this.outputSpargeRunnings = step.outputSpargeRunnings;
		this.outputCombinedWortVolume = step.outputCombinedWortVolume;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void apply(Volumes volumes, EquipmentProfile equipmentProfile, ErrorsAndWarnings log)
	{
		if (!volumes.contains(wortVolume))
		{
			log.addError(StringUtils.getProcessString("volumes.does.not.exist", wortVolume));
			return;
		}

		WaterAddition spargeWater = null;

		for (IngredientAddition item : getIngredients())
		{
			if (item instanceof WaterAddition)
			{
				spargeWater = (WaterAddition)item;
			}
		}

		if (spargeWater == null)
		{
			log.addError(StringUtils.getProcessString("batch.sparge.no.water.additions"));
			return;
		}

		Volume input = volumes.getVolume(wortVolume);
		Volume mash = volumes.getVolume(mashVolume);

		double totalGristWeight = 0;
		for (IngredientAddition f : mash.getIngredientAdditions(IngredientAddition.Type.FERMENTABLES))
		{
			totalGristWeight += ((FermentableAddition)f).getWeight().get(Quantity.Unit.GRAMS);
		}
		DensityUnit mashExtract = mash.getGravity();
		VolumeUnit absorbedWater = Equations.calcAbsorbedWater(new WeightUnit(totalGristWeight));

		// add the dead space, because that is still left over
		VolumeUnit totalMashWater = new VolumeUnit(
			absorbedWater.get(Quantity.Unit.MILLILITRES) + 0);
		// todo: move to equipment profile
//			mash.getTunDeadSpace().get(Quantity.Unit.MILLILITRES));

		// model the batch sparge as a dilution of the extract remaining

		DensityUnit spargeGravity = Equations.calcGravityWithVolumeChange(
			totalMashWater,
			mashExtract,
			new VolumeUnit(
				totalMashWater.get() + spargeWater.getVolume().get()));

		VolumeUnit volumeOut = new VolumeUnit(
			input.getVolume().get(Quantity.Unit.MILLILITRES) +
			spargeWater.getVolume().get(Quantity.Unit.MILLILITRES));

		DensityUnit gravityOut = Equations.calcCombinedGravity(
			input.getVolume(),
			input.getGravity(),
			spargeWater.getVolume(),
			spargeGravity);

		TemperatureUnit tempOut =
			Equations.calcNewFluidTemperature(
				input.getVolume(),
				input.getTemperature(),
				spargeWater.getVolume(),
				spargeWater.getTemperature());

		// todo: incorrect, fix for sparging!
		ColourUnit colourOut = input.getColour();

		// output the lautered mash volume, in case it needs to be input into further batch sparge steps
		volumes.addVolume(
			outputMashVolume,
			new Volume(
				outputMashVolume,
				Volume.Type.MASH,
				mash.getVolume(),
				mash.getIngredientAdditions(IngredientAddition.Type.FERMENTABLES),
				(WaterAddition)mash.getIngredientAddition(IngredientAddition.Type.WATER),
				mash.getTemperature(),
				spargeGravity,
				mash.getColour() // todo replace with sparge colour
				));

		// output the isolated sparge runnings, in case of partigyle brews
		volumes.addVolume(
			outputSpargeRunnings,
			new Volume(
				outputSpargeRunnings,
				Volume.Type.WORT,
				spargeWater.getVolume(),
				spargeWater.getTemperature(),
				input.getFermentability(),
				spargeGravity,
				input.getAbv(),
				colourOut, // todo replace with sparge colour
				input.getBitterness()));

		// output the combined worts, for convenience to avoid a combine step
		// right after every batch sparge step
		volumes.addVolume(
			outputCombinedWortVolume,
			new Volume(
				outputCombinedWortVolume,
				Volume.Type.WORT,
				volumeOut,
				tempOut,
				input.getFermentability(),
				gravityOut,
				new PercentageUnit(0D),
				colourOut,
				new BitternessUnit(0D)));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void dryRun(Recipe recipe, ErrorsAndWarnings log)
	{
		recipe.getVolumes().addVolume(outputMashVolume, new Volume(Volume.Type.MASH));
		recipe.getVolumes().addVolume(outputSpargeRunnings, new Volume(Volume.Type.WORT));
		recipe.getVolumes().addVolume(outputCombinedWortVolume, new Volume(Volume.Type.WORT));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String describe(Volumes v)
	{
		return StringUtils.getProcessString("batch.sparge.step.desc");
	}

	@Override
	public Collection<String> getInputVolumes()
	{
		return Arrays.asList(mashVolume, wortVolume);
	}

	@Override
	public Collection<String> getOutputVolumes()
	{
		return Collections.singletonList(outputCombinedWortVolume);
	}

	@Override
	public List<IngredientAddition.Type> getSupportedIngredientAdditions()
	{
		return Collections.singletonList(IngredientAddition.Type.WATER);
	}

	/*-------------------------------------------------------------------------*/

	public String getMashVolume()
	{
		return mashVolume;
	}

	public String getWortVolume()
	{
		return wortVolume;
	}

	public String getOutputCombinedWortVolume()
	{
		return outputCombinedWortVolume;
	}

	public void setMashVolume(String mashVolume)
	{
		this.mashVolume = mashVolume;
	}

	public void setWortVolume(String wortVolume)
	{
		this.wortVolume = wortVolume;
	}

	public void setOutputCombinedWortVolume(String outputCombinedWortVolume)
	{
		this.outputCombinedWortVolume = outputCombinedWortVolume;
	}

	public String getOutputMashVolume()
	{
		return outputMashVolume;
	}

	public String getOutputSpargeRunnings()
	{
		return outputSpargeRunnings;
	}

	public void setOutputMashVolume(String outputMashVolume)
	{
		this.outputMashVolume = outputMashVolume;
	}

	public void setOutputSpargeRunnings(String outputSpargeRunnings)
	{
		this.outputSpargeRunnings = outputSpargeRunnings;
	}
}
