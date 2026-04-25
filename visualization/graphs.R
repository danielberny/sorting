# ==============================================================================
# 0. NASTAVENÍ PRACOVNÍHO ADRESÁŘE
# ==============================================================================
# Automatické nastavení pracovní složky podle cesty ke skriptu (vyžaduje RStudio)
setwd(dirname(rstudioapi::getActiveDocumentContext()$path))

library(jsonlite)
library(ggplot2)
library(dplyr)

windowsFonts(Cambria = windowsFont("Cambria"))

# ==============================================================================
# 1. NAČTENÍ DAT A PARSOVÁNÍ STRUKTURY
# ==============================================================================
json_data <- fromJSON("vysledky5.json")
results <- json_data$results

df_list <- list()

for (size in names(results)) {
  for (algo in names(results[[size]])) {
    for (scenario in names(results[[size]][[algo]])) {
      avg_time <- results[[size]][[algo]][[scenario]]$average
      
      df_list[[length(df_list) + 1]] <- data.frame(
        Size = as.numeric(size),
        Algorithm = algo,
        Scenario = scenario,
        AverageTime = avg_time
      )
    }
  }
}

df <- do.call(rbind, df_list)
df <- df %>% filter(Algorithm != "Arrays.sort()")
df$Algorithm <- factor(df$Algorithm, levels = c("Bubble", "Selection", "Insertion", "Quick", "Heap", "Merge"))


# ==============================================================================
# 2. PŘÍPRAVA DAT A POMOCNÉ TABULKY PRO SROVNÁVACÍ GRAFY
# ==============================================================================
df_random <- df %>% 
  filter(Scenario == "random") %>%
  mutate(Group = ifelse(Algorithm %in% c("Bubble", "Selection", "Insertion"), 
                        "Základní", 
                        "Pokročilé")) %>%
  mutate(Group = factor(Group, levels = c("Základní", "Pokročilé")))

zero_points <- df_random %>%
  distinct(Algorithm, Group) %>%
  mutate(Size = 0, AverageTime = 0, Scenario = "random")

df_random_with_zeros <- bind_rows(zero_points, df_random)

facet_limits_p1 <- data.frame(
  Group = factor(c("Základní", "Pokročilé"), levels = c("Základní", "Pokročilé")),
  AverageTime = c(30, 0.03),
  Algorithm = c("Bubble", "Quick"), 
  Size = c(25000, 25000)            
)

facet_limits_p2 <- data.frame(
  Group = factor(c("Základní", "Pokročilé"), levels = c("Základní", "Pokročilé")),
  AverageTime = c(30, 0.03),
  Size = c(0, 0),
  Algorithm = c("Bubble", "Quick")
)


# ==============================================================================
# 3. SROVNÁVACÍ GRAFY (Všechny algoritmy)
# ==============================================================================

# --- Kategorické sloupcové grafy (g1) ---
g1 <- ggplot(df_random, aes(x = Algorithm, y = AverageTime, fill = factor(Size))) +
  geom_blank(data = facet_limits_p1, aes(y = AverageTime)) +
  geom_col(position = "dodge", color = "black", alpha = 0.85) +
  facet_wrap(~ Group, scales = "free") +
  scale_x_discrete(
    labels = c(
      "Bubble" = "bubble sort",
      "Selection" = "selection sort",
      "Insertion" = "insertion sort",
      "Quick" = "quicksort",
      "Heap" = "heapsort",
      "Merge" = "mergesort"
    )
  ) +
  scale_fill_discrete(
    labels = function(x) format(as.numeric(x), big.mark = " ", scientific = FALSE)
  ) +
  labs(
    title = "Srovnání časové složitosti podle velikosti vstupu",
    subtitle = "Náhodná posloupnost",
    x = "Algoritmus",
    y = "Průměrný čas v sekundách",
    fill = "Velikost pole (n):"
  ) +
  theme_bw() +
  theme(
    text = element_text(family = "Cambria", size = 12),
    plot.title = element_text(face = "bold", size = 14),
    legend.position = "bottom",
    panel.spacing = unit(2, "lines"), 
    strip.text = element_text(face = "bold", size = 11) 
  )

print(g1)

