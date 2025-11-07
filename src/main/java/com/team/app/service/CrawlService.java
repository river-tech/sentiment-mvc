package com.team.app.service;

import com.team.app.model.JobArticle;
import com.team.app.util.Logger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * CrawlService - Fetches articles for sentiment jobs using Google News RSS.
 */
public class CrawlService {

    /**
     * Fetch top 10 articles from Google News RSS for the provided keyword.
     *
     * @param keyword Keyword to search for.
     * @return List of {@link JobArticle} instances with sentiment defaulted to neutral.
     */
    public List<JobArticle> fetchArticles(String keyword) {
        List<JobArticle> articles = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            Logger.warn("Keyword is empty; skipping crawl");
            return articles;
        }

        try {
            String query = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String rssUrl = "https://news.google.com/rss/search?q=" + query + "&hl=vi&gl=VN&ceid=VN:vi";

            Logger.info("Fetching Google News RSS for keyword: " + keyword);
            Document doc = Jsoup.connect(rssUrl).timeout(10000).get();
            Elements items = doc.select("item");

            for (Element item : items) {
                Element titleEl = item.selectFirst("title");
                Element linkEl = item.selectFirst("link");
                Element descEl = item.selectFirst("description");

                if (titleEl == null || linkEl == null) {
                    continue;
                }

                JobArticle article = new JobArticle();
                article.setTitle(titleEl.text());
                article.setUrl(linkEl.text());
                article.setDescription(descEl != null ? descEl.text() : "");
                article.setSentiment("neutral");
                articles.add(article);

                if (articles.size() >= 10) {
                    break;
                }
            }

            Logger.info("Fetched " + articles.size() + " articles for: " + keyword);
        } catch (Exception e) {
            Logger.error("Failed to fetch articles for keyword: " + keyword, e);
        }
        return articles;
    }
}


