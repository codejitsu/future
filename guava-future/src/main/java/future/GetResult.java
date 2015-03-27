package future;

public class GetResult {
    public final String url;
    public final Long duration;

    public GetResult(final String url, final Long duration) {
        this.duration = duration;
        this.url = url;
    }
}
