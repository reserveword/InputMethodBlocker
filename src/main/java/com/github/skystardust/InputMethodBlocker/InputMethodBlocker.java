package com.github.skystardust.InputMethodBlocker;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.*;

@Mod(
        modid = InputMethodBlocker.MOD_ID,
        name = InputMethodBlocker.MOD_NAME,
        version = InputMethodBlocker.MOD_VERSION,
        acceptedMinecraftVersions = InputMethodBlocker.GAME_VERSION
)
@SideOnly(Side.CLIENT)
public class InputMethodBlocker {
    public static final String MOD_ID = "inputmethodblocker";
    public static final String MOD_NAME = "InputMethodBlocker";
    public static final String MOD_VERSION = "1.7.0";
    public static final String GAME_VERSION = "[1.11,1.12.1]";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        KeyBinding disableIMEKeyBinding = new KeyBinding("关闭IME", Keyboard.KEY_H, "IME 控制");
        KeyBinding enableIMEKeyBinding = new KeyBinding("开启IME", Keyboard.KEY_J, "IME 控制");
        ClientRegistry.registerKeyBinding(disableIMEKeyBinding);
        ClientRegistry.registerKeyBinding(enableIMEKeyBinding);
        MinecraftForge.EVENT_BUS.register(new GameEventHandle(disableIMEKeyBinding,enableIMEKeyBinding));
        try {
            saveNativeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        NativeUtils.inactiveInputMethod("");
    }

    public void saveNativeFile() throws IOException {
        OSChecker.OSType osType = OSChecker.getOsType();
        if (osType== OSChecker.OSType.WIN_X64){
            saveTempNativeFile("InputMethodBlocker-Natives-x64.dll");
        }
        else if (osType== OSChecker.OSType.WIN_X32){
            saveTempNativeFile("InputMethodBlocker-Natives-x32.dll");
        }
    }
    private void saveTempNativeFile(String fileName) throws IOException {
        InputStream fileInputStream = getClass().getClassLoader().getResource(fileName).openStream();
        File nativeFile = File.createTempFile("InputMethodBlocker", ".dll");
        FileOutputStream out = new FileOutputStream(nativeFile);
        int i;
        byte [] buf = new byte[1024];
        while((i=fileInputStream.read(buf))!=-1) {
            out.write(buf,0,i);
        }
        fileInputStream.close();
        out.close();
        nativeFile.deleteOnExit();
        System.load(nativeFile.toString());
    }
}
