package ru.alov.network.cloud.common.file_transfer.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ProtocolFileSender {

    private final ExecutorService executorService;

    private final CommandSender commandSender;


    public ProtocolFileSender(CommandSender commandSender) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.commandSender = commandSender;
    }

    public void sendFile(List<String> selectedFiles, Path currentDir, Channel channel) throws IOException {
        executorService.execute(() -> {
            for (String selectedFile : selectedFiles) {
                Path filePath = currentDir.resolve(selectedFile);
                try {
                    if (Files.isDirectory(filePath)) {
                        Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                            Path createPath;

                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                createPath = currentDir.relativize(dir);
                                commandSender.sendCreateDirectoryCommand(createPath.toString());
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                createPath = currentDir.relativize(file);
                                uploadFile(channel, createPath.toString(), file);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } else {
                        uploadFile(channel, selectedFile, filePath);
                    }
                    System.out.println("REFRESH");
                    commandSender.sendRefreshCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        });
    }

    private void uploadFile(Channel channel, String file, Path path) {
        try {
            FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
            byte[] filenameBytes = file.getBytes(StandardCharsets.UTF_8);
            // 1 + 4 + filenameBytes.length + 8 -> SIGNAL_BYTE FILENAME_LENGTH(int) + FILENAME + FILE_LENGTH(long)
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length + 8);
            buf.writeByte(NetworkCloudCommandsList.FILE_SEND_SIGNAL_BYTE);
            buf.writeInt(filenameBytes.length);
            buf.writeBytes(filenameBytes);
            buf.writeLong(Files.size(path));
            channel.writeAndFlush(buf);

            ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
//            if (finishListener != null) {
//                transferOperationFuture.addListener(finishListener);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long calculateFilesSize(List<String> selectedFiles, Path baseDir) {
        AtomicLong size = new AtomicLong();
        selectedFiles.stream()
                     .map(s -> Paths.get(baseDir.toString(), s))
                     .forEach(s -> {
                         try (Stream<Path> walk = Files.walk(s)) {
                             size.addAndGet(walk.map(Path::toFile)
                                                .filter(File::isFile)
                                                .mapToLong(File::length)
                                                .sum());
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     });
        return size.get();
    }

}