package mobi.omegacentauri.raspberryjammod;

import ibxm.Player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod(modid = RaspberryJamMod.MODID, version = RaspberryJamMod.VERSION, name = RaspberryJamMod.NAME,
guiFactory = "mobi.omegacentauri.raspberryjammod.GuiFactory")
public class RaspberryJamMod
{
	public static final String MODID = "raspberryjammod";
	public static final String VERSION = "0.08";
	public static final String NAME = "Raspberry Jam Mod";
	private MinecraftCommunicator mcc;
	public static Configuration configFile;
	public static int portNumber = 4711;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		configFile.load();
		System.out.println("configFile = "+configFile.getConfigFile().getPath());

		synchronizeConfig();
	}
	
	public static void synchronizeConfig() {
		portNumber = configFile.getInt("Port Number", Configuration.CATEGORY_GENERAL, 4711, 1, 65535, "Port number");
		
		if (configFile.hasChanged()) 
			configFile.save();
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		if (mcc != null) {
			mcc.close();
		}
	}
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		System.out.println("Raspberry Jam Mod started");
		
		final MCEventHandler eventHandler = new MCEventHandler();
		FMLCommonHandler.instance().bus().register(eventHandler);
		MinecraftForge.EVENT_BUS.register(eventHandler);
		try {
			mcc = new MinecraftCommunicator(eventHandler);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						mcc.communicate();
					} catch(IOException e) {
						System.out.println("RaspberryJamMod error "+e);
					}
					finally {
						System.out.println("Closing RaspberryJamMod");
						if (mcc != null)
							mcc.close();
					}
				}

			}).start();
		} catch (IOException e1) {
			System.out.println("Threw "+e1);
		}

		event.registerServerCommand(new PythonExternalCommand());
	}
}
