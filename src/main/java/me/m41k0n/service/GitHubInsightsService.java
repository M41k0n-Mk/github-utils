package me.m41k0n.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para enriquecer usuários com métricas (atividade pública, followers, repos, linguagens,
 * relação de follow) e aplicar filtros.
 */
@Service
public class GitHubInsightsService {

    private static final Logger log = LoggerFactory.getLogger(GitHubInsightsService.class);

    private final GitHubService gitHubService;
    private final APIConsume apiConsume;
    private final GitHubGraphQLClient graphQLClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubInsightsService(GitHubService gitHubService, APIConsume apiConsume, GitHubGraphQLClient graphQLClient) {
        this.gitHubService = gitHubService;
        this.apiConsume = apiConsume;
        this.graphQLClient = graphQLClient;
    }

    public static class FilterParams {
        public Integer inactiveDays; // last public activity older than N days
        public Integer lastPushDays; // last repo push older than N days
        public Integer followersLt;
        public Integer followersGt;
        public Integer reposLt;
        public Integer reposGt;
        public List<String> languages; // includes any of
        public Boolean followsYou; // se o usuário segue você
        public Integer contribLt; // estimativa por contagem de eventos
        public Integer contribGt;
        public int page = 1;
        public int size = 25;
    }

    public static class EnrichedUser {
        public final String login;
        public final String htmlUrl;
        public final Instant lastPublicActivity;
        public final Instant lastPushAt;
        public final int followersCount;
        public final int reposCount;
        public final Set<String> languages;
        public final boolean followsYou;
        public final boolean youFollow;
        public final int contributionsEstimate;

        public EnrichedUser(String login, String htmlUrl, Instant lastPublicActivity, Instant lastPushAt,
                             int followersCount, int reposCount, Set<String> languages,
                             boolean followsYou, boolean youFollow, int contributionsEstimate) {
            this.login = login;
            this.htmlUrl = htmlUrl;
            this.lastPublicActivity = lastPublicActivity;
            this.lastPushAt = lastPushAt;
            this.followersCount = followersCount;
            this.reposCount = reposCount;
            this.languages = languages;
            this.followsYou = followsYou;
            this.youFollow = youFollow;
            this.contributionsEstimate = contributionsEstimate;
        }
    }

    public static class PageResult {
        public final int totalCandidates;
        public final int totalMatched;
        public final int page;
        public final int size;
        public final List<EnrichedUser> users;

        public PageResult(int totalCandidates, int totalMatched, int page, int size, List<EnrichedUser> users) {
            this.totalCandidates = totalCandidates;
            this.totalMatched = totalMatched;
            this.page = page;
            this.size = size;
            this.users = users;
        }
    }

    /**
     * Aplica filtros sobre a sua lista de following (página solicitada) e devolve usuários filtrados com métricas.
     */
    public PageResult evaluateFilters(FilterParams params) {
        List<User> base = gitHubService.getFollowing(params.page, params.size);
        int totalCandidates = base.size();
        List<EnrichedUser> enriched = base.stream()
                .map(this::safeEnrich)
                .filter(Objects::nonNull)
                .filter(eu -> matchesAllFilters(eu, params))
                .collect(Collectors.toList());
        return new PageResult(totalCandidates, enriched.size(), params.page, params.size, enriched);
    }

    /** Preset: inativo > 180 dias E followers < 50 */
    public PageResult smartSuggest(int page, int size) {
        FilterParams p = new FilterParams();
        p.page = page; p.size = size; p.inactiveDays = 180; p.followersLt = 50;
        return evaluateFilters(p);
    }

    private EnrichedUser safeEnrich(User u) {
        try {
            Instant lastPublic = fetchLastPublicActivity(u.login());
            GraphInfo g = fetchGraphInfo(u.login());
            return new EnrichedUser(
                    u.login(), u.html_url(),
                    lastPublic,
                    g.lastPushAt,
                    g.followersCount,
                    g.reposCount,
                    g.languages,
                    g.isFollowingViewer,
                    g.viewerIsFollowing,
                    estimateContributions(u.login())
            );
        } catch (Exception ex) {
            log.debug("[FILTER] Falha ao enriquecer {}: {}", u.login(), ex.getMessage());
            return null;
        }
    }

    private boolean matchesAllFilters(EnrichedUser u, FilterParams p) {
        return inactiveOk(u, p) && lastPushOk(u, p) && followersOk(u, p) && reposOk(u, p)
                && languagesOk(u, p) && followsYouOk(u, p) && contributionsOk(u, p);
    }

    // ===== filtros =====
    private boolean inactiveOk(EnrichedUser u, FilterParams p) {
        if (p.inactiveDays == null) return true;
        if (u.lastPublicActivity == null) return true; // sem eventos públicos recentes — considerar ok
        Instant limit = Instant.now().minus(p.inactiveDays, ChronoUnit.DAYS);
        return u.lastPublicActivity.isBefore(limit);
    }

