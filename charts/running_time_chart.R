library(ggplot2)
# remotes::install_github("coolbutuseless/ggpattern")
library(ggpattern)
# library(reshape2) # for melt

runningTime <- read.csv(file = '/qse/charts/files/running-time-analysis.csv')


runningTimeChart <- ggplot(runningTime, aes(Datasets, Time)) +
  geom_bar_pattern(aes(fill = Approach, pattern = Approach), width = 0.6, position = "dodge", stat = "identity") +
  theme(aspect.ratio = 0.5) +
  # facet_wrap( ~ Support, ncol=4) +
  # facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  theme_minimal(base_family = "Times") +
  ylab("Running Time (minutes)") +
  xlab(" ") +
  theme_classic() +
  theme(legend.position = c(.35, .75), text = element_text(size = 14), strip.text = element_text(colour = 'black'))
# #strip.background =element_rect(fill="blue"),

runningTimeChart
png(file = "/qse/charts/charts/runningTime.png", res = 270, width = 1000, height = 750, pointsize = 15)
runningTimeChart
dev.off()

