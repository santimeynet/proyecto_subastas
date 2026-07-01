package com.utn.tpi.subasta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubastaScheduler {

    private final SubastaService subastaService;

    @Scheduled(fixedRate = 30000)
    public void procesarSubastas() {
        log.debug("Ejecutando transiciones automaticas de subastas");
        subastaService.procesarTransicionesAutomaticas();
    }
}
