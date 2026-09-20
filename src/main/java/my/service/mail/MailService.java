package my.service.mail;

import java.util.Map;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.regions.Region;
import org.springframework.beans.factory.annotation.Value;

@Component
public class MailService {

    @Value("${app.queue-url}")
    private String queueURL;

    public void sendMail(String mailTo, String title, String body) {
        SqsClient sqsClient = SqsClient.builder().region(Region.AP_NORTHEAST_1).build();
        Map<String, MessageAttributeValue> messageAttributeMap = Map.of(
            "title", MessageAttributeValue.builder()
                        .stringValue(title)
                        .dataType("String")
                        .build(),
            "body", MessageAttributeValue.builder()
                        .stringValue(body)
                        .dataType("String")
                        .build(),
            "mailto", MessageAttributeValue.builder()
                        .stringValue(mailTo)
                        .dataType("String")
                        .build()
        );
        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
            .queueUrl(queueURL)
            .messageBody(messageAttributeMap.get("body").stringValue())
            .messageAttributes(messageAttributeMap)
            .delaySeconds(1)
            .build();
        sqsClient.sendMessage(sendMsgRequest);
    }
}
