
/*
 * This file is part of brewday.
 *
 * brewday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * brewday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with brewday.  If not, see https://www.gnu.org/licenses.
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

class ImportBeerXmlDialog extends Dialog<BitSet>
{
	private final BitSet output = new BitSet();
	private Map<Class<?>, Map<String, V2DataObject>> objs;

	public ImportBeerXmlDialog() throws Exception
	{
		Scene scene = this.getDialogPane().getScene();
		JfxUi.styleScene(scene);
		Stage stage = (Stage)scene.getWindow();
		stage.getIcons().add(Icons.importXml);

		ButtonType cancelButtonType = new ButtonType(
			getUiString("ui.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		this.getDialogPane().getButtonTypes().add(cancelButtonType);

		this.setTitle(getUiString("tools.import.beerxml"));

		MigPane prepareImport = new MigPane();
		prepareImport.setPrefSize(550, 400);

		prepareImport.add(new Label(getUiString("tools.import.settings")), "span, wrap");
		TextArea details = new TextArea(getUiString("tools.import.beerxml.details"));
		details.setWrapText(true);
		details.setEditable(false);
		prepareImport.add(details, "span, wrap");

		CheckBox tags1 = new CheckBox(getUiString("tools.import.add.tags"));
		CheckBox tags2 = new CheckBox(getUiString("tools.import.add.tags.2"));
		CheckBox beersmithBugs = new CheckBox(getUiString("tools.import.compensate"));

		tags1.setSelected(true);
		tags2.setSelected(true);
		beersmithBugs.setSelected(true);

		prepareImport.add(tags1, "span, wrap");
		prepareImport.add(tags2, "span, wrap");
		prepareImport.add(beersmithBugs, "span, wrap");

		prepareImport.add(new Label(), "wrap");

		Button chooseFiles = new Button(getUiString("tools.import.choose.files"));
		chooseFiles.setPrefWidth(100);
		prepareImport.add(chooseFiles);

		Label filesChosen = new Label();
		prepareImport.add(filesChosen, "wrap");

		Button parse = new Button(getUiString("tools.import.parse"));
		parse.setPrefWidth(100);
		prepareImport.add(parse);
		parse.setDisable(true);

		this.getDialogPane().setContent(prepareImport);

		List<File> files = new ArrayList<>();

		// -----

		final Button cancelButton = (Button)this.getDialogPane().lookupButton(cancelButtonType);
		cancelButton.addEventFilter(ActionEvent.ACTION, event -> output.clear());

		chooseFiles.setOnAction(actionEvent ->
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(StringUtils.getUiString("tools.import.beerxml.title"));

			Settings settings = Database.getInstance().getSettings();
			String dir = settings.get(Settings.LAST_IMPORT_DIRECTORY);
			if (dir != null && new File(dir).exists())
			{
				fileChooser.setInitialDirectory(new File(dir));
			}

			List<File> f = fileChooser.showOpenMultipleDialog(
				JfxUi.getInstance().getMainScene().getWindow());

			if (f != null)
			{
				files.clear();
				files.addAll(f);

				String parent = files.get(0).getParent();
				if (parent != null)
				{
					settings.set(Settings.LAST_IMPORT_DIRECTORY, parent);
					Database.getInstance().saveSettings();
				}

				parse.setDisable(false);
				if (files.size() > 1)
				{
					filesChosen.setText(files.get(0).getAbsolutePath()+" (+"+(files.size()-1)+")");
				}
				else
				{
					filesChosen.setText(files.get(0).getAbsolutePath());
				}
			}
		});

		parse.setOnAction(event ->
		{
			try
			{
				objs = new BeerXmlParser().parse(
					files,
					tags1.isSelected(),
					tags2.isSelected(),
					beersmithBugs.isSelected());
				setToImportOptions();
			}
			catch (Exception e)
			{
				throw new BrewdayException(e);
			}
		});
	}

	/*-------------------------------------------------------------------------*/

	protected MigPane setToImportOptions()
	{
		MigPane importContent = new MigPane();

		importContent.add(new Label(getUiString("tools.import.imported")), "span, wrap");

		importContent.add(new Label(), "wrap");

		Database db = Database.getInstance();
		addCheckBoxs(importContent, output, ImportPane.Bit.WATER_NEW, ImportPane.Bit.WATER_UPDATE, objs.get(Water.class), db.getWaters(), "tools.import.imported.water");
		addCheckBoxs(importContent, output, ImportPane.Bit.FERMENTABLE_NEW, ImportPane.Bit.FERMENTABLE_UPDATE, objs.get(Fermentable.class), db.getFermentables(), "tools.import.imported.fermentable");
		addCheckBoxs(importContent, output, ImportPane.Bit.HOPS_NEW, ImportPane.Bit.HOPS_UPDATE, objs.get(Hop.class), db.getHops(), "tools.import.imported.hop");
		addCheckBoxs(importContent, output, ImportPane.Bit.YEASTS_NEW, ImportPane.Bit.YEASTS_UPDATE, objs.get(Yeast.class), db.getYeasts(), "tools.import.imported.yeast");
		addCheckBoxs(importContent, output, ImportPane.Bit.MISC_NEW, ImportPane.Bit.MISC_UPDATE, objs.get(Misc.class), db.getMiscs(), "tools.import.imported.misc");
		addCheckBoxs(importContent, output, ImportPane.Bit.STYLE_NEW, ImportPane.Bit.STYLE_UPDATE, objs.get(Style.class), db.getMiscs(), "tools.import.imported.style");
		addCheckBoxs(importContent, output, ImportPane.Bit.EQUIPMENT_NEW, ImportPane.Bit.EQUIPMENT_UPDATE, objs.get(EquipmentProfile.class), db.getEquipmentProfiles(), "tools.import.imported.equipment");
		addCheckBoxs(importContent, output, ImportPane.Bit.RECIPE_NEW, ImportPane.Bit.RECIPE_UPDATE, objs.get(Recipe.class), db.getRecipes(), "tools.import.imported.recipe");
		addCheckBoxs(importContent, output, ImportPane.Bit.BATCH_NEW, ImportPane.Bit.BATCH_UDPATE, objs.get(Batch.class), db.getBatches(), "tools.import.imported.batch");

		importContent.add(new Label(), "wrap");

		importContent.add(new Label(getUiString("tools.import.push.ok")), "span, wrap");
		importContent.add(new Label(getUiString("tools.import.then.save")), "span, wrap");

		this.getDialogPane().setContent(importContent);

		ButtonType okButtonType = new ButtonType(
			getUiString("ui.ok"), ButtonBar.ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().add(okButtonType);

		return importContent;
	}

	public BitSet getOutput()
	{
		return output;
	}

	public Map<Class<?>, Map<String, V2DataObject>> getImportedObjs()
	{
		return objs;
	}

	private void addCheckBoxs(
		MigPane pane,
		BitSet bitSet,
		ImportPane.Bit newBit,
		ImportPane.Bit updateBit,
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

			newCheckBox.setSelected(newItems > 0);
			dupeCheckBox.setSelected(dupes > 0);
		}
	}
}
