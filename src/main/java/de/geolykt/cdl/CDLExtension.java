package de.geolykt.cdl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.event.EventHandler;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.Listener;
import de.geolykt.starloader.api.event.lifecycle.ApplicationStopEvent;
import de.geolykt.starloader.api.gui.modconf.ConfigurationSection;
import de.geolykt.starloader.api.gui.modconf.IntegerOption;
import de.geolykt.starloader.api.gui.modconf.ModConf;
import de.geolykt.starloader.mod.Extension;

public class CDLExtension extends Extension {

    public static final AtomicInteger MAX_FAMILY_SIZE = new AtomicInteger(20);

    @Override
    public void preInitialize() {
        MinestomRootClassLoader.getInstance().addTransformer(new CDLASMTransformer());
    }

    @Override
    public void postInitialize() {
        this.loadConfig();
        ConfigurationSection conf = ModConf.createSection("Custom Dynasty Limiter");
        IntegerOption option = conf.addIntegerOption("Maximum Family Size", CDLExtension.MAX_FAMILY_SIZE.get(), 20, 1, Integer.MAX_VALUE - 1, NullUtils.asList(20));

        try {
            option.addValueChangeListenerI(this::saveConfig);
        } catch (IncompatibleClassChangeError err) {
            this.getLogger().warn("Unable to work with IntegerOption#addValueChangeListenerI; Using save-on-quit instead. Behaviour may be warped - that is changed only apply after a restart.");
            this.getLogger().debug("Unable to work with IntegerOption#addValueChangeListenerI", err);
            EventManager.registerListener(new Listener() {
                @EventHandler
                public void onStop(ApplicationStopEvent e) {
                    CDLExtension.this.saveConfig(option.getValue());
                }
            });
        }
    }

    public void saveConfig(int value) {
        CDLExtension.MAX_FAMILY_SIZE.set(value);
        try {
            Files.write(this.getSavePath(), Integer.toString(value).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            this.getLogger().error("Unable to save custom dynasty limit", ex);
        }
    }

    @NotNull
    public Path getSavePath() {
        List<URL> urls = this.getDescription().getOrigin().files;
        Path file = null;
        if (urls.size() == 1) {
            try {
                file = Paths.get(urls.get(0).toURI()).resolveSibling("custom-dynasty-limiter.dat");
            } catch (URISyntaxException | FileSystemNotFoundException | IllegalArgumentException ex) {
                this.getLogger().error("Unable to obtain configuration save path", ex);
            }
        }

        if (file == null) {
            file = Paths.get("mods", "custom-dynasty-limiter.dat");
        }

        return NullUtils.requireNotNull(file);
    }

    public void loadConfig() {
        try {
            Path file = this.getSavePath();
            if (Files.notExists(file)) {
                this.getLogger().info("Skipping loading of custom dynasty limit as the config does not exist yet");
                return;
            }
            String data = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            CDLExtension.MAX_FAMILY_SIZE.set(Integer.parseUnsignedInt(data));
        } catch (IOException | NumberFormatException ex) {
            this.getLogger().error("Unable to load custom dynasty limit", ex);
        }
    }
}
