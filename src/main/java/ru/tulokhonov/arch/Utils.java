package ru.tulokhonov.arch;

import ru.tulokhonov.arch.exceptions.ArchivingException;
import ru.tulokhonov.arch.exceptions.ExtractionException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toList;

public class Utils {
    /**
     * Возвращает список файлов и директорий как объекты File по указанному пути
     * @param path путь к директории
     * @return список файлов и директорий
     * @throws RuntimeException если произошла I/O ошибка доступа по указанному пути
     */
    public static List<File> getAllFilesInDir(String path) {
        try {
            return Files
                    .walk(Paths.get(path))
                    .map(Path::toFile)
                    .collect(toList());
        } catch (IOException exception) {
            throw new RuntimeException("Ошибка. Невозможно список файлов и папок для архивации", exception);
        }
    }

    /**
     * Ищет файлы и папки по указанным путям. Проверят существуют ли они.
     * @param paths пути к файлам и папкам
     * @return список файлов и папок по указанным путям
     * @throws IllegalArgumentException если файл или папка не существуют
     */
    public static List<File> getFiles(String[] paths) {
        List<File> files = new ArrayList<>();
        for (String path : paths) {
                File file = new File(path);
                if (!file.exists())
                    throw new IllegalArgumentException(
                            String.format("Ошибка! Файл или директория по имени \"%s\" не существует. Пожалуйста, проверьте имя и повторите снова",
                                    file.getName()));
                if (pathContainsDirTraversal(file.toPath()))
                    throw new IllegalArgumentException(
                            String.format("Ошибка! Запрещенное имя папки или директории \"%s\"", file.getName()));
                if (file.isDirectory())
                    files.addAll(getAllFilesInDir(path));
                else
                    files.add(file);
        }
        return files;
    }

    /**
     * Генерирует архив в виде ZIP файла и пишет результат в выходной поток outputStream
     * @param files список файлов и папок для архивации
     * @param outputStream выходной поток
     * @throws ArchivingException при ошибке ввода-вывода
     */
    public static void zip(List<File> files, OutputStream outputStream) {
        try (BufferedOutputStream bos = new BufferedOutputStream(outputStream);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            for (File file : files) {
                if (file.isFile())
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(getZipEntryName(file.toPath()));
                        zos.putNextEntry(zipEntry);
                        byte[] bytes = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(bytes)) >= 0) {
                            zos.write(bytes, 0, bytesRead);
                        }
                    }
                else if (file.isDirectory())
                   zos.putNextEntry(new ZipEntry(getZipEntryName(file.toPath())));
            }
        } catch (IOException exception) {
            throw new ArchivingException("Ошибка создания архива!", exception);
        }
    }

    /**
     * Извлекает данный Zip файл из входного потока в указанный путь
     * @param is входной поток
     * @param path путь для разархивации
     * @throws IllegalArgumentException если контент входного потока inputStream не является правильным Zip файлом или файл пуст
     * @throws ExtractionException при возникновении ошибки ввода-вывода
     */
    public static void unZip(InputStream is, Path path) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry zipEntry = zis.getNextEntry();

            if (zipEntry == null)
                throw new IllegalArgumentException("Ошибка! Неверный или пустой файл Zip");

            while (zipEntry != null) {
                slipProtect(zipEntry, path);
                Path newPath = path.resolve(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null)
                        if (Files.notExists(newPath.getParent()))
                            Files.createDirectories(newPath.getParent());

                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) >= 0) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException exception) {
                        throw new ExtractionException("Ошибка извлечения данных!", exception);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
        catch (IOException exception) {
            throw new ExtractionException("Ошибка извлечения данных!", exception);
        }
    }

    /**
     * Проверяет запись из Zip файла на наличие запрещенных символов перехода директорий "..", которые могут привести к извлечению данных вне указанного пути
     * @see "https://snyk.io/research/zip-slip-vulnerability"
     *
     * @param zipEntry ZipEntry запись из файла Zip
     * @param target путь для извлечения
     * @throws IOException если в результате извлечения файл мог быть записан вне пути для извлечения
     */
    static void slipProtect(ZipEntry zipEntry, Path target) throws IOException {
        String canonicalDestinationDirPath = target.toFile().getCanonicalPath();
        File destinationFile = new File(target.toFile(), zipEntry.getName());
        String canonicalDestinationFile = destinationFile.getCanonicalPath();
        if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
            throw new IOException("Ошибка! Неверная запись в файле Zip: " + zipEntry.getName());
        }
    }

    /**
     * Извлекает данные из входного потока в текущий путь
     * @param is входной поток
     */
    public static void unZip(InputStream is) {
        unZip(is, Paths.get("./"));
    }

    /**
     * Считает размер всех файлов в папке
     * @param folder путь к папке
     * @return размер файлов в папке в байтах
     * @throws IOException при возникновении ошибки ввода-вывода при обращении к папке
     */
    static long getFolderSize(Path folder) throws IOException {
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    /**
     * Проверяет содержит ли путь знаки перехода вверх ".."
     * @param path путь для проверки
     * @return да, если содержит, иначе возвращает false
     */
    static boolean pathContainsDirTraversal(Path path) {
        for (int i = 0; i < path.getNameCount(); i++) {
            if (path.getName(i).toString().equals("..")) return true;
        }
        return false;
    }

    /**
     * Генерирует имя для записей в Zip файле (ZipEntries)
     * @param path путь к файлу или папке
     * @return имя для записи Zip файла
     */
    static String getZipEntryName(Path path) {
        StringBuilder builder = new StringBuilder();
        Path normalizedPath = path.normalize();
        for (int i = 0; i < normalizedPath.getNameCount(); i++) {
            builder.append(normalizedPath.getName(i));
            if (i < normalizedPath.getNameCount() -1) builder.append("/");
        }
        if (path.toFile().isDirectory()) builder.append("/");
        return builder.toString();
    }
}