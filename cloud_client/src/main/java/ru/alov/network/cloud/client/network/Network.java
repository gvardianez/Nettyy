package ru.alov.network.cloud.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.alov.network.cloud.client.ApplicationProperties;
import ru.alov.network.cloud.client.command_receiver.ClientCommandReceiver;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileReceiver;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {

    private final static Network network;

    static {
        network = new Network();
    }

    public static Network getInstance() {
        return network;
    }

    private Network() {
    }

    private static Channel currentChannel;

    private static ProtocolHandler protocolHandler;

    public static Channel getCurrentChannel() {
        return currentChannel;
    }

    public static ProtocolHandler getProtoHandler() {
        return protocolHandler;
    }

    public void start(CountDownLatch connectionOpened) {
//        HandlerProvider handlerProvider = new SerializedHandler();
//        HandlerProvider handlerProvider = new ProtoAndSerializedHandler();
        ProtocolFileReceiver protocolFileReceiver = new ProtocolFileReceiver();
        protocolHandler = new ProtocolHandler(protocolFileReceiver, new ClientCommandReceiver());
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                           .channel(NioSocketChannel.class)
                           .remoteAddress(new InetSocketAddress(ApplicationProperties.SERVER_HOST, ApplicationProperties.SERVER_PORT))
                           .handler(new ChannelInitializer<SocketChannel>() {
                               protected void initChannel(SocketChannel socketChannel) {
                                   socketChannel.pipeline().addLast(protocolHandler);
                                   currentChannel = socketChannel;
                               }
                           });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            connectionOpened.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        currentChannel.close();
    }
}
