package com.example;

import edu.princeton.cs.algs4.*;

import java.util.Arrays;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater(InitialWindow::new);

        int n = 100000;
        Integer[] pole = new Integer[n];
        for (int i = 0; i < n; i++) {
            pole[i] = StdRandom.uniformInt(100000);
        }
        
        Integer[] poleBubble = pole.clone();
        Integer[] poleSelection = pole.clone();
        Integer[] poleInsertion = pole.clone();
        Integer[] poleQuick = pole.clone();
        Integer[] poleHeap = pole.clone();
        Integer[] poleMerge = pole.clone();

        System.out.println("Začátek...\n");
        
        Stopwatch timerBubble = new Stopwatch();
        Bubble.sort(poleBubble);
        System.out.println("Bubble sort:    " + timerBubble.elapsedTime() + " s");
        
        Stopwatch timerSelection = new Stopwatch();
        Selection.sort(poleSelection);
        System.out.println("Selection sort: " + timerSelection.elapsedTime() + " s");
        
        Stopwatch timerInsertion = new Stopwatch();
        Insertion.sort(poleInsertion);
        System.out.println("Insertion sort: " + timerInsertion.elapsedTime() + " s");
        
        Stopwatch timerQuick = new Stopwatch();
        Quick.sort(poleQuick);
        System.out.println("Quicksort:      " + timerQuick.elapsedTime() + " s");
        
        Stopwatch timerHeap = new Stopwatch();
        Heap.sort(poleHeap);
        System.out.println("Heapsort:       " + timerHeap.elapsedTime() + " s");
        
        Stopwatch timerMerge = new Stopwatch();
        Merge.sort(poleMerge);
        System.out.println("Mergesort:      " + timerMerge.elapsedTime() + " s");

        System.out.println("Konec");
    }
}