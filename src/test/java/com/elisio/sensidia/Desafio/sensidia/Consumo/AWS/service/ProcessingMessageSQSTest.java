package com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.service;

import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.application.service.ProcessingMessageSQS;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.domain.entities.FileMetadata;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.factoryMessage.FactoryMessage;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.framework.adapter.in.aws.consumer.S3Consumer;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.framework.adapter.in.dto.UploadResponseDTO;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.framework.adapter.out.aws.producer.ProcessingDynamoDb;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.framework.adapter.out.aws.producer.SnsReportProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProcessingMessageSQSTest {

    @Mock
    private ProcessingDynamoDb processingDynamoDb;

    @Mock
    private S3Consumer s3Consumer;

    @Mock
    private SnsReportProducer snsReportProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProcessingMessageSQS processingMessageSQS;


    @Test
    @DisplayName("Processando mensagens com sucesso")
    void sucessesWhenProcessingMessageSQS() throws IOException {

        var uploadResponseDTO = FactoryMessage.getUploadResponseDTO();
        var result = FactoryMessage.getProcessingResult();
        var toString = FactoryMessage.parseUploadResponseDtoToString();
        var resultDetail = FactoryMessage.getUploadResponseDynamoDbDTO();

        when(s3Consumer.downloadFileS3(uploadResponseDTO.getFile().getFileName())).thenReturn(result);
        when(objectMapper.readValue(toString, UploadResponseDTO.class)).thenReturn(uploadResponseDTO);
        when(processingDynamoDb.processingDataSqs(uploadResponseDTO, result)).thenReturn(resultDetail);
        when(objectMapper.writeValueAsString(resultDetail)).thenReturn(toString);

        processingMessageSQS.processingMessageSQS(toString);
        verify(s3Consumer, Mockito.times(1)).downloadFileS3(uploadResponseDTO.getFile().getFileName());
        verify(objectMapper, Mockito.times(1)).readValue(toString, UploadResponseDTO.class);
        verify(processingDynamoDb, Mockito.times(1)).processingDataSqs(uploadResponseDTO, result);
        verify(objectMapper, Mockito.times(1)).writeValueAsString(resultDetail);

    }

    @Test
    @DisplayName("Erro ObjectMapper ParseJson")
    void doThrowWhenErrorObjectMapper() throws JsonProcessingException {
        var toString = FactoryMessage.parseUploadResponseDtoToString();

        UploadResponseDTO uploadResponseDTO = new UploadResponseDTO();

        doThrow(JsonProcessingException.class).when(objectMapper).readValue(toString, UploadResponseDTO.class);

        assertThrows(RuntimeException.class, () -> processingMessageSQS.processingMessageSQS(toString));
    }

    @Test
    @DisplayName("Error ParseJson SendSNS")
    void doThrowWhenParseJsonSendSNS() throws JsonProcessingException {

        var uploadResponseDTO = FactoryMessage.getUploadResponseDTO();
        var result = FactoryMessage.getProcessingResult();
        var toString = FactoryMessage.parseUploadResponseDtoToString();
        var resultDetail = FactoryMessage.getUploadResponseDynamoDbDTO();

        when(s3Consumer.downloadFileS3(uploadResponseDTO.getFile().getFileName())).thenReturn(result);
        when(objectMapper.readValue(toString, UploadResponseDTO.class)).thenReturn(uploadResponseDTO);
        when(processingDynamoDb.processingDataSqs(uploadResponseDTO, result)).thenReturn(resultDetail);


        doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(resultDetail);

        assertThrows(RuntimeException.class, () -> processingMessageSQS.processingMessageSQS(toString));

        verify(s3Consumer, Mockito.times(1)).downloadFileS3(uploadResponseDTO.getFile().getFileName());
        verify(objectMapper, Mockito.times(1)).readValue(toString, UploadResponseDTO.class);
        verify(processingDynamoDb, Mockito.times(1)).processingDataSqs(uploadResponseDTO, result);
    }


}