package ru.alov.network.cloud.client.handler_provider;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.alov.network.cloud.client.command_receiver.ClientCommandReceiver;
import ru.alov.network.cloud.client.handlers.AbstractMessageHandler;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileReceiver;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolHandler;
import ru.alov.network.cloud.common.handler_provider.HandlerProvider;

public class ProtoAndSerializedHandler implements HandlerProvider {

    @Override
    public ChannelHandler[] getPipeline() {
        ProtocolFileReceiver protocolFileReceiver = new ProtocolFileReceiver();
        return new ChannelHandler[]{
                new ProtocolHandler(protocolFileReceiver, new ClientCommandReceiver()),
                new ObjectDecoder(2048576000, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AbstractMessageHandler()
        };
    }
}
