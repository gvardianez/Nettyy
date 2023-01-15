package ru.alov.network.cloud.common.file_handlers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FileDeleter {

    public static void deleteFiles(List<String> filesForDelete, Path baseDir) {
        for (String selectedFile : filesForDelete) {
            Path filePath = baseDir.resolve(selectedFile);
            try {
                if (Files.isDirectory(filePath)) {
                    Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            System.out.println("delete file: " + file.toString());
                            Files.deleteIfExists(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.deleteIfExists(dir);
                            System.out.println("delete dir: " + dir.toString());
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
