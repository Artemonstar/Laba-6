//Сделано с помощью ИИ "DeepSeek"
package org.example;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class FileDownloader {


    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String RED = "\u001B[31m";
    private static final String BOLD = "\u001B[1m";


    private static final DecimalFormat SIZE_FORMATTER = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");


    private static final AtomicLong downloadedBytes = new AtomicLong(0);
    private static long totalFileSize = 0;

    public static void main(String[] args) {
        displayWelcomeBanner();


        String fileURL = "https://dl3s5.muzika.fun/aHR0cDovL2YubXAzcG9pc2submV0L21wMy8wMDkvNDkzLzkzMi85NDkzOTMyLm1wMw==" + ".mp3";

        String saveDir = System.getProperty("user.home") +
                "/Documents/программирование/системное программирование/10 лаба (запуск и скачивание)";

        System.out.println(CYAN + BOLD + "\n══════════════════════════════════════════════════════" + RESET);
        System.out.println(PURPLE + BOLD + "               НАСТРОЙКИ ЗАГРУЗКИ                " + RESET);
        System.out.println(CYAN + BOLD + "══════════════════════════════════════════════════════" + RESET);

        displayDownloadInfo(fileURL, saveDir);

        try {
            createDirectoryIfNotExists(saveDir);

            System.out.println(CYAN + BOLD + "\n══════════════════════════════════════════════════════" + RESET);
            System.out.println(PURPLE + BOLD + "           ЗАГРУЗКА ФАЙЛА (NIO МЕТОД)            " + RESET);
            System.out.println(CYAN + BOLD + "══════════════════════════════════════════════════════" + RESET);

            long startTime = System.currentTimeMillis();
            String downloadedFile = downloadFileNIO(fileURL, saveDir);
            long endTime = System.currentTimeMillis();

            displayDownloadSummary(downloadedFile, startTime, endTime, saveDir);

            askForAlternativeDownload(fileURL, saveDir);

        } catch (Exception e) {
            displayErrorMessage("Ошибка при загрузке файла", e);
        }

        displayGoodbyeMessage();
    }

    private static void displayWelcomeBanner() {
        System.out.println(GREEN + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║               ПРОДВИНУТЫЙ ЗАГРУЗЧИК ФАЙЛОВ                 ║");
        System.out.println("║               (Java NIO & IO методы)                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println(YELLOW + "🕐 Время начала: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + RESET);
    }

    private static void displayDownloadInfo(String fileURL, String saveDir) {
        System.out.println("\n📡 " + BLUE + "Информация о загрузке:" + RESET);
        System.out.println(YELLOW + "   ──────────────────────────────────────" + RESET);
        System.out.println("   • URL источника: " + CYAN + truncateString(fileURL, 70) + RESET);
        System.out.println("   • Директория сохранения: " + GREEN + saveDir + RESET);
        System.out.println("   • Метод загрузки: " + PURPLE + "NIO (Files.copy)" + RESET);
        System.out.println("   • Ожидаемый тип файла: " + extractFileExtension(fileURL));
    }

    public static String downloadFileNIO(String fileURL, String saveDir) throws IOException {
        System.out.println("\n🚀 " + PURPLE + "Инициализация загрузки..." + RESET);

        URL url = new URL(fileURL);
        String fileName = getFileNameFromURL(url);
        String filePath = saveDir + File.separator + fileName;

        System.out.println("\n📄 " + BLUE + "Детали файла:" + RESET);
        System.out.println("   • Имя файла: " + CYAN + fileName + RESET);
        System.out.println("   • Полный путь: " + GREEN + filePath + RESET);
        System.out.println("   • Размер файла: " + YELLOW + "определяется..." + RESET);

        try {
            totalFileSize = url.openConnection().getContentLengthLong();
            System.out.println("   • Размер файла: " + formatFileSize(totalFileSize));

            System.out.println("\n⏳ " + YELLOW + "Начало загрузки..." + RESET);
            System.out.println("   " + getProgressBar(0));

            long startTime = System.currentTimeMillis();

            try (InputStream in = url.openStream()) {
                ProgressTrackingInputStream progressIn = new ProgressTrackingInputStream(in);

                Files.copy(progressIn, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

                System.out.println("   " + getProgressBar(100));
            }

            long endTime = System.currentTimeMillis();


            File downloadedFile = new File(filePath);
            if (downloadedFile.exists()) {
                long actualSize = downloadedFile.length();
                System.out.println("\n✅ " + GREEN + BOLD + "Файл успешно загружен!" + RESET);
                System.out.println(YELLOW + "   ──────────────────────────────────────" + RESET);
                System.out.println("   • Ожидаемый размер: " + formatFileSize(totalFileSize));
                System.out.println("   • Фактический размер: " + formatFileSize(actualSize));
                System.out.println("   • Время загрузки: " + formatTime(endTime - startTime));

                if (totalFileSize > 0 && actualSize == totalFileSize) {
                    System.out.println("   • " + GREEN + "✓ Проверка размера пройдена успешно" + RESET);
                } else if (totalFileSize > 0) {
                    System.out.println("   • " + YELLOW + "⚠ Размеры не совпадают (возможна неполная загрузка)" + RESET);
                }
            }

            return filePath;

        } catch (IOException e) {
            System.err.println(RED + "❌ Ошибка во время загрузки!" + RESET);
            throw e;
        }
    }

    public static void downloadFileIO(String fileURL, String saveDir) throws IOException {
        System.out.println("\n🚀 " + PURPLE + "Инициализация загрузки (IO метод)..." + RESET);

        URL url = new URL(fileURL);
        String fileName = getFileNameFromURL(url);
        String filePath = saveDir + File.separator + fileName;

        System.out.println("   • Метод: " + BLUE + "IO (InputStream/OutputStream)" + RESET);
        System.out.println("   • Буфер: " + "4 КБ");

        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(filePath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;

            totalFileSize = url.openConnection().getContentLengthLong();

            System.out.println("\n⏳ " + YELLOW + "Начало загрузки..." + RESET);
            System.out.println("   " + getProgressBar(0));

            long startTime = System.currentTimeMillis();

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (totalFileSize > 0) {
                    int progress = (int) ((totalRead * 100) / totalFileSize);
                    if (progress % 10 == 0) {
                        System.out.println("   " + getProgressBar(progress));
                    }
                }
            }

            long endTime = System.currentTimeMillis();


            System.out.println("   " + getProgressBar(100));

            System.out.println("\n✅ " + GREEN + BOLD + "Файл успешно загружен (IO метод)!" + RESET);
            System.out.println("   • Время загрузки: " + formatTime(endTime - startTime));
            System.out.println("   • Всего загружено: " + formatFileSize(totalRead));

        } catch (IOException e) {
            System.err.println(RED + "❌ Ошибка при загрузке (IO метод)!" + RESET);
            throw e;
        }
    }

    private static String getFileNameFromURL(URL url) {
        String path = url.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);


        if (fileName.length() > 50) {
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
                fileName = fileName.substring(0, Math.min(30, dotIndex)) + "..." + extension;
            }
        }

        return fileName;
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            System.out.println("\n📁 " + BLUE + "Создание директории..." + RESET);
            if (directory.mkdirs()) {
                System.out.println("   ✅ " + GREEN + "Директория создана: " + dirPath + RESET);
            } else {
                System.out.println("   ❌ " + RED + "Не удалось создать директорию!" + RESET);
            }
        } else {
            System.out.println("\n📁 " + GREEN + "Директория уже существует: " + dirPath + RESET);
        }
    }

    private static void displayDownloadSummary(String filePath, long startTime, long endTime, String saveDir) {
        File file = new File(filePath);

        System.out.println(CYAN + BOLD + "\n══════════════════════════════════════════════════════" + RESET);
        System.out.println(PURPLE + BOLD + "              ИТОГИ ЗАГРУЗКИ                   " + RESET);
        System.out.println(CYAN + BOLD + "══════════════════════════════════════════════════════" + RESET);

        if (file.exists()) {
            System.out.println("\n📊 " + BLUE + "Статистика загрузки:" + RESET);
            System.out.println(YELLOW + "   ──────────────────────────────────────" + RESET);
            System.out.println("   • Файл: " + CYAN + file.getName() + RESET);
            System.out.println("   • Размер: " + formatFileSize(file.length()));
            System.out.println("   • Путь: " + GREEN + file.getParent() + RESET);
            System.out.println("   • Время начала: " +
                    LocalDateTime.now().minusMonths(endTime - startTime)
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println("   • Время окончания: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println("   • Длительность: " + formatTime(endTime - startTime));
            System.out.println("   • Скорость загрузки: " +
                    calculateDownloadSpeed(file.length(), endTime - startTime));
        } else {
            System.out.println(RED + "\n⚠ Файл не найден по указанному пути!" + RESET);
        }
    }

    private static void askForAlternativeDownload(String fileURL, String saveDir) {
        System.out.println(CYAN + BOLD + "\n══════════════════════════════════════════════════════" + RESET);
        System.out.println(YELLOW + BOLD + "          ДОПОЛНИТЕЛЬНАЯ ОПЦИЯ                   " + RESET);
        System.out.println(CYAN + BOLD + "══════════════════════════════════════════════════════" + RESET);

        System.out.println("\n❓ " + BLUE + "Загрузить этот же файл альтернативным методом (IO)?" + RESET);
        System.out.print(YELLOW + "   Введите 'да' для продолжения или 'нет' для выхода: " + RESET);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String response = reader.readLine().trim().toLowerCase();

            if (response.equals("да") || response.equals("yes") || response.equals("y")) {
                System.out.println("\n🔄 " + PURPLE + "Запуск альтернативного метода загрузки..." + RESET);
                downloadFileIO(fileURL, saveDir);
            } else {
                System.out.println("\n👌 " + GREEN + "Пропускаем альтернативную загрузку." + RESET);
            }
        } catch (Exception e) {
            System.out.println(RED + "   Ошибка чтения ввода: " + e.getMessage() + RESET);
        }
    }

    private static void displayErrorMessage(String message, Exception e) {
        System.out.println("\n" + RED + BOLD + "══════════════════════════════════════════════════════" + RESET);
        System.out.println(RED + BOLD + "                    ОШИБКА                      " + RESET);
        System.out.println(RED + BOLD + "══════════════════════════════════════════════════════" + RESET);
        System.out.println("\n💥 " + RED + message + RESET);
        System.out.println(YELLOW + "   Причина: " + e.getMessage() + RESET);
        System.out.println("\n🔧 " + BLUE + "Рекомендации по устранению:" + RESET);
        System.out.println("   • Проверьте подключение к интернету");
        System.out.println("   • Убедитесь, что URL доступен");
        System.out.println("   • Проверьте права на запись в директорию");
        System.out.println("   • Попробуйте использовать другой URL");
    }

    private static void displayGoodbyeMessage() {
        System.out.println(CYAN + BOLD + "\n══════════════════════════════════════════════════════" + RESET);
        System.out.println(GREEN + BOLD + "        ПРОГРАММА ЗАВЕРШИЛА РАБОТУ               " + RESET);
        System.out.println(CYAN + BOLD + "══════════════════════════════════════════════════════" + RESET);
        System.out.println(YELLOW + "🕐 Время окончания: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + RESET);
        System.out.println("\n" + PURPLE + "Спасибо за использование загрузчика файлов! 🚀" + RESET);
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " байт";
        } else if (bytes < 1024 * 1024) {
            return SIZE_FORMATTER.format(bytes / 1024.0) + " КБ";
        } else if (bytes < 1024 * 1024 * 1024) {
            return SIZE_FORMATTER.format(bytes / (1024.0 * 1024.0)) + " МБ";
        } else {
            return SIZE_FORMATTER.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " ГБ";
        }
    }

    private static String formatTime(long millis) {
        if (millis < 1000) {
            return millis + " мс";
        } else if (millis < 60000) {
            return SIZE_FORMATTER.format(millis / 1000.0) + " сек";
        } else {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            return minutes + " мин " + seconds + " сек";
        }
    }

    private static String calculateDownloadSpeed(long bytes, long millis) {
        if (millis == 0) return "∞";
        double seconds = millis / 1000.0;
        double speed = bytes / seconds;

        if (speed < 1024) {
            return SIZE_FORMATTER.format(speed) + " Б/с";
        } else if (speed < 1024 * 1024) {
            return SIZE_FORMATTER.format(speed / 1024.0) + " КБ/с";
        } else {
            return SIZE_FORMATTER.format(speed / (1024.0 * 1024.0)) + " МБ/с";
        }
    }

    private static String getProgressBar(int percentage) {
        int bars = percentage / 2;
        StringBuilder progressBar = new StringBuilder();
        progressBar.append("[");

        for (int i = 0; i < 50; i++) {
            if (i < bars) {
                progressBar.append("█");
            } else {
                progressBar.append("░");
            }
        }

        progressBar.append("] ");
        progressBar.append(String.format("%3d", percentage)).append("%");

        return progressBar.toString();
    }

    private static String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static String extractFileExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < url.length() - 1) {
            String extension = url.substring(dotIndex + 1);
            if (extension.length() <= 5) { // Обычно расширения до 5 символов
                return extension.toUpperCase() + " файл";
            }
        }
        return "Неизвестный тип";
    }

    static class ProgressTrackingInputStream extends InputStream {
        private final InputStream wrapped;
        private long bytesRead = 0;
        private long lastUpdate = 0;

        public ProgressTrackingInputStream(InputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException {
            int result = wrapped.read();
            if (result != -1) {
                bytesRead++;
                updateProgress();
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = wrapped.read(b, off, len);
            if (result > 0) {
                bytesRead += result;
                updateProgress();
            }
            return result;
        }

        private void updateProgress() {
            if (totalFileSize > 0) {
                int progress = (int) ((bytesRead * 100) / totalFileSize);

                if (progress > lastUpdate) {
                    lastUpdate = progress;
                    if (progress % 10 == 0 || progress == 100) {
                        System.out.println("   " + getProgressBar(progress));
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
}