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

        // Створення пулу потоків
        ForkJoinPool pool = new ForkJoinPool();

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
                Files.walk(dir)
                        .parallel()  // Запускати пошук в паралельному режимі
                        .filter(this::isImage)  // Перевірка, чи є файл зображенням
                        .forEach(imageFiles::add);
            } catch (IOException e) {
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
