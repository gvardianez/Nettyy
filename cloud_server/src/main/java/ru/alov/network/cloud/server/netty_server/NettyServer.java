package ru.alov.network.cloud.server.netty_server;

import io.netty.channel.Channel;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileReceiver;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolHandler;
import ru.alov.network.cloud.server.ApplicationProperties;
import ru.alov.network.cloud.common.handler_provider.HandlerProvider;
import ru.alov.network.cloud.server.command_receiver.ServerCommandReceiver;
import ru.alov.network.cloud.server.handler_provider.SerializedHandler;
import ru.alov.network.cloud.server.services.ContextStoreService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import ru.alov.network.cloud.server.services.database.DataBaseService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class NettyServer {

    public static final List<String> nicknameAuthUsers;

    private static final NettyServer server;

    static {
        server = new NettyServer();
        nicknameAuthUsers = new CopyOnWriteArrayList<>();
    }

    public static NettyServer getInstance() {
        return server;
    }

    private ProtocolHandler protocolHandler;

    public ProtocolHandler getProtoHandler() {
        return protocolHandler;
    }

    private NettyServer() {
    }

    public static NettyServer getServer() {
        return server;
    }

    public void start() {
        HandlerProvider provider = new SerializedHandler(new ContextStoreService());
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                     .channel(NioServerSocketChannel.class)
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel channel) {
                             ProtocolFileReceiver protocolFileReceiver = new ProtocolFileReceiver();
                             protocolHandler = new ProtocolHandler(protocolFileReceiver, new ServerCommandReceiver(protocolFileReceiver));
                             channel.pipeline().addLast(protocolHandler);
                         }
                     });
            ChannelFuture future = bootstrap.bind(ApplicationProperties.SERVER_PORT).sync();
            log.debug("Server started...");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("e=", e);
        } finally {
            stopServer(auth, worker);
            DataBaseService.stop();
        }
    }

    private void stopServer(EventLoopGroup auth, EventLoopGroup worker) {
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }
}