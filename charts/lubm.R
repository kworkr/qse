library(ggplot2)
# remotes::install_github("coolbutuseless/ggpattern")
library(ggpattern)
# library(reshape2) # for melt

lubm <- read.csv(file = 'qse/charts/files/lubm/lubm.csv')
lubm$Support <- paste("Support" ,lubm$Support )

### LUBM CHARTS
lubm_NS_chart <-  ggplot(lubm, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_NS, stat = "identity", pattern=Confidence) , width = 0.4)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=1) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1'))) +
  theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("NS Count") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="right", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="qse/charts/charts/lubm_NS.png",res=175, width=600, height=500, pointsize=10)
  lubm_NS_chart
dev.off()


lubm_NSP_chart <-  ggplot(lubm, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_NSP, stat = "identity", pattern=Confidence) , width = 0.4)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=1) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1'))) +
  theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("PS Count") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="right", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="qse/charts/charts/lubm_NSP.png",res=175, width=600, height=500, pointsize=10)
  lubm_NSP_chart
dev.off()


lubm_psc <- read.csv(file = 'qse/charts/files/lubm/lubm_stacked.csv')
lubm_psc$Support <- paste("Support" ,lubm_psc$Support )

### LUBM PSc CHARTS
print(lubm_psc)
lubm_NSP_CONSTRAINS_CHART <-  ggplot(lubm_psc, aes(Confidence, fill = TYPE)) +
  geom_bar_pattern(aes(weight = COUNT, stat = "identity", pattern=TYPE) , width = 0.6)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=1) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1'))) +
  # facet_grid(~factor(Support, levels=c(' ≥ 1',' > 50',' > 100',' > 500', ' > 1000'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("PSc Count") +
  xlab("Confidence Percentage") +  theme_light() + theme(legend.position="right", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")

png(file="qse/charts/charts/lubm_NSP_CONSTRAINTS.png", res=175, width=600, height=500, pointsize=10)
  lubm_NSP_CONSTRAINS_CHART
dev.off()


