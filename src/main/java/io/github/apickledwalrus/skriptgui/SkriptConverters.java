package io.github.apickledwalrus.skriptgui;

import io.github.apickledwalrus.skriptgui.gui.GUI;
import org.bukkit.inventory.Inventory;

import static it.jakegblp.lusk.utils.DeprecationUtils.registerConverter;

public class SkriptConverters {

	public SkriptConverters() {
		registerConverter(GUI.class, Inventory.class, GUI::getInventory);
	}

}
