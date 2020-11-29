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
            System.out.println("Archiver. Archiving utility. Compresses and extracts files and folders using ZIP compression algorithm. Usage:");
        System.out.println("In order to archive, specify space-delimited list of files or directories and redirect output to a file (zip extension is recommended), for example: ./archiver ./file1 ./file2 ./dir1 > archive.zip");
        System.out.println("In order to extract, pipe output of a zip file to utility, for example: cat archive.zip | ./archiver");
        System.out.println("All files and folders will be extracted to the current directory. Files with the same name will be overwritten.");
    }

    static class Handler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            System.out.println(e.getMessage());
        }
    }
}
