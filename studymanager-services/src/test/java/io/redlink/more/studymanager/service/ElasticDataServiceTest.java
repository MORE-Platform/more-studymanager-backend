/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonpDeserializer;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpParser;
import com.fasterxml.jackson.core.JsonFactory;
import io.redlink.more.studymanager.core.datavalidity.MeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.StringFieldValue;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import jakarta.json.stream.JsonParser;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class ElasticDataServiceTest {

    private final static Logger LOG = LoggerFactory.getLogger(ElasticDataServiceTest.class);

    private ElasticDataService elasticDataService;

    private ElasticsearchClient client = Mockito.mock(ElasticsearchClient.class);

    private ParticipantService participantServic = Mockito.mock(ParticipantService.class);

    private StudyGroupService studyGroupService =  Mockito.mock(StudyGroupService.class);

    @BeforeEach
    void init() {
        elasticDataService = new ElasticDataService(client, participantServic, studyGroupService);
    }

    @Test
    public void testValidateObservationData() throws IOException {

        MeasurementSet measurementSet = new MeasurementSet("test", Set.of(
            new Measurement("activityType", Measurement.Type.STRING),
            new Measurement("activeTimeInSeconds", Measurement.Type.INTEGER),
            new Measurement("maxMotionIntensity", Measurement.Type.DOUBLE),
            new Measurement("active", Measurement.Type.BOOLEAN)
        ));
        SearchResponse<Void> expectedResponse;
        try(InputStream responseData = ElasticDataServiceTest.class.getClassLoader().getResourceAsStream("testdata/validateObservationDataResponse1.json")){
            expectedResponse = deserializeJsonToSearchResponse(responseData);
        }
        ArgumentCaptor<Long> requestCaptor = ArgumentCaptor.forClass(Long.class);
        when(client.search(any(SearchRequest.class), any(Class.class))).thenReturn(expectedResponse);

        var results = elasticDataService.validateObservationData(15,null,1, 11,
                new Timeframe(
                        Instant.parse("2025-12-01T16:00:00Z"),
                        Instant.parse("2025-12-02T00:00:00Z")),
                measurementSet);

        assertThat(results).isNotNull();
        assertThat(results.numDocs()).isEqualTo(49);
        assertThat(results.measurements().stream().map(MeasurementSummary::getMeasurement).collect(Collectors.toSet())).isEqualTo(measurementSet.values());
        assertThat(results.effectiveTime().min()).isEqualTo(Instant.parse("2025-12-01T16:00:00.000Z"));
        assertThat(results.effectiveTime().max()).isEqualTo(Instant.parse("2025-12-02T00:00:00.000Z"));

        MeasurementSummary activityTypeValue = results.measurements().stream().filter(mv -> mv.getMeasurement().getId().equals("activityType")).findFirst().orElse(null);
        assertThat(activityTypeValue).isNotNull();
        assertThat(activityTypeValue.getStringResult()).isNotNull();
        assertThat(activityTypeValue.getStringResult().values().stream().map(StringFieldValue::value).collect(Collectors.toList())).containsExactlyInAnyOrder("SEDENTARY", "WALKING", "GENERIC");
        assertThat(activityTypeValue.getStringResult().values().stream().filter(it -> "SEDENTARY".equals(it.value())).findFirst().get().count()).isEqualTo(40);
        assertThat(activityTypeValue.getStringResult().values().stream().filter(it -> "WALKING".equals(it.value())).findFirst().get().count()).isEqualTo(8);
        assertThat(activityTypeValue.getStringResult().values().stream().filter(it -> "GENERIC".equals(it.value())).findFirst().get().count()).isEqualTo(1);


        MeasurementSummary activeTimeInSecondsValue = results.measurements().stream().filter(mv -> mv.getMeasurement().getId().equals("activeTimeInSeconds")).findFirst().orElse(null);
        assertThat(activeTimeInSecondsValue).isNotNull();
        assertThat(activeTimeInSecondsValue.getNumericResult()).isNotNull();
        assertThat(activeTimeInSecondsValue.getNumericResult().missing()).isEqualTo(0);
        assertThat(activeTimeInSecondsValue.getNumericResult().min()).isEqualTo(0.0);
        assertThat(activeTimeInSecondsValue.getNumericResult().max()).isEqualTo(900.0);
        assertThat(activeTimeInSecondsValue.getNumericResult().avg()).isEqualTo(657.244, Offset.offset(0.01));
        assertThat(activeTimeInSecondsValue.getNumericResult().sum()).isEqualTo(32205.0);

        MeasurementSummary maxMotionIntensityValue = results.measurements().stream().filter(mv -> mv.getMeasurement().getId().equals("maxMotionIntensity")).findFirst().orElse(null);
        assertThat(maxMotionIntensityValue).isNotNull();
        assertThat(maxMotionIntensityValue.getNumericResult()).isNotNull();
        assertThat(maxMotionIntensityValue.getNumericResult().missing()).isEqualTo(0);
        assertThat(maxMotionIntensityValue.getNumericResult().min()).isEqualTo(0.0);
        assertThat(maxMotionIntensityValue.getNumericResult().max()).isEqualTo(6.0);
        assertThat(maxMotionIntensityValue.getNumericResult().avg()).isEqualTo(3.0816, Offset.offset(0.01));
        assertThat(maxMotionIntensityValue.getNumericResult().sum()).isEqualTo(151.0);

        MeasurementSummary activeStateValue = results.measurements().stream().filter(mv -> mv.getMeasurement().getId().equals("active")).findFirst().orElse(null);
        assertThat(activeStateValue).isNotNull();
        assertThat(activeStateValue.getBooleanResult()).isNotNull();
        assertThat(activeStateValue.getBooleanResult().values().size()).isEqualTo(1);
        //we only have missing values as the data do not provide any boolean Measurement
        assertThat(activeStateValue.getBooleanResult().values().stream().filter(it -> it.value() == null).findFirst().get().count()).isEqualTo(49);

    }

    public static SearchResponse<Void> deserializeJsonToSearchResponse(InputStream json) throws IOException {
        var jsonFactory = new JsonFactory();
        var jacksonParser = jsonFactory.createParser(json);
        var mapper = new JacksonJsonpMapper();
        JsonParser parser  = new JacksonJsonpParser(jacksonParser, mapper);

        var documentDeserializer = new JsonpDeserializer<Void>() {
            public Void deserialize(JsonParser parser , JsonpMapper mapper) {
                parser.skipArray();
                parser.skipObject();
                return null;
            }
            public Void deserialize(JsonParser parser , JsonpMapper mapper , JsonParser.Event event) {
            if (event == JsonParser.Event.VALUE_NULL) {
                return null;
            }
            parser.skipArray();
            parser.skipObject();
            return null;
            }

            public EnumSet<JsonParser.Event> acceptedEvents() {
                return EnumSet.of(JsonParser.Event.START_OBJECT, JsonParser.Event.VALUE_NULL);
            }

            public EnumSet<JsonParser.Event> nativeEvents() {
                return EnumSet.noneOf(JsonParser.Event.class);
            }
        };

        var responseDeserializer = SearchResponse.createSearchResponseDeserializer(documentDeserializer);
        return responseDeserializer.deserialize(parser, mapper);
    }

}
