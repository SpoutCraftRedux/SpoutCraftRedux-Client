package org.spoutcraftredux.client.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {
    @Accessor("frozen")
    void spoutcraftredux$setFrozen(boolean frozen);

    @Accessor("unregisteredIntrusiveHolders")
    Map<T, Holder.Reference<T>> spoutcraftredux$getUnregisteredIntrusiveHolders();

    @Accessor("unregisteredIntrusiveHolders")
    void spoutcraftredux$setUnregisteredIntrusiveHolders(Map<T, Holder.Reference<T>> holders);

    @Accessor("byId")
    ObjectList<Holder.Reference<T>> spoutcraftredux$getById();

    @Accessor("toId")
    Reference2IntMap<T> spoutcraftredux$getToId();

    @Accessor("byLocation")
    Map<Identifier, Holder.Reference<T>> spoutcraftredux$getByLocation();

    @Accessor("byKey")
    Map<ResourceKey<T>, Holder.Reference<T>> spoutcraftredux$getByKey();

    @Accessor("byValue")
    Map<T, Holder.Reference<T>> spoutcraftredux$getByValue();

    @Accessor("registrationInfos")
    Map<ResourceKey<T>, RegistrationInfo> spoutcraftredux$getRegistrationInfos();
}