# --- Kategorické grafy (g2) ---
g2 <- ggplot(df_random_with_zeros, aes(x = Size, y = AverageTime, color = Algorithm, group = Algorithm)) +
  geom_blank(data = facet_limits_p2, aes(y = AverageTime)) +
  geom_line(linewidth = 1.2) +
  geom_point(size = 3) +
  facet_wrap(~ Group, scales = "free_y") +
  scale_x_continuous(
    breaks = c(0, 25000, 50000, 75000, 100000),
    labels = c("0", "25 000", "50 000", "75 000", "100 000")
  ) +
  scale_color_discrete(
    labels = c(
      "Bubble" = "bubble sort",
      "Selection" = "selection sort",
      "Insertion" = "insertion sort",
      "Quick" = "quicksort",
      "Heap" = "heapsort",
      "Merge" = "mergesort"
    )
  ) +
  labs(
    title = "Srovnání časové složitosti podle velikosti vstupu",
    subtitle = "Náhodná posloupnost",
    x = "Velikost pole (n)",
    y = "Průměrný čas v sekundách",
    color = "Algoritmus:"
  ) +
  theme_bw() +
  theme(
    text = element_text(family = "Cambria", size = 12),
    plot.title = element_text(face = "bold", size = 14),
    legend.position = "bottom",
    aspect.ratio = 1,          
    panel.spacing = unit(2, "lines"), 
    strip.text = element_text(face = "bold", size = 11) 
  ) +
  guides(
    color = guide_legend(
      override.aes = list(linewidth = 1.2), 
      nrow = 2, 
      byrow = TRUE
    )
  )

print(g2)


# ==============================================================================
# 4. JEDNOTLIVÉ ALGORITMY - ZÁKLADNÍ (Limity osy Y: 0 - 30)
# ==============================================================================

# --- Bubble sort ---
df_bubble <- df %>% filter(Algorithm == "Bubble")
df_bubble$FacetTitle <- "Bubble sort"
df_bubble$Scenario <- factor(df_bubble$Scenario, 
                             levels = c("random", "partial", "duplicates", "sorted", "reversed"),
                             labels = c("Náhodná posloupnost", "Částečně seřazená posloupnost", "Posloupnost s duplikáty", "Seřazená posloupnost", "Pozpátku seřazená posloupnost"))

zero_bubble <- df_bubble %>% distinct(Scenario) %>% mutate(Size = 0, AverageTime = 0, Algorithm = "Bubble", FacetTitle = "Bubble sort")
df_bubble_plot <- bind_rows(zero_bubble, df_bubble)

plot_bubble <- ggplot(df_bubble_plot, aes(x = Size, y = AverageTime, color = Scenario, group = Scenario)) +
  geom_line(linewidth = 1.2) + geom_point(size = 3) + facet_wrap(~ FacetTitle) +
  scale_x_continuous(limits = c(0, 100000), breaks = c(0, 25000, 50000, 75000, 100000), labels = c("0", "25 000", "50 000", "75 000", "100 000")) +
  scale_y_continuous(limits = c(0, 30)) +
  labs(x = "Velikost pole (n)", y = "Průměrný čas v sekundách", color = "Rozložení vstupních dat:") +
  scale_color_brewer(palette = "Set1") + theme_bw() +
  theme(text = element_text(family = "Cambria", size = 12), aspect.ratio = 1, legend.position = "bottom", panel.grid.minor = element_blank(), legend.title = element_text(face = "bold"), strip.background = element_rect(fill = "gray90", color = "black"), strip.text = element_text(face = "bold", size = 12, family = "Cambria")) +
  guides(color = guide_legend(override.aes = list(linewidth = 1.2), nrow = 3, byrow = TRUE))

print(plot_bubble)

# --- Selection sort ---
df_selection <- df %>% filter(Algorithm == "Selection")
df_selection$FacetTitle <- "Selection sort"
df_selection$Scenario <- factor(df_selection$Scenario, 
                                levels = c("random", "partial", "duplicates", "sorted", "reversed"),
                                labels = c("Náhodná posloupnost", "Částečně seřazená posloupnost", "Posloupnost s duplikáty", "Seřazená posloupnost", "Pozpátku seřazená posloupnost"))

zero_selection <- df_selection %>% distinct(Scenario) %>% mutate(Size = 0, AverageTime = 0, Algorithm = "Selection", FacetTitle = "Selection sort")
df_selection_plot <- bind_rows(zero_selection, df_selection)

