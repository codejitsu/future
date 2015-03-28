package future.java8;

import future.GetResult;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java 8 Futures.
 */
public class FutureJava8Test {
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    private HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

    private List<String> urls;

    @Before
    public void setUp() {
        Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        Logger.getLogger("httpclient").setLevel(Level.OFF);

        urls = new ArrayList<>();

        urls.add("http://www.google.com");
        urls.add("http://www.yahoo.com");
        urls.add("http://www.bing.com");
        urls.add("http://www.notexisteddomain.org");
    }

    @Test
    public void createFutureAsync() throws IOException {
        for (final String url : urls) {
            System.out.println("Before future for " + url + ". Timestamp: " + System.currentTimeMillis());

            final Future<GetResult> fut = getUrl(url);

            System.out.println("Future for " + url + " created at " + System.currentTimeMillis() + ", completed: " + fut.isDone());
        }
    }

    @Test
    public void createFutureBlocking() throws IOException, ExecutionException, InterruptedException {
        for (final String url : urls) {
            try {
                final long start = System.currentTimeMillis();
                System.out.println("Start future for " + url + " at " + start);

                final Future<GetResult> fut = getUrl(url);
                fut.get();

                final long end = System.currentTimeMillis();

                System.out.println("Get future for " + url + " created at " + end + ", completed: " + fut.isDone() + ", duration = " + (end - start) + " ms.");
            } catch (ExecutionException ee) {
                System.err.println("Exception in the future for " + url + " message: " + ee.getMessage());
            }
        }
    }

    @Test
    public void createFutureWithOnCompletionBlock() throws IOException, InterruptedException {
        final CountDownLatch completed = new CountDownLatch(urls.size());

        for (final String url : urls) {
            System.out.println("Before future for " + url + ". Timestamp: " + System.currentTimeMillis());

            final CompletableFuture<Optional<GetResult>> fut = getUrl(url).handle((res, ex) -> {
                completed.countDown();

                if (res != null) {
                    System.out.println("Future for " + res.url + " completed.");
                    return Optional.of(res);
                } else {
                    System.out.println("Exception: " + ex.getMessage());
                    return Optional.empty();
                }
            });

            System.out.println("Future for " + url + " created at " + System.currentTimeMillis() + ", completed: " + fut.isDone());
        }

        completed.await();
        System.out.println("All futures with callbacks completed.");
    }

    public CompletableFuture<GetResult> getUrl(final String url) throws IOException {
        final CompletableFuture<GetResult> fut =
                CompletableFuture.supplyAsync(() -> {
                    final GetMethod get = new GetMethod(url);

                    final long start = System.currentTimeMillis();

                    try {
                        client.executeMethod(get);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }

                    return new GetResult(url, System.currentTimeMillis() - start);
                }, pool);

        return fut;
    }
}