    private boolean lastPushOk(EnrichedUser u, FilterParams p) {
        if (p.lastPushDays == null) return true;
        if (u.lastPushAt == null) return true;
        Instant limit = Instant.now().minus(p.lastPushDays, ChronoUnit.DAYS);
        return u.lastPushAt.isBefore(limit);
    }

    private boolean followersOk(EnrichedUser u, FilterParams p) {
        if (p.followersLt != null && !(u.followersCount < p.followersLt)) return false;
        if (p.followersGt != null && !(u.followersCount > p.followersGt)) return false;
        return true;
    }

    private boolean reposOk(EnrichedUser u, FilterParams p) {
        if (p.reposLt != null && !(u.reposCount < p.reposLt)) return false;
        if (p.reposGt != null && !(u.reposCount > p.reposGt)) return false;
        return true;
    }

    private boolean languagesOk(EnrichedUser u, FilterParams p) {
        if (p.languages == null || p.languages.isEmpty()) return true;
        Set<String> wanted = p.languages.stream().map(s -> s.toLowerCase()).collect(Collectors.toSet());
        for (String lang : u.languages) {
            if (wanted.contains(lang.toLowerCase())) return true;
        }
        return false;
    }

    private boolean followsYouOk(EnrichedUser u, FilterParams p) {
        if (p.followsYou == null) return true;
        return u.followsYou == p.followsYou.booleanValue();
    }

    private boolean contributionsOk(EnrichedUser u, FilterParams p) {
        if (p.contribLt != null && !(u.contributionsEstimate < p.contribLt)) return false;
        if (p.contribGt != null && !(u.contributionsEstimate > p.contribGt)) return false;
        return true;
    }

    // ===== coleta de dados =====
    private Instant fetchLastPublicActivity(String login) {
        String url = "https://api.github.com/users/" + login + "/events/public?per_page=1";
        String body = apiConsume.getData(url);
        try {
            JsonNode arr = mapper.readTree(body);
            if (arr.isArray() && !arr.isEmpty()) {
                String createdAt = arr.get(0).path("created_at").asText(null);
                return createdAt == null ? null : Instant.parse(createdAt);
            }
        } catch (Exception e) {
            log.debug("[FILTER] events/public inválido para {}: {}", login, e.getMessage());
        }
        return null;
    }

    private record GraphInfo(Instant lastPushAt, int followersCount, int reposCount, Set<String> languages,
                             boolean isFollowingViewer, boolean viewerIsFollowing) {}

    private GraphInfo fetchGraphInfo(String login) {
        String query = """
                { "query": "query($login:String!) {\\n  user(login:$login){\\n    followers{ totalCount }\\n    repositories(privacy: PUBLIC, first: 10, orderBy:{field:PUSHED_AT, direction:DESC}){\\n      totalCount\\n      nodes{ pushedAt updatedAt primaryLanguage{ name } }\\n    }\\n    isFollowingViewer\\n    viewerIsFollowing\\n  }\\n}",
                  "variables": {"login": "%s"} }
                """.formatted(login);
        String resp = graphQLClient.execute(query);
        try {
            JsonNode root = mapper.readTree(resp).path("data").path("user");
            int followers = root.path("followers").path("totalCount").asInt(0);
            JsonNode repos = root.path("repositories");
            int reposCount = repos.path("totalCount").asInt(0);
            Instant lastPush = null;
            Set<String> langs = new LinkedHashSet<>();
            for (JsonNode n : repos.path("nodes")) {
                String pushedAt = n.path("pushedAt").asText(null);
                if (pushedAt != null) {
                    Instant pi = Instant.parse(pushedAt);
                    if (lastPush == null || pi.isAfter(lastPush)) lastPush = pi;
                }
                String lang = n.path("primaryLanguage").path("name").asText(null);
                if (lang != null && !lang.isBlank()) langs.add(lang);
            }
            boolean isFollowingViewer = root.path("isFollowingViewer").asBoolean(false);
            boolean viewerIsFollowing = root.path("viewerIsFollowing").asBoolean(false);
            return new GraphInfo(lastPush, followers, reposCount, langs, isFollowingViewer, viewerIsFollowing);
        } catch (Exception e) {
            log.debug("[FILTER] GraphQL parse falhou para {}: {}", login, e.getMessage());
            return new GraphInfo(null, 0, 0, Collections.emptySet(), false, true);
        }
    }

    private int estimateContributions(String login) {
        // Heurística simplificada: quantidade de eventos nas últimas páginas pequenas
        try {
            String url = "https://api.github.com/users/" + login + "/events/public?per_page=30";
            String body = apiConsume.getData(url);
            JsonNode arr = mapper.readTree(body);
            if (arr.isArray()) return arr.size();
        } catch (Exception e) {
            // ignora
        }
        return 0;
    }
}
