package me.wiefferink.areashop.modules;

import com.google.inject.AbstractModule;
import me.wiefferink.areashop.adapters.platform.MinecraftPlatform;
import me.wiefferink.areashop.adapters.platform.OfflinePlayerHelper;
import me.wiefferink.areashop.platform.adapter.BlockBehaviourHelper;
import me.wiefferink.areashop.platform.adapter.PlatformAdapter;
import me.wiefferink.areashop.tools.BukkitSchedulerExecutor;
import org.jetbrains.annotations.NotNull;

public class PlatformModule extends AbstractModule {

    private final MinecraftPlatform minecraftPlatform;
    private final PlatformAdapter platformAdapter;

    public PlatformModule(@NotNull MinecraftPlatform platform,
                          @NotNull PlatformAdapter platformAdapter) {
        this.minecraftPlatform = platform;
        this.platformAdapter = platformAdapter;
    }
    @Override
    protected void configure() {
        bind(MinecraftPlatform.class).toInstance(this.minecraftPlatform);
        bind(OfflinePlayerHelper.class).toInstance(this.minecraftPlatform.offlinePlayerHelper());
        bind(PlatformAdapter.class).toInstance(this.platformAdapter);
        bind(BlockBehaviourHelper.class).toInstance(this.platformAdapter.blockBehaviourHelper());
        bind(BukkitSchedulerExecutor.class).asEagerSingleton();
    }
}