plot_selection <- ggplot(df_selection_plot, aes(x = Size, y = AverageTime, color = Scenario, group = Scenario)) +
  geom_line(linewidth = 1.2) + geom_point(size = 3) + facet_wrap(~ FacetTitle) +
  scale_x_continuous(limits = c(0, 100000), breaks = c(0, 25000, 50000, 75000, 100000), labels = c("0", "25 000", "50 000", "75 000", "100 000")) +
  scale_y_continuous(limits = c(0, 30)) +
  labs(x = "Velikost pole (n)", y = "Průměrný čas v sekundách", color = "Rozložení vstupních dat:") +
  scale_color_brewer(palette = "Set1") + theme_bw() +
  theme(text = element_text(family = "Cambria", size = 12), aspect.ratio = 1, legend.position = "bottom", panel.grid.minor = element_blank(), legend.title = element_text(face = "bold"), strip.background = element_rect(fill = "gray90", color = "black"), strip.text = element_text(face = "bold", size = 12, family = "Cambria")) +
  guides(color = guide_legend(override.aes = list(linewidth = 1.2), nrow = 3, byrow = TRUE))

print(plot_selection)

# --- Insertion sort ---
df_insertion <- df %>% filter(Algorithm == "Insertion")
df_insertion$FacetTitle <- "Insertion sort"
df_insertion$Scenario <- factor(df_insertion$Scenario, 
                                levels = c("random", "partial", "duplicates", "sorted", "reversed"),
                                labels = c("Náhodná posloupnost", "Částečně seřazená posloupnost", "Posloupnost s duplikáty", "Seřazená posloupnost", "Pozpátku seřazená posloupnost"))

zero_insertion <- df_insertion %>% distinct(Scenario) %>% mutate(Size = 0, AverageTime = 0, Algorithm = "Insertion", FacetTitle = "Insertion sort")
df_insertion_plot <- bind_rows(zero_insertion, df_insertion)

plot_insertion <- ggplot(df_insertion_plot, aes(x = Size, y = AverageTime, color = Scenario, group = Scenario)) +
  geom_line(linewidth = 1.2) + geom_point(size = 3) + facet_wrap(~ FacetTitle) +
  scale_x_continuous(limits = c(0, 100000), breaks = c(0, 25000, 50000, 75000, 100000), labels = c("0", "25 000", "50 000", "75 000", "100 000")) +
  scale_y_continuous(limits = c(0, 30)) +
  labs(x = "Velikost pole (n)", y = "Průměrný čas v sekundách", color = "Rozložení vstupních dat:") +
  scale_color_brewer(palette = "Set1") + theme_bw() +
  theme(text = element_text(family = "Cambria", size = 12), aspect.ratio = 1, legend.position = "bottom", panel.grid.minor = element_blank(), legend.title = element_text(face = "bold"), strip.background = element_rect(fill = "gray90", color = "black"), strip.text = element_text(face = "bold", size = 12, family = "Cambria")) +
  guides(color = guide_legend(override.aes = list(linewidth = 1.2), nrow = 3, byrow = TRUE))

print(plot_insertion)


# ==============================================================================
# 5. JEDNOTLIVÉ ALGORITMY - POKROČILÉ (Limity osy Y: 0 - 0.03)
# ==============================================================================

# --- Quicksort ---
df_quick <- df %>% filter(Algorithm == "Quick")
df_quick$FacetTitle <- "Quicksort"
df_quick$Scenario <- factor(df_quick$Scenario, 
                            levels = c("random", "partial", "duplicates", "sorted", "reversed"),
                            labels = c("Náhodná posloupnost", "Částečně seřazená posloupnost", "Posloupnost s duplikáty", "Seřazená posloupnost", "Pozpátku seřazená posloupnost"))

zero_quick <- df_quick %>% distinct(Scenario) %>% mutate(Size = 0, AverageTime = 0, Algorithm = "Quick", FacetTitle = "Quicksort")
df_quick_plot <- bind_rows(zero_quick, df_quick)

