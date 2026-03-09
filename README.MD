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