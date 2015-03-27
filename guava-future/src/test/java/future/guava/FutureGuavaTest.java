package future.guava;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
 * Guava Futures.
 */
public class FutureGuavaTest {
    private final ExecutorService backgroundPool = Executors.newFixedThreadPool(10);
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(backgroundPool);

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
    public void createFutureWithCallback() throws IOException, InterruptedException {
        final CountDownLatch completed = new CountDownLatch(urls.size());

        for (final String url : urls) {
            System.out.println("Before future for " + url + ". Timestamp: " + System.currentTimeMillis());

            final ListenableFuture<GetResult> fut = getUrl(url);

            fut.addListener(new Runnable() {
                @Override
                public void run() {
                    GetResult res = null;
                    try {
                        res = fut.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        System.out.println("Exception: " + e.getMessage());
                    } finally {
                        if (res != null) {
                            System.out.println("Future for " + res.url + " completed.");
                        }

                        completed.countDown();
                    }
                }
            }, MoreExecutors.newDirectExecutorService());

            System.out.println("Future for " + url + " created at " + System.currentTimeMillis() + ", completed: " + fut.isDone());
        }

        completed.await();
        System.out.println("All futures with callbacks completed.");
    }

    public ListenableFuture<GetResult> getUrl(final String url) throws IOException {
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
