package ru.tulokhonov.arch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        Handler globalExceptionHandler = new Handler();
        Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);

        if (System.in.available() > 0)
            Utils.unZip(System.in);
        else if (args.length > 0)
            Utils.zip(Utils.getFiles(args), System.out);
        else
            System.out.println("Archiver. Archiving utility. Compresses and extracts files and folders using ZIP compression algorithm. Usage:\n" +
                    "To create zip archive add space-delimited list of files and folders and redirect output to a zip file.\n" +
                    "Example: ./archiver ./file1 ./file2 ./dir1 > archive.zip\n" +
                    "To extract files from zip file, pipe zip file output to the archiver. Files and folders will be extracted into the current directory.\n" +
                    "Example: cat archive.zip | ./archiver"
            );
    }

    static class Handler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            System.err.println(e.getMessage());
        }
    }
}
