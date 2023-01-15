package ru.alov.network.cloud.common.handler_provider;

import io.netty.channel.ChannelHandler;

public interface HandlerProvider {

    ChannelHandler[] getPipeline();

}
