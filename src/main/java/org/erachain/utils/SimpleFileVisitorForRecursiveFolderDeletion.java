package org.erachain.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleFileVisitorForRecursiveFolderDeletion extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        java.nio.file.Files.delete(file);
        return FileVisitResult.CONTINUE;
    }


    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        java.nio.file.Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }


}
