# Archiver
Простой архиватор. Архивирует и извлекает файлы и папки из архива с использованием алгоритма Zip.

## Требования

- Java 1.8+
- Shell

## Использование

1. Скачайте репозиторий
2. Выполните `./mvnw clean package` для компиляции и построения приложения
2. Убедитесь, что файл `archiver` исполняемый `chmod +x ./archiver`

### Архивация

Для создания Zip архива к имени выполняемого файла добавьте через пробел список файлов и директорий для архивации, а затем перенаправьте вывод в новый zip-файл.

Пример: 

`$ ./archiver ./file1 ./file2 ./dir1 > archive.zip`

### Извлечение

Для извлечения файлов и папок из zip-файла перенаправьте вывод zip-файла архиватору. Данные будут извлечены в текущую папку. Файлы и папки с одинаковыми именами будут перезаписаны.

Пример:

`$ cat archive.zip | ./archiver`