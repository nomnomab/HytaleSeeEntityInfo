package com.nomnom.seeentityinfo;

public class TypeNameUtil {

    /**
     * Converts "com.hypixel.hytale.component.physics.VelocityComponent" to "VelocityComponent"
     */
    public static String getSimpleName(String fullTypeName) {
        if (fullTypeName == null || fullTypeName.isEmpty()) {
            return fullTypeName;
        }

        int lastDot = fullTypeName.lastIndexOf('.');
        if (lastDot == -1) {
            return fullTypeName;
        }

        return fullTypeName.substring(lastDot + 1);
    }

    /**
     * Converts "VelocityComponent" to "Velocity" (removes common suffixes)
     */
//    public static String getCleanName(String fullTypeName) {
//        String simple = getSimpleName(fullTypeName);
//
//        // Remove common suffixes
//        String[] suffixes = {"Component", "System", "Resource", "Data", "Type"};
//        for (String suffix : suffixes) {
//            if (simple.endsWith(suffix) && simple.length() > suffix.length()) {
//                return simple.substring(0, simple.length() - suffix.length());
//            }
//        }
//
//        return simple;
//    }

    /**
     * Converts "VelocityComponent" to "Velocity Component" (adds spaces before capitals)
     */
    public static String getDisplayName(String fullTypeName) {
        String name = getSimpleName(fullTypeName);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                result.append(' ');
            }
            result.append(c);
        }

        return result.toString();
    }

    /**
     * Process an array of type names
     */
    public static String[] getSimpleNames(String[] fullTypeNames) {
        String[] result = new String[fullTypeNames.length];
        for (int i = 0; i < fullTypeNames.length; i++) {
            result[i] = getSimpleName(fullTypeNames[i]);
        }
        return result;
    }
}