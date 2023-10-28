package useless.modernboats;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ConfigHandler;
import useless.modernboats.mixin.PacketMixin;

import java.util.Properties;


public class ModernBoats implements ModInitializer {
    public static final String MOD_ID = "modernboats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ConfigHandler config;
	static {
		Properties prop = new Properties();
		prop.setProperty("boat_packet_id", "210");
		config = new ConfigHandler(MOD_ID, prop);
	}
    @Override
    public void onInitialize() {
		PacketMixin.callAddIdClassMapping(config.getInt("boat_packet_id"), false, true, PacketBoatMovement.class);
        LOGGER.info("ModernBoats initialized.");
    }
}
