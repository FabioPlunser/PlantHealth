package at.ac.uibk.plant_health.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

// https://stackoverflow.com/a/55581431
public class LocalDateTimeJsonParser extends BaseMatcher<LocalDateTime> {

    private LocalDateTime from;
    private LocalDateTime to;
    private int misMatchAtIndex;

    public LocalDateTimeJsonParser(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public LocalDateTimeJsonParser(LocalDateTime from, TemporalAmount diff) {
        this.from = from;
        this.to = from.plus(diff);
    }

    public static LocalDateTimeJsonParser equalsWithTolerance(LocalDateTime dateTime, TemporalAmount tolerance) {
        return new LocalDateTimeJsonParser(dateTime.minus(tolerance), dateTime.plus(tolerance));
    }

    @Override
    public boolean matches(Object item) {
        var date = LocalDateTime.parse((String) item);
        return date.isAfter(from) && date.isBefore(to);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("All DateTime fields from %s to %s, mismatch at index %d",
                from, to, misMatchAtIndex));
    }
}