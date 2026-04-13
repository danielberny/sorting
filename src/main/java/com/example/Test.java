package com.example;

import edu.princeton.cs.algs4.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class Test {
    static final String[] ALGORITHMS = {
            Bubble.class.getSimpleName(),
            Selection.class.getSimpleName(),
            Insertion.class.getSimpleName(),
            Quick.class.getSimpleName(),
            Heap.class.getSimpleName(),
            Merge.class.getSimpleName(),
            "Arrays.sort()"
    };
    static final int ITERATIONS = 5;
    static final long SEED = 2026;
    static final int[] SIZES = {25000, 50000, 100000};

    static void main(String[] args) {
        System.out.print("zahřívání JVM... ");
        Integer[] warmup = new Integer[10000];
        for (int i = 0; i < warmup.length; i++) {
            warmup[i] = StdRandom.uniformInt(10000);
        }
        Bubble.sort(warmup.clone());
        Selection.sort(warmup.clone());
        Insertion.sort(warmup.clone());
        Quick.sort(warmup.clone());
        Heap.sort(warmup.clone());
        Merge.sort(warmup.clone());
        Arrays.sort(warmup.clone());
        System.out.println("hotovo.");
        
        System.out.print("příprava hlavních polí a JSONu... ");
        StdRandom.setSeed(SEED);
        Integer[] masterRandom = new Integer[100000];
        Integer[] masterDuplicates = new Integer[100000];
        for (int i = 0; i < 100000; i++) {
            masterRandom[i] = StdRandom.uniformInt(100000);
            masterDuplicates[i] = StdRandom.uniformInt(10);
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"seed\": ").append(SEED).append(",\n");
        json.append("    \"iterations\": ").append(ITERATIONS).append("\n");
        json.append("  },\n");
        json.append("  \"results\": {\n");
        System.out.println("hotovo.");

        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            System.out.println("\n### měření pro " + n + " ###");

            System.out.print("příprava... ");
            Integer[] random = new Integer[n];
            System.arraycopy(masterRandom, 0, random, 0, n);

            Integer[] sorted = random.clone();
            Arrays.sort(sorted);

            Integer[] reversed = sorted.clone();
            Collections.reverse(Arrays.asList(reversed));

            Integer[] partial = sorted.clone();
            int swaps = n / 10;
            while (swaps-- > 0) {
                int i1 = StdRandom.uniformInt(n);
                int i2 = StdRandom.uniformInt(n);
                exch(partial, i1, i2);
            }

            Integer[] duplicates = new Integer[n];
            System.arraycopy(masterDuplicates, 0, duplicates, 0, n);

            json.append("    \"").append(n).append("\": {\n");
            System.out.println("hotovo.");

            for (int j = 0; j < ALGORITHMS.length; j++) {
                String algorithm = ALGORITHMS[j];
                System.out.print(algorithm + "... ");

                double[] timesRandom = new double[ITERATIONS];
                double[] timesSorted = new double[ITERATIONS];
                double[] timesReversed = new double[ITERATIONS];
                double[] timesPartial = new double[ITERATIONS];
                double[] timesDuplicates = new double[ITERATIONS];

                for (int k = 0; k < ITERATIONS; k++) {
                    System.out.print((k+1) + ". měření... ");
                    timesRandom[k] = measureTime(algorithm, random.clone());
                    timesSorted[k] = measureTime(algorithm, sorted.clone());
                    timesReversed[k] = measureTime(algorithm, reversed.clone());
                    timesPartial[k] = measureTime(algorithm, partial.clone());
                    timesDuplicates[k] = measureTime(algorithm, duplicates.clone());
                }

                addAlgorithmToJSON(json, algorithm, timesRandom, timesSorted, timesReversed, timesPartial, timesDuplicates, j == ALGORITHMS.length - 1);
                System.out.println("hotovo.");
            }

            json.append("    }");
            if (i < SIZES.length - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  }\n}\n");
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
                throw new IllegalArgumentException("neznámý algoritmus " + algorithm);
        }

        double time = timer.elapsedTime();
        if (!isSorted(data)) {
            throw new RuntimeException("chybné řazení u algoritmu " + algorithm);
        }
        return time;
    }

    private static void addAlgorithmToJSON(StringBuilder sb, String algorithm, double[] random, double[] sorted, double[] reversed, double[] partial, double[] duplicates, boolean isLast) {
        sb.append("      \"").append(algorithm).append("\": {\n");
        sb.append("        \"random\": ").append(Arrays.toString(random)).append(",\n");
        sb.append("        \"sorted\": ").append(Arrays.toString(sorted)).append(",\n");
        sb.append("        \"reversed\": ").append(Arrays.toString(reversed)).append(",\n");
        sb.append("        \"partial\": ").append(Arrays.toString(partial)).append(",\n");
        sb.append("        \"duplicates\": ").append(Arrays.toString(duplicates)).append("\n");
        sb.append("      }");
        if (!isLast) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private static void saveToFile(String contents) {
        try {
            Path folder = Paths.get("reports");
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            Path file = folder.resolve("results.json");

            int counter = 1;
            while (Files.exists(file)) {
                file = folder.resolve("results" + counter + ".json");
                counter++;
            }

            Files.writeString(file, contents);
            System.out.println("\nvýsledky byly bezpečně uloženy do: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("chyba při zápisu do souboru: " + e.getMessage());
        }
    }
}