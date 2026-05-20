package org.citycare.patienttreatmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableFeignClients
public class PatientTreatmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientTreatmentServiceApplication.class, args);
        System.out.println("Patient service started port : 8082");
        System.out.println("owner is Tamizh");
    }

}
