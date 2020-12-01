package ru.tulokhonov.arch;

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
            System.out.println("Простой архиватор. Архивирует и извлекает файлы и папки из архива с использованием алгоритма Zip. Использование:\n" +
                    "Для создания Zip архива к имени выполняемого файла добавьте через пробел список файлов и директорий для архивации, а затем перенаправьте вывод в новый zip-файл.\n" +
                    "Пример: ./archiver ./file1 ./file2 ./dir1 > archive.zip\n" +
                    "Для извлечения файлов и папок из zip-файла перенаправьте вывод файла zip архиватору. Данные будут извлечены в текущую папку. Файлы и папки с одинаковыми именами будут перезаписаны.\n" +
                    "Пример: cat archive.zip | ./archiver"
            );
    }

    static class Handler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            System.err.println(e.getMessage());
        }
    }
}
