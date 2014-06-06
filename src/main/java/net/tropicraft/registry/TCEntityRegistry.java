package net.tropicraft.registry;

import net.minecraft.entity.Entity;
import net.tropicraft.Tropicraft;
import net.tropicraft.client.entity.render.EntityChair;
import cpw.mods.fml.common.registry.EntityRegistry;

public class TCEntityRegistry {

	private static int entityID = 0;
	
	public static void init() {
		registerEntity(EntityChair.class, "beachChair", 120, 10, false);
	}
	
	private static void registerEntity(Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		EntityRegistry.registerModEntity(entityClass, entityName, entityID++, Tropicraft.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
	}

}
