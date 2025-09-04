# Web Crawler

Parallel web crawler built with Java that crawls websites, counts word frequencies, and profiles performance.

## Features

- **Parallel Processing**: Uses ForkJoinPool for concurrent page crawling
- **JSON Configuration**: Configurable crawl depth, timeout, ignored URLs/words
- **Word Counting**: Finds most popular words with functional programming
- **Performance Profiling**: Tracks method execution times with dynamic proxies
- **Thread-Safe**: Concurrent data structures prevent race conditions

## Build & Run

```bash
mvn package
java -cp target/udacity-webcrawler-1.0.jar com.udacity.webcrawler.main.WebCrawlerMain config.json
```

## Configuration

Sample `config.json`:
```json
{
  "startPages": ["http://example.com"],
  "parallelism": 4,
  "maxDepth": 2,
  "timeoutSeconds": 10,
  "popularWordCount": 5,
  "ignoredUrls": [".*\\.pdf"],
  "ignoredWords": ["^.{1,3}$"]
}
```

## Output

Returns JSON with word counts and profiling data:
```json
{"wordCounts":{"web":45,"crawler":32},"urlsVisited":12}
```

## Implementation

- **ParallelWebCrawler**: RecursiveAction tasks with ForkJoinPool
- **ConfigurationLoader**: Jackson JSON parsing with builder pattern  
- **ProfilingMethodInterceptor**: Dynamic proxy for method timing
- **WordCounts**: Stream API for functional word sorting
- **Thread Safety**: ConcurrentHashMap and ConcurrentSkipListSet

## Testing

```bash
mvn test
```
