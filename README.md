# PW3
Практичне Завдання 3
Хворостяний Артем Кирилович
Варіант 5

1. Знайти у двовимірному великому масиві елемент, значення
якого співпадає з сумою його індексів. Варіант, що такого числа не
знайдеться – можливий.
Кількість елементів масиву, початкове та кінцеві значення має
задавати користувач. Значення елементів генеруйте рандомно.
У результаті на екран має бути виведено згенерований масив,
результат виконання задачі та час роботи програми.

WorkStealingSearch.java
Основний процес:
Програма генерує випадковий масив заданих розмірів.
Потім створюється пул потоків (ForkJoinPool), і за допомогою рекурсивних задач (SearchTask) програма шукає елементи, які дорівнюють сумі своїх індексів.
Рекурсивне розділення задачі:
Якщо розмір оброблюваної частини масиву малий, задача виконується безпосередньо.
Якщо частина масиву велика, вона розділяється на 4 підзадачі, кожна з яких обробляє свою частину матриці паралельно.
Результат:
Знайдені елементи додаються до списку результатів, і програма виводить всі знайдені елементи, які задовольняють умову того, що елемент масиву дорівнює сумі його індексів(координат).
Використовується Fork/Join для ефективного розподілу роботи між потоками, що дозволяє прискорити пошук у великих масивах.


WorkDealingSearch.java
Програма генерує випадковий масив розміром, заданим користувачем.
Розподіл роботи:
Масив розбивається на частини, і кожен потік обробляє свою частину.
Кожен потік шукає елементи, які рівні сумі їхніх індексів.
Використовується ExecutorService з пулом потоків для запуску паралельних задач.
Результати:
Знайдені елементи додаються до синхронізованого списку і виводяться на екран.
Виконання:
Програма вимірює час виконання і виводить його разом з результатами пошуку.

2. Напишіть програму, яка буде проходити по файлам певної
директорії та знаходити серед них усі зображення.
Директорію має обирати користувач.
У результаті потрібно вивести кількість знайдених файлів і
відкрити останній.

ImageSearchWithWorkStealing.java
Введення директорії: Користувач вводить шлях до директорії, в якій потрібно знайти зображення.
Пошук зображень:
Програма використовує клас ImageSearchTask, який реалізує RecursiveTask для паралельного пошуку зображень.
За допомогою Files.walk() програма перебирає всі файли в директорії та перевіряє, чи є вони зображеннями (за розширенням файлу).
Відкриття зображення: Після завершення пошуку відкривається останнє знайдене зображення, використовуючи Desktop API.
Час виконання: Програма вимірює та виводить час виконання пошуку.
Ключові моменти:
Пошук за допомогою Files.walk().parallel() дозволяє ефективно обробляти велику кількість файлів.
Пошук виконується в окремих потоках через ForkJoinPool.
Розширення файлів для зображень визначаються через список IMAGE_EXTENSIONS.
Програма відкриває останнє знайдене зображення, якщо воно є, і виводить час пошуку.

ImageSearchWithWorkDealing.java
Розподіл роботи серед потоків:
Директорія та її підкаталоги розділяються на частини для обробки кількома потоками (за допомогою пулу з 4 потоків).
Кожен потік перевіряє директорії та шукає зображення.
Пошук зображень:
Для кожної директорії перевіряється, чи є в ній файли з розширеннями, які вказано в IMAGE_EXTENSIONS.
Відкриття останнього зображення: Якщо знайдено зображення, відкривається останнє знайдене зображення через Desktop API.
Виведення результатів: Кількість знайдених зображень та час виконання виводяться на екран.
Програма розподіляє задачі між кількома потоками для швидшого пошуку зображень.
Розширення файлів для зображень визначаються через список IMAGE_EXTENSIONS.
Для відкриття зображення використовується Desktop API.
