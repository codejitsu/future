package future.java7;

import future.GetResult;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java 7 Futures.
 */
public class FutureJava7Test {
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
            final long start = System.currentTimeMillis();

            System.out.println("Before future for " + url + ". Timestamp: " + start);

            final Future<GetResult> fut = getUrl(url);

            System.out.println("After future for " + url + ". Timestamp: " + System.currentTimeMillis());

            System.out.println("Future for " + url + " created at " + start + ", completed: " + fut.isDone());
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

                System.out.println("Get future for " + fut.get().url + " created at " + start + ", completed: " + fut.isDone() + ", duration = " + fut.get().duration + " ms.");
            } catch (ExecutionException ee) {
                System.err.println("Exception in the future for " + url + " message: " + ee.getMessage());
            }
        }
    }

    @Test
    public void parallelFutures() throws IOException, ExecutionException, InterruptedException {
        final List<Future<GetResult>> futures = new ArrayList<>();

        for (final String url : urls) {
            final Future<GetResult> fut = getUrl(url);
            futures.add(fut);
        }

        while (true) {
            int allCompeted = 0;

            for (int i = 0; i < futures.size(); i++) {
                if (futures.get(i).isDone()) {
                    try {
                        System.out.println("Future completed, duration: " +
                                futures.get(i).get().url + " -> " + futures.get(i).get().duration);
                    } catch (ExecutionException ee) {
                        System.err.println("Future failed. Message: " + ee.getMessage());
                    }

                    allCompeted++;
                } else {
                    System.out.println("Future not completed yet.");
                    Thread.sleep(100);
                }
            }

            if (allCompeted >= futures.size()) break;
        }
    }

    public Future<GetResult> getUrl(final String url) throws IOException {
        return pool.submit(new Callable<GetResult>() {
            @Override
            public GetResult call() throws Exception {
                final GetMethod get = new GetMethod(url);

                final long start = System.currentTimeMillis();

                client.executeMethod(get);

                return new GetResult(url, System.currentTimeMillis() - start);
            }
        });
    }
}
