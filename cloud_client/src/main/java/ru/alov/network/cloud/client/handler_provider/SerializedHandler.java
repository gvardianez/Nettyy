package ru.alov.network.cloud.client.handler_provider;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.alov.network.cloud.client.handlers.AbstractMessageHandler;
import ru.alov.network.cloud.common.handler_provider.HandlerProvider;

public class SerializedHandler implements HandlerProvider {

    @Override
    public ChannelHandler[] getPipeline() {
        return new ChannelHandler[]{
                new ObjectDecoder(1048576000, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AbstractMessageHandler()
        };
    }

}