public final class InputNormalizer {
    private InputNormalizer() {
    }

    public static String toHalfWidth(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character >= '０' && character <= '９') {
                result.append((char) (character - '０' + '0'));
            } else if (character == '－' || character == 'ー' || character == '‐') {
                result.append('-');
            } else if (character == '：') {
                result.append(':');
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }
}
