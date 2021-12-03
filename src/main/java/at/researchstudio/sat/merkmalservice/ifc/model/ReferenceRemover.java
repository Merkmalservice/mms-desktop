package at.researchstudio.sat.merkmalservice.ifc.model;

public abstract class ReferenceRemover {
    public static String removeReferenceTo(String line, Integer itemId) {
        StringBuilder result = new StringBuilder();
        String toRemove = "#" + itemId;
        int toRemoveLength = toRemove.length();
        int pos = 0;
        int lastPos = 0;
        int depth = 0;
        char lastChar = '_';
        char curChar = '_';
        for (int i = 0; i < line.length(); i++) {
            lastChar = curChar;
            curChar = line.charAt(i);
            if (curChar == '(') {
                depth++;
            } else if (curChar == ')') {
                depth--;
            } else if (curChar == '#') {
                if (line.substring(i, i + toRemoveLength).equals(toRemove)) {
                    if (depth == 1) {
                        result.append("$");
                        i = i + toRemoveLength - 1;
                        continue;
                    } else if (depth > 1) {
                        // in a list: remove the reference and the trailing comma, the leading
                        // comma, or the list altogether
                        if (line.charAt(i + toRemoveLength) == ',') {
                            i = i + toRemoveLength; // swallow trailing ',' in lists
                        } else if (lastChar == ',') {
                            result.delete(
                                    result.length() - 1,
                                    result.length()); // swallow preceding ',' in lists
                            i = i + toRemoveLength - 1;
                        } else {
                            // assuming only one element in list, lastchar = '(', swallow that, the
                            // trailing ')', and replace by '$'
                            result.delete(result.length() - 1, result.length());
                            result.append("$");
                            i = i + toRemoveLength;
                        }
                        continue;
                    }
                }
            }
            result.append(curChar);
            continue;
        }
        return result.toString();
    }
}
