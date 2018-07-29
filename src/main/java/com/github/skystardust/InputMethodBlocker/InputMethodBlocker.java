package com.github.skystardust.inputmethodblocker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

@Mod(
        modid = InputMethodBlocker.MOD_ID,
        name = InputMethodBlocker.MOD_NAME,
        version = InputMethodBlocker.MOD_VERSION,
        acceptedMinecraftVersions = InputMethodBlocker.GAME_VERSION
)
public class InputMethodBlocker {
    public static final String MOD_ID = "inputmethodblocker";
    public static final String MOD_NAME = "InputMethodBlocker";
    public static final String MOD_VERSION = "1.7.0";
    public static final String GAME_VERSION = "[1.11,1.12.2]";
    private KeyBinding switchIMEKey;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent fmlPreInitializationEvent) {
        try {
            saveNativeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        switchIMEKey = new KeyBinding("切换IME", Keyboard.KEY_P, "InputMethodBlocker");
        ClientRegistry.registerKeyBinding(switchIMEKey);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onPlayerKeyDown(InputEvent.KeyInputEvent keyboardInputEvent) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        if (switchIMEKey.isPressed()) {

            if (IMENativeAccess.getIMEStatus()) {
                IMENativeAccess.inactiveIME();
                sendMessage("取消激活输入法");
            } else {
                IMENativeAccess.activeIME();
                sendMessage("激活输入法");
            }
        }
    }

    @SubscribeEvent
    public void onGUIScreenSwitch(GuiOpenEvent guiOpenEvent) {
        if (guiOpenEvent.getGui() == null) {
            if (IMENativeAccess.getIMEStatus()) {
                IMENativeAccess.inactiveIME();
            }
        }
        if (guiOpenEvent.getGui() instanceof GuiEditSign) {
            IMENativeAccess.activeIME();
        }
    }

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent guiScreenEvent){
        detectGui(guiScreenEvent.getGui());
    }

    private void detectGui(GuiScreen gui){
        if (gui.allowUserInput) {
            IMENativeAccess.activeIME();
        }
        for (Field field : gui.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object o = field.get(gui);
                if (o != null) {
                    if (o instanceof GuiTextField) {
                        if (((GuiTextField) o).isFocused()) {
                            IMENativeAccess.activeIME();
                            return;
                        }
                    }
                    IMENativeAccess.inactiveIME();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String string){
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        player.sendMessage(ITextComponent.Serializer.jsonToComponent("{\n" +
                "    \"text\": \""+string+"\"\n" +
                "}"));
    }

    public void saveNativeFile() throws IOException {
        PlatformChecker.Platform platform = PlatformChecker.getPlatform();
        if (platform == PlatformChecker.Platform.WIN_64) {
            saveTempNativeFile("InputMethodBlocker-Native-x64.dll");
            return;
        }
        saveTempNativeFile("InputMethodBlocker-Native-x86.dll");
    }

    private void saveTempNativeFile(String fileName) throws IOException {
        InputStream fileInputStream = getClass().getClassLoader().getResource(fileName).openStream();
        File nativeFile = File.createTempFile("InputMethodBlocker", ".dll");
        FileOutputStream out = new FileOutputStream(nativeFile);
        int i;
        byte[] buf = new byte[1024];
        while ((i = fileInputStream.read(buf)) != -1) {
            out.write(buf, 0, i);
        }
        fileInputStream.close();
        out.close();
        nativeFile.deleteOnExit();
        System.load(nativeFile.toString());
    }
}
