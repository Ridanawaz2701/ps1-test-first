/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy
     *
     * writtenBy():
     *   matches: 0, 1, many
     *   username case: same as author, different from author
     *   order of results preserved
     *
     * inTimespan():
     *   tweet position: before span, at start, inside, at end, after span
     *   span length: zero-length, positive
     *   results: none, some, all
     *   order of results preserved
     *
     * containing():
     *   words list size: 1, 2+
     *   matching tweets: 0, 1, many
     *   case: same, different
     *   whole word vs. substring of a word ("talk" vs "talking")
     *   punctuation attached to a word ("ok" vs "ok?")
     *   hashtag word ("#hype")
     */

    private static final Instant d0 = Instant.parse("2016-02-17T09:00:00Z");
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "Alyssa", "Talking about RIVEST again", d3);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ---------------- writtenBy ----------------

    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    @Test
    public void testWrittenByNoMatch() {
        assertTrue(Filter.writtenBy(Arrays.asList(tweet1, tweet2), "nobody").isEmpty());
    }

    @Test
    public void testWrittenByIgnoresCaseAndKeepsOrder() {
        List<Tweet> result = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3), "ALYSSA");
        assertEquals(Arrays.asList(tweet1, tweet3), result);
    }

    // ---------------- inTimespan ----------------

    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));

        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }

    @Test
    public void testInTimespanBoundariesInclusive() {
        List<Tweet> result = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(d1, d2));
        assertEquals(Arrays.asList(tweet1, tweet2), result);
    }

    @Test
    public void testInTimespanExcludesOutside() {
        List<Tweet> result = Filter.inTimespan(Arrays.asList(tweet1, tweet2, tweet3), new Timespan(d1, d2));
        assertEquals(Arrays.asList(tweet1, tweet2), result);
    }

    @Test
    public void testInTimespanNoMatch() {
        assertTrue(Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(d0, d0)).isEmpty());
    }

    @Test
    public void testInTimespanZeroLengthSpan() {
        List<Tweet> result = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(d1, d1));
        assertEquals(Arrays.asList(tweet1), result);
    }

    // ---------------- containing ----------------

    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));

        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    @Test
    public void testContainingCaseInsensitive() {
        List<Tweet> result = Filter.containing(Arrays.asList(tweet1, tweet2, tweet3), Arrays.asList("RIVEST"));
        assertEquals(Arrays.asList(tweet1, tweet2, tweet3), result);
    }

    @Test
    public void testContainingWholeWordOnly() {
        // "talk" must NOT match "Talking"
        List<Tweet> result = Filter.containing(Arrays.asList(tweet3), Arrays.asList("talk"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testContainingPunctuationAttached() {
        Tweet t = new Tweet(8, "x", "is it ok?", d1);
        assertTrue(Filter.containing(Arrays.asList(t), Arrays.asList("ok")).isEmpty());
        assertEquals(Arrays.asList(t), Filter.containing(Arrays.asList(t), Arrays.asList("ok?")));
    }

    @Test
    public void testContainingHashtagAndMultipleWords() {
        List<Tweet> result = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("#hype", "nothing"));
        assertEquals(Arrays.asList(tweet2), result);
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}