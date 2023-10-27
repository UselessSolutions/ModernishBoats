package useless.modernboats;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import useless.modernboats.mixin.PacketMixin;


public class ModernBoats implements ModInitializer {
    public static final String MOD_ID = "modernboats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
		PacketMixin.callAddIdClassMapping(210, false, true, PacketBoatMovement.class);
        LOGGER.info("ModernBoats initialized.");
    }
}
