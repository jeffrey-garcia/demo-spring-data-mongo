package com.example.jeffrey.demospringdatamongo.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTest {

    @Test
    public void testConvertLocalDateTimeToRfc3339() {
        Instant instant = Instant.parse("2019-01-01T00:00:00.000Z");

        LocalDateTime localDateTime_Tha = LocalDateTime.ofInstant(
                instant,
                ZonedDateTime.now(ZoneId.of("Asia/Bangkok")).getOffset()
        );
        ZonedDateTime zonedDateTime_Tha = toZonedDateTime(localDateTime_Tha, ZoneId.of("Asia/Bangkok"));

        String rfc3339 = toRfc3339(zonedDateTime_Tha);
        System.out.println(rfc3339);

        Assert.assertEquals("2019-01-01T07:00:00+07:00", rfc3339);
    }

    @Test
    public void testConvertRfc3339ToLocalDateTime() {
        String rfc3339 = "2019-01-01T07:00:00+07:00";
        ZonedDateTime zonedDateTime_Tha = fromRfc3339(rfc3339);

        /**
         * convert the universal ZonedDateTime (which preserve the date/time value corresponding to the original timezone)
         * into a LocalDateTime (date/time value respective to the running JVM's system timezone)
         */
        LocalDateTime localDateTime_JVM = fromZonedDateTime(zonedDateTime_Tha, ZoneId.systemDefault());

        Assert.assertEquals(2019, localDateTime_JVM.getYear());
        Assert.assertEquals(1, localDateTime_JVM.getMonth().getValue());
        Assert.assertEquals(1, localDateTime_JVM.getDayOfMonth());
        Assert.assertEquals(8, localDateTime_JVM.getHour());
        Assert.assertEquals(0, localDateTime_JVM.getMinute());
        Assert.assertEquals(0, localDateTime_JVM.getSecond());
    }

    /**
     * Convert a LocalDateTime into a ZonedDateTime representation of date/time, presumed you have knowledge about
     * the respective timezone of the LocalDateTime
     * @param localDateTime the LocalDateTime to convert
     * @param zoneId the ZoneId with respective to the timezone of the LocalDateTime
     * @return a ZonedDateTime representation of date/time containing timezone information corresponds to the designated ZoneId
     */
    protected ZonedDateTime toZonedDateTime(final LocalDateTime localDateTime, final ZoneId zoneId) {
        return ZonedDateTime.of(localDateTime, zoneId);
    }

    /**
     * Convert a ZonedDateTime into a LocalDateTime representation of date/time into your target timezone
     * @param zonedDateTime the ZonedDateTime to convert
     * @param zoneId the ZoneId with respective to the target timezone of the LocalDateTime
     * @return a LocalDateTime representation of date/time containing timezone information corresponds to the designated ZoneId
     */
    protected LocalDateTime fromZonedDateTime(final ZonedDateTime zonedDateTime, final ZoneId zoneId) {
        return LocalDateTime.ofInstant(zonedDateTime.toInstant(), zoneId);
    }

    /**
     * Convert a ZoneDateTime into a String representation of date/time, conforming to RFC3339 standard
     * @param zonedDateTime the ZonedDateTime to convert
     * @return a String representation of date/time containing timezone information corresponds to the ZoneDateTime
     */
    protected String toRfc3339(final ZonedDateTime zonedDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX:00");
        return dateTimeFormatter.format(zonedDateTime);
    }

    /**
     * Convert a String into a ZonedDateTime representation of date/time, original timezone information
     * will be restored
     * @param rfc3339 the String to be converted (must be conforming to RFC3339)
     * @return a ZonedDateTime representation of date/time containing timezone information restored
     */
    protected ZonedDateTime fromRfc3339(final String rfc3339) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX:00");
        return ZonedDateTime.parse(rfc3339, dateTimeFormatter);
    }

}
