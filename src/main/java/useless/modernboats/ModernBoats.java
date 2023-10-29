package useless.modernboats;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.NetworkHelper;


public class ModernBoats implements ModInitializer {
    public static final String MOD_ID = "modernboats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
		NetworkHelper.register(PacketBoatMovement.class, true, false);
        LOGGER.info("ModernBoats initialized.");
    }
}
