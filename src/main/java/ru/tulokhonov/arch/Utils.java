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
     * Returns all files or directories as File objects in the specified path
     * @param path Path string
     * @return List of File objects
     */
    public static List<File> getAllFilesInDir(String path) {
        try {
            return Files
                    .walk(Paths.get(path))
                    .map(Path::toFile)
                    .collect(toList());
        } catch (IOException exception) {
            throw new RuntimeException("Error! Cannot return files or directories", exception);
        }
    }

    /**
     * Returns list of Files instances by their paths
     * @param paths file paths from command line arguments
     * @return list of Files instances
     * @throws IllegalArgumentException if a file or directory doesn't exists
     */
    public static List<File> getFiles(String[] paths) {
        List<File> files = new ArrayList<>();
        for (String path : paths) {
                File file = new File(path);
                if (!file.exists())
                    throw new IllegalArgumentException(
                            String.format("Error! File or directory named \"%s\" does not exists. Please check the name and try again",
                                    file.getName()));
                if (pathContainsDirTraversal(file.toPath()))
                    throw new IllegalArgumentException("Illegal file or directory name");
                if (file.isDirectory())
                    files.addAll(getAllFilesInDir(path));
                else
                    files.add(file);
        }
        return files;
    }

    /**
     * Generates ZIP file and writes it to output stream
     * @param files list of files or directories to be zipped
     * @param outputStream Output stream
     * @throws ArchivingException if IOException occurs
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
            throw new ArchivingException("Error creating archive!", exception);
        }
    }

    /**
     * Extracts given Zip file from input stream to specified path
     * @param is Input stream
     * @param path extraction path
     * @throws ExtractionException if content provided in input stream is not valid zip file or IOException occurs
     */
    public static void unZip(InputStream is, Path path) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry zipEntry = zis.getNextEntry();

            if (zipEntry == null)
                throw new IllegalArgumentException("Error! Invalid or empty zip file");

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
                        throw new ExtractionException("Error extracting file", exception);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
        catch (IOException exception) {
            throw new ExtractionException("Error extracting file!", exception);
        }
    }

    /**
     * Protects against malicious zip files
     * @see "https://snyk.io/research/zip-slip-vulnerability"
     *
     * @param zipEntry ZipEntry
     * @param target Extraction path
     * @throws IOException if malicious zip entry detected
     */
    static void slipProtect(ZipEntry zipEntry, Path target) throws IOException {
        String canonicalDestinationDirPath = target.toFile().getCanonicalPath();
        File destinationFile = new File(target.toFile(), zipEntry.getName());
        String canonicalDestinationFile = destinationFile.getCanonicalPath();
        if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
            throw new IOException("Error! Invalid entry in zip file: " + zipEntry.getName());
        }
    }

    /**
     * Unzip data to current directory
     * @param is Input stream
     */
    public static void unZip(InputStream is) {
        unZip(is, Paths.get("./"));
    }

    /**
     * Returns folder size in bytes
     * @param folder Path to the folder
     * @return folder size in bytes
     * @throws IOException if an I/O error is thrown when accessing the the folder.
     */
    static long getFolderSize(Path folder) throws IOException {
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    /**
     * Check if path contains directory traversal signs
     * @param path Path
     * @return true if path contains directory traversal signs or false otherwise
     */
    static boolean pathContainsDirTraversal(Path path) {
        for (int i = 0; i < path.getNameCount(); i++) {
            if (path.getName(i).toString().equals("..")) return true;
        }
        return false;
    }

    /**
     * Generates name for Zip Entries
     * @param path file or directory path
     * @return Zip entry name
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
