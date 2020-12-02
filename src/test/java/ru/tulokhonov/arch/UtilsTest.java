package ru.tulokhonov.arch;

import org.junit.Test;
import ru.tulokhonov.arch.exceptions.ExtractionException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {
    @Test(expected = IOException.class)
    public void givenBadZipEntry_whenCheckThenThrowException() throws IOException {
        Utils.slipProtect(new ZipEntry("../../../evil.sh"), Paths.get("./"));
    }

    @Test
    public void givenGoodZipEntry_whenCheck_thenOk() throws IOException {
        Utils.slipProtect(new ZipEntry("dir/good.sh"), Paths.get("./"));
    }

    @Test
    public void givenFilesOrDirs_whenZipAndUnzip_thenOk() throws IOException {
        Path target = Paths.get("./target/files/" + Math.abs(new Random().nextLong()));
        Files.createDirectories(target.resolve("zipped"));
        Files.createDirectories(target.resolve("unzipped"));

        List<File> sourceFiles = Utils.getFiles(new String[] { "./pom.xml", "./src/test/resources" });
        try (OutputStream fos = new FileOutputStream(target.resolve("zipped/archive.zip").toFile())) {
            Utils.zip(sourceFiles, fos);
        }

        File zipFile = new File(target.resolve("zipped/archive.zip").toUri());
        assertTrue(zipFile.exists());

        // Извлечение файла из архива
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            Utils.unZip(fis, target.resolve("unzipped"));
        }
        // Сравнение размера папки с распакованными файлами с размером исходных файлов
        long unzippedFolderSize = Utils.getFolderSize(target.resolve("unzipped"));
        long sourceFilesSize = sourceFiles
                .stream()
                .filter(File::isFile)
                .mapToLong(File::length)
                .sum();
        assertEquals(unzippedFolderSize, sourceFilesSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenWrongFile_whenGetFiles_thenError() {
        List<File> sourceFiles = Utils.getFiles(new String[] { "./wrong-file-name" });
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInvalidName_whenGetFiles_thenError() {
        Utils.getFiles(new String[] { "../" });
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInvalidInput_whenUnzip_thenError() throws FileNotFoundException {
        InputStream is = new FileInputStream(new File("./src/test/resources/home.txt"));
        Utils.unZip(is);
    }

    @Test
    public void givenPaths_whenGetZipEntryNames_thenOk() {
        String folderName = Utils.getZipEntryName(Paths.get("./src/test/resources"));
        String fileName = Utils.getZipEntryName(Paths.get("./pom.xml"));
        assertEquals("src/test/resources/", folderName);
        assertEquals("pom.xml", fileName);
    }

//    @Test
//    public void givenFolder_WhenCalculateSize_thenOk() throws IOException {
//        long folderSize = Utils.getFolderSize(Paths.get("./src/test/resources"));
//        assertEquals(1000173L, folderSize);
//    }
}