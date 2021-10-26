library(ggplot2)
# remotes::install_github("coolbutuseless/ggpattern")
library(ggpattern)
# library(reshape2) # for melt

lubm <- read.csv(file = '/qse/charts/files/lubm/lubm_stacked.csv')
dbpedia <- read.csv(file = '/qse/charts/files/dbpedia/dbpedia_stacked.csv')
yago <- read.csv(file = '/qse/charts/files/yago/yago_stacked.csv')

lubm$Support <- paste("Support" ,lubm$Support )
yago$Support <- paste("Support" ,yago$Support )
dbpedia$Support <- paste("Support" ,dbpedia$Support )


### LUBM CHARTS

# lubm_NSP_CONSTRAINS_CHART <-  ggplot(lubm, aes(Confidence, fill = TYPE)) +
#   geom_bar_pattern(aes(weight = COUNT, stat = "identity", pattern=TYPE) , width = 0.6)+  theme(aspect.ratio = 0.2) +
#     facet_wrap( ~ Support, ncol=4) +
#   facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
#   # facet_grid(~factor(Support, levels=c(' ≥ 1',' > 50',' > 100',' > 500', ' > 1000'))) +
#   # theme_minimal(base_family = "Times")+
#   # scale_y_continuous(labels=scales::percent) +
#   ylab("PSc Count") +
#   xlab("Confidence Percentage") +  theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
#   # labs(fill = "")
#
# png(file="/qse/charts/charts/lubm_NSP_CONSTRAINTS.png",res=180, width=1000, height=500, pointsize=10)
#   lubm_NSP_CONSTRAINS_CHART
# dev.off()


### DBpedia CHARTS

dbpedia_NSP_CONSTRAINS_CHART <-  ggplot(dbpedia, aes(Confidence, fill = TYPE)) +
  geom_bar_pattern(aes(weight = COUNT, stat = "identity", pattern=TYPE) , width = 0.6)+  theme(aspect.ratio = 0.2) +
    facet_wrap( ~ Support, ncol=4) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  # facet_grid(~factor(Support, levels=c(' ≥ 1',' > 50',' > 100',' > 500', ' > 1000'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("PSc Count") +
  xlab("Confidence Percentage") +  theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")

png(file="/qse/charts/charts/dbpedia_NSP_CONSTRAINTS.png",res=180, width=1000, height=500, pointsize=10)
  dbpedia_NSP_CONSTRAINS_CHART
dev.off()


### YAGO CHARTS
yago_NSP_CONSTRAINS_CHART <-  ggplot(yago, aes(Confidence, fill = TYPE)) +
  geom_bar_pattern(aes(weight = COUNT, stat = "identity", pattern=TYPE) , width = 0.6)+  theme(aspect.ratio = 0.2) +
    facet_wrap( ~ Support, ncol=4) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  # facet_grid(~factor(Support, levels=c(' ≥ 1',' > 50',' > 100',' > 500', ' > 1000'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("PSc Count") +
  xlab("Confidence Percentage") +  theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")

png(file="/qse/charts/charts/yago_NSP_CONSTRAINTS.png",res=180, width=1000, height=500, pointsize=10)
  yago_NSP_CONSTRAINS_CHART
dev.off()

