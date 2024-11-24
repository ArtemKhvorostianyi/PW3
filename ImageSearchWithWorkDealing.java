import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.Desktop;

public class ImageSearchWithWorkDealing {

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

        // Створення пулу потоків
        int numberOfThreads = 4; // Кількість потоків
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Запуск пошуку зображень
        long startTime = System.nanoTime();
        ImageSearchTask task = new ImageSearchTask(rootDir);
        List<Path> images = task.splitAndExecute(executor, numberOfThreads); // Запускаємо пошук
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

        // Завершення роботи пулу потоків
        executor.shutdown();
    }

    // Метод для отримання введених даних
    public static String getInput(String prompt) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        return reader.readLine();
    }

    // Завдання для пошуку зображень з використанням Work Dealing
    static class ImageSearchTask {
        private final Path dir;

        ImageSearchTask(Path dir) {
            this.dir = dir;
        }

        // Метод для ручного розподілу роботи серед кількох потоків
        public List<Path> splitAndExecute(ExecutorService executor, int numberOfThreads) {
            List<Path> imageFiles = Collections.synchronizedList(new ArrayList<>());
            try {
                // Отримуємо список усіх директорій та підкаталогів
                List<Path> directories = new ArrayList<>();
                Files.walk(dir)
                        .filter(Files::isDirectory)
                        .forEach(directories::add);

                // Розподіляємо роботу по потоках
                int dirsPerThread = directories.size() / numberOfThreads;
                List<Callable<Void>> tasks = new ArrayList<>();

                for (int i = 0; i < numberOfThreads; i++) {
                    final int start = i * dirsPerThread;
                    final int end = (i == numberOfThreads - 1) ? directories.size() : (i + 1) * dirsPerThread;
                    tasks.add(() -> {
                        for (int j = start; j < end; j++) {
                            Path subDir = directories.get(j);
                            List<Path> imagePaths = processDirectory(subDir);
                            imageFiles.addAll(imagePaths); // Додаємо знайдені файли в загальний список
                        }
                        return null;
                    });
                }

                // Виконуємо завдання
                List<Future<Void>> futures = executor.invokeAll(tasks);
                for (Future<Void> future : futures) {
                    future.get(); // Чекаємо на завершення всіх задач
                }

            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return imageFiles;
        }

        // Метод для пошуку зображень у директорії
        private List<Path> processDirectory(Path dir) {
            List<Path> imageFiles = new ArrayList<>();
            try {
                Files.walk(dir)
                        .filter(this::isImage)
                        .forEach(imageFiles::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return imageFiles;
        }

        // Перевірка, чи є файл зображенням
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
