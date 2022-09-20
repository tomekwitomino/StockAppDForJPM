package com.tomk.android.stockapp.WebAccess;

import java.util.LinkedHashMap;

class CompanyOverviewMapObject {

   public  LinkedHashMap getCompanyOverviewMap(CompanyOverview companyOverview) {

      LinkedHashMap companyOverviewMap = new LinkedHashMap<String, String>();

      companyOverviewMap.put("symbol", companyOverview.getSymbol());
      companyOverviewMap.put("assetType", companyOverview.getAssetType());
      companyOverviewMap.put("description", companyOverview.getDescription());
      companyOverviewMap.put("cik", companyOverview.getCik());
      companyOverviewMap.put("exchange", companyOverview.getExchange());
      companyOverviewMap.put("currency", companyOverview.getCurrency());
      companyOverviewMap.put("country", companyOverview.getCountry());
      companyOverviewMap.put("sector", companyOverview.getSector());
      companyOverviewMap.put("industry", companyOverview.getIndustry());
      companyOverviewMap.put("address", companyOverview.getAddress());
      companyOverviewMap.put("fiscalYearEnd", companyOverview.getFiscalYearEnd());
      companyOverviewMap.put("latestQuarter", companyOverview.getLatestQuarter());
      companyOverviewMap.put("marketCapitalization", companyOverview.getMarketCapitalization());
      companyOverviewMap.put("ebitda", companyOverview.getEbitda());
      companyOverviewMap.put("pERatio", companyOverview.getPERatio());
      companyOverviewMap.put("pEGRatio", companyOverview.getPEGRatio());
      companyOverviewMap.put("bookValue", companyOverview.getBookValue());
      companyOverviewMap.put("dividendPerShare", companyOverview.getDividendPerShare());
      companyOverviewMap.put("dividendYield", companyOverview.getDividendYield());
      companyOverviewMap.put("eps", companyOverview.getEps());
      companyOverviewMap.put("revenuePerShareTTM", companyOverview.getRevenuePerShareTTM());
      companyOverviewMap.put("profitMargin", companyOverview.getProfitMargin());
      companyOverviewMap.put("operatingMarginTTM", companyOverview.getOperatingMarginTTM());
      companyOverviewMap.put("returnOnAssetsTTM", companyOverview.getReturnOnAssetsTTM());
      companyOverviewMap.put("returnOnEquityTTM", companyOverview.getReturnOnEquityTTM());
      companyOverviewMap.put("revenueTTM", companyOverview.getRevenueTTM());
      companyOverviewMap.put("grossProfitTTM", companyOverview.getGrossProfitTTM());
      companyOverviewMap.put("dilutedEPSTTM", companyOverview.getDilutedEPSTTM());
      companyOverviewMap.put("quarterlyEarningsGrowthYOY", companyOverview.getQuarterlyEarningsGrowthYOY());
      companyOverviewMap.put("quarterlyRevenueGrowthYOY", companyOverview.getQuarterlyRevenueGrowthYOY());
      companyOverviewMap.put("analystTargetPrice", companyOverview.getAnalystTargetPrice());
      companyOverviewMap.put("trailingPE", companyOverview.getTrailingPE());
      companyOverviewMap.put("forwardPE", companyOverview.getForwardPE());
      companyOverviewMap.put("priceToSalesRatioTTM", companyOverview.getPriceToSalesRatioTTM());
      companyOverviewMap.put("priceToBookRatio", companyOverview.getPriceToBookRatio());
      companyOverviewMap.put("eVToRevenue", companyOverview.getEVToRevenue());
      companyOverviewMap.put("eVToEBITDA", companyOverview.getEVToEBITDA());
      companyOverviewMap.put("beta", companyOverview.getBeta());
      companyOverviewMap.put("_52WeekHigh", companyOverview.get52WeekHigh());
      companyOverviewMap.put("_52WeekLow", companyOverview.get52WeekLow());
      companyOverviewMap.put("_50DayMovingAverage", companyOverview.get50DayMovingAverage());
      companyOverviewMap.put("_200DayMovingAverage", companyOverview.get200DayMovingAverage());
      companyOverviewMap.put("sharesOutstanding", companyOverview.getSharesOutstanding());
      companyOverviewMap.put("sharesFloat", companyOverview.getSharesFloat());
      companyOverviewMap.put("sharesShort", companyOverview.getSharesShort());
      companyOverviewMap.put("sharesShortPriorMonth", companyOverview.getSharesShortPriorMonth());
      companyOverviewMap.put("shortRatio", companyOverview.getShortRatio());
      companyOverviewMap.put("shortPercentOutstanding", companyOverview.getShortPercentOutstanding());
      companyOverviewMap.put("shortPercentFloat", companyOverview.getShortPercentFloat());
      companyOverviewMap.put("percentInsiders", companyOverview.getPercentInsiders());
      companyOverviewMap.put("percentInstitutions", companyOverview.getPercentInstitutions());
      companyOverviewMap.put("forwardAnnualDividendRate", companyOverview.getForwardAnnualDividendRate());
      companyOverviewMap.put("forwardAnnualDividendYield", companyOverview.getForwardAnnualDividendYield());
      companyOverviewMap.put("payoutRatio", companyOverview.getPayoutRatio());
      companyOverviewMap.put("dividendDate", companyOverview.getDividendDate());
      companyOverviewMap.put("exDividendDate", companyOverview.getExDividendDate());
      companyOverviewMap.put("lastSplitFactor", companyOverview.getLastSplitFactor());
      companyOverviewMap.put("lastSplitDate", companyOverview.getLastSplitDate());


      return companyOverviewMap;
   }

}
