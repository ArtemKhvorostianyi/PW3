import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.Desktop;

public class ImageSearchWithWorkStealing {

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff");

    public static void main(String[] args) throws IOException {
        // Введення користувачем директорії
        String directoryPath = getInput("Enter the directory path: ");
        Path rootDir = Paths.get(directoryPath);

        // Перевірка, чи існує директорія
        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
            System.out.println("The provided path is not a valid directory.");
            return;
        }

        // Створення пулу потоків з обмеженням на кількість потоків
        ForkJoinPool pool = new ForkJoinPool(4); // Обмежуємо кількість потоків до 4

        // Запуск пошуку зображень
        long startTime = System.nanoTime();
        ImageSearchTask task = new ImageSearchTask(rootDir);
        List<Path> images = pool.invoke(task);
        long endTime = System.nanoTime();

        // Виведення результатів
        System.out.println("Found " + images.size() + " image(s).");
        if (!images.isEmpty()) {
            // Відкриваємо останнє знайдене зображення
            Path lastImage = images.get(images.size() - 1);
            System.out.println("Opening last image: " + lastImage.toString());
            openImage(lastImage);
        }

        System.out.println("Execution time: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    // Метод для отримання введених даних
    public static String getInput(String prompt) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        return reader.readLine();
    }

    // Завдання для пошуку зображень з використанням Work Stealing
    static class ImageSearchTask extends RecursiveTask<List<Path>> {
        private final Path dir;

        ImageSearchTask(Path dir) {
            this.dir = dir;
        }

        @Override
        protected List<Path> compute() {
            List<Path> imageFiles = new ArrayList<>();
            try {
                // Перевіряємо поточний каталог
                Files.walk(dir)
                        .filter(this::isImage)  // Перевірка, чи є файл зображенням
                        .forEach(imageFiles::add);

                // Рекурсивно шукаємо в підкаталогах
                List<ImageSearchTask> subTasks = new ArrayList<>();
                Files.walk(dir)
                        .filter(Files::isDirectory)  // Фільтруємо лише директорії
                        .forEach(subDir -> {
                            if (!subDir.equals(dir)) {
                                ImageSearchTask task = new ImageSearchTask(subDir);
                                subTasks.add(task);
                                task.fork();  // Запускаємо підзадачі для кожної директорії
                            }
                        });

                // Чекаємо завершення всіх підзадач і збираємо результати
                for (ImageSearchTask task : subTasks) {
                    imageFiles.addAll(task.join());
                }

            } catch (IOException e) {
                System.out.println("Error walking directory: " + dir);
                e.printStackTrace();
            }
            return imageFiles;
        }

        // Перевірка, чи є файл зображенням (перевірка розширення)
        private boolean isImage(Path file) {
            String fileName = file.getFileName().toString().toLowerCase();
            return IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
        }
    }

    // Метод для відкриття зображення (використовує Desktop API)
    private static void openImage(Path imagePath) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(imagePath.toFile());
            } else {
                System.out.println("Desktop is not supported on this system.");
            }
        } catch (IOException e) {
            System.out.println("Error opening the image: " + e.getMessage());
        }
    }
}
