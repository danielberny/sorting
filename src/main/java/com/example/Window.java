package com.example;

import edu.princeton.cs.algs4.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class Window {
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
    private static final byte WARMUP_ITERATIONS = 5;

    private static long seed;
    private static byte iterations;
    private static short partial_swap_percentage;
    private static int duplicates_range;

    private static JDialog mainDialog;
    private static final JSpinner spinnerSeed =         new JSpinner(new SpinnerNumberModel(2026L, 1L, Long.MAX_VALUE, 1L));
    private static final JSpinner spinnerIterations =   new JSpinner(new SpinnerNumberModel((byte) 5, (byte) 1, (byte) 10, (byte) 1));
    private static final JSpinner spinnerPartial =      new JSpinner(new SpinnerNumberModel((short) 10, (short) 1, (short) 200, (short) 1));
    private static final JSpinner spinnerDuplicates =   new JSpinner(new SpinnerNumberModel(10, 2, 100000, 1));

    private static JDialog progressDialog;
    private static Path targetPath;
    private static volatile boolean isCancelled = false;

    public static void startUp() {
        mainDialog = new JDialog();
        mainDialog.setTitle("Konfigurace měření");
        mainDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        mainDialog.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String title = "<html><body style=\"width: 750px;\">"
                + "<h2 style=\"margin-top: 0px; margin-bottom: 2px;\">Analýza řadicích algoritmů</h2>"
                + "<p>Tento program provádí měření časové složitosti algoritmů v reálném běhovém "
                + "prostředí JVM s využitím programovacího jazyka Java. "
                + "Pro maximální eliminaci zkreslení dat je nejprve prováděno testování "
                + "v rámci zahřívací fáze (tzv. warm-up).</p>"

                + "<h3 style=\"margin-top: 6px; margin-bottom: 2px;\"><u>"
                + "Testování</u></h3>"
                + "<p style=\"margin-bottom: 4px;\">Způsob testování jednotlivých algoritmů "
                + "vychází z dostupných nástrojů z implementované závislosti na veřejném repozitáři "
                + "<u>https://github.com/kevin-wayne/algs4</u> přidruženého "
                + "ke knize <i>Algorithms, 4th Edition</i> (https://algs4.cs.princeton.edu) "
                + "od autorů R. Sedgewick a K. Wayne (ISBN-10: 0-321-57351-X, ISBN-13: 978-0-321-57351-3).</p>"
                + "<p>Program měří čas ve vteřinách napříč třemi fixními velikostmi standardních "
                + "polí (25 000, 50 000, 100 000) a pěti separátními datovými scénáři. "
                + "Všechny algoritmy operují nad objekty třídy Integer (využívající rozhraní "
                + "Comparable) ve snaze simulovat režii moderních objektových architektur.</p>"
                + "<ul style=\"margin-top: 4px; margin-bottom: 0px;\">"
                + "  <li>\"random\": Náhodně generované pole s daty v rozsahu 0–100 000.</li>"
                + "  <li>\"partial\": Seřazené \"random\" pole s procentuálním šumem.</li>"
                + "  <li>\"duplicates\": Náhodně generované pole s daty v omezeném rozsahu.</li>"
                + "  <li>\"sorted\": Seřazené pole \"random\".</li>"
                + "  <li>\"reversed\": Pozpátku seřazené pole \"random\".</li>"
                + "</ul>"

                + "<h3 style=\"margin-top: 6px; margin-bottom: 2px;\"><u>"
                + "Testované algoritmy</u></h3>"
                + "<p>Většina implementací pochází ze zmíněného repozitáře. "
                + "Vlastní implementací byl pro kompletnost doplněn algoritmus Bubble sort, "
                + "přičemž předmětem testování jsou následující algoritmy.</p>"
                + "<ul style=\"margin-top: 4px; margin-bottom: 0px;\">"
                + "  <li>Elementární: Bubble sort, Selection sort, Insertion sort.</li>"
                + "  <li>Pokročilé: Quicksort, Heapsort, Mergesort.</li>"
                + "  <li>Pro kompletnost probíhá testování i na nativní metodě Arrays.sort() "
                + "(algoritmus TimSort).</li>"
                + "</ul>"

                + "<h3 style=\"margin-top: 6px; margin-bottom: 2px;\"><u>"
                + "Vstupní parametry</u></h3>"
                + "<p>Veškeré vstupní parametry využívají pouze kladná celá čísla a ve většině případů "
                + "jsou navíc uměle omezeny (omezení jsou uvedena u každého parametru v závorkách).</p>"
                + "<ul style=\"margin-top: 4px; margin-bottom: 0px;\">"
                + "  <li>Seed: Zaručuje reprodukovatelnost testovacích dat (veškerá kladná čísla "
                + "datového typu long větší než nula).</li>"
                + "  <li>Počet opakování: Počet iterací jednotlivých měření (pro garanci "
                + "realizovatelnosti v rozumném čase v rozsahu 1–10).</li>"
                + "  <li>Procento promíchání: Generuje náhodný šum (v podobě záměny prvků na "
                + "náhodně generovaných indexech rovněž vycházejících z hodnoty seed) pro simulaci "
                + "reálných dat (vstupem je počet těchto záměn v podobě procent z velikosti "
                + "testovaného pole omezený na 1–200; při vyšších procentech ekvivalentem náhodných dat).</li>"
                + "  <li>Množství duplicitních hodnot: Omezuje rozsah unikátních hodnot v poli "
                + "v podobě 0-[hodnota-1] (2–100 000; při maximální hodnotě rovněž ekvivalentem "
                + "náhodných dat).</li>"
                + "</ul>"

                + "<h3 style=\"margin-top: 6px; margin-bottom: 2px;\"><u>"
                + "Výstup programu</u></h3>"
                + "<p>U všech výsledků je prováděna validace správnosti řazení a jednotlivé naměřené "
                + "vzorky společně s průměrem konkrétního měření se ukládají do zvoleného adresáře. "
                + "Ukládány jsou přehledně ve strukturované podobě formátu JSON do souboru "
                + "se stejnojmennou příponou a jsou tak připraveny pro další zpracování "
                + "(např. pro vizualizaci v jazyce R).</p>"

                + "<p style=\"margin-top: 6px;\">Nástroj pro generování dat byl zhotoven "
                + "v rámci bakalářské práce<br>"
                + "Daniel Berný, 2026</p>"
                + "</body></html>";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JLabel labelSeed = new JLabel("Seed:");
        spinnerSeed.setEditor(new JSpinner.NumberEditor(spinnerSeed, "#"));
        spinnerSeed.setPreferredSize(new Dimension(195, 25));

        JLabel labelIterations = new JLabel("Počet opakování:");
        spinnerIterations.setEditor(new JSpinner.NumberEditor(spinnerIterations, "#"));
        spinnerIterations.setPreferredSize(new Dimension(45, 25));

        inputPanel1.add(labelSeed);
        inputPanel1.add(spinnerSeed);
        inputPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        inputPanel1.add(labelIterations);
        inputPanel1.add(spinnerIterations);

        JPanel inputPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JLabel labelPartial = new JLabel("Procento promíchání:");
        spinnerPartial.setEditor(new JSpinner.NumberEditor(spinnerPartial, "#"));
        spinnerPartial.setPreferredSize(new Dimension(50, 25));

        JLabel labelDuplicates = new JLabel("Množství duplicitních hodnot:");
        spinnerDuplicates.setPreferredSize(new Dimension(75, 25));

        inputPanel2.add(labelPartial);
        inputPanel2.add(spinnerPartial);
        inputPanel2.add(new JLabel("%"));
        inputPanel2.add(Box.createRigidArea(new Dimension(10, 0)));
        inputPanel2.add(labelDuplicates);
        inputPanel2.add(spinnerDuplicates);

        JButton btnApprove = new JButton("Potvrdit");
        btnApprove.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnApprove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    spinnerSeed.commitEdit();
                    spinnerIterations.commitEdit();
                    spinnerPartial.commitEdit();
                    spinnerDuplicates.commitEdit();
                } catch (java.text.ParseException ex) {
                    JOptionPane.showMessageDialog(
                            mainDialog,
                            "Neplatný vstup!",
                            "Chyba formátu",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                seed = ((Number) spinnerSeed.getValue()).longValue();
                iterations = ((Number) spinnerIterations.getValue()).byteValue();
                partial_swap_percentage = ((Number) spinnerPartial.getValue()).shortValue();
                duplicates_range = ((Number) spinnerDuplicates.getValue()).intValue();

                int volba = JOptionPane.showConfirmDialog(
                        mainDialog,
                        "Seed: " + seed + "\nPočet opakování: " + iterations +
                                "\nProcento promíchání: " + partial_swap_percentage + "\nMnožství duplikátů: " + duplicates_range + "\n\nPokračovat?",
                        "Potvrzení",
                        JOptionPane.YES_NO_OPTION
                );

                if (volba == JOptionPane.YES_OPTION) {
                    mainDialog.dispose();
                    SwingUtilities.invokeLater(Window::startTest);
                }
            }
        });

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(inputPanel1);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(inputPanel2);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(btnApprove);

        mainDialog.add(mainPanel);
        mainDialog.pack();
        mainDialog.setLocationRelativeTo(null);
        mainDialog.setVisible(true);
    }

    public static void startTest() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("*.json", "json"));
        fileChooser.setDialogTitle("Uložení");
        fileChooser.setSelectedFile(new File("vysledky.json"));
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            targetPath = fileChooser.getSelectedFile().toPath();

            if (!targetPath.toString().toLowerCase().endsWith(".json")) {
                targetPath = Path.of(targetPath + ".json");
            }
        } else {
            return;
        }

        isCancelled = false;

        progressDialog = new JDialog();
        progressDialog.setTitle("Průběh měření");
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel statusLabel = new JLabel("Příprava...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 25));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCancel = new JButton("Přerušit");
        btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnCancel.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    progressDialog,
                    "Opravdu přerušit probíhající měření?\nData nebudou uložena.",
                    "Potvrzení",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == 0) {
                isCancelled = true;
                btnCancel.setEnabled(false);
                statusLabel.setText("Ukončování...");
            }
        });

        progressDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                btnCancel.doClick();
            }
        });

        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(btnCancel);

        progressDialog.add(mainPanel);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setVisible(true);

        new Thread(() -> {
            try {
                test(statusLabel, progressBar);

                if (!isCancelled) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(
                                null,
                                "Měření dokončeno.\n\nVýsledky byly uloženy do: " + targetPath.toAbsolutePath(),
                                "Úspěšné měření",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(
                                null,
                                "Měření bylo přerušeno.",
                                "Neplatné měření",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(
                            null,
                            "Chyba: " + ex.getMessage(),
                            "Neočekávaná chyba",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    private static void test(JLabel statusLabel, JProgressBar progressBar) {
        Arrays.sort(ARRAY_SIZES);
        int biggestSize = ARRAY_SIZES[ARRAY_SIZES.length - 1];
        Integer[] warmup = new Integer[biggestSize];
        int warmupIterations = WARMUP_ITERATIONS;

        updateUI(statusLabel, progressBar, "Aplikování algoritmů na zahřívací pole...", 0);

        while (warmupIterations-- > 0) {
            if (isCancelled) {
                return;
            }

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

        updateUI(statusLabel, progressBar, "Generování polí pro měření...", 5);
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"metadata\": {\n");
        json.append("    \"array_sizes\": ").append(Arrays.toString(ARRAY_SIZES)).append(",\n");
        json.append("    \"seed\": ").append(seed).append(",\n");
        json.append("    \"iterations\": ").append(iterations).append(",\n");
        json.append("    \"duplicates_range\": ").append(duplicates_range).append(",\n");
        json.append("    \"partial_swap_percentage\": ").append(partial_swap_percentage).append("\n  },\n");
        json.append("  \"results\": {\n");

        StdRandom.setSeed(seed);
        Integer[] masterRandom = new Integer[biggestSize];
        Integer[] masterDuplicates = new Integer[biggestSize];
        for (int i = 0; i < biggestSize; i++) {
            masterRandom[i] = StdRandom.uniformInt(biggestSize);
            masterDuplicates[i] = StdRandom.uniformInt(duplicates_range);
        }

        int totalSteps = ARRAY_SIZES.length * ALGORITHMS.length;
        int currentStep = 0;

        for (int i = 0; i < ARRAY_SIZES.length; i++) {
            if (isCancelled) {
                return;
            }
            int n = ARRAY_SIZES[i];

            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            Integer[] random = new Integer[n];
            System.arraycopy(masterRandom, 0, random, 0, n);

            Integer[] sorted = random.clone();
            Arrays.sort(sorted);

            Integer[] reversed = sorted.clone();
            Collections.reverse(Arrays.asList(reversed));

            Integer[] partial = sorted.clone();
            int swaps = (n * partial_swap_percentage) / 100;
            while (swaps-- > 0) {
                exch(partial, StdRandom.uniformInt(n), StdRandom.uniformInt(n));
            }

            Integer[] duplicates = new Integer[n];
            System.arraycopy(masterDuplicates, 0, duplicates, 0, n);

            json.append("    \"").append(n).append("\": {\n");

            for (int j = 0; j < ALGORITHMS.length; j++) {
                if (isCancelled) {
                    return;
                }
                String algorithm = ALGORITHMS[j];

                int percent = 5 + (int) (((double) currentStep / totalSteps) * 90);
                String fn = String.format(new Locale("cs", "CZ"), "%,d", n);
                updateUI(statusLabel, progressBar, "Měření algoritmu " + algorithm + " na polích o velikosti (n=" + fn + ")", percent);

                double[] timeRandom = new double[iterations];
                double[] timePartial = new double[iterations];
                double[] timeDuplicates = new double[iterations];
                double[] timeSorted = new double[iterations];
                double[] timeReversed = new double[iterations];

                for (int k = 0; k < iterations; k++) {
                    if (isCancelled) {
                        return;
                    }
                    timeRandom[k] = measureTime(algorithm, random.clone());
                    timePartial[k] = measureTime(algorithm, partial.clone());
                    timeDuplicates[k] = measureTime(algorithm, duplicates.clone());
                    timeSorted[k] = measureTime(algorithm, sorted.clone());
                    timeReversed[k] = measureTime(algorithm, reversed.clone());
                }

                addAlgorithmToJSON(json, algorithm, timeRandom, timePartial, timeDuplicates, timeSorted, timeReversed, j == ALGORITHMS.length - 1);
                currentStep++;
            }
            json.append("    }");
            if (i < ARRAY_SIZES.length - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  }\n}");

        if (!isCancelled) {
            updateUI(statusLabel, progressBar, "Ukládání výsledků...", 100);
            saveToFile(json.toString());
        }
    }

    private static void updateUI(JLabel label, JProgressBar bar, String text, int percentage) {
        SwingUtilities.invokeLater(() -> {
            label.setText(text);
            bar.setValue(percentage);
        });
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
                throw new IllegalArgumentException("Neznámý algoritmus: " + algorithm);
        }

        double time = timer.elapsedTime();
        if (!isSorted(data)) {
            isCancelled = true;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    progressDialog,
                    "Algoritmus " + algorithm + " neseřadil posloupnost správně.",
                    "Neočekávaná chyba",
                    JOptionPane.ERROR_MESSAGE
            ));
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

        json.append("        \"").append(scenario).append("\": {\n");
        json.append("          \"average\": ").append(BigDecimal.valueOf(average).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()).append(",\n");
        json.append("          \"iterations\": ").append(Arrays.toString(iterations)).append("\n");
        json.append("        }");
        if (!isLast) {
            json.append(",");
        }
        json.append("\n");
    }

    private static void saveToFile(String contents) {
        try {
            Files.writeString(targetPath, contents);
        } catch (IOException ex) {
            isCancelled = true;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    progressDialog,
                    "Chyba při zápisu do souboru: " + ex.getMessage(),
                    "Neočekávaná chyba",
                    JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    private static void exch(Object[] a, int i, int j) {
        Object swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    private static boolean isSorted(Comparable[] array) {
        for (int i = 1; i < array.length; i++)
            if (array[i].compareTo(array[i - 1]) < 0) {
                return false;
            }
        return true;
    }

    static void main(String[] args) {
        SwingUtilities.invokeLater(Window::startUp);
    }
}