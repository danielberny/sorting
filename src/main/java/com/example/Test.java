package com.example;

import edu.princeton.cs.algs4.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class Test {
    private static final String[] ALGORITHMS = {
            Bubble.class.getSimpleName(),
            Selection.class.getSimpleName(),
            Insertion.class.getSimpleName(),
            Quick.class.getSimpleName(),
            Heap.class.getSimpleName(),
            Merge.class.getSimpleName(),
            "Arrays.sort()"
    };
    private static final int[] ARRAY_SIZES = {25000, 50000, 100000};

    private static final long SEED = 2026;
    private static final byte ITERATIONS = 5;
    private static final byte WARMPUP_ITERATIONS = 5;
    private static final short PARTIAL_SWAP_PERCENTAGE = 10;
    private static final int DUPLICATES_RANGE = 10;

    static void main(String[] args) {
        Arrays.sort(ARRAY_SIZES);
        int biggestSize = ARRAY_SIZES[ARRAY_SIZES.length - 1];
        Integer[] warmup = new Integer[biggestSize];
        int warmupIterations = WARMPUP_ITERATIONS;

        System.out.print("Aplikování algoritmů na zahřívacích polích... ");

        while (warmupIterations-- > 0) {
            for (int i = 0; i < warmup.length; i++) {
                warmup[i] = StdRandom.uniformInt(biggestSize);
            }

            Bubble.sort(warmup.clone());
            Selection.sort(warmup.clone());
            Insertion.sort(warmup.clone());
            Quick.sort(warmup.clone());
            Heap.sort(warmup.clone());
            Merge.sort(warmup.clone());
            Arrays.sort(warmup.clone());
        }

        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        System.out.println("hotovo.");
        
        System.out.print("Generování polí pro měření... ");
        StdRandom.setSeed(SEED);
        Integer[] masterRandom = new Integer[biggestSize];
        Integer[] masterDuplicates = new Integer[biggestSize];
        for (int i = 0; i < biggestSize; i++) {
            masterRandom[i] = StdRandom.uniformInt(biggestSize);
            masterDuplicates[i] = StdRandom.uniformInt(DUPLICATES_RANGE);
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"array_sizes\": ").append(Arrays.toString(ARRAY_SIZES)).append(",\n");
        json.append("    \"seed\": ").append(SEED).append(",\n");
        json.append("    \"iterations\": ").append(ITERATIONS).append(",\n");
        json.append("    \"duplicates_range\": ").append(DUPLICATES_RANGE).append(",\n");
        json.append("    \"partial_swap_percentage\": ").append(PARTIAL_SWAP_PERCENTAGE).append("\n");
        json.append("  },\n");
        json.append("  \"results\": {\n");
        System.out.println("hotovo.");

        for (int i = 0; i < ARRAY_SIZES.length; i++) {
            int n = ARRAY_SIZES[i];
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            String fn = String.format(new Locale("cs", "CZ"), "%,d", n);
            System.out.println("\nMěření na polích n=(" + fn + ")");
            System.out.print("Příprava... ");
            Integer[] random = new Integer[n];
            System.arraycopy(masterRandom, 0, random, 0, n);

            Integer[] sorted = random.clone();
            Arrays.sort(sorted);

            Integer[] reversed = sorted.clone();
            Collections.reverse(Arrays.asList(reversed));

            Integer[] partial = sorted.clone();
            int swaps = (n * PARTIAL_SWAP_PERCENTAGE) / 100;
            while (swaps-- > 0) {
                exch(partial, StdRandom.uniformInt(n), StdRandom.uniformInt(n));
            }

            Integer[] duplicates = new Integer[n];
            System.arraycopy(masterDuplicates, 0, duplicates, 0, n);

            json.append("    \"").append(n).append("\": {\n");
            System.out.println("hotovo.");

            for (int j = 0; j < ALGORITHMS.length; j++) {
                String algorithm = ALGORITHMS[j];
                System.out.print(algorithm + "... ");

                double[] timeRandom = new double[ITERATIONS];
                double[] timePartial = new double[ITERATIONS];
                double[] timeDuplicates = new double[ITERATIONS];
                double[] timeSorted = new double[ITERATIONS];
                double[] timeReversed = new double[ITERATIONS];

                for (int k = 0; k < ITERATIONS; k++) {
                    System.out.print((k+1) + ". měření... ");
                    timeRandom[k] = measureTime(algorithm, random.clone());
                    timePartial[k] = measureTime(algorithm, partial.clone());
                    timeDuplicates[k] = measureTime(algorithm, duplicates.clone());
                    timeSorted[k] = measureTime(algorithm, sorted.clone());
                    timeReversed[k] = measureTime(algorithm, reversed.clone());
                }

                addAlgorithmToJSON(json, algorithm, timeRandom, timePartial, timeDuplicates, timeSorted, timeReversed, j == ALGORITHMS.length - 1);
                System.out.println("hotovo.");
            }

            json.append("    }");
            if (i < ARRAY_SIZES.length - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  }\n}");
        saveToFile(json.toString());
    }

    private static void exch(Object[] a, int i, int j) {
        Object swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    private static boolean isSorted(Comparable[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i].compareTo(array[i - 1]) < 0) {
                return false;
            }
        }
        return true;
    }

    private static double measureTime(String algorithm, Integer[] data) {
        Stopwatch timer = new Stopwatch();

        switch (algorithm) {
            case "Bubble":
                Bubble.sort(data);
                break;
            case "Selection":
                Selection.sort(data);
                break;
            case "Insertion":
                Insertion.sort(data);
                break;
            case "Quick":
                Quick.sort(data);
                break;
            case "Heap":
                Heap.sort(data);
                break;
            case "Merge":
                Merge.sort(data);
                break;
            case "Arrays.sort()":
                Arrays.sort(data);
                break;
            default:
                throw new IllegalArgumentException("Neznámý algoritmus " + algorithm);
        }

        double time = timer.elapsedTime();
        if (!isSorted(data)) {
            throw new RuntimeException("Chybné řazení u algoritmu " + algorithm);
        }
        return time;
    }

    private static void addAlgorithmToJSON(StringBuilder json, String algorithm, double[] random, double[] partial, double[] duplicates, double[] sorted, double[] reversed, boolean isLast) {
        json.append("      \"").append(algorithm).append("\": {\n");
        appendScenario(json, "random", random, false);
        appendScenario(json, "partial", partial, false);
        appendScenario(json, "duplicates", duplicates, false);
        appendScenario(json, "sorted", sorted, false);
        appendScenario(json, "reversed", reversed, true);
        json.append("      }");
        if (!isLast) {
            json.append(",");
        }
        json.append("\n");
    }

    private static void appendScenario(StringBuilder json, String scenario, double[] iterations, boolean isLast) {
        double sum = 0;
        for (double t : iterations) {
            sum += t;
        }
        double average = sum / iterations.length;
        DecimalFormat df = new DecimalFormat("0.0##", new DecimalFormatSymbols(Locale.US));

        json.append("        \"").append(scenario).append("\": {\n");
        json.append("          \"average\": ").append(df.format(average)).append(",\n");
        json.append("          \"iterations\": ").append(Arrays.toString(iterations)).append("\n");
        json.append("        }");
        if (!isLast) {
            json.append(",");
        }
        json.append("\n");
    }

    private static void saveToFile(String contents) {
        try {
            Path folder = Paths.get("vysledky");
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            Path file = folder.resolve("vysledky.json");

            int counter = 1;
            while (Files.exists(file)) {
                file = folder.resolve("vysledky" + counter + ".json");
                counter++;
            }

            Files.writeString(file, contents);
            System.out.println("\nVýsledky byly uloženy do: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Chyba při zápisu do souboru: " + e.getMessage());
        }
    }
}