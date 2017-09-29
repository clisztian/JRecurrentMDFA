require(FinancialInstrument)

getFXDaily<-function(pair1,pair2)
{
  pair<-paste(pair1,"/",pair2,sep="")
  fxpair<-paste(pair1,pair2,sep="")
  
  period1<-getFX(pair,from="2005-01-01",to="2010-01-01", auto.assign = FALSE)
  period2<-getFX(pair,from="2010-01-02",to="2015-01-01", auto.assign = FALSE)
  period3<-getFX(pair,from="2015-01-02",to="2016-11-01", auto.assign = FALSE)
  mypair<-rbind.xts(period1,period2,period3)
  
  return(mypair)
}


getETFDaily<-function()
{
  
  portfolio<-c('GOOG','MSFT','AAPL','YHOO','XOM','SPY','QQQ','IWM',
               'EEM','FIX','VXX','GDX', 'USO', 'XLF', 'EWJ', 'XLE',
               'CMG','BAC','DAL','HAL','AMZN','IBM','INTC','FXE','UUP')
  
  stock<-getSymbols(portfolio[1], src="yahoo", from='2006-01-01', to = '2017-12-01', auto.assign = FALSE)
  
  ports<-Ad(stock)
  
  for(i in 2:length(portfolio))
  {
    stock<-getSymbols(portfolio[i], src="yahoo", from='2006-01-01', to = '2017-12-01', auto.assign = FALSE)
    ports<-cbind.xts(ports, Ad(stock))
  }
  
  return(ports)
}


getETFDailySep<-function()
{
  
  portfolio<-c('GOOG','MSFT','AAPL','YHOO','XOM','SPY','QQQ','IWM',
               'EEM','FIX','VXX','GDX', 'USO', 'XLF', 'EWJ', 'XLE',
               'CMG','BAC','DAL','HAL','AMZN','IBM','INTC','FXE','UUP')
  
  for(i in 1:length(portfolio))
  {
    
    stock<-getSymbols.google(portfolio[i], from='2010-01-01', auto.assign = FALSE)
    ports<-Cl(stock)
    portFile = paste(portfolio[i],".IB.dat",sep='')
    write.zoo(file = portFile, ports, sep=',', col.names = FALSE)
  }
  
}