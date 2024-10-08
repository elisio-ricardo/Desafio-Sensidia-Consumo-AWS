package com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.framework.adapter.in.aws.consumer;


import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.application.service.ProcessingMessageSQS;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SQSConsumer {

    private final ProcessingMessageSQS processingMessageSQS;

    public SQSConsumer(ProcessingMessageSQS processingMessageSQS) {
        this.processingMessageSQS = processingMessageSQS;
    }


    @SqsListener("${spring.cloud.aws.sqs.queue-name}")
    public void listen(String message) {
        log.info("------------------------------------------------------------INICIANDO 2° ETAPA PROCESSANDO MSGS DO SQS ------------------------------------------------------------");
        log.info("Consumindo fila SQS com a message: " + message);
        processingMessageSQS.processingMessageSQS(message);
    }




}
