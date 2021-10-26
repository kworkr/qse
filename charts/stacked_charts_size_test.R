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

### DBpedia CHARTS/test



### DBpedia CHARTS

dbpedia_NSP_CONSTRAINS_CHART <-  ggplot(dbpedia, aes(Confidence, fill = TYPE)) +
  geom_bar_pattern(aes(weight = COUNT, stat = "identity", pattern=TYPE, width = 0.1))+  theme(aspect.ratio = 0.75) +
  facet_wrap( ~ Support, ncol=5) +
    facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 50','Support  > 100','Support  > 500', 'Support  > 1000'))) +
  # facet_grid(~factor(Support, levels=c(' ≥ 1',' > 50',' > 100',' > 500', ' > 1000'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("Number of Constraints") +
  xlab("Confidence") +  theme_light() + theme(legend.position="top")
  # labs(fill = "")

png(file="/qse/charts/charts/test/dbpedia_NSP_CONSTRAINTS.png",res=180, width=1700, height=600, pointsize=15)
  dbpedia_NSP_CONSTRAINS_CHART
dev.off()



#
# dbpedia_NSP_chart <-  ggplot(dbpedia, aes(Confidence, fill = Confidence)) +
#   geom_bar_pattern(aes(weight = COUNT_NSP, stat = "identity", pattern=Confidence))+
#   facet_wrap( ~ Support, ncol=5) +
#   facet_grid(~factor(Support, levels=c('Support  ≥ 1','Support  > 50','Support  > 100','Support  > 500', 'Support  > 1000'))) +
#   # theme_minimal(base_family = "Times")+
#   # scale_y_continuous(labels=scales::percent) +
#   ylab("Count of Node Shape Properties") +
#   xlab("Confidence")
#   # labs(fill = "")
#
#
# png(file="/qse/charts/charts/test/dbpedia_NSP.png",res=180, width=2900, height=1200, pointsize=30)
#   dbpedia_NSP_chart
# dev.off()
#
