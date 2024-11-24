import java.util.*;
import java.util.concurrent.*;

public class WorkDealingSearch {

    public static void main(String[] args) {
        // Введення користувача
        int rows = getInput("Enter the number of rows: ");
        int cols = getInput("Enter the number of columns: ");

        // Генерація випадкової матриці
        int[][] array = generateRandomArray(rows, cols);

        // Виведення згенерованого масиву
        System.out.println("Generated Array:");
        printArray(array);

        // Створення пулу потоків
        int numberOfThreads = 4;  // Кількість потоків
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Запуск пошуку за допомогою Work Dealing
        long startTime = System.nanoTime();
        List<int[]> results = splitAndExecute(array, rows, cols, executor, numberOfThreads);
        long endTime = System.nanoTime();

        // Виведення результатів
        if (!results.isEmpty()) {
            for (int[] result : results) {
                System.out.println("Found element " + result[0] + " at indices (" + result[1] + ", " + result[2] + ")");
            }
        } else {
            System.out.println("No element found that equals the sum of its indices.");
        }
        System.out.println("Execution time: " + (endTime - startTime) / 1_000_000 + " ms");

        // Завершення роботи пулу потоків
        executor.shutdown();
    }

    // Генерація випадкового масиву
    public static int[][] generateRandomArray(int rows, int cols) {
        Random rand = new Random();
        int[][] array = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = rand.nextInt(100);  // випадкове значення від 0 до 99
            }
        }
        return array;
    }

    // Виведення масиву
    public static void printArray(int[][] array) {
        for (int[] row : array) {
            for (int element : row) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    // Метод для отримання введених даних
    public static int getInput(String prompt) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer.");
            scanner.next();  // Очистити неправильний ввід
        }
        return scanner.nextInt();
    }

    // Метод для розподілу роботи між потоками
    public static List<int[]> splitAndExecute(int[][] array, int rows, int cols, ExecutorService executor, int numberOfThreads) {
        List<int[]> results = Collections.synchronizedList(new ArrayList<>());  // Використовуємо синхронізований список

        // Розподіл роботи між потоками
        int rowsPerThread = rows / numberOfThreads;
        List<Callable<Void>> tasks = new ArrayList<>();

        // Створюємо задачі для кожного потоку
        for (int i = 0; i < numberOfThreads; i++) {
            final int rowStart = i * rowsPerThread;
            final int rowEnd = (i == numberOfThreads - 1) ? rows : (i + 1) * rowsPerThread;

            tasks.add(() -> {
                // Кожен потік обробляє свою частину масиву
                for (int iRow = rowStart; iRow < rowEnd; iRow++) {
                    for (int iCol = 0; iCol < cols; iCol++) {
                        if (array[iRow][iCol] == (iRow + iCol)) {
                            results.add(new int[]{array[iRow][iCol], iRow, iCol});  // Додаємо знайдений елемент
                        }
                    }
                }
                return null;
            });
        }

        try {
            // Виконуємо завдання
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();  // Чекаємо на завершення всіх задач
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return results;
    }
}
