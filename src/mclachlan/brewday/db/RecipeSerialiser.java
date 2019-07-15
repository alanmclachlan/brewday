package mclachlan.brewday.db;

import java.util.*;
import mclachlan.brewday.db.v2.V2SerialiserMap;
import mclachlan.brewday.db.v2.V2Utils;
import mclachlan.brewday.process.ProcessStep;
import mclachlan.brewday.recipe.Recipe;

/**
 *
 */
public class RecipeSerialiser implements V2SerialiserMap<Recipe>
{
	private StepSerialiser stepSerialiser = new StepSerialiser();

	/*-------------------------------------------------------------------------*/
	@Override
	public Map toMap(Recipe recipe)
	{
		Map result = new HashMap();

		result.put("name", recipe.getName());
		result.put("equipmentProfile", recipe.getEquipmentProfile());
		result.put("style", recipe.getStyle());
		result.put("steps", V2Utils.serialiseList(recipe.getSteps(), stepSerialiser));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Recipe fromMap(Map<String, ?> map)
	{
		String name = (String)map.get("name");
		String equipmentProfile = (String)map.get("equipmentProfile");
		String style = (String)map.get("style");
		List<ProcessStep> steps = V2Utils.deserialiseList((List)map.get("steps"), stepSerialiser);

		return new Recipe(name, equipmentProfile, style, steps);
	}
}
