import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.ArrayList;
import java.util.List;

public class WorkStealingSearch {

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
        ForkJoinPool pool = new ForkJoinPool();

        // Запуск пошуку за допомогою ForkJoinTask
        long startTime = System.nanoTime();
        SearchTask task = new SearchTask(array, 0, rows, 0, cols);
        List<int[]> results = pool.invoke(task);
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

    // Завдання для пошуку елементів з використанням Fork/Join
    static class SearchTask extends RecursiveTask<List<int[]>> {
        int[][] array;
        int rowStart, rowEnd, colStart, colEnd;

        SearchTask(int[][] array, int rowStart, int rowEnd, int colStart, int colEnd) {
            this.array = array;
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.colStart = colStart;
            this.colEnd = colEnd;
        }

        @Override
        protected List<int[]> compute() {
            List<int[]> results = new ArrayList<>(); // Список для збереження знайдених елементів

            // Базовий випадок: якщо кількість рядків або стовпців мале
            if (rowEnd - rowStart <= 10 && colEnd - colStart <= 10) {
                for (int i = rowStart; i < rowEnd; i++) {
                    for (int j = colStart; j < colEnd; j++) {
                        if (array[i][j] == (i + j)) {
                            results.add(new int[]{array[i][j], i, j}); // Додаємо знайдений елемент
                        }
                    }
                }
            } else {
                // Розбиваємо задачу на 4 частини
                int midRow = (rowStart + rowEnd) / 2;
                int midCol = (colStart + colEnd) / 2;

                SearchTask task1 = new SearchTask(array, rowStart, midRow, colStart, midCol);
                SearchTask task2 = new SearchTask(array, midRow, rowEnd, colStart, midCol);
                SearchTask task3 = new SearchTask(array, rowStart, midRow, midCol, colEnd);
                SearchTask task4 = new SearchTask(array, midRow, rowEnd, midCol, colEnd);

                // Запускаємо підзадачі
                task1.fork();
                task2.fork();
                task3.fork();
                task4.fork();

                // Чекаємо результатів з кожної підзадачі
                results.addAll(task1.join());
                results.addAll(task2.join());
                results.addAll(task3.join());
                results.addAll(task4.join());
            }

            return results;
        }
    }
}
