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

import java.io.File;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mclachlan.brewday.BrewdayException;
import mclachlan.brewday.Settings;
import mclachlan.brewday.StringUtils;
import mclachlan.brewday.batch.Batch;
import mclachlan.brewday.db.Database;
import mclachlan.brewday.db.v2.V2DataObject;
import mclachlan.brewday.equipment.EquipmentProfile;
import mclachlan.brewday.importexport.beerxml.BeerXmlParser;
import mclachlan.brewday.ingredients.*;
import mclachlan.brewday.recipe.Recipe;
import mclachlan.brewday.style.Style;
import org.tbee.javafx.scene.layout.fxml.MigPane;

import static mclachlan.brewday.StringUtils.getUiString;
import static mclachlan.brewday.ui.jfx.ImportPane.ImportDialog.Bit.*;

/**
 *
 */
public class ImportPane extends MigPane
{
	private TrackDirty parent;

	public ImportPane(TrackDirty parent)
	{
		this.parent = parent;
		Button importBeerXml = new Button(
			getUiString("tools.import.beerxml"),
			JfxUi.getImageView(JfxUi.importIcon, 32));

		this.add(importBeerXml, "wrap");

		// ----

		importBeerXml.setOnAction(event -> importBeerXml());
	}

	/*-------------------------------------------------------------------------*/
	private void importBeerXml()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(StringUtils.getUiString("tools.import.beerxml.title"));

		Settings settings = Database.getInstance().getSettings();
		String dir = settings.get(Settings.LAST_IMPORT_DIRECTORY);
		if (dir != null)
		{
			fileChooser.setInitialDirectory(new File(dir));
		}

		List<File> files = fileChooser.showOpenMultipleDialog(JfxUi.getInstance().getMainScene().getWindow());

