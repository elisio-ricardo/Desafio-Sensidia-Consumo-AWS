package com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.framework.adapter.in.aws.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.domain.entities.ProcessingResult;
import com.elisio.sensidia.Desafio.sensidia.Consumo.AWS.domain.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class S3Consumer {

    @Value("${spring.cloud.aws.s3.bucket-name}")
    private String bucketName;
    private final AmazonS3 s3Client;

    public S3Consumer(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public ProcessingResult downloadFileS3(String key) {
        var processingResult = new ProcessingResult();

        try {
            log.info("Iniciando Download do file no S3");
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8));

            var countLines = countLines(reader);
            reader.close();
            s3Object.close();


            processingResult.setQtdLinhas(countLines);
            processingResult.setMessage("Sucesso ao processar arquivo");
            processingResult.setStatus(ResultEnum.CONCLUIDO);

            log.info("Download e processamento do file no S3 concluido");
            return processingResult;

        } catch (Exception e) {
            log.error("Erro ao tentar fazer download no s3: " + e.getMessage());
            processingResult.setMessage("Erro ao tentar fazer download no s3: " + e.getMessage());
            processingResult.setStatus(ResultEnum.ERROR);
        }
        return processingResult;
    }

    private static Long countLines(BufferedReader reader) throws IOException {
        String line;
        Long lineCount = 0L;

        while ((line = reader.readLine()) != null) {
            lineCount++;
        }
        log.info("Número total de linhas: " + lineCount);
        return lineCount;
    }
}
