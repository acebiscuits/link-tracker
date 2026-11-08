package backend.academy.linktracker.scrapper.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import backend.academy.linktracker.scrapper.service.LinksCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkCheckScheduler {

    private final LinksCheckService linksCheckService;
    
    @Scheduled(fixedDelayString = "${app.scheduler.check-interval-ms}")
    public void checkLinks() {
        linksCheckService.checkAllLinks();
    }
}
