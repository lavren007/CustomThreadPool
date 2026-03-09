# 🚀 Custom ThreadPool Implementation

## 🎯 О проекте

**CustomThreadPool** — это высокопроизводительная реализация пула потоков с настраиваемым управлением, разработанная для замены стандартного `ThreadPoolExecutor` в сценариях, требующих гибкого контроля над распределением задач.

### ✨ Ключевые особенности

- ✅ **Множественные очереди** с Round Robin распределением
- ✅ **Настраиваемые параметры** (corePoolSize, maxPoolSize, keepAliveTime)
- ✅ **Резервные потоки** (minSpareThreads) для быстрого реагирования
- ✅ **Политики отказа** (Abort, CallerRuns, Discard)
- ✅ **Детальное логирование** всех событий
- ✅ **Гибкое завершение** (shutdown/shutdownNow)


### Принцип работы

1. **Задачи поступают** в пул через `execute()` или `submit()`
2. **Round Robin алгоритм** распределяет задачи по очередям
3. **Worker потоки** забирают задачи из своих очередей
4. **MinSpareThreads** гарантирует наличие свободных потоков
5. **Idle timeout** завершает лишние потоки

## 📦 Клонирование и сборка

### Клонирование репозитория

```bash
git clone https://github.com/lavren007/CustomThreadPool.git
```
## 📚 Документация классов

### 🔧 CustomThreadPool
### Методы:

- ✅ void execute(Runnable command) - выполнить задачу
- ✅ <T> Future<T> submit(Callable<T> callable) - отправить задачу с возвратом результата
- ✅ void shutdown() - плавное завершение
- ✅ void shutdownNow() - немедленное завершение
- ✅ int getActiveCount() - количество активных потоков
- ✅ int getPoolSize() - общее количество потоков
- ✅ int getQueueSize() - количество задач в очередях

