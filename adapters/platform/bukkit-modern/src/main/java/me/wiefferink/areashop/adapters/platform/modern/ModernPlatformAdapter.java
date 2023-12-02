package me.wiefferink.areashop.adapters.platform.modern;

import me.wiefferink.areashop.platform.adapter.BlockBehaviourHelper;
import me.wiefferink.areashop.platform.adapter.PlatformAdapter;

public class ModernPlatformAdapter implements PlatformAdapter {

    private final BlockBehaviourImpl blockBehaviourHelper = new BlockBehaviourImpl();

    @Override
    public BlockBehaviourHelper blockBehaviourHelper() {
        return this.blockBehaviourHelper;
    }

}