		if (files != null)
		{
			String parent = files.get(0).getParent();
			if (parent != null)
			{
				settings.set(Settings.LAST_IMPORT_DIRECTORY, parent);
				Database.getInstance().saveSettings();
			}

			try
			{
				Map<Class<?>, Map<String, V2DataObject>> objs = new BeerXmlParser().parse(files);

				ImportDialog importDialog = new ImportDialog(objs);
				importDialog.showAndWait();

				if (!importDialog.output.isEmpty())
				{
					importData(objs, importDialog.output);
				}
			}
			catch (Exception e)
			{
				throw new BrewdayException(e);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void importData(
		Map<Class<?>, Map<String, V2DataObject>> objs,
		BitSet options)
	{
		Database db = Database.getInstance();

		importData(objs.get(Water.class), db.getWaters(), JfxUi.WATER, options.get(WATER_NEW.ordinal()), options.get(WATER_UPDATE.ordinal()));
		importData(objs.get(Fermentable.class), db.getFermentables(), JfxUi.FERMENTABLES, options.get(FERMENTABLE_NEW.ordinal()), options.get(FERMENTABLE_UPDATE.ordinal()));
		importData(objs.get(Hop.class), db.getHops(), JfxUi.HOPS, options.get(HOPS_NEW.ordinal()), options.get(HOPS_UPDATE.ordinal()));
		importData(objs.get(Yeast.class), db.getYeasts(), JfxUi.YEAST, options.get(YEASTS_NEW.ordinal()), options.get(YEASTS_UPDATE.ordinal()));
		importData(objs.get(Misc.class), db.getMiscs(), JfxUi.MISC, options.get(MISC_NEW.ordinal()), options.get(MISC_UPDATE.ordinal()));
		importData(objs.get(Style.class), db.getStyles(), JfxUi.STYLES, options.get(STYLE_NEW.ordinal()), options.get(STYLE_UPDATE.ordinal()));
		importData(objs.get(EquipmentProfile.class), db.getEquipmentProfiles(), JfxUi.EQUIPMENT_PROFILES, options.get(EQUIPMENT_NEW.ordinal()), options.get(EQUIPMENT_UPDATE.ordinal()));
		importData(objs.get(Recipe.class), db.getRecipes(), JfxUi.RECIPES, options.get(RECIPE_NEW.ordinal()), options.get(RECIPE_UPDATE.ordinal()));
		importData(objs.get(Batch.class), db.getBatches(), JfxUi.BATCHES, options.get(BATCH_NEW.ordinal()), options.get(BATCH_UDPATE.ordinal()));
	}

	/*-------------------------------------------------------------------------*/
	private void importData(
		Map<String, V2DataObject> imported,
		Map map,
		String dirtyFlag,
		boolean importNew,
		boolean importDupes)
	{
		if (imported.size() > 0)
		{
			boolean dirty = false;

			for (String name : imported.keySet())
			{
				if (map.containsKey(name) && importDupes)
				{
					if (importDupes)
					{
						map.put(name, imported.get(name));
						dirty = true;
					}
				}
				else
				{
					if (importNew)
					{
						map.put(name, imported.get(name));
						dirty = true;
					}
				}

			}

			if (dirty)
			{
				parent.setDirty(dirtyFlag);
				parent.setDirty(imported.values().toArray());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static class ImportDialog extends Dialog<BitSet>
	{
		private final BitSet output = new BitSet();

		enum Bit
		{
			WATER_NEW, WATER_UPDATE, FERMENTABLE_NEW, FERMENTABLE_UPDATE, HOPS_NEW,
			HOPS_UPDATE, YEASTS_NEW, YEASTS_UPDATE, MISC_NEW, MISC_UPDATE,
			STYLE_NEW, STYLE_UPDATE, EQUIPMENT_NEW, EQUIPMENT_UPDATE, RECIPE_NEW,
			RECIPE_UPDATE, BATCH_NEW, BATCH_UDPATE
		}

		public ImportDialog(Map<Class<?>, Map<String, V2DataObject>> objs)
		{
			Scene scene = this.getDialogPane().getScene();
			JfxUi.styleScene(scene);
			Stage stage = (Stage)scene.getWindow();
			stage.getIcons().add(JfxUi.importIcon);

			ButtonType okButtonType = new ButtonType(
				getUiString("ui.ok"), ButtonBar.ButtonData.OK_DONE);
			ButtonType cancelButtonType = new ButtonType(
				getUiString("ui.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
			this.getDialogPane().getButtonTypes().add(okButtonType);
			this.getDialogPane().getButtonTypes().add(cancelButtonType);

			this.setTitle(StringUtils.getUiString("tools.import.beerxml"));

			MigPane content = new MigPane();

			content.add(new Label(StringUtils.getUiString("tools.import.beerxml.imported")), "span, wrap");

			content.add(new Label(), "wrap");

			Database db = Database.getInstance();
			addCheckBoxs(content, output, WATER_NEW, WATER_UPDATE, objs.get(Water.class), db.getWaters(), "tools.import.beerxml.imported.water");
			addCheckBoxs(content, output, Bit.FERMENTABLE_NEW, Bit.FERMENTABLE_UPDATE, objs.get(Fermentable.class), db.getFermentables(), "tools.import.beerxml.imported.fermentable");
			addCheckBoxs(content, output, Bit.HOPS_NEW, Bit.HOPS_UPDATE, objs.get(Hop.class), db.getHops(), "tools.import.beerxml.imported.hop");
			addCheckBoxs(content, output, Bit.YEASTS_NEW, Bit.YEASTS_UPDATE, objs.get(Yeast.class), db.getYeasts(), "tools.import.beerxml.imported.yeast");
			addCheckBoxs(content, output, Bit.MISC_NEW, Bit.MISC_UPDATE, objs.get(Misc.class), db.getMiscs(), "tools.import.beerxml.imported.misc");
			addCheckBoxs(content, output, Bit.STYLE_NEW, Bit.STYLE_UPDATE, objs.get(Style.class), db.getMiscs(), "tools.import.beerxml.imported.style");
			addCheckBoxs(content, output, Bit.EQUIPMENT_NEW, Bit.EQUIPMENT_UPDATE, objs.get(EquipmentProfile.class), db.getEquipmentProfiles(), "tools.import.beerxml.imported.equipment");
			addCheckBoxs(content, output, Bit.RECIPE_NEW, Bit.RECIPE_UPDATE, objs.get(Recipe.class), db.getRecipes(), "tools.import.beerxml.imported.recipe");
			addCheckBoxs(content, output, Bit.BATCH_NEW, Bit.BATCH_UDPATE, objs.get(Batch.class), db.getBatches(), "tools.import.beerxml.imported.batch");

			content.add(new Label(), "wrap");

			content.add(new Label(StringUtils.getUiString("tools.import.beerxml.push.ok")), "span, wrap");
			content.add(new Label(StringUtils.getUiString("tools.import.beerxml.then.save")), "span, wrap");

			this.getDialogPane().setContent(content);

			// -----

			final Button cancelButton = (Button)this.getDialogPane().lookupButton(cancelButtonType);
			cancelButton.addEventFilter(ActionEvent.ACTION, event -> output.clear());
		}

		private void addCheckBoxs(
			MigPane pane,
			BitSet bitSet,
			Bit newBit,
			Bit updateBit,
			Map<String, V2DataObject> v2DataObjects,
			Map<String, ?> dbObjs,
			String uiLabelPrefix)
		{
			if (v2DataObjects.size() > 0)
			{
				int newItems = 0;
				int dupes = 0;

				for (V2DataObject obj : v2DataObjects.values())
				{
					if (dbObjs.containsKey(obj.getName()))
					{
						dupes++;
					}
					else
					{
						newItems++;
					}
				}

				CheckBox newCheckBox = new CheckBox(getUiString(uiLabelPrefix + ".new", newItems));
				CheckBox dupeCheckBox = new CheckBox(getUiString(uiLabelPrefix + ".update", dupes));

				pane.add(newCheckBox);
				pane.add(dupeCheckBox, "wrap");

				newCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
				{
					bitSet.set(newBit.ordinal(), newValue);
				});
				dupeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
				{
					bitSet.set(updateBit.ordinal(), newValue);
				});

				newCheckBox.setSelected(newItems>0);
				dupeCheckBox.setSelected(dupes>0);
			}
		}
	}
}