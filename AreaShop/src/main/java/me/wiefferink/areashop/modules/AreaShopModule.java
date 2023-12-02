package me.wiefferink.areashop.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.features.FeatureFactory;
import me.wiefferink.areashop.features.signs.SignManager;
import me.wiefferink.areashop.features.signs.SignsModule;
import me.wiefferink.areashop.interfaces.WorldEditInterface;
import me.wiefferink.areashop.interfaces.WorldGuardInterface;
import me.wiefferink.areashop.managers.CommandManager;
import me.wiefferink.areashop.managers.FeatureManager;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.managers.SignErrorLogger;
import me.wiefferink.areashop.managers.SignLinkerManager;
import me.wiefferink.areashop.platform.adapter.PlatformAdapter;
import me.wiefferink.areashop.regions.ImportJobFactory;
import me.wiefferink.areashop.regions.RegionModule;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class AreaShopModule extends AbstractModule {

    private final AreaShop instance;
    private final WorldGuardInterface worldGuardInterface;
    private final WorldEditInterface worldEditInterface;
    private final PlatformAdapter platformAdapter;
    private final MessageBridge messageBridge;
    private final SignErrorLogger signErrorLogger;
    private final AbstractModule[] extras;

    public AreaShopModule(@Nonnull AreaShop instance,
                          @Nonnull MessageBridge messageBridge,
                          @Nonnull PlatformAdapter platformAdapter,
                          @Nonnull WorldEditInterface worldEditInterface,
                          @Nonnull WorldGuardInterface worldGuardInterface,
                          @Nonnull SignErrorLogger signErrorLogger,
                          @Nonnull AbstractModule... extras
    ) {
        this.instance = instance;
        this.messageBridge = messageBridge;
        this.platformAdapter = platformAdapter;
        this.signErrorLogger = signErrorLogger;
        this.worldEditInterface = worldEditInterface;
        this.worldGuardInterface = worldGuardInterface;
        this.extras = extras;
    }

    @Override
    protected void configure() {
        Arrays.stream(this.extras).forEach(this::install);
        bind(Plugin.class).toInstance(this.instance);
        bind(AreaShop.class).toInstance(this.instance);
        bind(MessageBridge.class).toInstance(this.messageBridge);
        bind(WorldGuardInterface.class).toInstance(this.worldGuardInterface);
        bind(SignManager.class).asEagerSingleton();
        bind(WorldEditInterface.class).toInstance(this.worldEditInterface);
        bind(SignErrorLogger.class).toInstance(this.signErrorLogger);
        // Setup managers
        bind(IFileManager.class).to(FileManager.class).asEagerSingleton();
        bind(FeatureManager.class).in(Singleton.class);
        bind(CommandManager.class).in(Singleton.class);
        bind(SignLinkerManager.class).in(Singleton.class);
        install(new SignsModule());
        install(new RegionModule());
        install(new FactoryModuleBuilder().build(FeatureFactory.class));
        install(new FactoryModuleBuilder().build(ImportJobFactory.class));
        requestStaticInjection(Utils.class);
    }
}
