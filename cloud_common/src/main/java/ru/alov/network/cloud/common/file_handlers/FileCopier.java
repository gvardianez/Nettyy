package ru.alov.network.cloud.common.file_handlers;

import ru.alov.network.cloud.common.file_transfer.protocol.CommandSender;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FileCopier {

    public void copyFiles(List<String> filesForCopy, Path sourcePath, Path destPath) throws IOException {
        for (String selectedFile : filesForCopy) {
            String path;
            Path filePath = sourcePath.resolve(selectedFile);

            if (Files.isDirectory(filePath)) {
                Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        System.out.println("pre visit " + dir.toString());
                        System.out.println("selected file " + selectedFile);
                        System.out.println("dest path " + destPath.toString());
                        Files.createDirectory(Paths.get(destPath.toString() + sourcePath.relativize(dir)));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, Paths.get(destPath.toString() + sourcePath.relativize(file)));
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.copy(sourcePath.resolve(selectedFile), Paths.get(destPath.toString() + selectedFile));
            }
        }
    }

}