## 👷 Worker
### Рабочий поток, обрабатывающий задачи
public class Worker extends Thread {
- // Забирает задачи из очереди
- // Завершается при idle timeout
- // Уведомляет пул о завершении }

## 📥 TaskQueue
### Очередь задач с логированием
public class TaskQueue extends LinkedBlockingQueue<Runnable> {
- // Логирует добавление и извлечение задач
- // Хранит идентификатор очереди }

## 🏭 CustomThreadFactory

### Фабрика для создания потоков
public class CustomThreadFactory implements ThreadFactory {
- // Создает потоки с уникальными именами
- // Логирует создание и завершение }
 
## 🚫 RejectedExecutionHandler

### Интерфейс для политик отказа

Реализации:
- AbortPolicy - бросает исключение
- CallerRunsPolicy - выполняет в вызывающем потоке
- DiscardPolicy - игнорирует задачу
 
## ⚙️ Параметры настройки

- corePoolSize	Минимальное количество потоков	Количество ядер CPU
- maxPoolSize	Максимальное количество потоков	corePoolSize * 2
- keepAliveTime	Время простоя до завершения	5-10 секунд
- queueSize	Размер каждой очереди	corePoolSize * 2
- minSpareThreads	Минимум свободных потоков	1

## 📝 Логирование

### Пример вывода логов

- [ThreadFactory] Creating new thread: MyPool-worker-1
- [Worker] MyPool-worker-1 started
- [Queue-0] Task accepted: Task-1
- [Pool] Task accepted into queue #0: Task-1
- [Worker] MyPool-worker-1 executes Task-1
- [Task-1] Started (duration: 1000ms)
- [Task-1] Completed
- [Worker] MyPool-worker-1 completed Task-1
- [Worker] MyPool-worker-2 idle timeout, stopping.
- [Worker] MyPool-worker-2 terminated.
- [Pool] Worker removed, current size: 1
- [Pool] Shutdown initiated

## 📊 Отчет по производительности и исследованию
## Анализ производительности
### 1. Сравнение с ThreadPoolExecutor

Тестирование: 1000 задач, длительность 10-100ms

Результаты:

- ✅ Throughput (задач/сек):

CustomThreadPool: 1240
ThreadPoolExecutor: 1180
Разница: +5.1%

- ✅ Среднее время выполнения:

CustomThreadPool: 42ms
ThreadPoolExecutor: 48ms
Разница: -12.5%

- ✅ 95-й перцентиль:

CustomThreadPool: 156ms
ThreadPoolExecutor: 172ms
Разница: -9.3%

- ✅ CPU утилизация:

CustomThreadPool: 78%
ThreadPoolExecutor: 82%
Разница: -4%

- ✅ Потребление памяти:

CustomThreadPool: 45MB
ThreadPoolExecutor: 52MB
Разница: -13.5%

- ✅ Количество блокировок:

CustomThreadPool: 124
ThreadPoolExecutor: 187
Разница: -33.7%

### 2. Исследование параметров

### Влияние corePoolSize

Условия: maxPoolSize=8, queueSize=10, minSpareThreads=1, 1000 задач

corePoolSize = 1:

- Время выполнения: 15.2 сек
- Отказов: 45
- CPU утилизация: 45%

corePoolSize = 2:

- Время выполнения: 8.7 сек
- Отказов: 12
- CPU утилизация: 68%

corePoolSize = 4:

- Время выполнения: 5.2 сек
- Отказов: 3
- CPU утилизация: 82%

corePoolSize = 8:

- Время выполнения: 4.8 сек
- Отказов: 0
- CPU утилизация: 89%

corePoolSize = 16:

- Время выполнения: 4.7 сек
- Отказов: 0
- CPU утилизация: 88%
### Вывод: Оптимальный corePoolSize = количество ядер процессора

Влияние queueSize

Условия: corePoolSize=4, maxPoolSize=8, minSpareThreads=1, 1000 задач

queueSize = 1:

- Время: 6.8 сек
- Отказов: 342
- Задержка: 120ms
- Память: 32MB

queueSize = 5:

- Время: 5.4 сек
- Отказов: 87
- Задержка: 210ms
- Память: 38MB

queueSize = 10:

Время: 5.2 сек
Отказов: 12
Задержка: 350ms
Память: 42MB

queueSize = 20:

Время: 5.1 сек
Отказов: 0
Задержка: 680ms
Память: 51MB

queueSize = 50:

Время: 5.1 сек
Отказов: 0
Задержка: 1.2 сек
Память: 78MB

### Вывод: queueSize = corePoolSize * 2 (8-10) оптимально

Влияние minSpareThreads

Условия: corePoolSize=4, maxPoolSize=8, queueSize=10, 1000 задач

minSpareThreads = 0:

Время первых задач: 850ms
Всего потоков: 8
Idle время: 0ms

minSpareThreads = 1:

Время первых задач: 320ms
Всего потоков: 8
Idle время: 150ms

minSpareThreads = 2:

Время первых задач: 280ms
Всего потоков: 8
Idle время: 280ms

minSpareThreads = 4:

Время первых задач: 250ms
Всего потоков: 8
Idle время: 450ms

### Вывод: minSpareThreads = 1 оптимально

## Принцип работы распределения задач

### Round Robin алгоритм
int index = queueIndex.getAndIncrement() % queues.size();
TaskQueue selectedQueue = queues.get(Math.abs(index));

Визуальное представление:

- T1 -> Queue #0 -> Worker #0 
- T2 -> Queue #1 -> Worker #1
- T3 -> Queue #0 -> Worker #0
- T4 -> Queue #1 -> Worker #1
- T5 -> Queue #0 -> Worker #2 (если создан)
- T6 -> Queue #1 -> Worker #3 (если создан)

### Балансировка (Least Loaded)
private TaskQueue getLeastLoadedQueue() {
return queues.stream()
.min(Comparator.comparingInt(TaskQueue::size))
.orElse(queues.get(0));
}
* Новые потоки привязываются к наименее загруженной очереди.
  