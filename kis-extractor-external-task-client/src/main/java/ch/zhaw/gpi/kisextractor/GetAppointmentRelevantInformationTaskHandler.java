package ch.zhaw.gpi.kisextractor;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ExternalTaskSubscription("GetAppointmentRelevantInformation")
public class GetAppointmentRelevantInformationTaskHandler implements ExternalTaskHandler {

    @Autowired
    private RestTemplate kisRestTemplate;

    @Override
    public void execute(ExternalTask et, ExternalTaskService ets) {
        // Fall-ID auslesen
        Long caseId = (Long) et.getVariable("case_id");

        try {
            // Fall via Fall-ID aus KIS erhalten
            ResponseEntity<MedicalCaseRepresentation> kisResponse = kisRestTemplate.exchange("http://localhost:8090/api/medicalCases/{caseId}", HttpMethod.GET, null, MedicalCaseRepresentation.class, caseId);
            
            // Frühst- und spätmöglichstes Datum als Prozessvariablen speichern
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("case_appointment_earliest", kisResponse.getBody().getDateEarliest());
            variables.put("case_appointment_latest", kisResponse.getBody().getDateLatest());
            ets.complete(et, variables);
        } catch (Exception e) {
            ets.handleFailure(et, "Fall-ID nicht gefunden", e.getLocalizedMessage(), 0, 0);
        }
    }
}