plot_quick <- ggplot(df_quick_plot, aes(x = Size, y = AverageTime, color = Scenario, group = Scenario)) +
  geom_line(linewidth = 1.2) + geom_point(size = 3) + facet_wrap(~ FacetTitle) +
  scale_x_continuous(limits = c(0, 100000), breaks = c(0, 25000, 50000, 75000, 100000), labels = c("0", "25 000", "50 000", "75 000", "100 000")) +
  scale_y_continuous(limits = c(0, 0.03)) +
  labs(x = "Velikost pole (n)", y = "Průměrný čas v sekundách", color = "Rozložení vstupních dat:") +
  scale_color_brewer(palette = "Set1") + theme_bw() +
  theme(text = element_text(family = "Cambria", size = 12), aspect.ratio = 1, legend.position = "bottom", panel.grid.minor = element_blank(), legend.title = element_text(face = "bold"), strip.background = element_rect(fill = "gray90", color = "black"), strip.text = element_text(face = "bold", size = 12, family = "Cambria")) +
  guides(color = guide_legend(override.aes = list(linewidth = 1.2), nrow = 3, byrow = TRUE))

print(plot_quick)

# --- Heapsort ---
df_heap <- df %>% filter(Algorithm == "Heap")
df_heap$FacetTitle <- "Heapsort"
df_heap$Scenario <- factor(df_heap$Scenario, 
                           levels = c("random", "partial", "duplicates", "sorted", "reversed"),
                           labels = c("Náhodná posloupnost", "Částečně seřazená posloupnost", "Posloupnost s duplikáty", "Seřazená posloupnost", "Pozpátku seřazená posloupnost"))

zero_heap <- df_heap %>% distinct(Scenario) %>% mutate(Size = 0, AverageTime = 0, Algorithm = "Heap", FacetTitle = "Heapsort")
df_heap_plot <- bind_rows(zero_heap, df_heap)

plot_heap <- ggplot(df_heap_plot, aes(x = Size, y = AverageTime, color = Scenario, group = Scenario)) +
  geom_line(linewidth = 1.2) + geom_point(size = 3) + facet_wrap(~ FacetTitle) +
  scale_x_continuous(limits = c(0, 100000), breaks = c(0, 25000, 50000, 75000, 100000), labels = c("0", "25 000", "50 000", "75 000", "100 000")) +
  scale_y_continuous(limits = c(0, 0.03)) +
  labs(x = "Velikost pole (n)", y = "Průměrný čas v sekundách", color = "Rozložení vstupních dat:") +
  scale_color_brewer(palette = "Set1") + theme_bw() +
  theme(text = element_text(family = "Cambria", size = 12), aspect.ratio = 1, legend.position = "bottom", panel.grid.minor = element_blank(), legend.title = element_text(face = "bold"), strip.background = element_rect(fill = "gray90", color = "black"), strip.text = element_text(face = "bold", size = 12, family = "Cambria")) +
  guides(color = guide_legend(override.aes = list(linewidth = 1.2), nrow = 3, byrow = TRUE))

print(plot_heap)

# --- Mergesort ---
df_merge <- df %>% filter(Algorithm == "Merge")
df_merge$FacetTitle <- "Mergesort"
df_merge$Scenario <- factor(df_merge$Scenario, 
                            levels = c("random", "partial", "duplicates", "sorted", "reversed"),
                            labels = c("Náhodná posloupnost", "Částečně seřazená posloupnost", "Posloupnost s duplikáty", "Seřazená posloupnost", "Pozpátku seřazená posloupnost"))

zero_merge <- df_merge %>% distinct(Scenario) %>% mutate(Size = 0, AverageTime = 0, Algorithm = "Merge", FacetTitle = "Mergesort")
df_merge_plot <- bind_rows(zero_merge, df_merge)

plot_merge <- ggplot(df_merge_plot, aes(x = Size, y = AverageTime, color = Scenario, group = Scenario)) +
  geom_line(linewidth = 1.2) + geom_point(size = 3) + facet_wrap(~ FacetTitle) +
  scale_x_continuous(limits = c(0, 100000), breaks = c(0, 25000, 50000, 75000, 100000), labels = c("0", "25 000", "50 000", "75 000", "100 000")) +
  scale_y_continuous(limits = c(0, 0.03)) +
  labs(x = "Velikost pole (n)", y = "Průměrný čas v sekundách", color = "Rozložení vstupních dat:") +
  scale_color_brewer(palette = "Set1") + theme_bw() +
  theme(text = element_text(family = "Cambria", size = 12), aspect.ratio = 1, legend.position = "bottom", panel.grid.minor = element_blank(), legend.title = element_text(face = "bold"), strip.background = element_rect(fill = "gray90", color = "black"), strip.text = element_text(face = "bold", size = 12, family = "Cambria")) +
  guides(color = guide_legend(override.aes = list(linewidth = 1.2), nrow = 3, byrow = TRUE))

print(plot_merge)