package backend.academy.linktracker.scrapper.service.scrapping.factory;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

import backend.academy.linktracker.scrapper.exc.apiException.UnsupportedLinkException;
import backend.academy.linktracker.scrapper.service.scrapping.ScrappingService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScrappingServiceFactory {
    
    private final List<ScrappingService> scrappingServices;

    public ScrappingService scrappingService(URI url) {
        return scrappingServices.stream()
            .filter(service -> service.supports(url))
            .findFirst()
            .orElseThrow(() -> new UnsupportedLinkException("No suitable scrapping service for this link" + url));
    }
}
