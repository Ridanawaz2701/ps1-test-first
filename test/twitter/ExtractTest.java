/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy
     *
     * getTimespan():
     *   number of tweets: 0, 1, 2+
     *   order of timestamps: ascending, unordered
     *   timestamps: all distinct, some equal
     *
     * getMentionedUsers():
     *   number of mentions: 0, 1, 2+ distinct
     *   same user mentioned repeatedly: same case, different case
     *   position of mention: start, middle, end of text
     *   char right after mention: space, punctuation, end of text
     *   email address (valid username char before @): not a mention
     *   username chars: letters, digits, underscore, hyphen
     *   lone "@" with no username
     *   mentions spread across several tweets
     *
     * Note: the spec leaves the case of returned usernames unspecified, so
     * tests lowercase the result before comparing. The spec also leaves the
     * result for an empty list underdetermined, so we only check that the
     * interval has zero length.
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T09:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "user_1", "no mentions here", d3);
    private static final Tweet tweet4 = new Tweet(4, "carol", "same time as tweet1", d1);

    // ---- helpers (kept in the test class, not in Extract) ----

    private static Set<String> lower(Set<String> in) {
        Set<String> out = new HashSet<>();
        for (String s : in) {
            out.add(s.toLowerCase());
        }
        return out;
    }

    private static Set<String> set(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    private static Set<String> mentionsIn(String text) {
        return lower(Extract.getMentionedUsers(Arrays.asList(new Tweet(99, "x", text, d1))));
    }

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ---------------- getTimespan ----------------

    @Test
    public void testGetTimespanEmptyList() {
        Timespan timespan = Extract.getTimespan(new ArrayList<Tweet>());
        assertEquals("expected zero-length interval", timespan.getStart(), timespan.getEnd());
    }

    @Test
    public void testGetTimespanSingleTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanUnorderedTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet3, tweet1));
        assertEquals("expected start", d3, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanEqualTimestamps() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet4));
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    // ---------------- getMentionedUsers ----------------

    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testMentionSingle() {
        assertEquals(set("bob"), mentionsIn("hi @bob how are you"));
    }

    @Test
    public void testMentionAtStartAndEnd() {
        assertEquals(set("alice", "bob"), mentionsIn("@alice hello @bob"));
    }

    @Test
    public void testMentionDuplicateSameCase() {
        assertEquals(set("bob"), mentionsIn("@bob and @bob"));
    }

    @Test
    public void testMentionDuplicateDifferentCase() {
        Set<String> result = Extract.getMentionedUsers(Arrays.asList(
                new Tweet(5, "x", "@Bob and @bOB", d1)));
        assertEquals("username should appear at most once", 1, result.size());
    }

    @Test
    public void testMentionFollowedByPunctuation() {
        assertEquals(set("bob"), mentionsIn("thanks @bob, see you"));
    }

    @Test
    public void testMentionEmailIsNotMention() {
        assertTrue(mentionsIn("mail bitdiddle@mit.edu now").isEmpty());
    }

    @Test
    public void testMentionUsernameChars() {
        assertEquals(set("a_b-c9"), mentionsIn("hey @a_b-c9!"));
    }

    @Test
    public void testMentionLoneAt() {
        assertTrue(mentionsIn("meet @ noon").isEmpty());
    }

    @Test
    public void testMentionAcrossTweets() {
        Set<String> result = lower(Extract.getMentionedUsers(Arrays.asList(
                new Tweet(6, "x", "@ann hi", d1),
                new Tweet(7, "x", "@ben hi", d2))));
        assertEquals(set("ann", "ben"), result);
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}