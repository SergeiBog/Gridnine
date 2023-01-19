package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Factory class to get sample list of flights.
 */
class Main {
    public static void main(String[] args) {
        List<Flight> flights = FlightBuilder.createFlights();
        System.out.println("Все полеты:\n" + flights);
        System.out.println("Полеты без вылета до текущего момента времени:\n" + new buildFlightWithFilter(flights).flightBeforeNow().flights());
        System.out.println("Полеты, не содержащие сегменты с датой прилёта раньше даты вылета:\n" + new buildFlightWithFilter(flights).segmentsArrivalBeforeNow().flights());
        System.out.println();
    }
}

class FlightBuilder {
    static List<Flight> createFlights() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        return Arrays.asList(
                //A normal flight with two hour duration
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                //A normal multi segment flight
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                //A flight departing in the past
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                //A flight that departs before it arrives
                createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
                //A flight with more than two hours ground time
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                //Another flight with more than two hours ground time
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7)));
    }

    private static Flight createFlight(final LocalDateTime... dates) {
        if ((dates.length % 2) != 0) {
            throw new IllegalArgumentException(
                    "you must pass an even number of dates");
        }
        List<Segment> segments = new ArrayList<>(dates.length / 2);
        for (int i = 0; i < (dates.length - 1); i += 2) {
            segments.add(new Segment(dates[i], dates[i + 1]));
        }
        return new Flight(segments);
    }
}

/**
 * Bean that represents a flight.
 */
class Flight {
    private final List<Segment> segments;

    Flight(final List<Segment> segs) {
        segments = segs;
    }

    List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return segments.stream().map(Object::toString)
                .collect(Collectors.joining(" "));
    }
}

/**
 * Bean that represents a flight segment.
 */
class Segment {
    private final LocalDateTime departureDate;

    private final LocalDateTime arrivalDate;

    Segment(final LocalDateTime dep, final LocalDateTime arr) {
        departureDate = Objects.requireNonNull(dep);
        arrivalDate = Objects.requireNonNull(arr);
    }

    LocalDateTime getDepartureDate() {
        return departureDate;
    }

    LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return '[' + departureDate.format(fmt) + '|' + arrivalDate.format(fmt)
                + ']';
    }
}

abstract class flightFilter {
    abstract List<Flight> flights();

    abstract flightFilter flightBeforeNow();

    abstract flightFilter segmentsArrivalBeforeNow();

    abstract flightFilter timeOnEarthMoreTwoHours();
}

class buildFlightWithFilter extends flightFilter{
    private final List<Flight> flights;

    buildFlightWithFilter(List<Flight> flights) {
        this.flights = new ArrayList<>(flights);
    }

    @Override
    List<Flight> flights() {
        return flights;
    }

    @Override
    flightFilter flightBeforeNow() {
        flights.removeIf(flight -> flight.getSegments().stream().allMatch(segment -> segment.getDepartureDate().isBefore(LocalDateTime.now())));
        return this;
    }

    @Override
    flightFilter segmentsArrivalBeforeNow() {
        flights.removeIf(flight -> flight.getSegments().stream().allMatch(segment -> segment.getArrivalDate().isBefore(segment.getDepartureDate())));
        return this;
    }

    @Override
    flightFilter timeOnEarthMoreTwoHours() {

        return null;
    }
}