package rimz.util;

public final class TextCodec {
    private TextCodec() { }

    /** Makes text safe to store in a '|' separated line. */
    public static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '|':  sb.append("\\p");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Reverses escape(). */
    public static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                switch (next) {
                    case 'p':  sb.append('|');  break;
                    case 'n':  sb.append('\n'); break;
                    default:   sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}