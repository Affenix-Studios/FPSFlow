package dev.fpsflow.compatibility;

import java.util.Locale;

public final class MinecraftVersionCompat {

    private MinecraftVersionCompat() {}

    public static boolean isSupportedRuntime() {
        return isAtLeast(1, 21, 11);
    }

    public static boolean isAtLeast(int major, int minor, int patch) {
        int[] runtime = parseRuntimeVersion(getGameVersion());
        int[] required = {major, minor, patch};

        for (int i = 0; i < required.length; i++) {
            int runtimePart = i < runtime.length ? runtime[i] : 0;
            int requiredPart = required[i];
            if (runtimePart > requiredPart) return true;
            if (runtimePart < requiredPart) return false;
        }
        return true;
    }

    public static String getGameVersion() {
        String runtimeVersion = System.getProperty("minecraft.version");
        if (runtimeVersion != null && !runtimeVersion.isBlank()) {
            return runtimeVersion;
        }

        String gameVersion = System.getProperty("net.minecraft.client.version");
        if (gameVersion != null && !gameVersion.isBlank()) {
            return gameVersion;
        }

        return "unknown";
    }

    private static int[] parseRuntimeVersion(String version) {
        if (version == null || version.isBlank()) {
            return new int[]{0, 0, 0};
        }

        String normalized = version.toLowerCase(Locale.ROOT);
        String[] tokens = normalized.split("[^0-9]+");
        int[] values = new int[3];
        int index = 0;

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (index >= values.length) {
                break;
            }
            values[index++] = Integer.parseInt(token);
        }

        return values;
    }
}
