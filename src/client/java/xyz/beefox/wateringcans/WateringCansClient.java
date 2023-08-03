package xyz.beefox.wateringcans;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.Item;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WateringCansClient implements ClientModInitializer {

	public static final String MOD_ID = "wateringcans";
	public static final Item WATERING_CAN = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "watering_can"), new WateringCanItem(new FabricItemSettings().maxCount(1)));

	@Override
	public void onInitializeClient() {

		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}