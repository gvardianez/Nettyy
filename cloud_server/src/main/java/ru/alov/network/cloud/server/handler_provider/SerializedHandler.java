package ru.alov.network.cloud.server.handler_provider;

import ru.alov.network.cloud.common.handler_provider.HandlerProvider;
import ru.alov.network.cloud.server.ApplicationProperties;
import ru.alov.network.cloud.server.netty_server.AbstractMessageHandler;
import ru.alov.network.cloud.server.services.ContextStoreService;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class SerializedHandler implements HandlerProvider {

    private final ContextStoreService contextStoreService;

    public SerializedHandler(ContextStoreService contextStoreService) {
        this.contextStoreService = contextStoreService;
    }

    @Override
    public ChannelHandler[] getPipeline() {
        return new ChannelHandler[]{
                new ObjectDecoder(ApplicationProperties.MAX_OBJECT_DECODING_SIZE, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder()
//                new AbstractMessageHandler(contextStoreService)
        };
    }

}