package hk.timeslogistics.wms.utils;

public class SubmitResult extends RemoteResult {
    public SubmitResult(Integer newStatus) {
        super(newStatus);
    }

    @Override
    public String toString() {
        return "SubmitResult{" +
                '}';
    }
}
