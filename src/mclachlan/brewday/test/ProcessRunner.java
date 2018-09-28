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

package mclachlan.brewday.test;

import java.util.*;
import mclachlan.brewday.database.Database;
import mclachlan.brewday.ingredients.Fermentable;
import mclachlan.brewday.ingredients.Hop;
import mclachlan.brewday.process.*;
import mclachlan.brewday.recipe.*;

/**
 *
 */
public class ProcessRunner
{
	public static void main(String[] args) throws Exception
	{
		Recipe recipe = getBatch();

		for (ProcessStep s : recipe.getSteps())
		{
			ErrorsAndWarnings log = new ErrorsAndWarnings();
			System.out.println("["+s.getName()+"]");
			Collection<String> outputVolumes = s.getOutputVolumes();
			s.apply(recipe.getVolumes(), recipe, log);
			for (String vs : outputVolumes)
			{
				System.out.println(recipe.getVolumes().getVolume(vs));
			}
		}
	}

	public static Recipe getBatch()
	{
		List<ProcessStep> p = new ArrayList<ProcessStep>();

		Map<String, Fermentable> ferms = Database.getInstance().getReferenceFermentables();
		Map<String, Hop> hops = Database.getInstance().getReferenceHops();

		FermentableAddition baseMalt = new FermentableAddition(ferms.get("Pale Malt (2 Row) UK"), 6000);
		FermentableAdditionList grainBill = new FermentableAdditionList("Grain Bill 1", baseMalt);

		WaterAddition mashWater = new WaterAddition("Mash Water", 15000, 70);
		WaterAddition spargeWater = new WaterAddition("Sparge Water 1", 10000, 75);

		HopAddition cascade20g = new HopAddition(hops.get("Cascade"), 20);
		HopAdditionList hopCharge60 = new HopAdditionList("Hop Charge 60min", cascade20g);

		HopAddition citra20g = new HopAddition(hops.get("Citra"), 20);
		HopAdditionList hopCharge20 = new HopAdditionList("Hop Charge 20min", citra20g);

		List<AdditionSchedule> mashAdditions = new ArrayList<AdditionSchedule>();
		mashAdditions.add(new AdditionSchedule(grainBill.getName(), 60));
		mashAdditions.add(new AdditionSchedule(mashWater.getName(), 60));

		List<AdditionSchedule> hopCharges = new ArrayList<AdditionSchedule>();
		hopCharges.add(new AdditionSchedule(hopCharge60.getName(), 60));
		hopCharges.add(new AdditionSchedule(hopCharge20.getName(), 20));

		Volumes brew = new Volumes();
		brew.addInputVolume(grainBill.getName(), grainBill);
		brew.addInputVolume(mashWater.getName(), mashWater);
		brew.addInputVolume(spargeWater.getName(), spargeWater);
		brew.addInputVolume(hopCharge60.getName(), hopCharge60);
		brew.addInputVolume(hopCharge20.getName(), hopCharge20);

		p.add(new Mash("single infusion mash", "my mash desc", mashAdditions, "The Mash", 60D, 20D));
		p.add(new MashOut("mash out, drain", "gather first runnings", "The Mash", "First Runnings", 3000));

		p.add(new BatchSparge("batch sparge", "my batch sparge", "The Mash", "Sparge Water 1", "First Runnings", "Pre-boil"));

		p.add(new Boil("boil 60 min", "60 minute rolling boil", "Pre-boil", "Post-boil", hopCharges, 60D));
		p.add(new Stand("hop stand", "30 minute hop stand", "Post-boil", "Post hop stand", 30D));
		p.add(new Dilute("dilute to 30l", "top up and chill", "Post hop stand", "Post dilution", 30000, 5));
		p.add(new Cool("cool to 20C", "drop to fermentation temp", "Post dilution", "Post cool", 20));
		p.add(new Ferment("ferment to 1010", "primary fermentation", "Post cool", "Post fermentation", 10));
		p.add(new Cool("cold crash", "cold crash prior to packaging", "Post fermentation", "Post Cold Crash", 1));
		p.add(new PackageStep("package", "package", "Post Cold Crash", "My Pale Ale", 500));


		return new Recipe("Test Batch 1", p, brew);
	}
}
