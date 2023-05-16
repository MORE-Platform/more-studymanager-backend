package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.EndpointToken;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.IntegrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntegrationService {

    private final StudyStateService studyStateService;
    private final IntegrationRepository repository;

    public IntegrationService(StudyStateService studyStateService, IntegrationRepository repository) {
        this.studyStateService = studyStateService;
        this.repository = repository;
    }

    public EndpointToken addToken(Long studyId, Integer observationId, String tokenLabel) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.addToken(studyId, observationId,
                new EndpointToken()
                        .setTokenLabel(tokenLabel)
                        .setToken(RandomTokenGenerator.generate())
        ); //TODO Token zu kurz, reg_token anschauen, oder datagateway
    }

    public EndpointToken getToken(Long studyId, Integer observationId, Integer tokenId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.getToken(studyId, observationId, tokenId);
    }

    public List<EndpointToken> getTokens(Long studyId, Integer observationId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        return repository.getAllTokens(studyId, observationId);
    }

    public void deleteToken(Long studyId, Integer observationId, Integer tokenId) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        repository.deleteToken(studyId, observationId, tokenId);
    }
}
