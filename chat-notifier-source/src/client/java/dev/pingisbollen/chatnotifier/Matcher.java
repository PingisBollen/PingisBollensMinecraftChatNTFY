package dev.pingisbollen.chatnotifier;

import java.util.regex.Pattern;

public final class Matcher {
    private Matcher() {}
    public static boolean matches(String message, String keyword, boolean caseSensitive, boolean wholeWord) {
        if (message == null || keyword == null || keyword.isBlank()) return false;
        String haystack = caseSensitive ? message : message.toLowerCase(java.util.Locale.ROOT);
        String needle = caseSensitive ? keyword : keyword.toLowerCase(java.util.Locale.ROOT);
        if (!wholeWord) return haystack.contains(needle);
        return Pattern.compile("(?<![\\p{L}\\p{N}_])" + Pattern.quote(needle) + "(?![\\p{L}\\p{N}_])").matcher(haystack).find();
    }
}
