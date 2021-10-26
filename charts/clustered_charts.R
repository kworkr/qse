library(ggplot2)
# remotes::install_github("coolbutuseless/ggpattern")
library(ggpattern)
# library(reshape2) # for melt

lubm <- read.csv(file = '/qse/charts/files/lubm/lubm.csv')
dbpedia <- read.csv(file = '/qse/charts/files/dbpedia/dbpedia.csv')
yago <- read.csv(file = '/qse/charts/files/yago/yago.csv')

lubm$Support <- paste("Support" ,lubm$Support )
yago$Support <- paste("Support" ,yago$Support )
dbpedia$Support <- paste("Support" ,dbpedia$Support )



### DBpedia CHARTS

dbpedia_NS_chart <-  ggplot(dbpedia, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_NS, stat = "identity", pattern=Confidence) , width = 0.4)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=4) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("NS Count") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/dbpedia_NS.png",res=180, width=1000, height=500, pointsize=10)
  dbpedia_NS_chart
dev.off()


dbpedia_NSP_chart <-  ggplot(dbpedia, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_NSP, stat = "identity", pattern=Confidence) , width = 0.4)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=4) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("PS Count") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/dbpedia_NSP.png",res=180, width=1000, height=500, pointsize=10)
  dbpedia_NSP_chart
dev.off()


### YAGO CHARTS


yago_NS_chart <-  ggplot(yago, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_NS, stat = "identity", pattern=Confidence) , width = 0.4)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=4) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("NS Count") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/yago_NS.png",res=180, width=1000, height=500, pointsize=10)
  yago_NS_chart
dev.off()


yago_NSP_chart <-  ggplot(yago, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_NSP, stat = "identity", pattern=Confidence) , width = 0.4)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=4) +
  facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("PS Count") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="top", text = element_text(size = 13), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/yago_NSP.png",res=180, width=1000, height=500, pointsize=10)
  yago_NSP_chart
dev.off()
