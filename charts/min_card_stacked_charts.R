library(ggplot2)
# remotes::install_github("coolbutuseless/ggpattern")
library(ggpattern)
# library(res=175, width=600, height=500, pointsize=10)

lubm <- read.csv(file = '/qse/charts/files/lubm/lubm_min_card.csv')
dbpedia <- read.csv(file = '/qse/charts/files/dbpedia/dbpedia_min_card.csv')
yago <- read.csv(file = '/qse/charts/files/yago/yago_min_card.csv')

lubm$Support <- paste("LUBM: Support" ,lubm$Support )
yago$Support <- paste("YAGO: Support" ,yago$Support )
dbpedia$Support <- paste("DBpedia: Support" ,dbpedia$Support )

print(lubm)

### DBpedia CHARTS

dbpedia_MCC_chart <-  ggplot(dbpedia, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_MCC,  pattern=Confidence) ,width = 0.5)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=1) +
  facet_grid(~factor(Support, levels=c('DBpedia: Support  ≥ 1'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("sh:minCount Constraints") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="right", text = element_text(size = 14), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/dbpedia_MCC.png",res=175, width=650, height=500, pointsize=10)
  dbpedia_MCC_chart
dev.off()


### LUBM CHARTS

lubm_MCC_chart <-  ggplot(lubm, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_MCC,  pattern=Confidence) ,width = 0.5)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=1) +
  facet_grid(~factor(Support, levels=c('LUBM: Support  ≥ 1'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("sh:minCount Constraints") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="right", text = element_text(size = 12), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/lubm_MCC.png",res=175, width=600, height=500, pointsize=10)
  lubm_MCC_chart
dev.off()



### yago CHARTS

yago_MCC_chart <-  ggplot(yago, aes(Confidence, fill = Confidence)) +
  geom_bar_pattern(aes(weight = COUNT_MCC,  pattern=Confidence) ,width = 0.5)+  theme(aspect.ratio = 0.2) +
  facet_wrap( ~ Support, ncol=1) +
  facet_grid(~factor(Support, levels=c('YAGO: Support  ≥ 1'))) +
  # theme_minimal(base_family = "Times")+
  # scale_y_continuous(labels=scales::percent) +
  ylab("sh:minCount Constraints") +
  xlab("Confidence Percentage") +   theme_light() + theme(legend.position="right", text = element_text(size = 12), strip.background =element_rect(fill="#f0f0f0"), strip.text = element_text(colour = 'black'))
  # labs(fill = "")


png(file="/qse/charts/charts/yago_MCC.png",res=175, width=600, height=500, pointsize=10)
  yago_MCC_chart
dev.off()


