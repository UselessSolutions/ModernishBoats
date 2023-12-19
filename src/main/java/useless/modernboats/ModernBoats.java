package useless.modernboats;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.NetworkHelper;
import turniplabs.halplibe.util.GameStartEntrypoint;


public class ModernBoats implements GameStartEntrypoint, ModInitializer {
    public static final String MOD_ID = "modernboats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
		NetworkHelper.register(PacketBoatMovement.class, true, false);
        LOGGER.info("ModernBoats initialized.");
    }

	@Override
	public void beforeGameStart() {

	}

	@Override
	public void afterGameStart() {
		if (!Global.isServer){
			ModernBoatsClient.init();
		}
	}
}
